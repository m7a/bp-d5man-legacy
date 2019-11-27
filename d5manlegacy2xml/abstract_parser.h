/*
 * Include this file to write a parser like meta.c using the same set of
 * functions to initiate parser* pointers and process characters.
 */

parser* parser_init(void* callback);
int parser_proc(parser* p, char chr, void (*fun)(parser* p));
int parser_incstack(parser* p);
void parser_error_display(parser* p);
void parser_free(parser* p);
