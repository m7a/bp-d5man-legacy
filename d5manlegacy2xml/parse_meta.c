#include <stdlib.h>
#include <assert.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"
#include "meta.h"

#include "parse_shared.h"
#include "parse_callback.h"
#include "parse_meta.h"

#include "parse.h"

static void parse_section_external(parser* p);

int parse_invoc_meta_directly(parser* p, char cc)
{
	assert(p->mode < 50);
	switch(meta_proc(p, cc)) {
	case PARSER_ERROR: return PARSER_ERROR;
	case PARSER_OK:    return PARSER_OK;
	case PARSER_END:
		p->x    = X_NORMAL;
		p->mode = MODE_INIT;
		p->pos  = -1;
		return PARSER_OK;
	default:
		p->error = "Unknown mode.";
		return PARSER_ERROR;
	}
}

char parse_external(parser* p)
{
	if(p->mode < 50 && p->mode != MODE_INIT) {
		parse_section_external(p);
		return 1;
	} else {
		return 0;
	}
}

static void parse_section_external(parser* p)
{
	switch(meta_section_proc(p)) {
	case META_SECTION_PROC_MORE_DATA:
		break;
	case META_SECTION_PROC_BUF_HAS_SECTION:
		CALLBACK1(section, p->buf);
		p->pos = -1;
		break;
	case META_SECTION_PROC_UNRELATED_DATA:
		p->mode = MODE_PREPROCESS;
		if(p->pos != -1)
			parse_unrelated_data(p);
		else
			parse_character(p, '\n');
		break;
	}
}
