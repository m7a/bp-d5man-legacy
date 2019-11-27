package ma.d5man.lib.lls;

import java.io.InputStream;

public abstract class D5StreamWrapper extends D5Stream {

	protected final D5Stream orig;

	protected D5StreamWrapper(D5Stream orig) {
		super();
		this.orig = orig;
	}

	@Override
	public InputStream openInputStreamRepresentation()
							throws D5IOException {
		return orig.openInputStreamRepresentation();
	}

	@Override
	public boolean hasEnd() {
		return orig.hasEnd();
	}

	@Override
	public long getLength() throws D5IOException {
		return orig.getLength();
	}

	@Override
	public long getTimestamp() throws D5IOException {
		return orig.getTimestamp();
	}

	@Override
	public String getEtag() throws D5IOException {
		return orig.getEtag();
	}

}
