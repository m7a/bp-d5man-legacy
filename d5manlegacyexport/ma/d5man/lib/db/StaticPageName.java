package ma.d5man.lib.db;

public class StaticPageName extends PageName {

	private final int section;
	private final String name;

	public StaticPageName(int section, String name) {
		super();
		this.section = section;
		this.name    = name;
	}


	/** e.g. test(42) */
	public static StaticPageName fromNameSection(String data) {
		int open = data.indexOf('(');
		return new StaticPageName(
			Integer.parseInt(data.substring(open + 1).
					substring(0, data.length() - open - 2)),
			data.substring(0, open)
		);
	}

	@Override
	public int getSection() {
		return section;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode() * 100 + section;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof StaticPageName) {
			StaticPageName x = (StaticPageName)o;
			return x.section == section && x.name.equals(name);
		} else {
			return o.equals(this);
		}
	}

}
