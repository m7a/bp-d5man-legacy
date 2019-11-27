/*
 * Include this file before meta.h or any own parser built with meta_parsing.h
 */

typedef struct parse_data parser;

enum parser_general {
	PARSER_OK    = 1,
	PARSER_END   = 2,
	PARSER_ERROR = 3
};
