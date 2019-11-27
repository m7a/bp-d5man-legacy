#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

#include "parse_shared.h"
#include "parse_callback.h"
#include "parse_preprocess.h"
#include "parse_preprocess_private.h"

char parse_preprocess(parser* p)
{
	switch(p->mode) {
	case MODE_PREPROCESS:          parse_preprocess_main(p);     break;
	case MODE_PREPROCESS_PCODE:    parse_preprocess_pcode(p);    break;
	case MODE_PREPROCESS_CODE:     parse_preprocess_code(p);     break;
	case MODE_PREPROCESS_CODE_ESC: parse_preprocess_code_esc(p); break;
	case MODE_PREPROCESS_LINK:     parse_preprocess_link(p);     break;
	case MODE_PREPROCESS_TEX:      parse_preprocess_tex(p);      break;
	default: return 0;
	}
	return 1;
}

static void parse_preprocess_main(parser* p)
{
	const char begin = parse_is_at_begin(p);
	char pchr;
	if(begin || p->buf[p->pos - 1] != '\\') {
		/* TODO BAD CODE */
		if(!begin) {
			pchr = p->buf[p->pos - 1];
			if(cchr == '`' &&
				parse_preprocess_can_occur_before_code(pchr)) {
				p->mode = MODE_PREPROCESS_CODE;
				return;
			} else if(cchr == '(' && pchr != ' ') {
				p->mode = MODE_PREPROCESS_LINK;
				return;
			}
		} else if(cchr == '`') {
			p->mode = MODE_PREPROCESS_PCODE;
			return;
		}
		if(cchr == '{') {
			p->mode = MODE_PREPROCESS_TEX;
		} else if(cchr == '\n') {
			assert(p->x == X_NORMAL); /* ~ parse_rest.c/MODE_INIT */
			if(begin) {
				CALLBACK0(paragraph);
				p->mode = MODE_INIT;
				parse_consume(p);
			} else {
				cchr = 0;
				p->mode = MODE_READAHEAD;
			}
		}
	}
}

char parse_is_at_begin(parser* p)
{
	/*
	 * We consider it a begin if
	 * a) p->pos == 0
	 *    i.e. the buffer is empty
	 * b) p->buf[p->pos-1] == 0
	 *    i.e. the next char in the buffer is a 0 byte
	 * c) p->x > 0 && p->pos == p->x + 2
	 *    i.e. we are in table mode (p->x > 0) and our position is exactly
	 *         the indent + initial spaces (this is needed to allow
	 *         table cells to start with Tex)
	 *    Currently, this part of the condition could also be handled
	 *    in parse_tex_main (parse_tex.c)
	 */
	return p->pos == 0 || p->buf[p->pos - 1] == 0 ||
					(p->x > 0 && p->pos == p->x + 2);
}

static void parse_preprocess_pcode(parser* p)
{
	if(cchr == '`') { /* not to confuse quotes */
		/* revert to normal mode if we found another quote mark */
		p->mode = MODE_PREPROCESS;
	} else {
		p->mode = MODE_PREPROCESS_CODE;
		parse_preprocess_code(p); /* to handle escapes */
	}
}

static char parse_preprocess_can_occur_before_code(char c)
{
	return strchr(PARSE_SHARED_SEPS "-", c) != NULL;
}

static void parse_preprocess_code(parser* p)
{
	/* skip over the codeblock */
	switch(cchr) {
	case '\\': p->mode = MODE_PREPROCESS_CODE_ESC; break;
	case '`':  p->mode = MODE_PREPROCESS;          break;
	}
}

static void parse_preprocess_code_esc(parser* p)
{
	p->mode = MODE_PREPROCESS_CODE;
}

static void parse_preprocess_link(parser* p)
{
	if(cchr == ' ' || cchr == '\n')
		parse_consume(p);
	else if(cchr == ')')
		p->mode = MODE_PREPROCESS;
}

static void parse_preprocess_tex(parser* p)
{
	/* TODO CSTAT BUG W/ BUFFER EXCEEDED ... HOW TO SOLVE: IDEA: Handle now following space like a newline (to make the program behave correctly like when we do indeed have a newline and all is OK) */
	if(cchr == '}' && parse_preprocess_reverse_is_balanced(p))
		p->mode = MODE_PREPROCESS;
}

static char parse_preprocess_reverse_is_balanced(parser* p)
{
	int balance = 0;
	int pos;

	for(pos = p->pos; pos >= 0; pos--) {
		if(p->buf[pos] == '}')
			balance--;
		else if(p->buf[pos] == '{')
			balance++;
	}

	assert(balance >= 0);
	return balance == 0;
}
