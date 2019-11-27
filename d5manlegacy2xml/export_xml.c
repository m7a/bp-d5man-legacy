#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "parser_general.h"
#include "parse_callback.h"
#include "parse.h"
#include "mawxml.h"
#include "export_xml.h"
#include "export_xml_private.h"

#define DOCSTAT          struct doc_status* d = data
#define MAWXML0(M)       mawxml_##M(d->m)
#define MAWXML1(M, X)    mawxml_##M(d->m, X)
#define MAWXML2(M, X, Y) mawxml_##M(d->m, X, Y)

void export_xml_init_parse_callback(struct parse_callback* c)
{
	struct doc_status* d            = malloc(sizeof(struct doc_status));
	d->m                            = mawxml_init(stdout);
	c->data                         = d;

	c->begin_document               = begin_document;
	c->meta_kv                      = meta_kv;
	c->meta_tex                     = meta_tex;
	c->meta_end                     = meta_end;

	c->end_document                 = end_document;
	c->begin_tex                    = begin_tex;
	c->begin_tex_math               = begin_tex_math;
	c->begin_tex_html               = begin_tex_html;
	c->texchar                      = texchar;
	c->end_tex                      = end_tex;
	c->end_tex_math                 = end_tex_math;
	c->end_tex_html                 = end_tex_html;
	c->section                      = section;
	c->subsection                   = subsection;
	c->cdata                        = cdata2;
	c->paragraph                    = paragraph;
	c->forced_space                 = forced_space;
	c->link                         = link;
	c->begin_emphasis               = begin_emphasis;
	c->end_emphasis                 = end_emphasis;
	c->begin_english_quot           = begin_english_quot;
	c->end_english_quot             = end_quot;
	c->begin_german_quot            = begin_german_quot;
	c->end_german_quot              = end_quot;
	c->half_space                   = sym_half_space;
	c->sym_rightarrow1              = sym_rightarrow1;
	c->sym_rightarrow2              = sym_rightarrow2;
	c->sym_leftarrow                = sym_leftarrow;
	c->sym_dots                     = sym_dots;
	c->sym_smiley                   = sym_smiley;
	c->sym_math_in                  = sym_math_in;
	c->sym_dash                     = sym_dash;
	c->sym_exclamation              = sym_exclamation;
	c->exp                          = process_exp;
	c->begin_shortcut               = begin_shortcut;
	c->shortcut_key                 = shortcut_key;
	c->end_shortcut                 = end_shortcut;
	c->begin_code_inline            = begin_code_inline;
	c->end_code_inline              = end_code_inline;
	c->begin_table                  = begin_table;
	c->begin_table_field            = begin_table_field;
	c->end_table_field              = end_table_field;
	c->table_newline                = table_newline;
	c->table_mid_sep                = table_mid_sep;
	c->end_table                    = end_table;
	c->begin_list_group             = begin_list_group;
	c->begin_list                   = begin_list;
	c->begin_list_description_title = begin_list_description_title;
	c->end_list_description_title   = end_list_description_title;
	c->begin_list_item              = begin_list_item;
	c->end_list_item                = end_list_item;
	c->end_list                     = end_list;
	c->end_list_group               = end_list_group;
	c->begin_code                   = begin_code;
	c->end_code                     = end_code;

	MAWXML1(xml_line,   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	MAWXML1(xml_line,   "\n<!DOCTYPE d5man SYSTEM \"d5manxml.dtd\">\n");
	MAWXML1(open_block, "d5man format_ver=\"1.1.0.0\"");
}

void export_xml_reset_parse_callback(struct parse_callback* c)
{
	struct doc_status* d = c->data;
	d->insec     = 0;
	d->insubsec  = 0;
	d->inmetatex = 0;
}

void export_xml_close_parse_callback(struct parse_callback* c)
{
	struct doc_status* d = c->data;
	MAWXML1(close, "d5man");
	MAWXML0(free);
	free(d);
}

static void begin_document(void* data)
{
	DOCSTAT;
	MAWXML1(open_block, "page");
	MAWXML1(open_block, "meta");
}

static void meta_kv(void* data, char* key, char* value)
{
	DOCSTAT;
	MAWXML1(begin_block, "kv");
	MAWXML2(attribute, "k", key);
	MAWXML2(attribute, "v", value);
	MAWXML0(empty);
}

/* TODO z HACKY / IMPROVE THIS USING HOLDBACK MENCHANISM / or move to core d5man2xml. Note that it has a bug when meta_tex is just one line it will generate open tag but not strip off the trailing '> ' */
static void meta_tex(void* data, char* line)
{
	size_t llen = strlen(line);
	char* l2o = malloc(sizeof(char) * (llen + 1));
	char* l2 = l2o;
	DOCSTAT;
	strcpy(l2, line);
	switch(d->inmetatex) {
	case 0:
		if(line[0] == '<' && line[1] == ' ') {
			MAWXML1(open_block, "raw_xml");
			d->inmetatex = 2;
			l2 += 2; /* strip leading "< " */
		} else {
			MAWXML1(open_block, "tex");
			d->inmetatex = 1;
		}
		break;
	case 1:
		break;
	case 2:
		if(strcmp(l2 + llen - 2, " >") == 0) {
			l2[llen - 2] = 0; /* strip off tailing " >" */
			d->inmetatex = 3;
		}
		break;
	case 3:
		MAWXML0(newline);
		MAWXML1(content, " >");
		break;
	}
	MAWXML1(content, l2);
	if(d->inmetatex != 3)
		MAWXML0(newline);
	free(l2o);
}

static void meta_end(void* data)
{
	DOCSTAT;
	if(d->inmetatex) {
		switch(d->inmetatex) {
		case 1:
			MAWXML1(close, "tex"); break;
		case 3:
			MAWXML0(newline); /* fall through */
		case 2:
			MAWXML1(close, "raw_xml");
		}
		d->inmetatex = 0;
	}
	MAWXML1(close, "meta");
}

static void end_document(void* data)
{
	DOCSTAT;
	close_subsec(data);
	MAWXML1(close, "section");
	MAWXML1(close, "page");
	/*
	 * It is vital that the indentation of the closing tag is 1 in order for
	 * D5Man2XML Java side to recognize it (as an error, not as a hanging
	 * program)
	 */
	assert(MAWXML0(get_stack_usage) == 1);
	fflush(stdout);
}

static void begin_tex(void* data)
{
	DOCSTAT;
	MAWXML1(open_raw, "tex");
}

static void begin_tex_math(void* data)
{
	DOCSTAT;
	MAWXML1(open_raw, "math");
}

static void begin_tex_html(void* data)
{
	DOCSTAT;
	MAWXML1(open_inline, "raw_xml");
}

static void texchar(void* data, char cc)
{
	DOCSTAT;
	char c[2];
	c[0] = cc;
	c[1] = 0;
	MAWXML1(content, c);
}

static void end_tex(void* data)
{
	DOCSTAT;
	MAWXML1(close, "tex");
}

static void end_tex_math(void* data)
{
	DOCSTAT;
	MAWXML1(close, "math");
}

static void end_tex_html(void* data)
{
	DOCSTAT;
	MAWXML1(close, "raw_xml");
}

/* TODO z largely redundant w/ subsection */
static void section(void* data, char* title)
{
	DOCSTAT;

	close_subsec(d);

	if(d->insec)
		MAWXML1(close, "section");
	else
		d->insec = 1;

	MAWXML1(begin_block, "section");
	MAWXML2(attribute, "title", title);
	MAWXML0(open);
}

static void close_subsec(struct doc_status* d)
{
	if(d->insubsec) {
		MAWXML1(close, "section");
		d->insubsec = 0;
	}
}

static void subsection(void* data, char* title)
{
	DOCSTAT;

	if(d->insubsec)
		MAWXML1(close, "section");
	else
		d->insubsec = 1;

	MAWXML1(begin_block, "section");
	MAWXML2(attribute, "title", title);
	MAWXML0(open);
}

static void cdata2(void* data, char* cdata3)
{
	DOCSTAT;
	MAWXML1(content, cdata3);
}

static void paragraph(void* data)
{
	DOCSTAT;
	MAWXML1(begin_block, "par");
	MAWXML0(empty);
}

static void forced_space(void* data)
{
	DOCSTAT;
	MAWXML1(begin_inline, "sym");
	MAWXML2(attribute, "v", "space_forced");
	MAWXML1(set_escaping, 0);
	MAWXML2(attribute, "raw", "&#160;");
	MAWXML1(set_escaping, 1);
	MAWXML0(empty);
}

static void link(void* data, char* dest, char* title)
{
	DOCSTAT;
	int x = atol(dest);

	MAWXML1(begin_inline, "link");

	if(x == 0)
		MAWXML2(attribute, "url", dest);
	else
		MAWXML2(attribute, "section", dest);

	MAWXML2(attribute, "title", title);
	MAWXML0(empty);
}

static void begin_emphasis(void* data)
{
	DOCSTAT;
	MAWXML1(open_inline, "em");
}

static void end_emphasis(void* data)
{
	DOCSTAT;
	MAWXML1(close, "em");
}

static void begin_english_quot(void* data)
{
	DOCSTAT;
	begin_quot(d, "en");
}

static void begin_quot(struct doc_status* d, char* lang)
{
	MAWXML1(begin_inline, "q");
	MAWXML2(attribute, "lang", lang);
	MAWXML0(open);
}

static void end_quot(void* data)
{
	DOCSTAT;
	MAWXML1(close, "q");
}

static void begin_german_quot(void* data)
{
	DOCSTAT;
	begin_quot(d, "de");
}

#define ISYMF(F, S, C) static void sym_##F(void* data) { symbol(data, S, C); }
ISYMF(rightarrow1, "rightarrow1", 8594)
ISYMF(rightarrow2, "rightarrow2", 8658)
ISYMF(leftarrow,   "leftarrow",   8592)
ISYMF(dots,        "dots",        8230)
ISYMF(smiley,      "smiley",      9786)
ISYMF(math_in,     "math_in",     8714)
ISYMF(dash,        "dash",        8211)
ISYMF(half_space,  "half_space",  8239)
ISYMF(exclamation, "exclamation", 9888)
#undef ISYMF

static void symbol(void* data, char* name, unsigned entity)
{
	DOCSTAT;
	char eval[sizeof(entity) * 2 + 1];
	/* TODO z PROBLEM SYMBOL TAGS NOT INDENTED (NEED TO BE TREATED LIKE CHARS) */
	MAWXML1(begin_raw, "sym");
	MAWXML2(attribute, "v", name);
	MAWXML1(set_escaping, 0);
	MAWXML1(content, " raw=\"&#x");
	sprintf(eval, "%x", entity);
	MAWXML1(content, eval);
	MAWXML1(content, ";\"");
	MAWXML1(set_escaping, 1);
	MAWXML0(empty);
}

static void process_exp(void* data, char* a, char* b)
{
	DOCSTAT;
	MAWXML1(begin_inline, "exp");
	MAWXML2(attribute, "base", a);
	MAWXML2(attribute, "exponent", b);
	MAWXML0(empty);
}

static void begin_shortcut(void* data)
{
	DOCSTAT;
	MAWXML1(open_inline, "shortcut");
}

static void shortcut_key(void* data, char* key)
{
	DOCSTAT;
	MAWXML1(begin_inline, "key");
	MAWXML2(attribute, "x", key);
	MAWXML0(empty);
}

static void end_shortcut(void* data)
{
	DOCSTAT;
	MAWXML1(close, "shortcut");
}

static void begin_code_inline(void* data)
{
	DOCSTAT;
	MAWXML1(open_inline, "c");
}

static void end_code_inline(void* data)
{
	DOCSTAT;
	MAWXML1(close, "c");
}

static void begin_table(void* data, char* caption)
{
	DOCSTAT;
	MAWXML1(begin_block, "table");
	if(caption != NULL)
		MAWXML2(attribute, "title", caption);
	MAWXML0(open);
}

static void begin_table_field(void* data)
{
	DOCSTAT;
	MAWXML1(open_block, "tf");
}

static void end_table_field(void* data)
{
	DOCSTAT;
	MAWXML1(close, "tf");
}

static void table_newline(void* data)
{
	DOCSTAT;
	MAWXML1(begin_block, "tnl");
	MAWXML0(empty);
}

static void table_mid_sep(void* data)
{
	DOCSTAT;
	MAWXML1(begin_block, "tms");
	MAWXML0(empty);
}

static void end_table(void* data)
{
	DOCSTAT;
	MAWXML1(close, "table");
}

static void begin_list_group(void* data)
{
	DOCSTAT;
	MAWXML1(open_block, "lg");
}

static void begin_list(void* data, char lc)
{
	DOCSTAT;
	char* type;
	switch(lc) {
	case 0:   type = "desc"; break;
	case '*': type = "ul";   break;
	case '1': type = "num";  break;
	case '+': type = "pro";  break;
	case '-': type = "con";  break;
	default:  type = "?";    break;
	}
	MAWXML1(begin_block, "list");
	MAWXML2(attribute, "type", type);
	MAWXML0(open);
}

static void begin_list_description_title(void* data)
{
	DOCSTAT;
	MAWXML1(open_inline, "lt");
}

static void end_list_description_title(void* data)
{
	DOCSTAT;
	MAWXML1(close, "lt");
}

static void begin_list_item(void* data)
{
	DOCSTAT;
	MAWXML1(open_block, "i");
}

static void end_list_item(void* data)
{
	DOCSTAT;
	MAWXML1(close, "i");
}

static void end_list(void* data)
{
	DOCSTAT;
	MAWXML1(close, "list");
}

static void end_list_group(void* data)
{
	DOCSTAT;
	MAWXML1(close, "lg");
}

static void begin_code(void* data)
{
	DOCSTAT;
	MAWXML1(begin_raw, "codeblock");
	MAWXML2(attribute, "xml:space", "preserve");
	MAWXML0(open);
}

static void end_code(void* data)
{
	DOCSTAT;
	MAWXML1(close, "codeblock");
	MAWXML0(newline);
}
