/*
 * Ma_Sys.ma XML Writer Module 1.0.0.1, Copyright (c) 2014, 2015 Ma_Sys.ma.
 * For further info send an e-mail to Ma_Sys.ma@web.de.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <stdlib.h>
#include <stdio.h>
#include <assert.h>

#include "mawxml.h"
#include "mawxml_private.h"

mawxml* mawxml_init(FILE* out)
{
	mawxml* ret              = malloc(sizeof(mawxml));
	ret->out                 = out;
	ret->is_escaping_enabled = 1;
	ret->is_holdback_enabled = 0;
	ret->current_indentation = 0;
	ret->current_pos         = 0;
	ret->last_was_space      = 0;

	mawxml_stack_init(&ret->holdback, MAWXML_INITIAL_HOLDBACK,
							MAWXML_HOLDBACK_GROWTH);
	mawxml_stack_init(&ret->stack, MAWXML_INITIAL_STACK,
							MAWXML_STACK_GROWTH);

	return ret;
}

static void mawxml_stack_init(struct mawxml_stack* s, size_t initial_size,
								size_t growth)
{
	s->data = malloc(initial_size * sizeof(char));
	s->size = initial_size;
	s->growth = growth;
	s->used = 0;
}

void mawxml_begin_block(mawxml* m, char* name)
{
	mawxml_stack_push(&m->stack, MAWXML_TAG_TYPE_BLOCK);
	mawxml_auto_indent(m);
	mawxml_lowlevel_begin_tag(m, name);
}

static void mawxml_stack_push(struct mawxml_stack* s, char val)
{
	s->data[s->used++] = val;
	if(s->used >= s->size) {
		s->size += s->growth;
		s->data = realloc(s->data, s->size * sizeof(char));
	}
}

static void mawxml_auto_indent(mawxml* m)
{
	mawxml_newline(m);
	mawxml_indent(m);
}

void mawxml_newline(mawxml* m)
{
	if(m->current_pos != 0)
		mawxml_lowlevel_putchar(m, '\n');
}

static void mawxml_lowlevel_putchar(mawxml* m, char val)
{
	switch(val) {
	case '\n': m->current_pos = 0;  break;
	case '\t': m->current_pos += 8; break;
	default:   m->current_pos++;    break;
	}

	m->last_was_space = mawxml_is_space(val);

	if(m->is_holdback_enabled)
		mawxml_stack_push(&m->holdback, val);
	else
		fputc(val, m->out);
}

static char mawxml_is_space(char c)
{
	return c == '\n' || c == '\t' || c == ' ';
}

static void mawxml_indent(mawxml* m)
{
	size_t i;
	for(i = 0; i < m->current_indentation; i++)
		mawxml_lowlevel_putchar(m, '\t');

	assert(m->current_pos == 8 * m->current_indentation);
}

static void mawxml_lowlevel_begin_tag(mawxml* m, char* name)
{
	mawxml_lowlevel_putchar(m, '<');
	mawxml_lowlevel_puts(m, name);
}

static void mawxml_lowlevel_puts(mawxml* m, char* str)
{
	char* ptr;
	for(ptr = str; *ptr != 0; ptr++)
		mawxml_lowlevel_putchar(m, *ptr);
}

void mawxml_begin_inline(mawxml* m, char* name)
{
	if(m->last_was_space)
		mawxml_auto_indent(m);
	mawxml_stack_push(&m->stack, MAWXML_TAG_TYPE_INLINE);
	mawxml_lowlevel_begin_tag(m, name);
}

void mawxml_begin_raw(mawxml* m, char* name)
{
	mawxml_stack_push(&m->stack, MAWXML_TAG_TYPE_RAW);
	mawxml_lowlevel_begin_tag(m, name);
}

void mawxml_attribute(mawxml* m, char* key, char* val)
{
	if(!m->last_was_space)
		mawxml_lowlevel_putchar(m, ' ');
	mawxml_lowlevel_puts(m, key);
	mawxml_lowlevel_puts(m, "=\"");
	mawxml_content_direct(m, val, 1);
	mawxml_lowlevel_putchar(m, '"');
}

void mawxml_open(mawxml* m)
{
	mawxml_lowlevel_putchar(m, '>');

	if(mawxml_stack_top(&m->stack) == MAWXML_TAG_TYPE_BLOCK) {
		m->current_indentation++;
		mawxml_newline(m);
	}
}

static char mawxml_stack_top(struct mawxml_stack* s)
{
	return s->data[s->used - 1];
}

void mawxml_empty(mawxml* m)
{
	char s = mawxml_stack_top(&m->stack);
	mawxml_lowlevel_puts(m, "/>");
	if(s == MAWXML_TAG_TYPE_BLOCK)
		mawxml_newline(m);
	mawxml_stack_pop(&m->stack);
}

void mawxml_open_block(mawxml* m, char* name)
{
	mawxml_begin_block(m, name);
	mawxml_open(m);
}

void mawxml_open_inline(mawxml* m, char* name)
{
	mawxml_begin_inline(m, name);
	mawxml_open(m);
}

void mawxml_open_raw(mawxml* m, char* name)
{
	mawxml_begin_raw(m, name);
	mawxml_open(m);
}

void mawxml_close(mawxml* m, char* name)
{
	char isblock = mawxml_stack_top(&m->stack) == MAWXML_TAG_TYPE_BLOCK;
	if(isblock) {
		assert(m->current_indentation > 0);
		m->current_indentation--;
		mawxml_auto_indent(m);
	}
	mawxml_stack_pop(&m->stack);
	mawxml_lowlevel_puts(m, "</");
	mawxml_lowlevel_puts(m, name);
	mawxml_lowlevel_putchar(m, '>');
	if(isblock)
		mawxml_newline(m);
}

static void mawxml_stack_pop(struct mawxml_stack* s)
{
	assert(s->used > 0);
	s->used--;
}

void mawxml_xml_line(mawxml* m, char* data)
{
	mawxml_auto_indent(m);
	mawxml_lowlevel_puts(m, data);
}

void mawxml_content(mawxml* m, char* cnt)
{
	mawxml_content_direct(m, cnt,
			mawxml_stack_top(&m->stack) == MAWXML_TAG_TYPE_RAW);
}

static void mawxml_content_direct(mawxml* m, char* cnt, char israw)
{
	char* ptr;

	if(!israw && m->current_pos == 0)
		mawxml_auto_indent(m);

	for(ptr = cnt; *ptr != 0; ptr++)
		mawxml_content_char(m, *ptr, israw);
}

static void mawxml_content_char(mawxml* m, char cnt, char israw)
{
	if(m->is_escaping_enabled) {
		switch(cnt) {
		case '<': mawxml_lowlevel_puts(m, "&lt;");   return;
		case '>': mawxml_lowlevel_puts(m, "&gt;");   return;
		case '"': mawxml_lowlevel_puts(m, "&quot;"); return;
		case '&': mawxml_lowlevel_puts(m, "&amp;");  return;
		/* TODO This can unfortunately not be represented in XML */
		case 0xc: /* delete character */ return;
		}
	}

	/*
	 * It is not perfect but this does not get any better.
	 * We need to accept some superflous newlines.
	 */
	if(!israw && mawxml_is_space(cnt)) {
		if(m->current_pos >= MAWXML_WRAP_LIMIT) {
			mawxml_newline(m);
			mawxml_auto_indent(m);
		} else if(!m->last_was_space) {
			mawxml_lowlevel_putchar(m, ' ');
		}
	} else {
		mawxml_lowlevel_putchar(m, cnt);
	}
}

char mawxml_is_holdback(mawxml* m)
{
	return m->is_holdback_enabled;
}

void mawxml_set_holdback(mawxml* m, char is_holdback_enabled)
{
	m->is_holdback_enabled = is_holdback_enabled;
}

void mawxml_flush_holdback(mawxml* m)
{
	char bakesc = m->is_escaping_enabled;
	m->is_escaping_enabled = 0;
	mawxml_stack_push(&m->holdback, 0);
	mawxml_content(m, m->holdback.data);
	m->holdback.used = 0;
	m->is_escaping_enabled = bakesc;
}

void mawxml_set_escaping(mawxml* m, char is_escaping_enabled)
{
	m->is_escaping_enabled = is_escaping_enabled;
}

void mawxml_free(mawxml* m)
{
	assert(m->holdback.used == 0);
	assert(m->stack.used == 0);
	free(m->holdback.data);
	free(m->stack.data);
	free(m);
}

size_t mawxml_get_stack_usage(mawxml* m)
{
	return m->stack.used;
}
