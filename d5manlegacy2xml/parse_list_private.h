#define MARK_BEFORE_END_OF_INDENTATION 'a'
#define MARK_ONE_AFTER_INDENTATION     'b'

static char parse_is_numeric_list_entry(char* str);
static void parse_list_pre_bol(parser* p);
static void parse_list_begin_of_line(parser* p);
static void parse_list_begin_of_line_at_potential_begin_of_cnt(parser* p,
								char complete);
static char parse_list_is_spaces_only(char* str);
static void parse_list_begin_of_line_with_list_item(parser* p, int s0);
static void parse_list_recursive_end(parser* p, int prev_ind, int prev_start,
						int now_ind,  int now_start);
static void parse_list_handle_pro_contra_mixed_list(parser* p, char* c,
							char* prev_list_marker);
static int parse_list_process_mid(parser* p, char* c, int prev_start);
static void parse_list_sublist_start(parser* p);
static void parse_list_at_sublist_item(parser* p);
