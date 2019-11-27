/* external interface (general purpose) */

struct d5man_db_page_id {
	unsigned long internal_id; /* database id (0 if unknown) */
	int section;               /* numeric d5man section */
	char* name;                /* short page name */
};

char d5man_db_open();
char* d5man_db_get_error();
void d5man_db_close();

/* semi-internal */
char* d5man_db_strdup(char* in);
void d5man_db_internal_set_error_from_sql();

#define FRIN(X) if(X != NULL) free(X)
