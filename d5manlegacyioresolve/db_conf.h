/*
 * external interface
 *
 * Although they are largely unrelated, conf and db have been merged because
 * they are often needed together (and DB always needs conf...)
 *
 * Remember that configuration data is only available if open has been called.
 * Remember that you may never close more times than open because this closes
 * another part of the program's access to the configuration (the part which
 * opened it in the first place...)
 */

struct d5man_db_conf_io_provider_command {
	int section;     /* page section                                 */
	char* name_pre;  /* name prefix for pages                        */
	char* cmd_pre;   /* command prefix (needs concrete page to work) */
};

/* @return error message on failure / registers for configuration accessing */
char* d5man_db_conf_open();

/* @return list of struct d5man_db_conf_io_provider_command */
struct list* d5man_db_conf_io_provider_commands();
/* @return path for public, informal, internal (directory/d5man root) */
char* d5man_db_conf_io_compl_a();
/* @return path for personal ... qqvx (also a directory typically in $HOME) */
char* d5man_db_conf_io_compl_b();
/* @return list of paths (including ZIP files) */
struct list* d5man_db_conf_io_roots();

/* @return path to VIM executable */
char* d5man_db_conf_vim();
/* @return path to .vim file for VIM-Plugin */
char* d5man_db_conf_vim_plugin();
/* @return path which contains at least a directory `website_fs_override` */
char* d5man_db_conf_common_res();
/* @return path to database file */
char* d5man_db_conf_db_locate();
/* @return path to io resolver executable */
char* d5man_db_conf_io_resolver();
/* @return path to d5mandbsync executable. */
char* d5man_db_conf_db_sync();
/* @return path to d5man2xml executable */
char* d5man_db_conf_d5man2xml();
/* @return path to media (image) converter executable */
char* d5man_db_conf_media_converter();
/* @return path to program to be invoked for handling `location` changes */
char* d5man_db_conf_redirect_processor();

/* closes the configuration if this is called the same number times as open() */
void d5man_db_conf_close();

/* hacky function but portable */
char d5man_db_conf_file_exists(char* path);
