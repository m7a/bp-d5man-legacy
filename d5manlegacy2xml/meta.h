/*
 * Include this file to use a meta parsing facility
 */

struct meta_callback {
	void* data;
	void (*meta_begin)(void* data);
	void (*meta_kv)(void* data, char* key, char* value);
	void (*meta_tex)(void* data, char* tex);
	void (*meta_end)(void* data);
};

parser* meta_init(struct meta_callback* callback);
int meta_proc(parser* m, char chr);
void meta_error_display(parser* m);
void meta_free(parser* m);
