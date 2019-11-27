package ma.d5man.lib.procxml;

import ma.d5man.lib.db.AbstractDownload;
import ma.d5man.lib.db.Download;

class ProcDownload implements AbstractDownload {

	final int downloadID;

	String url;
	String name;
	String description;
	String version;       // optional, null if not present
	long size;            // KiB, optional -1 if not present
	long checked;         // optional -1 if not present
	char[] sha256;

	ProcDownload(int id) {
		super();
		downloadID  = id;
		url         = null;
		name        = null;
		description = null;
		version     = null;
		size        = -1;
		checked     = -1;
		sha256      = null;
	}

	@Override
	public String getNameWOAccess() {
		return name;
	}

	@Override
	public Download access() {
		return new Download(url, name, description, version, size,
							checked, sha256);
	}

}
