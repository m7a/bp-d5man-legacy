#define CALLBACK ((struct parse_callback*)(p->callback))

#define X_NORMAL  0
#define X_LIST   -2

enum modes2 {
	MODE_PREPROCESS                   = 52,
	MODE_PREPROCESS_PCODE             = 100,
	MODE_PREPROCESS_CODE              = 98,
	MODE_PREPROCESS_CODE_ESC          = 99,
	MODE_PREPROCESS_LINK              = 53,
	MODE_PREPROCESS_TEX               = 54,
	MODE_READAHEAD                    = 55,
	MODE_READAHEAD_DESCRIPTION        = 66,
	MODE_READAHEAD_DESCR_SUB          = 79,
	MODE_READAHEAD_SUBSECTION         = 83,
	MODE_PREWORD                      = 56,
	MODE_PREWORD_POT_EMPTY_SYM        = 97,
	MODE_INWORD                       = 57,
	MODE_INWORD_POTENTIAL_SEP         = 96,
	MODE_ESCAPE_INWORD                = 58,
	MODE_POT_START_EXCL               = 84,
	MODE_POT_END_EXCL                 = 68,
	MODE_POTENTIAL_SHORTCUT           = 85,
	MODE_IN_SHORTCUT                  = 86,
	MODE_SHORTCUT_MULTI               = 87,
	MODE_SHORTCUT_END                 = 88,
	MODE_TABLE_OR_CODE                = 59,
	MODE_TABLE_OR_CODE_LINE_2         = 60,
	MODE_TABLE_OR_CODE_L2S            = 82,
	MODE_TABLE_FIELD_BEGIN            = 61,
	MODE_TABLE_FIRST_SEP_SPACE        = 62,
	MODE_TABLE_MID_SEP_OR_END         = 75,
	MODE_TABLE_SKIP_SPACES            = 76,
	MODE_TABLE_POST                   = 63,
	MODE_LIST_PRE_BOL                 = 77,
	MODE_LIST_BEGIN_OF_LINE_AT_PBOCNT = 78,
	MODE_LIST_BEGIN_OF_LINE           = 64,
	MODE_LIST_SUBLIST_START           = 65,
	MODE_LIST_AT_SUBLIST_ITEM         = 51,
	MODE_LINK                         = 67,
	MODE_LINK_ESC                     = 101, /* largest */
	MODE_PCODE                        = 69,
	MODE_PRE_EQUOT                    = 70,
	MODE_PREQUOT_GER                  = 71,
	MODE_CODE                         = 72,
	MODE_CODE_ESC                     = 73,
	MODE_CODE_MULT                    = 80,
	MODE_CODE_MULT_IND                = 81,
	MODE_TEX_MAIN                     = 74,
	MODE_TEX_BEGIN                    = 89,
	MODE_TEX_MATH                     = 90,
	MODE_TEX_POT_HTML                 = 91,
	MODE_TEX_MATH_PEND                = 92,
	MODE_TEX_HTML                     = 93,
	MODE_TEX_HTML_PEND1               = 94,
	MODE_TEX_HTML_PEND2               = 95
};

#define PARSE_SHARED_SEPS " \n:"

void parse_unrelated_data(parser* p);
int parse_reverse_search(char* array, int es, char c);
void parse_unrelated_data_from_to(parser* p, int start, int end);
void parse_direct_cdata(parser* p, char c);
void parse_consume(parser* p);
void parse_go_inword(parser* p);
char* parse_skip_indentation(char* cdptr);
char* parse_skip_list_marker(char* cdptr);
int parse_count_indentation(char* str);
char parse_is_table_line_mid(char c);
char parse_is_list_prefix_char(char c);
char parse_is_table_line_main(char x);
