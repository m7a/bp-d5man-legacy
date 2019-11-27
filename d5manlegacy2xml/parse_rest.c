#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

#include "parse_shared.h"
#include "parse_callback.h"
#include "parse.h"
#include "parse_rest.h"
#include "parse_table.h"
#include "parse_preprocess.h"
#include "parse_word.h"
#include "parse_rest_private.h"

/* TODO z Linking previous qutoation is currently impossible */

char parse_rest(parser* p)
{
	switch(p->mode) {
	case MODE_INIT:          parse_initial(p);              break;
	case MODE_LINK:          parse_link(p);                 break;
	case MODE_LINK_ESC:      parse_link_esc(p);             break;
	case MODE_PCODE:         parse_potential_code(p);       break;
	case MODE_PRE_EQUOT:     parse_pre_end_quotation(p);    break;
	case MODE_PREQUOT_GER:   parse_pre_german_quotation(p); break;
	case MODE_CODE:          parse_code(p);                 break;
	case MODE_CODE_ESC:      parse_code_esc(p);             break;
	case MODE_CODE_MULT:     parse_code_mult(p);            break;
	case MODE_CODE_MULT_IND: parse_code_mult_ind(p);        break;
	default: return 0;
	}
	return 1;
}

/* -----------------------------------------------------------[ MODE_INIT ]-- */

static void parse_initial(parser* p)
{
	if(meta_section_proc(p) == META_SECTION_PROC_UNRELATED_DATA) {
		if(cchr == ' ') {
			p->mode = MODE_TABLE_OR_CODE;
			parse_table_or_code(p);
		} else {
			/*
			 * This ensures we are not in some list mode. List modes
			 * do not allow switching to the preprocessor which
			 * only works for p->x == X_NORMAL. IOW: Normal
			 * list processing bypasses preprocessing.
			 */
			assert(p->x == X_NORMAL);
			p->mode = MODE_PREPROCESS;
			parse_preprocess(p);
		}
	}
}

/* ---------------------------------------------------------------[ Links ]-- */

static void parse_link(parser* p)
{
	switch(cchr) {
	case ' ':  /* Ignore space to allow URLs to be split */
	case '\n': parse_consume(p);                          return;
	case ')':  parse_link_closed(p);                      return;
	case '\\': parse_consume(p); p->mode = MODE_LINK_ESC; return;
	}
}

static void parse_link_closed(parser* p)
{
	int link_start;
	int content_start;
	int content_start_wo_spc;
	cchr = 0; /* => consumes ')' and marks end of string for link name */
	link_start = parse_reverse_search(p->buf, p->pos - 1, 0);
	assert(link_start > 0);
	content_start = parse_reverse_search(p->buf, link_start - 1, 0);

	content_start_wo_spc = content_start;
	/* implicit +1 stips off leading '0' char. */
	while(p->buf[++content_start_wo_spc] == ' ')
		;

	CALLBACK2(link, p->buf + link_start + 1, p->buf + content_start_wo_spc);
	p->pos = content_start;
	p->mode = MODE_INWORD;
}

static void parse_link_esc(parser* p)
{
	/*
	 * we accept the character by not doing anything and continue
	 * with normal parsing
	 */
	p->mode = MODE_LINK;
}

/* -----------------------------------------------------------[ Quotation ]-- */

static void parse_potential_code(parser* p)
{
	if(cchr == '`') {
		CALLBACK0(begin_english_quot);
		p->pos -= 2; /* <=> 2x consume */
		p->mode = MODE_PREWORD;
	} else {
		/*
		 * Marks begin of code (#restref1)
		 * This is only necessary to prevent superflous spaces of codes
		 * which start at the begin of table cells.
		 */
		p->buf[p->pos - 1] = 0;
		p->mode = MODE_CODE;
		parse_code(p);
	}
}

static void parse_pre_end_quotation(parser* p)
{
	char prev;
	char isem;
	if(cchr == '\'') {
		prev = p->buf[p->pos - 1];
		p->pos -= 2; /* remove two quotation marks */
		isem = cchr == '_';
		if(isem)
			parse_consume(p);
		parse_flush_stack(p, 1, isem);
		if(prev == '\'')
			CALLBACK0(end_english_quot);
		else
			CALLBACK0(end_german_quot);
		p->mode = MODE_INWORD; /* next space flushes word */
	} else {
		parse_go_inword(p);
	}
}

static void parse_pre_german_quotation(parser* p)
{
	if(cchr == '`') {
		CALLBACK0(begin_german_quot);
		p->pos -= 2;
		p->mode = MODE_PREWORD;
	} else {
		parse_go_inword(p);
	}
}

/* ----------------------------------------------------------[ MODE_CODE* ]-- */

static void parse_code(parser* p)
{
	switch(cchr) {
	case '\\':
		parse_consume(p);
		p->mode = MODE_CODE_ESC;
		break;
	case '`':
		parse_consume(p);
		CALLBACK0(begin_code_inline);
		parse_flush_stack(p, 0, 2); /* the 2 is currently superflous */
		parse_consume(p); /* consume leading 0 as well (#restref1) */
		CALLBACK0(end_code_inline);
		p->mode = MODE_INWORD; /* following space terminates word */
		break;
	default:
		break;
	}
}

static void parse_code_esc(parser* p)
{
	switch(cchr) {
	case '\\':
	case '`':
		p->mode = MODE_CODE;
		break;
	default:
		p->error = "Inlined code only needs to escape '`' and "
							"'\\' characters.";
		break;	
	}
}

static void parse_code_mult(parser* p)
{
	if(cchr == '\n')
		p->mode = MODE_CODE_MULT_IND;

	parse_direct_cdata(p, cchr);
	parse_consume(p);
}

static void parse_code_mult_ind(parser* p)
{
	int r0;
	if(cchr == ' ') {
		assert(p->x > 0);
		if(--p->x == 0) {
			r0      = parse_reverse_search(p->buf, p->pos - 1, 0);
			p->x    = p->pos - r0;
			p->pos  = r0;
			p->mode = MODE_CODE_MULT;
		}
	} else if(cchr == '\n') {
		CALLBACK0(end_code);
		p->mode = MODE_INIT;
		p->x    = X_NORMAL;
		parse_consume(p);
		CALLBACK1(cdata, "\n");
	} else {
		p->error = "Code wrongly indented/unexpected end of code.";
	}
}

/* ---------------------------------------------------------[ Subsections ]-- */

void parse_end_of_subsection_title(parser* p)
{
	int mid = parse_reverse_search(p->buf, p->pos - 1, 0);
	int start = parse_reverse_search(p->buf, mid - 1, 0);

/*
	TODO z UNICODE MEANS THE TITLE CAN BE LONGER IN BYTES THAN THE SECTION
		UNDERLINING / -> FIND A BETTER WAY?
	if(p->pos - mid <= mid - start && p->pos - mid >= 1) {
*/
	p->buf[mid] = 0;
	p->pos = start;
	CALLBACK1(subsection, p->buf + start + 1);
	p->mode = MODE_INIT;
/*
	} else {
		p->error = "Subsection not underlined correctly.";
	}
*/
}
