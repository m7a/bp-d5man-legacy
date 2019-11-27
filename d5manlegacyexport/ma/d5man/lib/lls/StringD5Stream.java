package ma.d5man.lib.lls;

import java.security.MessageDigest;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StringD5Stream extends D5Stream {

	private final long tInit;
	private final byte[] buf;

	public StringD5Stream(String data) {
		this(data, System.currentTimeMillis());
	}

	public StringD5Stream(String data, long tInit) {
		super();
		this.tInit = tInit;
		buf        = data.getBytes(UTF_8);
	}

	public StringD5Stream(byte[] data) {
		super();
		tInit = System.currentTimeMillis();
		buf   = data;
	}

	public InputStream openInputStreamRepresentation()
							throws D5IOException {
		return new ByteArrayInputStream(buf);
	}

	@Override
	public byte[] getMD5() throws D5IOException {
		try {
			return MessageDigest.getInstance("MD5").digest(buf);
		} catch(Exception ex) {
			throw D5IOException.wrapIn(ex);
		}
	}

	@Override
	public boolean hasEnd() {
		return true;
	}

	@Override
	public long getLength() {
		return buf.length;
	}

	public long getTimestamp() {
		return tInit;
	}

}
