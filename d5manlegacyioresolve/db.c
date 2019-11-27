#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "db.h"
#include "db_conf.h"

char* d5man_db_strdup(char* in) /* strdup is not portable => own impl */
{
	int len;
	char* ret;
	if(in == NULL) {
		ret = NULL;
	} else {
		len = strlen(in) + 1;
		ret = malloc(len * sizeof(char));
		memcpy(ret, in, len);
	}
	return ret;
}
