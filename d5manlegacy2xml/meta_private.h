/* Never include this file in external programs. */

enum modes1 {
	MODE_PSEC     = 4,
	MODE_UPSEC    = 5,
	MODE_INSEC    = 6,
	MODE_ESEC     = 7,
	MODE_LINE     = 8,
	MODE_ENDL     = 9,
	MODE_TEXL     = 10,
	MODE_MULTI    = 11,
	MODE_NO_META  = 12,
	MODE_ESEC_EXT = 13
};

#define CALLBACK ((struct meta_callback*)(p->callback))

static void meta_run(parser* p);
static void meta_predoc(parser* p);
static void meta_initial(parser* p);
static int meta_is_line(char c);
static void meta_potential_section(parser* p);
static void meta_upsec(parser* p);
static void meta_in_section(parser* p);
static void meta_process_section_name(parser* p);
static void meta_end_section(parser* p);
static void meta_line(parser* p);
static void meta_end_line(parser* p);
static void meta_proc_kv(parser* p);
static void meta_tex_line(parser* p);
static void meta_end(parser* p);
static void meta_flush_tex(parser* p);
static void meta_multi(parser* p);
