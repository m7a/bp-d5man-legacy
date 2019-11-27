package ma.d5man.lib.db;

import ma.d5man.lib.Compliance;

public abstract class CompliantPageName extends MD5EnabledPageName {

	protected Compliance compliance;

	protected CompliantPageName() {
		super();
	}

	public Compliance getCompliance() {
		return compliance;
	}

}
