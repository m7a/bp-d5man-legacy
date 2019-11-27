enum d5man_db_name_insert_result_mode {
	DB_FAIL    = 0, /* need to be 0 for simple if(!...)ERR checks */
	DB_OK      = 1,
	DB_MISSING = 2
};
