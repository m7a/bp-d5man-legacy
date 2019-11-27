#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta.h"
#include "meta_parsing.h"
#include "meta_private.h"

parser* meta_init(struct meta_callback* callback)
{
	return parser_init(callback);
}

int meta_proc(parser* p, char chr)
{
	return parser_proc(p, chr, meta_run);
}

int meta_section_proc(parser* p)
{
	if(p->mode == MODE_ESEC_EXT) {
		assert(p->pos == 0);
		p->mode = MODE_ESEC;
	}
	meta_run(p);
	if(p->error != NULL)
		return META_SECTION_PROC_ERROR;
	switch(p->mode) {
	case MODE_PSEC:
	case MODE_UPSEC:
	case MODE_INSEC:
	case MODE_ESEC:
		return META_SECTION_PROC_MORE_DATA;
	case MODE_ESEC_EXT:
		return META_SECTION_PROC_BUF_HAS_SECTION;
	case MODE_LINE:
	case MODE_NO_META:
		return META_SECTION_PROC_UNRELATED_DATA;
	default:
		p->error = "Unexpected mode entered. Internal error.";
		return META_SECTION_PROC_ERROR;
	}
}

static void meta_run(parser* p)
{
	switch(p->mode) {
	case MODE_PREDOC: meta_predoc(p);                  /* fall through */
	case MODE_INIT:   meta_initial(p);                             break;
	case MODE_PSEC:   meta_potential_section(p);                   break;
	case MODE_UPSEC:  meta_upsec(p);                               break;
	case MODE_INSEC:  meta_in_section(p);                          break;
	case MODE_ESEC:   meta_end_section(p);                         break;
	case MODE_LINE:   meta_line(p);                                break;
	case MODE_ENDL:   meta_end_line(p);                            break;
	case MODE_TEXL:   meta_tex_line(p);                            break;
	case MODE_MULTI:  meta_multi(p);                               break;
	case MODE_END:    p->error = "Run after end. Internal error."; break;
	default:          p->error = "Unknown mode. Internal error.";  break;
	}
}

static void meta_predoc(parser* p)
{
	CALLBACK0(meta_begin);
	p->mode = MODE_INIT;
}

static void meta_initial(parser* p)
{
	if(meta_is_line(cchr))
		p->mode = MODE_PSEC;
	else
		p->mode = MODE_NO_META;
}

static int meta_is_line(char c)
{
	return c == '-';
	/* || c == 0xe2 || c == 0x95 || c == 0x90 || c == 0x80 */
}

static void meta_potential_section(parser* p)
{
	if(cchr == '[') {
		p->mode = MODE_UPSEC;
		p->pos = -1;
	} else if(!meta_is_line(cchr)) {
		p->mode = MODE_NO_META;
	}
}

static void meta_upsec(parser* p)
{
	if(cchr == ' ') {
		p->mode = MODE_INSEC;
		p->pos--;
	} else {
		p->error = "Section opener needs to be followed by a space.";
	}
}

static void meta_in_section(parser* p)
{
	if(cchr == ']') {
		if(p->buf[p->pos - 1] != ' ')
			p->error = "Section closer needs to be preceded by a "
								"space.";
		else
			meta_process_section_name(p);
	}
}

static void meta_process_section_name(parser* p)
{
	p->buf[p->pos - 1] = 0;
	if(p->x == X_META) {
		if(strcmp(p->buf, "Meta") != 0)
			p->error = "The first section nees to be \"Meta\".";
		p->mode = MODE_ESEC;
		p->pos = -1;
	} else {
		p->mode = MODE_ESEC_EXT;
	}
}

static void meta_end_section(parser* p)
{
	if(cchr == '\n' && p->pos > 0 && p->buf[p->pos - 1] == '\n') {
		p->mode = MODE_LINE;
		p->pos = -1;
	} else if(!(cchr == '\n' || meta_is_line(cchr))) {
		p->error = "A section has to end with ]--\\n\\n.";
	}
}

static void meta_line(parser* p)
{
	if(cchr == '\n') {
		if(p->pos == 0) {
			p->pos = -1;
			p->mode = MODE_TEXL;
		} else {
			p->mode = MODE_ENDL;
		}
	}
}

static void meta_end_line(parser* p)
{
	switch(cchr) {
	case '\n':
		meta_proc_kv(p);
		p->pos = -1;
		p->mode = MODE_TEXL;
		break;
	case ' ':
		p->pos--;
		cchr = ' ';
		p->mode = MODE_MULTI;
		break;
	default:
		meta_proc_kv(p);
		p->buf[0] = cchr;
		p->pos = 0;
		p->mode = MODE_LINE;
		break;
	}
}

static void meta_proc_kv(parser* p)
{
	char* val;
	char* key;
	p->buf[p->pos - 1] = 0;
	val = strchr(p->buf, ' ');
	if(val == NULL) {
		p->error = "Metadata has to be key val\\n";
		return;
	}

	*val = 0;;
	key = p->buf;
	if(*key == 0) {
		p->error = "Empty key not allowed.";
		return;
	}

	val++;
	while(*val == ' ')
		val++;

	if(*val == 0)
		p->error = "Empty value not allowed.";
	else
		CALLBACK2(meta_kv, key, val);
}

static void meta_tex_line(parser* p)
{
	if(p->pos == 0 && meta_is_line(p->buf[0]))
		meta_end(p);
	else if(cchr == '\n')
		meta_flush_tex(p);
}

static void meta_end(parser* p)
{
	p->mode = MODE_END;
	CALLBACK0(meta_end);
}

static void meta_flush_tex(parser* p)
{
	if(p->pos > 0) {
		cchr = 0;
		assert(p->buf[0] != 0);
		CALLBACK1(meta_tex, p->buf);
	}
	p->pos = -1;
}

static void meta_multi(parser* p)
{
	switch(cchr) {
	case ' ':
		p->pos--;
		break;
	case '\n':
		p->error = "Syntax error. Indented value can not be empty.";
		break;
	default:
		p->mode = MODE_LINE;
		break;
	}
}

void meta_error_display(parser* p)
{
	parser_error_display(p);
}

void meta_free(parser* p)
{
	parser_free(p);
}
