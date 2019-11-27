/*
 * although they are largely unrelated, io resolve and db have been merged
 * because you often need the DB as well
 */

enum d5man_db_io_loc_type {

	/*
	 * real files just return a pathname (to files),
	 * potentially including nonexistent parent directories
	 */
	D5MAN_DB_IO_LOC_TYPE_REALFILE = 0,

	/*
	 * Format OS_PATH!ZIP_SUBPATH
	 * e.g. /usr/share/mdvl/test.zip!/test.js
	 */
	D5MAN_DB_IO_LOC_TYPE_ZIPFILE  = 1,

	/*
	 * Means that data is read or written from or to a special command.
	 * Generally, this format is like the old `d5manio` 
	 * e.g. /usr/bin/d5manio
	 */
	D5MAN_DB_IO_LOC_TYPE_COMMAND  = 2,

	/*
	 * Means that error message can be obtained from arg
	 * Error messages are static => no free necessary
	 */
	D5MAN_DB_IO_LOC_TYPE_ERROR    = 3

};

struct d5man_db_io_loc {

	/* ------------------------------------------------------[ input ]-- */
	int section;      /* section                                         */
	char* page;       /* page shortname                                  */
	char* attachment; /* NULL if not asking for attachment               */
	short compliance; /* page compliance (-2 if path necessarily exists) */
	char is_writing;  /* for commands: sets appropriate parameter        */

	/* -----------------------------------------------------[ output ]-- */
	short type;       /* one of enum d5man_io_loc_type                   */
	char* arg;        /* alloc. filename formatted after the loc type    */

};

/*
 * Before you use IO resolve for the first time, initialize
 * d5man_db_conf_open() and finally d5man_db_conf_close() to allow this
 * part of the program to access configuration files
 */
void d5man_db_io_resolve(struct d5man_db_io_loc* loc);

/*
 * Returns a static string describing the given type.
 * Basically it is the last part of the constant name
 */
char* d5man_db_io_resolve_type_name(short type);
