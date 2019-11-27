/* external interface (static no db access necessary) */

enum d5man_compliance_levels {
	D5MAN_COMPLIANCE_PUBLIC       =  10, /* instantly posted online       */
	D5MAN_COMPLIANCE_INFORMAL     =  20, /* not automatically advertised  */
	D5MAN_COMPLIANCE_PRERELEASE   =  30, /* published later               */
	D5MAN_COMPLIANCE_INTERNAL     =  40, /* Ma_Zentral, portable data     */
	D5MAN_COMPLIANCE_PERSONAL     =  50, /* NSFW                          */
	D5MAN_COMPLIANCE_CONFIDENTIAL =  60, /* not safe for Ma_Zentral media */
	D5MAN_COMPLIANCE_RESTRICTED   =  70, /* not to be shared              */
	D5MAN_COMPLIANCE_SECRET       =  80, /* special care                  */
	D5MAN_COMPLIANCE_QQV          =  90, /* encryption required           */
	D5MAN_COMPLIANCE_QQVX         = 100  /* does not exist, actually ...  */
};

enum d5man_db_changefreq {
	D5MAN_CHG_ALWAYS              = 1,
	D5MAN_CHG_HOURLY              = 2,
	D5MAN_CHG_DAILY               = 3,
	D5MAN_CHG_WEEKLY              = 4,
	D5MAN_CHG_MONTHLY             = 5,
	D5MAN_CHG_YEARLY              = 6,
	D5MAN_CHG_NEVER               = 7
};

char* d5man_db_compliance_to_string(short compliance);
short d5man_db_compliance_from_string(char* compliance);
char* d5man_db_changefreq_to_string(short changefreq);
short d5man_db_changefreq_from_string(char* chfreq);
