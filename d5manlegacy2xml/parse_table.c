#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

#include "parse_shared.h"
#include "parse_callback.h"
#include "parse_table.h"
#include "parse_list.h"
#include "parse_preprocess.h"
#include "parse_word.h"
#include "parse_table_private.h"

/* TODO z table captions may not contain formatting. */

char parse_table(parser* p)
{
	switch(p->mode) {
	case MODE_TABLE_OR_CODE:         parse_table_or_code(p);         break;
	case MODE_TABLE_OR_CODE_LINE_2:  parse_table_or_code_line_2(p);  break;
	case MODE_TABLE_OR_CODE_L2S:     parse_table_or_code_l2s(p);     break;
	case MODE_TABLE_FIELD_BEGIN:     parse_table_field_begin(p);     break;
	case MODE_TABLE_FIRST_SEP_SPACE: parse_table_first_sep_space(p); break;
	case MODE_TABLE_MID_SEP_OR_END:  parse_table_mid_sep_or_end(p);  break;
	case MODE_TABLE_SKIP_SPACES:     parse_table_skip_spaces(p);     break;
	case MODE_TABLE_POST:            parse_table_post(p);            break;
	default:                         return 0;
	}
	return 1;
}

void parse_table_or_code(parser* p) /* or list */
{
	if(cchr == '\n')
		parse_table_or_code_end_of_unknown_line(p);
}

static void parse_table_or_code_end_of_unknown_line(parser* p)
{
	const int start = parse_reverse_search(p->buf, p->pos, 0);
	char* sub = p->buf + start + 1;
	char* list_identifier = parse_skip_indentation(sub);
	char lc;
	if((lc = parse_check_for_list(list_identifier)) != 0) {
		CALLBACK0(begin_list_group);
		CALLBACK1(begin_list, lc);
		parse_process_begin_list(p, sub);
	} else {
		cchr = 0;
		parse_move_indentation_to_x(p, start);
		/* remember: sub now points to a different part of the string */
		if(parse_check_for_table(sub)) {
			p->mode = MODE_TABLE_FIELD_BEGIN;
			p->pos = start;
			CALLBACK1(begin_table, NULL);
		} else {
			p->mode = MODE_TABLE_OR_CODE_LINE_2;
		}
	}
}

static void parse_move_indentation_to_x(parser* p, int start)
{
	char* sub = p->buf + start + 1;
	/* new pointer - old pointer = indentation character count */
	int ic = parse_skip_indentation(sub) - sub;
	p->x = parse_count_indentation(sub);
	parse_strcpy(sub, p->buf + start + 1 + ic);
	p->pos = strlen(sub);
}

/*
 * This should be a standard implementation of strcpy _except_ for the fact that
 * it is defined to work with overlapping objects in the sense that if src lies
 * behind dest in the same input memory, this function will still work
 * correctly. The other way around, however, will result in chaos.
 */
static char* parse_strcpy(char* dest, char* src)
{
	/*
	 * compare http://stackoverflow.com/questions/13719663/implementation-of
	 * 		-strcpy-and-strcat-that-get-a-reference-of-a-pointer-bug
	 */
	while((*(dest++) = *(src++)) != 0);
	return dest;
}

static char parse_check_for_table(char* str)
{
	while(*str != 0) {
		if(!parse_is_table_line_main(*str))
			return 0;
		str++;
	}
	return 1;
}

static void parse_table_or_code_line_2(parser* p)
{
	if(parse_is_table_line_main(cchr) && (p->buf[p->pos - 1] == 0 ||
						p->buf[p->pos - 1] == ' ')) {
		p->mode = MODE_TABLE_OR_CODE_L2S;
		parse_consume(p);
	} else if(cchr == ' ') {
		p->mode = MODE_TABLE_OR_CODE_L2S;
	} else if(cchr == '\n') {
		parse_consume(p);
		parse_table_go_code(p);
		CALLBACK0(end_code);
		p->mode = MODE_INIT;
		p->x    = X_NORMAL;
	} else {
		p->error = "Table or code parsing failed.";
	}
}

static void parse_begin_table_titled(parser* p)
{
	int start_ind   = parse_reverse_search(p->buf, p->pos    - 1, 0);
	int start_title = parse_reverse_search(p->buf, start_ind - 1, 0);
	int delta;

	assert(start_title >= -1 && start_ind > 0);

	cchr = 0;
	delta = parse_count_indentation(p->buf + start_ind + 1) - p->x;

	if(delta < 0) {
		p->x += delta;
		p->mode = MODE_TABLE_FIELD_BEGIN;
		CALLBACK1(begin_table, p->buf + start_title + 1);
		p->pos = start_title;
	} else if(delta == 0) {
		p->mode = MODE_CODE_MULT_IND;
		parse_table_code_sub(p, start_title);
		p->pos = start_title;
	} else {
		p->error = "Bad indentation in line following table title.";
	}
}

static void parse_table_go_code(parser* p)
{
	int start_current = parse_reverse_search(p->buf, p->pos, 0);
	int start_prev    = parse_reverse_search(p->buf, start_current - 1, 0);
	assert(start_current > 0 && start_prev >= -1);
	if(parser_incstack(p)) {
		cchr = 0;
		p->mode = MODE_CODE_MULT;
		parse_table_code_sub(p, start_prev);
		if(p->buf[start_current + 1] == ' ')
			CALLBACK1(cdata, p->buf + start_current + p->x + 1);
		p->pos = start_prev;
	}
}

static void parse_table_code_sub(parser* p, int start_prev)
{
	CALLBACK0(begin_code);
	CALLBACK1(cdata, p->buf + start_prev + 1);
	CALLBACK1(cdata, "\n");
}

static void parse_table_or_code_l2s(parser* p)
{
	if(parse_is_table_line_main(cchr) && (p->buf[p->pos - 1] == 0 ||
						p->buf[p->pos - 1] == ' '))
		parse_consume(p);
	else if(cchr == '\n')
		parse_begin_table_titled(p);
	else if(cchr != ' ')
		parse_table_go_code(p);
}

static void parse_table_field_begin(parser* p)
{
	int delta;
	if(cchr != ' ') {
		delta = parse_reverse_count_delta_indentation(p);
		if(delta == 0) {
			p->mode = MODE_TABLE_MID_SEP_OR_END;
			/* first char not evaluated to be able to cmp w/ prev */
		} else if(delta == 2) {
			CALLBACK0(begin_table_field);
			p->mode = MODE_PREWORD;
			parse_word(p);
		} else {
			p->error = "Incorrectly indented table.";
		}
	}
}

static int parse_reverse_count_delta_indentation(parser* p)
{
	int rev_ind = 0;
	int pos = p->pos;
	while(pos-- > 0 && p->buf[pos] != 0)
		rev_ind++;
	return rev_ind - p->x;
}

static void parse_table_mid_sep_or_end(parser* p)
{
	if(parse_is_at_begin(p)) {
		if(!parse_is_table_line_main(cchr) &&
						!parse_is_table_line_mid(cchr))
			p->error = "Invalid zero indented part in table.";
	} else if(cchr == '\n') {
		if(parse_is_table_line_main(p->buf[p->pos - 1])) {
			CALLBACK0(end_table);
			p->x = X_NORMAL;
			p->mode = MODE_TABLE_POST;
		} else if(parse_is_table_line_mid(p->buf[p->pos - 1])) {
			CALLBACK0(table_mid_sep);
			p->mode = MODE_TABLE_FIELD_BEGIN;
		} else {
			p->error = "Inconsistent table mid or end line / "
						"internal program error";
		}
		p->pos = parse_reverse_search(p->buf, p->pos - 1, 0);
	} else {
		if(cchr != p->buf[p->pos - 1])
			p->error = "Inconsistent table mid or end line.";
		parse_consume(p);
	}
}

static void parse_table_first_sep_space(parser* p)
{
	if(cchr == '\n' || cchr == ' ') {
		parse_consume(p);
		CALLBACK0(end_table_field);
		if(cchr == '\n') {
			p->mode = MODE_TABLE_FIELD_BEGIN;
			CALLBACK0(table_newline);
		} else {
			p->mode = MODE_TABLE_SKIP_SPACES;
			CALLBACK0(begin_table_field);
		}
	} else {
		parse_go_preword(p);
	}
}

static void parse_table_skip_spaces(parser* p)
{
	if(cchr == ' ')
		parse_consume(p);
	else
		parse_go_preword(p);
}

void parse_go_preword(parser* p)
{
	char result;
	p->mode = MODE_PREWORD;
	result = parse_word(p);
	assert(result);
}

static void parse_table_post(parser* p)
{
	if(cchr == '\n')
		p->mode = MODE_INIT;
	else
		p->error = "Tables must be followed by an empty line.";

	parse_consume(p);
}
