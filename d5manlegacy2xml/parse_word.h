char parse_word(parser* p);
int parse_proc_esc(parser* p);
void parse_flush_stack(parser* p, char substitute, char endproc);
void parse_word_end_space(parser* p);
char parse_is_soft_word_end(char* str);
