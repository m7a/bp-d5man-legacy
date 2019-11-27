#define MAWXML_INITIAL_HOLDBACK 0x100
#define MAWXML_HOLDBACK_GROWTH  0x300

#define MAWXML_INITIAL_STACK 10
#define MAWXML_STACK_GROWTH  20

#define MAWXML_WRAP_LIMIT 72

struct mawxml_stack {
	char* data;
	size_t size;
	size_t growth;
	size_t used;
};

struct mawxml {
	FILE* out;
	char is_escaping_enabled;   /* escaping ~ htmlspecialchars for data */
	char is_holdback_enabled;
	size_t current_indentation; /* in number of tabs */
	size_t current_pos;         /* in number of characters from the left */
	char last_was_space;        /* or tab or newline */
	struct mawxml_stack holdback;
	struct mawxml_stack stack;
};

enum mawxml_tag_types {
	MAWXML_TAG_TYPE_BLOCK  = 1,
	MAWXML_TAG_TYPE_INLINE = 2,
	MAWXML_TAG_TYPE_RAW    = 3
};

static void mawxml_stack_init(struct mawxml_stack* s, size_t initial_size,
								size_t growth);
static void mawxml_stack_push(struct mawxml_stack* s, char val);
static void mawxml_auto_indent(mawxml* m);
static void mawxml_lowlevel_putchar(mawxml* m, char val);
static char mawxml_is_space(char c);
static void mawxml_indent(mawxml* m);
static void mawxml_lowlevel_begin_tag(mawxml* m, char* name);
static void mawxml_lowlevel_puts(mawxml* m, char* str);
static char mawxml_stack_top(struct mawxml_stack* s);
static void mawxml_stack_pop(struct mawxml_stack* s);
static void mawxml_content_direct(mawxml* m, char* cnt, char israw);
static void mawxml_content_char(mawxml* m, char cnt, char israw);
