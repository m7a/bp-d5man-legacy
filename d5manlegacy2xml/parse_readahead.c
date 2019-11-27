#include <stdlib.h>
#include <assert.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

#include "parse_shared.h"
#include "parse_callback.h"
#include "parse.h" /* parse character */
#include "parse_rest.h"
#include "parse_readahead.h"
#include "parse_readahead_private.h"
#include "parse_list.h"

char parse_readahead(parser* p)
{
	switch(p->mode) {
	case MODE_READAHEAD:             parse_readahead_main(p);        break;
	case MODE_READAHEAD_DESCRIPTION: parse_readahead_description(p); break;
	case MODE_READAHEAD_DESCR_SUB:   parse_readahead_descr_sub(p);   break;
	case MODE_READAHEAD_SUBSECTION:  parse_readahead_subsection(p);  break;
	default: return 0;
	}
	return 1;
}

static void parse_readahead_main(parser* p)
{
	switch(cchr) {
	case ' ': p->mode = MODE_READAHEAD_DESCRIPTION;            break;
	case '-': p->mode = MODE_READAHEAD_SUBSECTION;             break;
	default:  parse_abort_readahead_and_flush_preprocessed(p); break;
	}
}

static void parse_abort_readahead_and_flush_preprocessed(parser* p)
{
	char bak = cchr;
	int start;

	p->pos--;
	start = parse_reverse_search(p->buf, p->pos - 1, 0) + 1;

	p->mode = MODE_PREWORD;
	parse_unrelated_data_from_to(p, start, p->pos);
	if(p->error == NULL)
		parse_character(p, '\n');
	if(p->error == NULL)
		parse_character(p, bak);
}

static void parse_readahead_description(parser* p)
{
	if(cchr == '\n')
		p->error = "Expected description list content instead of end "
								"of line.";
	else if(cchr != ' ')
		p->mode = MODE_READAHEAD_DESCR_SUB;
}

static void parse_readahead_descr_sub(parser* p)
{
	if(cchr == '\n') {
		CALLBACK0(begin_list_group);
		parse_begin_description_list(p);
	}
}

static void parse_readahead_subsection(parser* p)
{
	if(cchr == '\n')
		parse_end_of_subsection_title(p);
	else if(cchr != '-')
		parse_abort_readahead_subsection_and_flush_preprocessed(p);
}

static void parse_abort_readahead_subsection_and_flush_preprocessed(parser* p)
{
	int start = parse_reverse_search(p->buf, p->pos, 0);
	assert(start > 0);
	p->buf[start] = '\n';
	start = parse_reverse_search(p->buf, start - 1, 0) + 1;
	/*
	 * It is not clear if this works once we have nesting here. Thus the
	 * assection exists. If it ever fails w/ valid input, try to remove
	 * it and check if the result is OK.
	 */
	assert(start == 0);
	if(parser_incstack(p)) {
		cchr = 0;
		p->mode = MODE_PREWORD;
		parse_unrelated_data_from_to(p, start, p->pos);
	}
}
