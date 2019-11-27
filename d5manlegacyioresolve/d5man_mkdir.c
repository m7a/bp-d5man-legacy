#include <stdlib.h>
#include <sys/stat.h>

#include "d5man_mkdir.h"

/*
 * this is a separate file in order to be able to implement it differently on
 * non-unix systems
 */

/* returns static error message on failure */
char* d5man_mkdir_ensure_dir_exists(char* dir)
{
	struct stat s;
	if(stat(dir, &s) == 0 && S_ISDIR(s.st_mode))
		return NULL; /* all OK */

	if(mkdir(dir, 0755) == 0)
		return NULL; /* created successfully */
	else
		return "Failed to create directory.";
}
