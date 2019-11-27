#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

#include "parse_shared.h"
#include "parse_shared_private.h"
#include "parse_callback.h"
#include "parse.h"
#include "parse_word.h"

/* -------------------------------------------------------------[ Generic ]-- */

void parse_unrelated_data(parser* p)
{
	int start = parse_reverse_search(p->buf, p->pos, 0) + 1;
	int end;
	if(parser_incstack(p)) {
		cchr = 0;
		end = p->pos;
		parse_unrelated_data_from_to(p, start, end);
	}
}

int parse_reverse_search(char* array, int es, char c)
{
	assert(es >= 0);
	for(; es >= 0; es--)
		if(array[es] == c)
			return es;
	return -1;
}

/* end exclusive */
void parse_unrelated_data_from_to(parser* p, int start, int end)
{
	int pos2;
	int len;

	assert(p->mode >= 50);
	for(pos2 = start; pos2 < end; pos2++) {
		parse_character(p, p->buf[pos2]);
		if(p->error != NULL) /* fail fast */
			return;
	}

	len = p->pos - end;
	assert(len >= 0);

	parse_interleaved_memcpy(p->buf + start, p->buf + (end + 1), len);
	p->pos = start + len - 1;
	assert(p->pos >= -1);
}

void parse_direct_cdata(parser* p, char c)
{
	char x[2];
	x[0] = c;
	x[1] = 0;
	CALLBACK1(cdata, x);
}

static void parse_interleaved_memcpy(char* out, char* in, int n)
{
	int i;
	for(i = 0; i < n; i++)
		*(out++) = *(in++);
}

/* The opposite of parse_incstack */
void parse_consume(parser* p)
{
	p->pos--;
	assert(p->pos >= -1);
}

/*
 * This function is used when the array already holds a char of the coming word
 * but it is not yet sure if the word has an escape sequence at the very first
 * position. ``Inword'' in this context only refers to the fact, that all
 * ``Preword'' specialities have already been dismissed.
 */
void parse_go_inword(parser* p)
{
	if(!parse_proc_esc(p)) {
		p->mode = MODE_INWORD;
		if(cchr == ' ')
			parse_word_end_space(p);
	}
}

#define PARSE_SKIP_SPACE(CMP) \
	while(*cdptr CMP ' ') { if(*(cdptr++) == 0) return NULL; } return cdptr

char* parse_skip_indentation(char* cdptr)
{
	PARSE_SKIP_SPACE(==);
}

char* parse_skip_list_marker(char* cdptr)
{
	PARSE_SKIP_SPACE(!=);
}

int parse_count_indentation(char* str)
{
	int ind = 0;
	for(; *str == ' '; str++)
		ind++;
	return ind;
}

/* ----------------------------------------[ Character specific functions ]-- */

char parse_is_table_line_mid(char c)
{
	return c == '+' /* || c == '\xe2' || c == '\x94' || c == '\x80' */;
}

char parse_is_list_prefix_char(char c)
{
	return c == '*' || c == '+' || c == '-';
}

char parse_is_table_line_main(char x)
{
	return x == 'o' /* || x == '\xe2' || x == '\x95' || x == '\x90' */;
}
