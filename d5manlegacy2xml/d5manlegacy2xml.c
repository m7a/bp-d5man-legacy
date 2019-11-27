#include <stdlib.h>
#include <stdio.h>

#ifdef DEBUG
#include <sys/resource.h>
#include <assert.h>
#endif

#include "parser_general.h"
#include "meta.h"
#include "parse_callback.h"
#include "parse.h"
#include "export_xml.h"
#include "d5manlegacy2xml.h"
#include "d5manlegacy2xml_private.h"

int main(int argc, char** argv)
{
#ifdef DEBUG
	/* http://stackoverflow.com/questions/4636456/stack-trace-for-c-using-
									gcc */
	struct rlimit core_limit = { RLIM_INFINITY, RLIM_INFINITY };
	int result = setrlimit(RLIMIT_CORE, &core_limit);
	assert(result == 0);
#endif
	switch(argc) {
	case 1:
		return work();
	case 2:
		help(argv[0]);
		return EXIT_SUCCESS;
	default:
		fprintf(stderr, "%s does not take arguments.\n", argv[0]);
		return EXIT_FAILURE;
	}
}

static int work()
{
	struct parse_callback c;
	parser* data;
	int current;

	/*
	 * This is necessary in order to be able to communicate w/ d5man2xml as
	 * a background filter process.
	 */
	setbuf(stdout, NULL);

	export_xml_init_parse_callback(&c);
	while(!feof(stdin)) {
		export_xml_reset_parse_callback(&c);
		data = parse_init(&c);
		while((current = getchar()) != 0 && current != EOF)
			if(parse_character(data, current) == PARSER_ERROR)
				handle_error(data);
		fflush(stdout);
		if(!parse_close(data))
			handle_error(data);
		parse_free(data);
	}
	export_xml_close_parse_callback(&c);

	return EXIT_SUCCESS;
}

static void handle_error(parser* p)
{
	fflush(stdout);
	parse_error_display(p);
	parse_free(p);
	exit(EXIT_FAILURE);
}

static void help(char* prog)
{
	printf("Ma_Sys.ma %s 1.0.0, Copyright (c) 2014, 2019 Ma_Sys.ma.\n",
									prog);
	puts("For further info send an e-mail to Ma_Sys.ma@web.de\n");
	printf("USAGE %s\n\n", prog);
	puts(
"D5 Manpages are read from stdin separated with 0 terminators. If multiple\n"
"pages are given this way, they will be printed as a single XML document.\n"
"Consult d5manxml.dtd (part of d5manserver) for further details about the\n"
"XML format."
	);
}
