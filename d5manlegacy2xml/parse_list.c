#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <assert.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

#include "parse_shared.h"
#include "parse_callback.h"
#include "parse_list.h"
#include "parse_table.h"
#include "parse.h"
#include "parse_list_private.h"

/*
	TODO list items w/ different number lengths are not supported. cf.

		 1.                          01.
		 2.  currently needs to be   02.
		 3.                          03.
		10.                          10.

	TODO support a) b) c) lists

	TODO support such constructs:

		1. Idea
		    * sublist
		   Continued thoughts on the first idea...
*/

char parse_list(parser* p)
{
	switch(p->mode) {
	case MODE_LIST_PRE_BOL:         parse_list_pre_bol(p);            break;
	case MODE_LIST_BEGIN_OF_LINE:   parse_list_begin_of_line(p);      break;
	case MODE_LIST_BEGIN_OF_LINE_AT_PBOCNT:
		parse_list_begin_of_line_at_potential_begin_of_cnt(p, 1); break;
	case MODE_LIST_SUBLIST_START:   parse_list_sublist_start(p);      break;
	case MODE_LIST_AT_SUBLIST_ITEM: parse_list_at_sublist_item(p);    break;
	default: return 0;
	}
	return 1;
}

/*
 * Terminates list markers by the magic byte combination \1 \0 for recognition
 * regardless of potentially interleaved buffer data from parse_unrelated...
 */
void parse_process_begin_list(parser* p, char* cdptr)
{
	int start, end;
	char bakchr[2];

	p->mode = MODE_PREWORD;
	p->x    = X_LIST;

	/*
	 * let this not be obscured by the error handling. we merely invoke two
	 * different functions on the same variable one after each other.
	 * NULL is only returned in error cases => normally both calls are run.
	 */
	if((cdptr = parse_skip_indentation(cdptr)) == NULL ||
			(cdptr = parse_skip_list_marker(cdptr)) == NULL) {
		p->error = "Unexpected end of line";
		return;
	}

	bakchr[0] = *(++cdptr); /* bak first chr of list item cnt (skip spc) */
	*cdptr = 1;             /* mark end for later search */
	bakchr[1] = *(++cdptr); /* bak second chr of list item cnt (s. prev) */
	*cdptr = 0;             /* mark end for parse_unrelated_data */
	assert(p->pos >= (p->buf - cdptr));

	CALLBACK0(begin_list_item);
	start = parse_reverse_search(p->buf, p->pos - 1, 0) + 1;
	if(parser_incstack(p)) {
		cchr = 0;
		end = p->pos;
		parse_character(p, bakchr[0]);
		if(p->error == NULL) {
			parse_character(p, bakchr[1]);
			if(p->error == NULL)
				parse_unrelated_data_from_to(p, start, end);
		}
	}
}

/* @return list identifier char */
char parse_check_for_list(char* str)
{
	if(parse_is_list_prefix_char(str[0]))
		return *str;
	else if(parse_is_numeric_list_entry(str))
		return '1';
	else
		return 0; /* error value */
}

static char parse_is_numeric_list_entry(char* str)
{
	while(isdigit(*str)) {
		str++;
		if(*str == 0)
			return 0;
	}
	return *str == '.' && *(str + 1) == ' ';
}

/*
 * The idea behind this function is that we need to know when the list marker of
 * the now coming list item is going to end. Therefore we keep the list marker
 * of the previous item and mark-forward (a.k.a. ``permark'') the buffer with
 * signal characters which are then verified by parse_list_begin_of_line below.
 * In order to stay compatible w/ parse_unrelated* functions we need to
 * temporarily move the cursor after our premarked data and reverse this in
 * parse_list_pre_bol (should be directly below this function)
 * 
 * Examples (_ = space, # = not interesting, 0 = 0 byte, 1 = 1 byte, 2. list nr)
 *
 * a) ###0_2._10 -> ###0_2._10aaaab    pos stays at the second 0 byte)
 * b) _2._10     -> _2._10aaaab        - || -
 * c) _2._10###0 -> _2._10###0aaaab    - || -
 */
void parse_premark_indentation_characters(parser* p)
{
	int before, end, i;

	assert(cchr == 0);
	before = parse_reverse_search(p->buf, p->pos - 1, 1);
	assert(before > -1);
	/* -1 to ignore the 1 char when counting (0 is already excluded) */
	end = p->pos + before - parse_reverse_search(p->buf, before - 1, 0) - 1;

	if(end + 2 >= STACKSIZ) {
		p->error = "Stack size exceeded.";
	} else {
		for(i = p->pos + 1; i < end + 1; i++)
			p->buf[i] = MARK_BEFORE_END_OF_INDENTATION;
		p->buf[i] = MARK_ONE_AFTER_INDENTATION;
		/*
		 * this is necessary to let parse_unrelated_data_from_to copy
		 * this back properly.
		 */
		p->pos = i;
	}
}

/* Reverse p->pos = i; */
static void parse_list_pre_bol(parser* p)
{
	char bakchr = cchr;
	p->pos      = parse_reverse_search(p->buf, p->pos, 0) + 1;
	cchr        = bakchr;
	p->mode     = MODE_LIST_BEGIN_OF_LINE;
	parse_list_begin_of_line(p);
}

static void parse_list_begin_of_line(parser* p)
{
	/* garanted to exist by marking function */
	char nextchr = p->buf[p->pos + 1];
	if(nextchr == MARK_ONE_AFTER_INDENTATION)
		p->mode = MODE_LIST_BEGIN_OF_LINE_AT_PBOCNT;
	else if(nextchr != MARK_BEFORE_END_OF_INDENTATION)
		p->error = "List control char mark not properly terminated. "
					"This is most likely a program bug.";
	else if(cchr == '\n')
		parse_list_begin_of_line_at_potential_begin_of_cnt(p, 0);

	/*
	 * The last case happens if we end a document while we expect some chars
	 * to complete the `premarking`. In order for the document to end
	 * correctly if no characters follow, we test for sudden newlines during
	 * the marking process. These newlines can never occur in list markers
	 * and are therefore safe to process separately. (HTH)
	 */
}

static void parse_list_begin_of_line_at_potential_begin_of_cnt(parser* p,
								char complete)
{
	int now_start = parse_reverse_search(p->buf, p->pos - 1, 0);
	char bk = cchr;
	cchr = 0;

	if(complete && parse_list_is_spaces_only(p->buf + now_start + 1)) {
		/* EXPECT 0_1._0____0| bk=? => continued item or sublist */
		if(bk == ' ') {
			/* EXPECT 0_1._0____0| bk=_ => sublist starts here */
			cchr = bk;
			p->mode = MODE_LIST_SUBLIST_START;
		} else {
			/* EXPECT 0_1._0____0| bk=? => continue list item */
			p->pos = now_start;    /* ignore all the spaces     */
			CALLBACK1(cdata, " "); /* and replace them with one */
			if(p->error == NULL) {
				p->mode = MODE_PREWORD;
				parse_character(p, bk);
			}
		}
	} else {
		/* EXPECT 0_1._0_2._0| bk=? => new list item */
		parse_list_begin_of_line_with_list_item(p, now_start);
		if(p->error == NULL)
			parse_character(p, bk);
	}
}

static char parse_list_is_spaces_only(char* str)
{
	while(*str == ' ')
		str++;
	return *str == 0;
}

static void parse_list_begin_of_line_with_list_item(parser* p, int s0)
{
	/* int now_start = s0; (FYI) */
	int now_ind    = parse_count_indentation(p->buf + s0 + 1);
	int prev_start = parse_reverse_search(p->buf, s0 - 1, 0);
	int prev_ind   = parse_count_indentation(p->buf + prev_start + 1);

	if(prev_ind < now_ind) {
		p->error = "Disruptive mix of tabs and spaces detected. "
			"Indent same things using the same chars or mixture.";
	} else {
		CALLBACK0(end_list_item);
		parse_list_recursive_end(p, prev_ind, prev_start, now_ind, s0);
	}
}

/*
 * The name of this function is mainly of historical interest. It is now also
 * used to handle the case of a simple new list item being created. Closing is
 * optional (i.e. the while(prev_ind > now_ind) does not need to be entered...)
 */
#define COPY do { p->buf[++p->pos] = *c; } while(*(c++) != 0)
static void parse_list_recursive_end(parser* p, int prev_ind, int prev_start,
						int now_ind,  int now_start)
{
	char* c = p->buf + now_start + 1;
	int rpprev_start;
	/* TODO z THIS IS NOT IDEAL: WE DO NOT WANT TO HAVE TO SPLIT REAL
		DESCRIPTION LISTS BUT WANT A DEDICATED DESCRIPTION LIST TYPE! CSTAT HONOR THIS! */
	char is_real_description_list = now_ind == 0 &&
						p->buf[now_start + 1] != '\n';

	p->mode = MODE_PREWORD;

	while(prev_ind > now_ind) {
		/*
		if(prev_start == -1 && is_real_description_list) {
			p->error = "EDBUG";
			return;
		}
		TODO z IDEA / UTILIZE BEGIN DESCRIPTION LIST?
			1. REMOVE 0 marker between "indentation" and first char of content
			2. make x normal
			3. switch to mode which is description list title mode (currently this is all done in begin_description_list -> need states for that
			4. process charaters already got
			5. go on just like normal begin description list
		=>
			Begin description list is replaced by three phases
			1. START_DESCRIPTION_LIST (creates <list>)
			2. START_DESCRIPTION_LIST_TITLE (creates <lt>)
			3. "END_DESCRIPTION_LIST_TITLE" to extract the marker etc.

		if(prev_start == -1 && is_real_description_list) {
			assert(now_ind == 0);
			p->pos = -1;
			COPY;
			parse_unrelated_data_from_to(p, 0, p->pos);
			return;
		}
		*/
		CALLBACK0(end_list);
		if(prev_start == -1) {
			assert(now_ind == 0);
			p->x = X_NORMAL;
			p->pos = -1;
			if(is_real_description_list) /* insert required \n */
				p->buf[++p->pos] = '\n';
			COPY;
			CALLBACK0(end_list_group);
			parse_unrelated_data_from_to(p, 0, p->pos);
			return;
		} else {
			prev_start = parse_reverse_search(p->buf,
							prev_start - 1, 0);
			prev_ind = parse_count_indentation(p->buf +
								prev_start + 1);
			CALLBACK0(end_list_item);
		}
	}

	assert(prev_ind == now_ind);

	/*
	 * EXPECT e.g. ..._1._10_____*_10_____*_sub_2\n0...
	 *                      ^   ^   ^    ^
	 *                      |   |   |    |
	 *                      |   |   |    now_ind
	 *                      |   |   now_start
	 *                      |   prev_ind
	 *                      prev_start
	 * We must now cut off the list item marker (e.g. _1._) and treat the
	 * rest normal list item (content), i.e. normal document data.
	 */

	if((c = parse_skip_indentation(c)) == NULL) {
		p->error = "Unexpected end of line";
		return;
	}

	parse_list_handle_pro_contra_mixed_list(p, c, p->buf + prev_start +
								prev_ind + 1);

	CALLBACK0(begin_list_item);

	rpprev_start = parse_list_process_mid(p, c, prev_start);
	if(rpprev_start == -2)
		return;

	/* EXPECT e.g. ..._1._10______*_10sub_2\n0... */

	/*
	 * TODO z AN OBSCURE BUG
	 *
	 * CONSIDER THIS:
	 *
	 * 	_1._
	 *	_____*_
	 *	________+_        -> length a (indentation) = 8
	 *	_2._X\n           -> length b (two lines)   = 7
	 *	\n
	 *
	 * if b < a => recursive invocation
	 * 	while this parse_unrelated_data statement is in the process of
	 *	being evaluated, this function is invoked again leading to
	 *	unspecified results because we currently assume that all parts
	 *	are consecutive.
	 */
	parse_unrelated_data_from_to(p, rpprev_start + 1, p->pos);

}

static void parse_list_handle_pro_contra_mixed_list(parser* p, char* c,
							char* prev_list_marker)
{
	/*
	 * This special code is responsible for allowing the special case of pro
	 * contra lists changing their orientation (contra list -> pro list or 
	 * the other way around) without any additional newline. It is a bit
	 * hacky but fully functional.
	 */
	if(*prev_list_marker == '-' && *c == '+') {
		CALLBACK0(end_list);
		CALLBACK1(begin_list, '+');
		*prev_list_marker = '+';
	} else if(*prev_list_marker == '+' && *c == '-') {
		CALLBACK0(end_list);
		CALLBACK1(begin_list, '-');
		*prev_list_marker = '-';
	}
}

static int parse_list_process_mid(parser* p, char* c, int prev_start)
{
	int rpprev_start;

	if((c = parse_skip_list_marker(c)) == NULL) {
		p->error = "Unexpected end of line";
		return -2;
	}

	c++; /* skip leading space */

	rpprev_start = prev_start;
	while(p->buf[++rpprev_start] != 0)
		;

	p->pos = rpprev_start;
	COPY;

	assert(cchr == 0);
	return rpprev_start;
}

#undef COPY

static void parse_list_sublist_start(parser* p)
{
	if(cchr != ' ')
		p->mode = MODE_LIST_AT_SUBLIST_ITEM;
}

static void parse_list_at_sublist_item(parser* p)
{
	char lc;
	int start;
	char* chck;
	assert(p->x == X_LIST);
	if(cchr == ' ') {
		start = parse_reverse_search(p->buf, p->pos - 1, 0) + 1;
		if(parser_incstack(p)) {
			cchr = 1;
			if(parser_incstack(p)) {
				cchr = 0;
				chck = parse_skip_indentation(p->buf + start);
				p->mode = MODE_PREWORD;
				if((lc = parse_check_for_list(chck)) == 0) {
					p->error = "Improper list item marker.";
				} else {
					CALLBACK1(begin_list, lc);
					CALLBACK0(begin_list_item);
				}
			}
		}
	}
}

void parse_begin_description_list(parser* p)
{
	int start_title, start_data;
	char lc;
	char* list_identifier;
	char* sub;

	assert(p->mode == MODE_READAHEAD_DESCR_SUB); /* we come from here */
	cchr = 0;

	/* EXPECT ... 0 title 0 line 0| ... */
	/* TODO z PROBLEM: WHAT IF TITLE CONTAINS MULTILINE URL? */
	/* TODO z ASTAT FIRST PHASE */
	CALLBACK0(begin_list_description_title);
	start_data      = parse_reverse_search(p->buf, p->pos     - 1, 0);
	start_title     = parse_reverse_search(p->buf, start_data - 1, 0);
	p->mode         = MODE_PREWORD;
	parse_unrelated_data_from_to(p, start_title + 1, start_data);
	parse_character(p, '\n'); /* flush */
	if(p->error != NULL)
		return;
	CALLBACK0(end_list_description_title);

	/* TODO z ASTAT SECOND (AND MORE) PHASE */
	/* EXPECT ... 0 line 0| ... */
	/*  => start_data (~ start_line) is now invalid */
	/* TODO z PART RED w/ table parse_table_or_code_end_of_unknown_line */
	start_data      = parse_reverse_search(p->buf, p->pos - 1, 0);
	sub             = p->buf + start_data + 1;
	list_identifier = parse_skip_indentation(sub);
	lc              = parse_check_for_list(list_identifier);
	CALLBACK1(begin_list, lc); /* TODO z ASTAT IMPLEMENT REAL DESCRIPION LIST... HOW? / IDEA: WHENEVER A LIST ENDS we set a flag to close the list when the preprocessing results in no further description list / otherwise we do not open the list again here. / requires checking the usage of p->x after list has ended. Could a constant be a value to mark this? / Seems OK as we use X_LIST and X_... to mark this. / Better: Use p->mode to recognize this */

	if(lc == 0) { /* text only */
		if(list_identifier - sub < 3) {
			p->error = "Description list content needs to be "
						"indented with three or more "
						"spaces or at least one tab";
			return;
		} else {
			/* EXPECT ... 0 sub list_identifier cnt| */
			/* EXPECT ... 0 _ _ _ T e x t| */
			/* sub        ^       ^ */
			/* list identifier    | */
			*(list_identifier - 2) = '*';
			/* EXPECT ... 0 _ * _ T e x t| */
		}
	}

	/* TODO z ASTAT LAST DESCRIPTION LIST START PHASE */
	cchr = '\n'; /* mark end of list line */
	parse_process_begin_list(p, sub);
}
