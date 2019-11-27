package ma.d5man.lib;

public enum Compliance {

	PUBLIC        ("public",        10),
	INFORMAL      ("informal",      20),
	PRERELEASE    ("prerelease",    30),
	INTERNAL      ("internal",      40),
	PERSONAL      ("personal",      50),
	CONFIDENTIAL  ("confidential",  60),
	RESTRICTED    ("restricted",    70),
	SECRET        ("secret",        80),
	QQV           ("qqv",           90),
	QQVX          ("qqvx",         100);

	private final String name;
	private final int    level;

	private Compliance(String name, int level) {
		this.name  = name;
		this.level = level;
	}

	public static Compliance fromInt(int level) {
		for(Compliance c: Compliance.class.getEnumConstants())
			if(level == c.level)
				return c;

		throw new IllegalArgumentException("Unknown compliance level: "
								+ level);
	}

	// TODO z might want to use compiler-generated `valueOf` function instead
	public static Compliance fromString(String name) {
		for(Compliance c: Compliance.class.getEnumConstants())
			if(name.equals(c.name))
				return c;

		throw new IllegalArgumentException("Unknown compliance name: \""
								+ name + "\"");
	}

	public static String[] getAllComplianceNames() {
		Compliance[] all = Compliance.class.getEnumConstants();
		String[] allStr = new String[all.length];

		for(int i = 0; i < all.length; i++)
			allStr[i] = all[i].name;

		return allStr;
	}

	/**
	 * Checks if this compliance is less or equally restrictive as the
	 * parameter. Assuming this is a limit, you can also read this as
	 * ``is the parameter within the limit''
	 *
	 * @param c Parameter to check for
	 * @return if the parameter is compliant with the current compliance
	 */
	public boolean isCompliant(Compliance c) {
		return level >= c.level;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return name;
	}

}
