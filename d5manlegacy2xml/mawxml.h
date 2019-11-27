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

typedef struct mawxml mawxml;

mawxml* mawxml_init(FILE* out);
void mawxml_begin_block(mawxml* m, char* name);
void mawxml_begin_inline(mawxml* m, char* name);
void mawxml_begin_raw(mawxml* m, char* name);
void mawxml_attribute(mawxml* m, char* key, char* val);
void mawxml_open(mawxml* m);
void mawxml_empty(mawxml* m);
void mawxml_open_block(mawxml* m, char* name);
void mawxml_newline(mawxml* m);
void mawxml_open_inline(mawxml* m, char* name);
void mawxml_open_raw(mawxml* m, char* name);
void mawxml_close(mawxml* m, char* name);
void mawxml_xml_line(mawxml* m, char* data);
void mawxml_content(mawxml* m, char* cnt);
char mawxml_is_holdback(mawxml* m);
void mawxml_set_holdback(mawxml* m, char is_holdback_enabled);
void mawxml_flush_holdback(mawxml* m);
void mawxml_set_escaping(mawxml* m, char is_escaping_enabled);
void mawxml_free(mawxml* m);
size_t mawxml_get_stack_usage(mawxml* m);
