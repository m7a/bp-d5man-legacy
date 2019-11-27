package ma.d5man.lib.db;

public abstract class MD5EnabledPageName extends PageName {

	protected char[] md5;

	protected MD5EnabledPageName() {
		super();
	}

	public char[] getMD5() {
		return md5;
	}

}
