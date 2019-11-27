package ma.d5man.lib.db;

public class Attachment {

	public final long modified;
	public final char[] md5;

	public Attachment(long modified, char[] md5) {
		super();
		this.modified = modified;
		this.md5      = md5;
	}

}
