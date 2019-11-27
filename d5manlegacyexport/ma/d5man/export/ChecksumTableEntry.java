package ma.d5man.export;

import java.util.Arrays;
import ma.tools2.util.HexUtils;

class ChecksumTableEntry {

	private final String etag;
	private final long mod;
	private final byte[] md5;

	private boolean touched;

	ChecksumTableEntry(String etag, long mod, byte[] md5, boolean isNew) {
		super();
		this.etag = etag;
		this.mod  = mod;
		this.md5  = md5;
		touched   = isNew;
	}

	void touch() {
		touched = true;
	}

	boolean hasBeenTouched() {
		return touched;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof ChecksumTableEntry) {
			ChecksumTableEntry cmp = (ChecksumTableEntry)o;
			assert etag != null || mod != -1 || md5 != null;
			return
				(etag != null && cmp.etag != null &&
						etag.equals(cmp.etag)) ||
				(md5 != null && cmp.md5 != null &&
						Arrays.equals(md5, cmp.md5)) ||
				(mod != -1 && cmp.mod != -1 && mod == cmp.mod);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return (etag == null? "_": etag) + "," + (mod == -1? "_": mod) +
			"," + (md5 == null? "_":
			HexUtils.formatAsHex(md5, false, false).toString());
	}

}
