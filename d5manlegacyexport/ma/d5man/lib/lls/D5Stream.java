package ma.d5man.lib.lls;

import java.io.InputStream;

import ma.tools2.util.NotImplementedException;

/** @version 1.1 */
public abstract class D5Stream {

	public abstract InputStream openInputStreamRepresentation()
							throws D5IOException;

	public boolean hasEnd() {
		return false;
	}

	public long getLength() throws D5IOException {
		throw new NotImplementedException();
	}

	public abstract long getTimestamp() throws D5IOException;

	public String getEtag() throws D5IOException {
		return "D5ETG-" + getClass().getSimpleName() + "-" +
						(getTimestamp() / 1000);
	}

	/** @return <code>null</code> if not available */
	public byte[] getMD5() throws D5IOException {
		return null;
	}

}
