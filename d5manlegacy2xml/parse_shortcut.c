#include <assert.h>
#include <stdlib.h>
#include <ctype.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

#include "parse_shared.h"
#include "parse_callback.h"
#include "parse_word.h"
#include "parse_shortcut.h"
#include "parse_shortcut_private.h"

#include "parse.h"

char parse_shortcut(parser* p)
{
	switch(p->mode) {
	case MODE_POTENTIAL_SHORTCUT:
	case MODE_IN_SHORTCUT:
		parse_shortcut_generic(p);
		break;
	case MODE_SHORTCUT_MULTI:
		parse_shortcut_multi(p);
		break;
	case MODE_SHORTCUT_END:
		parse_shortcut_end(p);
		break;
	default:
		return 0;
	}
	return 1;
}

static void parse_shortcut_generic(parser* p)
{
	if(cchr == ']') {
		p->mode = MODE_SHORTCUT_END;
	} else if(!parse_is_upper_alnum(cchr)) {
		if(p->mode == MODE_POTENTIAL_SHORTCUT)
			parse_not_a_shortcut(p);
		else if(p->mode == MODE_IN_SHORTCUT)
			p->error = "Lowercase letters are not allowed in "
								"shortcuts.";
		else
			p->error = "parse_shortcut() only applies to "
				"POTENTIAL_SHORCUT and IN_SHORTCUT. This "
				"is an internal error which may only be "
				"caused by program bugs.";
	}
}

static char parse_is_upper_alnum(char c)
{
	return isupper(c) || isdigit(c);
}

static void parse_not_a_shortcut(parser* p)
{
	int start = parse_reverse_search(p->buf, p->pos - 1, '[');
	int pos;

	parse_direct_cdata(p, '[');

	for(pos = start; pos < p->pos; pos++)
		p->buf[pos] = p->buf[pos + 1];

	cchr = 0;

	p->mode = MODE_PREWORD;
	parse_unrelated_data_from_to(p, start, p->pos);
}

static void parse_shortcut_multi(parser* p)
{
	if(cchr == '[')  {
		p->mode = MODE_IN_SHORTCUT;
		parse_consume(p);
	} else {
		p->error = "A shortcut with multiple keys needs to be "
				"formatted like this: [KEY]-[KEY]-[KEY]-...";
	}
}

static void parse_shortcut_end(parser* p)
{
	if(cchr == '-') {
		p->buf[p->pos - 1] = '-'; /* overwrite ] */
		parse_consume(p);
		p->mode = MODE_SHORTCUT_MULTI;
	} else if(parse_is_soft_word_end_char(cchr)) {
		parse_flush_shortcut(p);
	} else {
		p->error = "A shortcut must be followed by another key to "
				"press, i.e. first a '-' char or a space to "
				"terminate the shortcut.";
	}
}

static char parse_is_soft_word_end_char(char chr)
{
	char c[2];
	c[0] = chr;
	c[1] = 0;
	return parse_is_soft_word_end(c);
}

static void parse_flush_shortcut(parser* p)
{
	int start = parse_reverse_search(p->buf, p->pos - 1, '[');
	char bak = cchr;
	assert(start != -1);

	p->buf[p->pos - 1] = 0; /* terminate sequence / replace ']' w/ 0 */

	CALLBACK0(begin_shortcut);
	parse_flush_shortcut_inner_callbacks(p, p->buf + start + 1);
	CALLBACK0(end_shortcut);
	p->pos = start - 1; /* we want the '[' to be overwritten */

	p->mode = MODE_INWORD; /* end the shortcut "word" properly */
	if(p->error == NULL)
		parse_character(p, bak);
}

static void parse_flush_shortcut_inner_callbacks(parser* p, char* current)
{
	char has_next = 0;
	char* next = current + 1;

	while(1) {
		if(*next == '-') {
			*next = 0;
			has_next = 1;
		}
		if(*next == 0) {
			CALLBACK1(shortcut_key, current);
			if(!has_next)
				return;
			current = next + 1;
			has_next = 0;
		}
		next++;
	}
}
