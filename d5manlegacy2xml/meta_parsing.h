/*
 * Include this file to build an own parser using the same structure as meta.c.
 */

/* TODO z we want to have no more than two lines in the stack and then we can reduce this back to 256... currently LaTeX is always processed as a single line and thus the buffer needs to accommodate for the largest continous LaTeX part ever entered... */
#define STACKSIZ 0xfe3

struct parse_data {

	int mode; /* Current parser mode (see MODE_INIT, MODE_END) */
	int pos;  /* Current buffer position */

	/*
	 * Buffers the current input. Do not store significantly more than a
	 * line here, because this buffer is quite small.
	 */
	char buf[STACKSIZ];

	/*
	 * x represents different things. In meta.c it is used to determine if
	 * we are scanning the meta section or headings only. For the meta
	 * section, x = -1, otherwise x != -1.
	 */
	int x;

	/*
	 * Stores the last error message. If this is not null, the parser should
	 * reject any attempt to parse more data => Errors are always fatal.
	 */
	char* error;

	/*
	 * Parser dependent callback functions to be invoked if some part of the
	 * data has been successfully recognized as a specific structure.
	 * Usually, this refers to a struct of function pointers (refer to the
	 * implementations for examples)
	 */
	void* callback;

};

/*
 * Marks this parser as a parser which is only responsible for metadata parsing.
 * This is used in meta.c to make sure, the meta section is always called "Meta"
 * and such.
 */
#define X_META -1

/* all implementation-defined modes must be bigger or equal to 50 */
enum modes0 {
	MODE_PREDOC = 1,
	MODE_INIT   = 2,
	MODE_END    = 3
};

/* Special constants which are returned by meta_section_proc(parser*). */
enum meta_section_proc_ret {
	META_SECTION_PROC_MORE_DATA       = 1,
	META_SECTION_PROC_BUF_HAS_SECTION = 2,
	META_SECTION_PROC_UNRELATED_DATA  = 3,
	META_SECTION_PROC_ERROR           = 4
};

/*
 * ``Implementing'' files declare a simple CALLBACK macro to be able to access
 * their callback functions efficiently
 */
#define CALLBACK0(N)       CALLBACK->N(CALLBACK->data)
#define CALLBACK1(N, A)    CALLBACK->N(CALLBACK->data, (A))
#define CALLBACK2(N, A, B) CALLBACK->N(CALLBACK->data, (A), (B))

/*
 * Useful macro to refer to the ``current'' character in buf. We use a lowercase
 * name to make assignments to cchr look like a global variable. With consistent
 * naming, this is likely to work.
 */
#define cchr p->buf[p->pos]

/* invoke when possibly expecting a section w/ chr@buf */
int meta_section_proc(struct parse_data* m);
