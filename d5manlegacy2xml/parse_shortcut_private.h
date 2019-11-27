static void parse_shortcut_generic(parser* p);
static char parse_is_upper_alnum(char c);
static void parse_not_a_shortcut(parser* p);
static void parse_shortcut_multi(parser* p);
static void parse_shortcut_end(parser* p);
static char parse_is_soft_word_end_char(char chr);
static void parse_flush_shortcut(parser* p);
static void parse_flush_shortcut_inner_callbacks(parser* p, char* current);
