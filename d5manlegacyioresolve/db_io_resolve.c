#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>

#include "db.h" /* for strdup only */
#include "db_io_resolve.h"
#include "slist.h"
#include "db_conf.h"
#include "db_constants.h"
#include "d5man_mkdir.h"

static char d5man_db_io_resolve_command(struct d5man_db_io_loc* loc);
static char d5man_db_io_resolve_command_against(struct d5man_db_io_loc* loc,
				struct d5man_db_conf_io_provider_command* cmd);
static void d5man_db_io_resolve_populate_loc_w_cmd(struct d5man_db_io_loc* loc,
				struct d5man_db_conf_io_provider_command* cmd);
static unsigned long d5man_db_io_resolve_blength(struct d5man_db_io_loc* loc);
static char* d5man_db_io_resolve_subpath(struct d5man_db_io_loc* loc);
static char d5man_db_io_resolve_fs_by_existence(struct d5man_db_io_loc* loc);
static char d5man_db_io_resolve_one_file(struct d5man_db_io_loc* loc,
								char* fsroot);
static char* d5man_db_io_resolve_prep(struct d5man_db_io_loc* loc, char* str,
								char sep);
static char d5man_db_io_resolve_from_zip(struct d5man_db_io_loc* loc);
static void d5man_db_io_resolve_by_base_path(struct d5man_db_io_loc* loc);
static char* get_base_path(struct d5man_db_io_loc* loc);

void d5man_db_io_resolve(struct d5man_db_io_loc* loc)
{
	if(loc->section <= 0 || loc->section >= 100) {
		loc->type = D5MAN_DB_IO_LOC_TYPE_ERROR;
		loc->arg  = "Section number length exceeded. "
				"Halted to prevent buffer overflow.";
	} else if(!d5man_db_io_resolve_command(loc)) {
		char* subpath = d5man_db_io_resolve_subpath(loc);
		loc->arg = subpath; /* to transfer this to subfunctions */
		if(loc->compliance == -2) { /* r/o */
			if(!d5man_db_io_resolve_fs_by_existence(loc) &&
					!d5man_db_io_resolve_from_zip(loc)) {
				loc->type = D5MAN_DB_IO_LOC_TYPE_ERROR;
				loc->arg  = "Page not found.";
			}
		} else {
			d5man_db_io_resolve_by_base_path(loc);
		}
		free(subpath);
	}
}

static char d5man_db_io_resolve_command(struct d5man_db_io_loc* loc)
{
	struct list* io_providers = d5man_db_conf_io_provider_commands();
	struct list_data* i;
	FOREACH(io_providers, i)
		if(d5man_db_io_resolve_command_against(loc, i->data))
			return 1;

	return 0;
}

/* returns 1 if should halt (that is fatal error or found) */
static char d5man_db_io_resolve_command_against(struct d5man_db_io_loc* loc,
				struct d5man_db_conf_io_provider_command* cmd)
{
	if(loc->section == cmd->section && memcmp(cmd->name_pre, loc->page,
						strlen(cmd->name_pre)) == 0) {
		d5man_db_io_resolve_populate_loc_w_cmd(loc, cmd);
		return 1;
	} else {
		return 0;
	}
}

static void d5man_db_io_resolve_populate_loc_w_cmd(struct d5man_db_io_loc* loc,
				struct d5man_db_conf_io_provider_command* cmd)
{
	/* 3 for space after command, space after section, space after -w/-r and
								2 for -r/-w */
	char parchar = loc->is_writing? 'w': 'r';
	loc->arg = malloc((strlen(cmd->cmd_pre) +
			d5man_db_io_resolve_blength(loc) + 5) * sizeof(char));
	loc->type = D5MAN_DB_IO_LOC_TYPE_COMMAND;

	if(loc->attachment == NULL)
		sprintf(loc->arg, "%s -%c %d %s", cmd->cmd_pre, parchar,
				loc->section, loc->page);
	else
		sprintf(loc->arg, "%s -%c %d %s %s", cmd->cmd_pre, parchar,
				loc->section, loc->page, loc->attachment);
}

static unsigned long d5man_db_io_resolve_blength(struct d5man_db_io_loc* loc)
{
	/*
	 * 2 for section, 1 for attachement separator, 1 for tailing 0,
	 * 1 for page and section separator
	 */
	return 4 + strlen(loc->page) + (loc->attachment == NULL?
					0: (strlen(loc->attachment) + 1));
}

static char* d5man_db_io_resolve_subpath(struct d5man_db_io_loc* loc)
{
	char* ret;
	char* sub;
	int cend;

	/* 4 = strlen(_att/.d5i) */
	ret = malloc((d5man_db_io_resolve_blength(loc) + 4) * sizeof(char));

	cend = sprintf(ret, "%d/%s", loc->section, loc->page);
	assert(cend > 3);

	sub = strchr(ret, '/'); /* skip NN/ from section */
	assert(sub != NULL && *sub != 0);
	sub++; /* skip over first slash */
	/* replace all separators w/ underscores */
	while((sub = strchr(sub, '/')) != NULL)
		*sub = '_';

	if(loc->attachment == NULL)
		sprintf(ret + cend, ".d5i");
	else
		sprintf(ret + cend, "_att/%s", loc->attachment);

	return ret;
}

static char d5man_db_io_resolve_fs_by_existence(struct d5man_db_io_loc* loc)
{
	struct list* locations = d5man_db_conf_io_roots();
	struct list_data* i;

	FOREACH(locations, i)
		if(d5man_db_io_resolve_one_file(loc, i->data))
			return 1;

	return 0;
}

static char d5man_db_io_resolve_one_file(struct d5man_db_io_loc* loc,
								char* fsroot)
{
	char* combined = d5man_db_io_resolve_prep(loc, fsroot, '/');
	if(d5man_db_conf_file_exists(combined)) {
		loc->type = D5MAN_DB_IO_LOC_TYPE_REALFILE;
		loc->arg  = combined;
		return 1;
	} else {
		free(combined);
		return 0;
	}
}

static char* d5man_db_io_resolve_prep(struct d5man_db_io_loc* loc, char* str,
								char sep)
{
	char* result = malloc((strlen(loc->arg) + strlen(str) + 2) *
								sizeof(char));
	sprintf(result, "%s%c%s", str, sep, loc->arg);
	return result;
}

static char d5man_db_io_resolve_from_zip(struct d5man_db_io_loc* loc)
{
	struct list* locations = d5man_db_conf_io_roots();
	struct list_data* i;
	char* cpath;
	long clen;

	FOREACH(locations, i) {
		cpath = i->data;
		if((clen = strlen(cpath) - 4) >= 0 &&
				strcmp(cpath + clen, ".zip") == 0) {
			loc->arg  = d5man_db_io_resolve_prep(loc, cpath, '!');
			loc->type = D5MAN_DB_IO_LOC_TYPE_ZIPFILE;
			return 1;
		}
	}
	return 0;
}

static void d5man_db_io_resolve_by_base_path(struct d5man_db_io_loc* loc)
{
	char* base = get_base_path(loc);
	char* dir = NULL;
	char* msg;
	char* pos;
	if(base != NULL) {
		loc->arg  = d5man_db_io_resolve_prep(loc, base, '/');
		loc->type = D5MAN_DB_IO_LOC_TYPE_REALFILE;
		dir = d5man_db_strdup(loc->arg);
		pos = strrchr(dir, '/');
		if(pos == NULL) {
			free(loc->arg);
			loc->arg = "Misformatted file name. Program bug or "
						"Windows portability issue.";
			loc->type = D5MAN_DB_IO_LOC_TYPE_ERROR;
		} else {
			*pos = 0;
			if((msg = d5man_mkdir_ensure_dir_exists(dir)) != NULL) {
				free(loc->arg);
				loc->arg = msg;
				loc->type = D5MAN_DB_IO_LOC_TYPE_ERROR;
			}
			free(dir);
		}
	}
}

char* d5man_db_io_resolve_type_name(short type)
{
	switch(type) {
	case D5MAN_DB_IO_LOC_TYPE_REALFILE: return "REALFILE";
	case D5MAN_DB_IO_LOC_TYPE_ZIPFILE:  return "ZIPFILE";
	case D5MAN_DB_IO_LOC_TYPE_COMMAND:  return "COMMAND";
	case D5MAN_DB_IO_LOC_TYPE_ERROR:    return "ERROR";
	default:                            return NULL;
	}
}

/* return value is static -> no freeing necessary */
static char* get_base_path(struct d5man_db_io_loc* loc)
{
	switch(loc->compliance) {
	case D5MAN_COMPLIANCE_PUBLIC:
	case D5MAN_COMPLIANCE_INFORMAL:
	case D5MAN_COMPLIANCE_PRERELEASE:
	case D5MAN_COMPLIANCE_INTERNAL:
		return d5man_db_conf_io_compl_a();
	case D5MAN_COMPLIANCE_PERSONAL:
	case D5MAN_COMPLIANCE_CONFIDENTIAL:
	case D5MAN_COMPLIANCE_RESTRICTED:
	case D5MAN_COMPLIANCE_SECRET:
	case D5MAN_COMPLIANCE_QQV:
		return d5man_db_conf_io_compl_b();
	default:
		loc->type = D5MAN_DB_IO_LOC_TYPE_ERROR;
		loc->arg  = "Unknown compliance level supplied.";
		return NULL;
	}
}
