#include <stdlib.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

#include "parse_shared.h"
#include "parse_callback.h"
#include "parse_preprocess.h"
#include "parse.h"
#include "parse_tex.h"
#include "parse_tex_private.h"

/* -----------------------------------------------------------[ MODE_TEX* ]-- */

char parse_tex(parser* p)
{
	switch(p->mode) {
	case MODE_TEX_BEGIN:      parse_tex_begin(p);      break;
	case MODE_TEX_MATH:       parse_tex_math(p);       break;
	case MODE_TEX_POT_HTML:   parse_tex_pot_html(p);   break;
	case MODE_TEX_MATH_PEND:  parse_tex_math_pend(p);  break;
	case MODE_TEX_HTML:       parse_tex_html(p);       break;
	case MODE_TEX_HTML_PEND1: parse_tex_html_pend1(p); break;
	case MODE_TEX_HTML_PEND2: parse_tex_html_pend2(p); break;
	case MODE_TEX_MAIN:       parse_tex_main(p);            break;
	default: return 0;
	}
	return 1;
}

/* TODO z largely redundant */

static void parse_tex_begin(parser* p)
{
	switch(cchr) {
	case '$':
		p->mode = MODE_TEX_MATH;
		CALLBACK0(begin_tex_math);
		parse_consume(p);
		break;
	case '<':
		p->mode = MODE_TEX_POT_HTML;
		parse_consume(p);
		break;
	default:
		p->mode = MODE_TEX_MAIN;
		CALLBACK0(begin_tex);
		parse_tex_main(p);
	}
}

static void parse_tex_math(parser* p)
{
	if(cchr == '$' && is_prev_at_begin(p)) {
		p->mode = MODE_TEX_MATH_PEND;
		parse_consume(p);
	} else {
		parse_tex_main(p);
	}
}

static char is_prev_at_begin(parser* p)
{
	/* see is_at_begin in (parse_preprocess.c) for explanation */
	return p->pos == 1 || p->buf[p->pos - 2] == 0 ||
					(p->x > 0 && p->pos == p->x + 3);
}

static void parse_tex_math_pend(parser* p) /* pend := potential end */
{
	char cbak = cchr;
	parse_consume(p);
	if(cbak == '}') {
		parse_consume(p); /* consume '{' */
		CALLBACK0(end_tex_math);
		p->mode = MODE_INWORD;
	} else {
		p->mode = MODE_TEX_MAIN;
		parse_character(p, '$');
		parse_bak_and_return_to_mode(p, cbak, MODE_TEX_MATH);
	}
}

static void parse_bak_and_return_to_mode(parser* p, char bak, int mode)
{
	if(p->error == NULL) {
		parse_character(p, bak);
		if(p->error == NULL) {
			if(p->mode == MODE_TEX_MAIN)
				p->mode = mode;
			else
				p->error = "Unexpected mode change. Expected "
					"MODE_TEX_MAIN. This might be a result "
					"of an incorrectly terminated math "
					"or xml sequence.";
		}
	}
}

static void parse_tex_pot_html(parser* p)
{
	char cbak = cchr;
	parse_consume(p);
	if(cbak == ' ') {
		p->mode = MODE_TEX_HTML;
		CALLBACK0(begin_tex_html);
	} else {
		CALLBACK0(begin_tex);
		p->mode = MODE_TEX_MAIN;
		parse_character(p, '<');
		if(p->error == NULL)
			parse_character(p, cbak);
	}
}

static void parse_tex_html(parser* p)
{
	if(cchr == ' ' && is_prev_at_begin(p)) {
		p->mode = MODE_TEX_HTML_PEND1;
		parse_consume(p);
	} else {
		parse_tex_main(p);
	}
}

static void parse_tex_html_pend1(parser* p)
{
	char cbak = cchr;
	parse_consume(p);
	if(cbak == '>') {
		p->mode = MODE_TEX_HTML_PEND2;
	} else {
		p->mode = MODE_TEX_MAIN;
		parse_character(p, ' ');
		parse_bak_and_return_to_mode(p, cbak, MODE_TEX_HTML);
	}
}

static void parse_tex_html_pend2(parser* p)
{
	char cbak = cchr;
	parse_consume(p);
	if(cbak == '}') {
		parse_consume(p); /* consume '{' */
		CALLBACK0(end_tex_html);
		p->mode = MODE_INWORD;
	} else {
		p->mode = MODE_TEX_MAIN;
		CALLBACK0(begin_tex);
		parse_character(p, ' ');
		if(p->error == NULL) {
			parse_character(p, '>');
			parse_bak_and_return_to_mode(p, cbak, MODE_TEX_HTML);
		}
	}
}

static void parse_tex_main(parser* p)
{
	/* TODO z DOES NOT ALLOW '{' s or such to occur unbalanced in comments
							or escape secuences */
	char cc = cchr;
	char begin;
	if(cc != '{') {
		parse_consume(p);
		if(cc == '}') {
			begin = parse_is_at_begin(p);
			parse_consume(p);
			if(begin) {
				CALLBACK0(end_tex);
				p->mode = MODE_INWORD;
				return; /* final } is not part of the TeX */
			}
		}
	}
	CALLBACK1(texchar, cc);
}
