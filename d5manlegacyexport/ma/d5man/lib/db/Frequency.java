package ma.d5man.lib.db;

public enum Frequency {

	ALWAYS, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY, NEVER;

	public static Frequency fromString(String str) {
		for(Frequency f: Frequency.class.getEnumConstants())
			if(f.toString().equals(str))
				return f;

		throw new IllegalArgumentException("Unknown frequency: \"" +
								str + "\"");
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}

}
