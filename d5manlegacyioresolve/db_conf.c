#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "db.h"
#include "slist.h"
#include "db_conf.h"

/*
 * TODO DOCUMENT THE FOLLOWING CHANGES
 *
 *  - conf dir obsolete use file /etc/d5man.conf
 *  - env D5MAN_CONF also points to file
 *  - common_res dir instead of WEBSITE_FS_OVERRIDE key
 *  - io provider commands formatted
 *    SECTION,NAMEPREFIX=COMMANDPREFIX
 *  - COMMANDPREFIX SECTION NAME [ATTACHMENT] is the format used for invocation
 *    of an external program!
 *  - conf only ever accessed w/ this library (exported to XML for server)
 *  - all paths now %-separated
 *  - zip locations formatted ZIPFILE!SUBPATH
 *  - required variable D5MAN_VIM
 *  - media converter
 *     - takes SVG from STDIN and writes PDF to stdout (can have parameters but
 *       executable path may not contain spaces)
 */

static char* getconf();
static char procline(char* key, char* val);
static char procdb(char* val);
static char proc_potential_io_provider(char* key, char* val);
static void process_root(char* val);
static char* validate();
static void free_io_provider_cnt(void* data);

#define D5MAN_CONF_PATHSEP     "%" /* not common in windows paths and sh OK */
#define D5MAN_CONF_DEFAULT     "/etc/d5man.conf"
#define D5MAN_CONF_LINE_LENGTH 256

struct d5man_conf_all_data {
	unsigned clients;
	struct list* io_provider_commands;
	struct list* io_roots;
	char* io_compl_a;
	char* io_compl_b;
	char* vim;
	char* vim_plugin;
	char* common_res;
	char* db_sel;
	char* io_resolver;
	char* db_sync;
	char* d5man2xml;
	char* media_converter;
	char* redirect_processor;
};

static struct d5man_conf_all_data cconf = { 0, NULL, NULL, NULL, NULL, NULL,
			NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL };

char* d5man_db_conf_open()
{
	char* ret = NULL;
	if(cconf.clients == 0) {
		cconf.io_provider_commands = alloc_list();
		cconf.io_roots             = alloc_list();
		cconf.io_compl_a           = NULL;
		cconf.io_compl_b           = NULL;
		cconf.vim                  = NULL;
		cconf.vim_plugin           = NULL;
		cconf.common_res           = NULL;
		cconf.db_sel               = NULL;
		cconf.io_resolver          = NULL;
		cconf.db_sync              = NULL;
		cconf.d5man2xml            = NULL;
		cconf.media_converter      = NULL;
		cconf.redirect_processor   = NULL;
		ret = getconf();
	}
	if(ret == NULL)
		cconf.clients++;
	return ret;
}

static char* getconf()
{
	char* err = NULL;
	char* conff = getenv("D5MAN_CONF");
	FILE* conf_stream;
	char* confrest;
	char line[D5MAN_CONF_LINE_LENGTH];

	if(conff == NULL)
		conff = D5MAN_CONF_DEFAULT;

	conf_stream = fopen(conff, "r");
	if(conf_stream == NULL)
		return "Failed to open configuration file";

	while(!feof(conf_stream) && fgets(line, D5MAN_CONF_LINE_LENGTH,
							conf_stream) != NULL) {
		confrest = strchr(line, '=');
		if(confrest == NULL) {
			err = "Missing `=`-separator in configuration line.";
			break;
		}
		*confrest = 0;
		confrest++;
		/* cut off terminating newline */
		confrest[strlen(confrest) - 1] = 0;
		if(!procline(line, confrest)) {
			err = "Failed to process configuration line.";
			break;
		}
	}

	if(err == NULL)
		err = validate();

	if(err != NULL)
		d5man_db_conf_close();

	fclose(conf_stream);
	return err;
}

static char procline(char* key, char* val)
{
	/* TODO z define macro for strassoc */
	if(strcmp(key, "D5MAN_DB_SEARCH") == 0)
		return procdb(val);
	else if(strcmp(key, "D5MAN_COMMON_RES") == 0)
		cconf.common_res         = d5man_db_strdup(val);
	else if(strcmp(key, "D5MAN_VIM_PLUGIN") == 0)
		cconf.vim_plugin         = d5man_db_strdup(val);
	else if(strcmp(key, "D5MAN_VIM") == 0)
		cconf.vim                = d5man_db_strdup(val);
	else if(strcmp(key, "D5MAN_COMPL_A") == 0)
		cconf.io_compl_a         = d5man_db_strdup(val);
	else if(strcmp(key, "D5MAN_COMPL_B") == 0)
		cconf.io_compl_b         = d5man_db_strdup(val);
	else if(strcmp(key, "D5MAN_IO_RESOLVER") == 0)
		cconf.io_resolver        = d5man_db_strdup(val);
	else if(strcmp(key, "D5MAN_DB_SYNC") == 0)
		cconf.db_sync            = d5man_db_strdup(val);
	else if(strcmp(key, "D5MAN_D5MAN2XML") == 0)
		cconf.d5man2xml          = d5man_db_strdup(val);
	else if(strcmp(key, "D5MAN_MEDIA_CONVERTER") == 0)
		cconf.media_converter    = d5man_db_strdup(val);
	else if(strcmp(key, "D5MAN_REDIRECT_PROCESSOR") == 0)
		cconf.redirect_processor = d5man_db_strdup(val);
	else if(strcmp(key, "D5MAN_FILE_ROOT") == 0)
		process_root(val);
	else
		return proc_potential_io_provider(key, val);

	return 1; /* simple assoc always ok */
}

static char procdb(char* val)
{
	char* pdb = strtok(val, D5MAN_CONF_PATHSEP);
	while(pdb != NULL) {
		if(d5man_db_conf_file_exists(pdb)) {
			cconf.db_sel = d5man_db_strdup(pdb);
			return 1;
		}
		pdb = strtok(NULL, D5MAN_CONF_PATHSEP);
	}
	return 0; /* no database found */
}

char d5man_db_conf_file_exists(char* path)
{
	/* Hacky but portable */
	FILE* tst = fopen(path, "r");
	if(tst == NULL) {
		return 0;
	} else {
		fclose(tst);
		return 1;
	}
}

static char proc_potential_io_provider(char* key, char* val)
{
	struct d5man_db_conf_io_provider_command* ccmd;
	char* sub = strchr(key, ',');
	if(sub == NULL)
		return 1; /* ignore this entry */
	*sub = 0; /* separate */
	ccmd = malloc(sizeof(struct d5man_db_conf_io_provider_command));
	ccmd->section  = atoi(key); /* only section, rest cut off by 0 */
	ccmd->name_pre = d5man_db_strdup(sub + 1);
	ccmd->cmd_pre  = d5man_db_strdup(val);
	add_to_list(cconf.io_provider_commands, ccmd);
	return 1;
}

static void process_root(char* val)
{
	char* tok = strtok(val, D5MAN_CONF_PATHSEP);
	while(tok != NULL) {
		add_to_list(cconf.io_roots, d5man_db_strdup(tok));
		tok = strtok(NULL, D5MAN_CONF_PATHSEP);
	}
}

static char* validate()
{
	/*
	 * Unfortunately there is no standard means of checking if a
	 * directory exists. Thus we just assume it is OK for now.
	 */
	if(cconf.io_compl_a == NULL) /* compl b not req */
		return "D5MAN_COMPL_A not set";
	if(cconf.vim_plugin == NULL)
		return "D5MAN_VIM_PLUGIN not set";
	if(cconf.vim == NULL)
		return "D5MAN_VIM not set";
	if(!d5man_db_conf_file_exists(cconf.vim_plugin))
		return "D5MAN_VIM not found";
	if(cconf.common_res == NULL)
		return "D5MAN_COMMON_RES not set";
	if(cconf.db_sel == NULL)
		return "D5MAN_DB not set";

	return NULL; /* all ok so far */
}

struct list* d5man_db_conf_io_provider_commands()
{
	return cconf.io_provider_commands;
}

char* d5man_db_conf_io_compl_a()
{
	return cconf.io_compl_a;
}

char* d5man_db_conf_io_compl_b()
{
	return cconf.io_compl_b;
}

struct list* d5man_db_conf_io_roots()
{
	return cconf.io_roots;
}

char* d5man_db_conf_vim()
{
	return cconf.vim;
}

char* d5man_db_conf_vim_plugin()
{
	return cconf.vim_plugin;
}

char* d5man_db_conf_common_res()
{
	return cconf.common_res;
}

char* d5man_db_conf_db_locate()
{
	return cconf.db_sel;
}

char* d5man_db_conf_io_resolver()
{
	return cconf.io_resolver;
}

char* d5man_db_conf_db_sync()
{
	return cconf.db_sync;
}

char* d5man_db_conf_d5man2xml()
{
	return cconf.d5man2xml;
}

char* d5man_db_conf_media_converter()
{
	return cconf.media_converter;
}

char* d5man_db_conf_redirect_processor()
{
	return cconf.redirect_processor;
}

void d5man_db_conf_close()
{
	cconf.clients--;
	if(cconf.clients <= 0) {
		cconf.clients = 0;
		free_list_by_func(cconf.io_provider_commands,
							&free_io_provider_cnt);
		free_list(cconf.io_roots);
		FRIN(cconf.io_compl_a);
		FRIN(cconf.io_compl_b);
		FRIN(cconf.vim);
		FRIN(cconf.vim_plugin);
		FRIN(cconf.common_res);
		FRIN(cconf.db_sel);
		FRIN(cconf.io_resolver);
		FRIN(cconf.db_sync);
		FRIN(cconf.d5man2xml);
		FRIN(cconf.media_converter);
		FRIN(cconf.redirect_processor);
	}
}

static void free_io_provider_cnt(void* data)
{
	struct d5man_db_conf_io_provider_command* c = data;
	free(c->name_pre);
	free(c->cmd_pre);
}
