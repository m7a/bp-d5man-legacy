#include <stdlib.h>
#include <assert.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

#include "parse_shared.h"
#include "parse_callback.h"
#include "parse.h"
#include "parse_meta.h"
#include "parse_preprocess.h"
#include "parse_readahead.h"
#include "parse_word.h"
#include "parse_table.h"
#include "parse_list.h"
#include "parse_shortcut.h"
#include "parse_tex.h"
#include "parse_rest.h"

static void parse_character_sub(parser* p);

parser* parse_init(struct parse_callback* callbacks)
{
	assert(callbacks != NULL);
	return parser_init(callbacks);
}

int parse_character(parser* p, char cc)
{
	if(p->x == X_META)
		return parse_invoc_meta_directly(p, cc);
	else
		return parser_proc(p, cc, parse_character_sub);
}

char parse_close(parser* p)
{
	assert(p->mode != MODE_PREPROCESS && p->error == NULL);

	if(p->mode == MODE_READAHEAD)
		parse_character(p, '\n');

	if(p->error == NULL) {
		parse_character(p, '\n');

		if(p->error == NULL && p->pos != -1)
			p->error = "Unclean end of document.";
		else
			CALLBACK0(end_document);
	}

	return p->error == NULL;
}

void parse_error_display(parser* p)
{
	parser_error_display(p);
}

void parse_free(parser* p)
{
	parser_free(p);
}

static void parse_character_sub(parser* p)
{
	assert(p->x != X_META);

	#define MNOT(X) (!parse_##X(p) && p->error == NULL)

	if(MNOT(external) && MNOT(preprocess) && MNOT(readahead) && MNOT(word)
				&& MNOT(table) && MNOT(list) && MNOT(shortcut)
				&& MNOT(tex) && MNOT(rest))
		p->error = "Unknown mode.";
}
