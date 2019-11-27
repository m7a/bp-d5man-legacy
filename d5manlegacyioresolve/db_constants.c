#include <string.h>

#include "db_constants.h"

char* d5man_db_compliance_to_string(short compliance)
{
	switch(compliance) {
	case D5MAN_COMPLIANCE_PUBLIC:       return "public";
	case D5MAN_COMPLIANCE_INFORMAL:     return "informal";
	case D5MAN_COMPLIANCE_PRERELEASE:   return "prerelease";
	case D5MAN_COMPLIANCE_INTERNAL:     return "internal";
	case D5MAN_COMPLIANCE_PERSONAL:     return "personal";
	case D5MAN_COMPLIANCE_CONFIDENTIAL: return "confidential";
	case D5MAN_COMPLIANCE_RESTRICTED:   return "restricted";
	case D5MAN_COMPLIANCE_SECRET:       return "secret";
	case D5MAN_COMPLIANCE_QQV:          return "qqv";
	case D5MAN_COMPLIANCE_QQVX:         return "qqvx";
	default:                            return NULL;
	}
}

short d5man_db_compliance_from_string(char* compliance)
{
	#define CLASSOC(X, Y) if(strcmp(compliance, X) == 0) \
						return D5MAN_COMPLIANCE_##Y
	CLASSOC("public",       PUBLIC);
	CLASSOC("informal",     INFORMAL);
	CLASSOC("prerelease",   PRERELEASE);
	CLASSOC("internal",     INTERNAL);
	CLASSOC("personal",     PERSONAL);
	CLASSOC("confidential", CONFIDENTIAL);
	CLASSOC("restricted",   RESTRICTED);
	CLASSOC("secret",       SECRET);
	CLASSOC("qqv",          QQV);
	CLASSOC("qqvx",         QQVX);
	#undef CLASSOC
	return -1;
}

char* d5man_db_changefreq_to_string(short changefreq)
{
	switch(changefreq) {
	case D5MAN_CHG_ALWAYS:  return "always";
	case D5MAN_CHG_HOURLY:  return "hourly";
	case D5MAN_CHG_DAILY:   return "daily";
	case D5MAN_CHG_WEEKLY:  return "weekly";
	case D5MAN_CHG_MONTHLY: return "monthly";
	case D5MAN_CHG_YEARLY:  return "yearly";
	case D5MAN_CHG_NEVER:   return "never";
	default:                return NULL;
	}
}

short d5man_db_changefreq_from_string(char* chfreq)
{
	#define CHASSOC(X, Y) if(strcmp(chfreq, X) == 0) return D5MAN_CHG_##Y;
	CHASSOC("always",  ALWAYS);
	CHASSOC("hourly",  HOURLY);
	CHASSOC("daily",   DAILY);
	CHASSOC("weekly",  WEEKLY);
	CHASSOC("monthly", MONTHLY);
	CHASSOC("yearly",  YEARLY);
	CHASSOC("never",   NEVER);
	#undef CHASSOC
	return -1;
}
