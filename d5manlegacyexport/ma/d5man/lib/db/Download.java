package ma.d5man.lib.db;

import java.util.Arrays;

public class Download implements AbstractDownload {

	public final String url;
	public final String name;
	public final String description;
	public final String version; // optional, null if not present
	public final long size;      // KiB, optional -1 if not present
	public final long checked;   // optional -1 if not present
	public final char[] sha256;

	public Download(String url, String name, String description,
						String version, long size,
						long checked, char[] sha256) {
		super();
		this.url         = url;
		this.name        = name;
		this.description = description;
		this.version     = version;
		this.size        = size;
		this.checked     = checked;
		this.sha256      = sha256;
	}

	@Override
	public String getNameWOAccess() {
		return name;
	}

	@Override
	public Download access() {
		return this;
	}

	@Override
	public boolean equals(Object b) {
		if(b instanceof Download) {
			Download d = (Download)b;
			return url.equals(d.url) && name.equals(d.name) &&
				description.equals(d.description) &&
				version.equals(d.version) && size == d.size &&
				checked == d.checked &&
				Arrays.equals(sha256, d.sha256);
		} else {
			return false;
		}
	}

}
