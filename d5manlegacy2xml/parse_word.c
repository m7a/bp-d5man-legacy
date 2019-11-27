#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

#include "parse_shared.h"
#include "parse_callback.h"
#include "parse_word.h"
#include "parse_list.h"
#include "parse_word_private.h"

#include "parse.h"

char parse_word(parser* p)
{
	switch(p->mode) {
	case MODE_PREWORD:               parse_preword(p);               break;
	case MODE_INWORD:                parse_inword(p);                break;
	case MODE_ESCAPE_INWORD:         parse_escape_inword(p);         break;
	case MODE_POT_START_EXCL:        parse_pot_start_excl(p);        break;
	case MODE_POT_END_EXCL:          parse_pot_end_excl(p);          break;
	case MODE_INWORD_POTENTIAL_SEP:  parse_inword_potential_sep(p);  break;
	case MODE_PREWORD_POT_EMPTY_SYM: parse_preword_pot_empty_sym(p); break;
	default: return 0;
	}
	return 1;
}

/* --------------------------------------------------------[ MODE_PREWORD ]-- */

static void parse_preword(parser* p)
{
	switch(cchr) {
	case '\n':
		parse_consume(p);
		CALLBACK1(cdata, "\n");
		p->mode = MODE_INIT;
		break;
	case ' ':
		parse_consume(p);
		/*
		 * This helps program debugging and makes the creation of bad
		 * input files more difficult. Space errors are fatal!
		 */
		p->error = "Unexpected space.";
		break;
	case '`':
		p->mode = MODE_PCODE;
		break;
	case '\"':
		p->mode = MODE_PREQUOT_GER;
		break;
	case '_':
		parse_consume(p);
		CALLBACK0(begin_emphasis);
		break;
	case '(':
		p->mode = MODE_POT_START_EXCL;
		break;
	case '[':
		p->mode = MODE_POTENTIAL_SHORTCUT;
		break;
	case '{':
		/* important: bracket is not consumed */
		p->mode = MODE_TEX_BEGIN;
		break;
	case '~':
		if(p->x > 0) { /* this is only relevant inside tables */
			parse_consume(p);
			p->mode = MODE_PREWORD_POT_EMPTY_SYM;
			break;
		}
	default:
		parse_go_inword(p);
		break;
	}
}

/* ---------------------------------------------------------[ MODE_INWORD ]-- */

static void parse_inword(parser* p)
{
	if(parse_proc_esc(p))
		return;
	switch(cchr) {
	case '(':
		/* :( and :) are smileys => N_PROC */
		if(p->buf[p->pos - 1] != ':') {
			cchr = 0;
			p->mode = MODE_LINK;
		}
		break;
	case ' ':
	case '\n':
		parse_word_end_space(p);
		break;
	case '\"':
	case '\'':
		p->mode = MODE_PRE_EQUOT;
		break;
	case '~':
		parse_consume(p);
		parse_flush_stack(p, 1, 0);
		CALLBACK0(forced_space);
		p->mode = MODE_PREWORD;
		break;
	case '-':
		parse_consume(p);
		p->mode = MODE_INWORD_POTENTIAL_SEP;
		break;
	default:
		if(parse_is_inword_end_of_emphasis(p)) {
			p->buf[p->pos - 1] = cchr;
			parse_consume(p);
			parse_flush_stack(p, 1, 1);
		}
		break;
	}
}

int parse_proc_esc(parser* p)
{
	if(cchr == '\\') {
		parse_consume(p);
		p->mode = MODE_ESCAPE_INWORD;
		return 1;
	} else {
		return 0;
	}
}

/* note: echr vs cchr. */
void parse_word_end_space(parser* p)
{
	char end_of_emphasis = p->buf[p->pos - 1] == '_';
	char echr = cchr;
	char end_of_table_element = (echr == '\n' && p->x > 0);

	assert(echr == ' ' || echr == '\n');

	if(end_of_emphasis) {
		p->pos--;
		assert(cchr == '_');
		cchr = echr;
	}

	if(end_of_table_element) {
		assert(cchr == ' ' || cchr == '\n');
		parse_consume(p);
	}

	parse_flush_stack(p, 1, end_of_emphasis);

	if(end_of_table_element) {
		p->mode = MODE_TABLE_FIELD_BEGIN;
		CALLBACK0(end_table_field);
		CALLBACK0(table_newline);
	} else if(echr == '\n') {
		if(p->x == X_NORMAL) {
			p->mode = MODE_INIT;
		} else if(p->x == X_LIST) {
			p->mode = MODE_LIST_PRE_BOL;
			parse_premark_indentation_characters(p);
		} else {
			p->error = "Unreachable code executed. Program bug.";
			return;
		}
	} else if(p->x > 0) {
		p->mode = MODE_TABLE_FIRST_SEP_SPACE;
	} else {
		p->mode = MODE_PREWORD;
	}
}

void parse_flush_stack(parser* p, char substitute, char endproc)
{
	int offset;
	char* str;
	/*
	 * Concerning p->pos != -1: Empty word does not need to be flushed.
	 *
	 * INWORD does not only mean we are in a word which is guranteed to
	 * contain characters -- it can just as well be set after having
	 * processed some data and knowing a space is to be followed to process
	 * the space just as normal. In tables and such, however, this space
	 * will be consumed which means the word is really _empty_ and therefore
	 * we need this check.
	 */
	if(p->pos != -1 && parser_incstack(p)) {
		cchr = 0; /* NUL terminate */
		offset = parse_reverse_search(p->buf, p->pos - 1, 0);
		str = p->buf + (offset + 1);
		if(substitute)
			parse_callback_after_substitution(p, str, endproc);
		else
			CALLBACK1(cdata, str);
		p->pos = offset;
	}
}

/* TODO z code style: looks a bit strange because we need to differenciate different types of word ends for symbol recogniztion etc. */
static void parse_callback_after_substitution(parser* p, char* str, char isem)
{
	void (*fun)(void* data);
	char* echr = str + (strlen(str) - 1);
	char end_is_word_end = parse_is_soft_word_end_for_symbols(echr);
	char bak = 'X'; /* otherwise gcc complains about uninitialized */

	if(p->x > 0) { /* skip leading spaces in tables */
		while(*str == ' ')
			str++;
	}

	if(end_is_word_end) {
		bak = *echr;
		*echr = 0; /* cut off "space" character (cf. below) */
	}

	fun = parse_get_symbol_function(p, str);

	if((!end_is_word_end) && parse_is_soft_word_end_for_rest(echr)) {
		bak = *echr;
		*echr = 0; /* cut off "space" character (cf. below) */
		end_is_word_end = 1;
	}

	if(fun == NULL)
		parse_callback_after_exponentiation(p, str);
	else
		fun(CALLBACK->data);

	if(isem)
		CALLBACK0(end_emphasis);

	if(end_is_word_end) {
		/*
		 * Process word end separately because it would otherwise
		 * interfer w/ exponentiation or special symbol processing.
		 */
		*echr = bak;
		/*
		 * do not process tailing '.' if three dots have been replaced
		 * w/ a ... symbol
		 */
		if(!(bak == '.' && fun == CALLBACK->sym_dots))
			CALLBACK1(cdata, echr);
	}
}

static char parse_is_soft_word_end_for_symbols(char* str)
{
	return strstr(PARSE_SHARED_SEPS ")]'\"}", str) != NULL;
}

static void (*parse_get_symbol_function(parser* p, char* str))(void* data)
{
	#define ASSOC(S, F) if(strcmp(S, str) == 0) return CALLBACK->sym_##F
	ASSOC("->",  rightarrow1);
	ASSOC("=>",  rightarrow2);
	ASSOC("<-",  leftarrow);
	ASSOC("...", dots);
	ASSOC(":)",  smiley);
	ASSOC("e",   math_in);
	ASSOC("--",  dash);
	#undef ASSOC
	return NULL;
}

static char parse_is_soft_word_end_for_rest(char* str)
{
	return strstr(".,!;/\\", str) != NULL;
}

static void parse_callback_after_exponentiation(parser* p, char* str)
{
	char* found = strchr(str, '^');
	/* avoid too short strings and match at end of string */
	if(found == NULL || strlen(str) < 3 || *(found + 1) == 0) {
		CALLBACK1(cdata, str);
	} else {
		*found = 0;
		if(*str == 0) /* avoid match at begin of string */
			CALLBACK1(cdata, str);
		else
			CALLBACK2(exp, str, found + 1);
	}
}

static char parse_is_inword_end_of_emphasis(parser* p)
{
	char c[2];
	c[0] = cchr;
	c[1] = 0;
	return parse_is_soft_word_end(c) && p->buf[p->pos - 1] == '_';
}

char parse_is_soft_word_end(char* str)
{
	return parse_is_soft_word_end_for_symbols(str) ||
					parse_is_soft_word_end_for_rest(str);
}

/* --------------------------------------------------[ MODE_ESCAPE_INWORD ]-- */

static void parse_escape_inword(parser* p)
{
	if(cchr == ',') {
		parse_consume(p);
		parse_flush_stack(p, 1, 0);
		CALLBACK0(half_space);
		p->mode = MODE_PREWORD;
	} else {
		p->mode = MODE_INWORD;
	}
}

/* -----------------------------------------------------[ MODE_POT_*_EXCL ]-- */

static void parse_pot_start_excl(parser* p)
{
	if(cchr == '!')
		p->mode = MODE_POT_END_EXCL;
	else
		parse_pot_start_excl_failed(p);
}

static void parse_pot_start_excl_failed(parser* p)
{
	parse_direct_cdata(p, p->buf[p->pos - 1]);
	p->pos -= 2;
	p->mode = MODE_PREWORD;
	/* + 2 reverses -= 2 from before */
	parse_character(p, p->buf[p->pos + 2]);
}

static void parse_pot_end_excl(parser* p)
{
	if(cchr == ')') {
		CALLBACK0(sym_exclamation);
		p->pos -= 3;
		p->mode = MODE_INWORD; /* next space terminates "word" */
	} else {
		parse_pot_end_excl_failed(p);
	}
}

static void parse_pot_end_excl_failed(parser* p)
{
	char minibuf[2];

	parse_direct_cdata(p, p->buf[p->pos - 2]);

	minibuf[0] = p->buf[p->pos - 1];
	minibuf[1] = cchr;
	p->mode = MODE_PREWORD;

	parse_character(p, minibuf[0]);
	if(p->error == NULL)
		parse_character(p, minibuf[1]);
}

/* -------------------------------------------[ MODE_INWORD_POTENTIAL_SEP ]-- */

static void parse_inword_potential_sep(parser* p)
{
	char cbak = cchr;
	if(cbak == '-') {
		parse_consume(p);
		parse_flush_stack(p, 1, 0);
		CALLBACK0(sym_dash);
		p->mode = MODE_PREWORD;
	} else {
		cchr = '-';
		p->mode = MODE_INWORD;
		parse_character(p, cbak);
	}
}

/* ---------------------------------------[ MODE_PREWORD_POT_EMPTY_SYMBOL ]-- */

static void parse_preword_pot_empty_sym(parser* p)
{
	if(cchr == ' ' || cchr == '\n') {
		parse_word_end_space(p);
	} else if(parser_incstack(p)) {
		cchr = p->buf[p->pos - 1];
		p->buf[p->pos - 1] = '~';
		p->mode = MODE_INWORD;
	}
}
