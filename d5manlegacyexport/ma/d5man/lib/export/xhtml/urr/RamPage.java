package ma.d5man.lib.export.xhtml.urr;

import java.io.*;
import java.security.MessageDigest;
import java.security.DigestOutputStream;
import java.security.NoSuchAlgorithmException;
import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.lls.D5Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

abstract class RamPage<T> extends SingleDataSource {

	private final long initTime;
	private final byte[] md5;
	private final byte[] data;

	protected final T d;

	RamPage() throws IOException {
		this(null);
	}

	RamPage(T d) throws IOException {
		super();
		this.d = d;
		initTime = System.currentTimeMillis();
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException ex) {
			throw new IOException(ex);
		}
		ByteArrayOutputStream bos = null;
		try(OutputStream os = new DigestOutputStream(bos =
					new ByteArrayOutputStream(), digest)) {
			try(Writer out = new OutputStreamWriter(os, UTF_8)) {
				writePageTo(out);
			}
		}
		md5 = digest.digest();
		data = bos.toByteArray();
	}

	abstract void writePageTo(Writer w) throws IOException;

	@Override
	D5Stream openResource() throws D5IOException {
		return new D5Stream() {
			@Override
			public InputStream openInputStreamRepresentation()
							throws D5IOException {
				return new ByteArrayInputStream(data);
			}
			@Override
			public long getTimestamp() throws D5IOException {
				return initTime;
			}
			@Override
			public boolean hasEnd() {
				return true;
			}
			@Override
			public long getLength() throws D5IOException {
				return data.length;
			}
			@Override
			public String getEtag() throws D5IOException {
				return super.getEtag() + "-" + getName();
			}
			@Override
			public byte[] getMD5() {
				return md5;
			}
		};
	}

}
