package ma.d5man.lib.lls;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import ma.tools2.util.HexUtils;
import ma.tools2.util.NotImplementedException;
import ma.tools2.util.BufferUtils;

public class ChecksumingD5StreamWrapper extends D5StreamWrapper {

	private final String algorithm;
	private DigestInputStream di;

	public ChecksumingD5StreamWrapper(D5Stream parent, String algorithm) {
		super(parent);
		this.algorithm = algorithm;
		di = null;
	}

	@Override
	public InputStream openInputStreamRepresentation()
							throws D5IOException {
		if(di != null)
			throw new NotImplementedException(
				"A " + getClass().getName() + " expects " +
				"data to only be requested once. If you need " +
				"it differently, go to the source and " +
				"remove this `throw`-Satement. For users " +
				"this normally indicates a program bug."
			);

		try {
			return (di = new DigestInputStream(
				super.openInputStreamRepresentation(),
				MessageDigest.getInstance(algorithm)
			));
		} catch(D5IOException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new D5IOException(ex);
		}
	}

	/**
	 * requires <code>openInputStreamRepresentation()</code>
	 * exactly once before this
	 */
	public void skipRestForCorrectChecksum() throws IOException {
		// TODO z a bit idiotic... but works
		byte[] tmpbuf = new byte[BufferUtils.DEFAULT_BUFFER];
		while(di.read(tmpbuf, 0, tmpbuf.length) == tmpbuf.length)
			;
		tmpbuf = null; // explicitely delete buffer
	}

	public char[] getChecksum() {
		return HexUtils.formatAsHex(di.getMessageDigest().digest(),
					false, false).toString().toCharArray();
	}

}
