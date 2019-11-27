parser* parse_init(struct parse_callback* callbacks);
int parse_character(parser* p, char cc);
char parse_close(parser* p);
void parse_error_display(parser* p);
void parse_free(parser* p);
