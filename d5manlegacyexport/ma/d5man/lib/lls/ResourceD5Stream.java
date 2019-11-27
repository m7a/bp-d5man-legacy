package ma.d5man.lib.lls;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class ResourceD5Stream extends D5Stream {

	private final URLConnection conn;

	public ResourceD5Stream(String res) throws IOException {
		super();
		conn = getClass().getResource(res).openConnection();
	}

	@Override
	public InputStream openInputStreamRepresentation()
							throws D5IOException {
		try {
			return conn.getInputStream();
		} catch(IOException ex) {
			throw new D5IOException(ex);
		}
	}

	@Override
	public long getTimestamp() throws D5IOException {
		return conn.getLastModified();
	}

	@Override
	public long getLength() throws D5IOException {
		return conn.getContentLengthLong();
	}

	@Override
	public boolean hasEnd() {
		try {
			return getLength() != -1;
		} catch(IOException ex) {
			return false;
		}
	}

}
