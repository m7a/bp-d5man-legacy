#define CHECKRESULT(X) \
	if(X == SQLITE_DONE) { \
		return DB_MISSING; \
	} else if(X != SQLITE_ROW) { \
		d5man_db_internal_set_error_from_sql(); \
		return DB_FAIL; \
	}
