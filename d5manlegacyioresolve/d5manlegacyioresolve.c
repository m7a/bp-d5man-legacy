#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "db.h"
#include "db_constants.h"
#include "db_conf.h"
#include "db_io_resolve.h"

static int run();
static void procline(char* line);
static char* get_field(char* ptr, struct d5man_db_io_loc* query,
		char (*func)(char* val, struct d5man_db_io_loc*), char hasnext);
static char get_action(char* ptr, struct d5man_db_io_loc* query);
static char get_section(char* ptr, struct d5man_db_io_loc* query);
static char get_name(char* ptr, struct d5man_db_io_loc* query);
static char get_compliance(char* ptr, struct d5man_db_io_loc* query);
static char get_attachment(char* ptr, struct d5man_db_io_loc* query);
static int help(char* an);

/* d5man does not support longer page names than 256 characters anyway */
#define ACCEPTED_LINE_LENGTH 400

int main(int argc, char** argv)
{
	return argc == 1? run(): help(argv[0]);
}

static int run()
{
	char line[ACCEPTED_LINE_LENGTH];
	char* error;

	if((error = d5man_db_conf_open()) != NULL) {
		printf("ERROR,Failed to open D5Man Configuration: %s\n", error);
		return EXIT_FAILURE;
	}

	while(!feof(stdin) && fgets(line, ACCEPTED_LINE_LENGTH, stdin)
								!= NULL) {
		procline(line);
		fflush(stdout);
	}

	d5man_db_conf_close();
	fclose(stdin);
	return EXIT_SUCCESS;
}

static void procline(char* line)
{
	struct d5man_db_io_loc query;
	char* ptr = line;
	char* rtype;
	char is_create_mode;
	unsigned long lenm1;

	if(*line == 0) {
		printf("ERROR,Empty line supplied.\n");
		return;
	}

	lenm1 = strlen(line) - 1;
	if(line[lenm1] != '\n') {
		/* be aware that we do not complete an output line until an
						input line was completed! */
		/* z TODO unexpected behaviour: this is also printed if someone
			immediately cancels w CTRL-D or EOF */
		printf("ERROR,Incomplete line / Next yielded: ");
		return;
	}
	line[lenm1] = 0; /* cut off newline */

	#define DO(X, Y) if((ptr = get_field(ptr, &query, &X, Y)) == NULL) \
									return;
	DO(get_action, 1);
	DO(get_section, 1);

	is_create_mode = (strcmp(line, "CREATE") == 0);

	DO(get_name, is_create_mode);

	if(is_create_mode) { /* create mode */
		DO(get_compliance, 0);
	} else {
		query.compliance = -2;
	}

	DO(get_attachment, 0);
	#undef DO

	d5man_db_io_resolve(&query);

	rtype = d5man_db_io_resolve_type_name(query.type);
	if(rtype == NULL) {
		printf("ERROR,Invalid result: %d\n", query.type);
		return;
	}

	printf("%s,%s\n", rtype, query.arg);
}

static char* get_field(char* ptr, struct d5man_db_io_loc* query,
		char (*func)(char* val, struct d5man_db_io_loc*), char hasnext)
{
	char* pptr = ptr;
	if((ptr = strchr(ptr, ',')) == NULL) {
		if(hasnext) {
			printf("ERROR,Could not obtain field starting at "
								"`%s`\n", pptr);
			return NULL;
		}
	} else {
		*ptr = 0;
	}
	if(!func(pptr, query))
		return NULL;
	/* point to 0 if no more elements */
	return ptr == NULL? pptr + strlen(pptr): ptr + 1;
}

static char get_action(char* ptr, struct d5man_db_io_loc* query)
{
	query->is_writing = !(strcmp(ptr, "READ") == 0);
	return 1;
}

static char get_section(char* ptr, struct d5man_db_io_loc* query)
{
	if((query->section = atol(ptr)) == 0) {
		printf("ERROR,Invalid section: `%s`\n", ptr);
		return 0;
	} else {
		return 1;
	}
}

static char get_name(char* ptr, struct d5man_db_io_loc* query)
{
	query->page = ptr;
	if(strlen(ptr) < 2) {
		printf("ERROR,Page name may not be empty or one character.\n");
		return 0;
	} else {
		return 1;
	}
}

static char get_compliance(char* ptr, struct d5man_db_io_loc* query)
{
	query->compliance = d5man_db_compliance_from_string(ptr);
	if(query->compliance == -1) {
		printf("ERROR,Invalid compliance supplied: `%s`\n", ptr);
		return 0;
	} else {
		return 1;
	}
}

static char get_attachment(char* ptr, struct d5man_db_io_loc* query)
{
	query->attachment = strlen(ptr) == 0? NULL: ptr;
	return 1;
}

static int help(char* an)
{
	printf(
"Ma_Sys.ma D5Man Interactive IO Resolution Utility 1.0.0.0,\n"
"Copyright (c) 2015 Ma_Sys.ma.\n"
"For further info send an e-mail to Ma_Sys.ma@web.de.\n\n"
"USAGE %s\n\n"
"This program runs interactively taking lines (queries) from stdin and\n"
"writing results (answers) to stdout.\n\n"
"The input format is one of the following:\n"
"        READ/WRITE,section,name,[attachment]\n"
"        CREATE,section,name,compliance,[attachment]\n"
"The output format is as follows\n"
"        REALFILE/ZIPFILE/COMMAND/ERROR,data\n\n",
	an
	);
	printf(
"Uppercase is text to be input/output, lowercase are the field names\n\n"
"        oooooooooooooooooooooooooooooooooooooooooooooooooooooo\n"
"          section     D5Man Page Section\n"
"          name        D5Man Page Name\n"
"          attachment  If present: Name of the attachment\n"
"          compliance  Compliance in string form.\n"
"          data        Result string (file name/command/etc.)\n"
"        oooooooooooooooooooooooooooooooooooooooooooooooooooooo\n\n"
	);
	printf(
"The modes are defined as follows\n\n"
"        oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo\n"
"          READ    Read this page/attachment from any source\n"
"          WRITE   Write existing page/attachment to any destination\n"
"          CREATE  Write (potentially nonexistent) to any destination\n"
"        oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo\n\n"
	);
	printf(
"The output formats are defined as follows\n\n"
"        oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo\n"
"          REALFILE  An existing FS file (use fopen etc.)\n"
"          ZIPFILE   /path/to/zipfile!/path/inside/zipfile.txt\n"
"          COMMAND   /usr/bin/screenindex -r 42 test attachment.txt\n"
"          ERROR     Any error message which might have occurred.\n"
"        oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo\n"
	);
	return EXIT_FAILURE;
}
