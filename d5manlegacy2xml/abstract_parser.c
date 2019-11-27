#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <ctype.h>
#include <assert.h>

#include "parser_general.h"
#include "abstract_parser.h"
#include "meta_parsing.h"

static void parser_proc_tab(parser* p, void (*fun)(parser* p));

parser* parser_init(void* callback)
{
	/*
	 * Although we use calloc here, the perasers need to treat everything
	 * in the buffer as undefined which they have not ensured to be set to
	 * some specific values by other means. Usually this means that anything
	 * behind the cursor position is to be treated as undefined (There is
	 * an exception for special list processing features which ``premark''
	 * some buffer elements).
	 *
	 * The reason we choose calloc here is of different nature: Valgrind
	 * complained about access to undefined storage in the error message
	 * generation function parser_error() resulting from the access of
	 * fields which indeed had never been initialized. To remove these
	 * messages (to be able to see the real problems) we decided to use
	 * calloc here.
	 */
	parser* ret   = calloc(1, sizeof(parser));
	ret->mode     = MODE_PREDOC;
	ret->pos      = -1;
	ret->error    = NULL;
	ret->callback = callback;
	ret->x        = X_META;
	return ret;
}

int parser_proc(parser* p, char chr, void (*fun)(parser* p))
{
	if(p->error != NULL) {
		p->error = "Invocation after failure.";
		return PARSER_ERROR;
	} else if(chr == '\t') {
		parser_proc_tab(p, fun);
	} else if(chr != '\r') { /* ignore all \r to support Windows newlines */
		if(!parser_incstack(p))
			return PARSER_ERROR;

		cchr = chr;
		fun(p);
	}

#ifdef DEBUG
	if(p->buf[0] == 0)
		p->error = "BUG: p->buf[0] == 0";

	if(p->pos < -1) {
		p->error = "BUG: p->pos < -1";
		p->pos = -1;
	}
#endif

	if(p->error != NULL)
		return PARSER_ERROR;
	else if(p->mode == MODE_END)
		return PARSER_END;
	else
		return PARSER_OK;
}

int parser_incstack(parser* p)
{
	if(++p->pos >= STACKSIZ) {
		p->error = "Buffer overvlow. Stop lines at 80 char width.";
		return 0;
	} else {
		return 1;
	}
}

/*
 * Eleminates all tabs by replacing them with 8 spaces. This happens at file
 * input time to ensure no tabs ever reach the individual implementations
 * thereby easifying all space handling significantly
 */
static void parser_proc_tab(parser* p, void (*fun)(parser* p))
{
	int i;
	for(i = 0; i < 8 && parser_proc(p, ' ', fun) != PARSER_ERROR; i++)
		;
}

void parser_error_display(parser* p)
{
	size_t chr, cs, ce, j;
	int val;
	char* fmt;

	#define WMAXHS "3"
	#define EPL 18

	fprintf(stderr, "D5Man parsing error: %s\n\n", p->error);

	chr = 0;
	while(chr < STACKSIZ) {
		for(j = 0; j < 3; j++) {
			switch(j) {
			case 0: fmt = "%0" WMAXHS "x "; break;
			case 1: fmt = "%"  WMAXHS "c "; break;
			case 2: fmt = "%0" WMAXHS "x "; break;
			}
			fprintf(stderr, "        ");
			cs = chr;
			ce = chr + EPL;
			while(cs < ce) {
				switch(j) {
				case 0: val = cs; break;
				case 1: val = isgraph(p->buf[cs])? p->buf[cs]:
								(int)'?'; break;
				case 2: val = (unsigned char)p->buf[cs]; break;
				}
				fprintf(stderr, fmt, val);
				cs++;
			}
			fprintf(stderr, "\n");
		}
		chr = cs;
		fprintf(stderr, "\n");
	}

	assert(p->x > -999 && p->x < 999);
	assert(p->mode < 10000 && p->mode > 0);
	assert(p->pos < 0xffff && p->pos >= -1);
	fprintf(stderr, "Cursor before: 0x%04x, Mode: %04d, x: %+04d\n",
						p->pos + 1, p->mode, p->x);

}

void parser_free(parser* p)
{
	free(p);
}
