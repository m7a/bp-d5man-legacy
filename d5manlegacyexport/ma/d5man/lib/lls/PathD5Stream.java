package ma.d5man.lib.lls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import ma.tools2.util.NotImplementedException;

public class PathD5Stream extends D5Stream {

	private final Path s;

	public PathD5Stream(Path p) {
		super();
		s = p.toAbsolutePath();
	}

	public InputStream openInputStreamRepresentation()
							throws D5IOException {
		try {
			return Files.newInputStream(s);
		} catch(IOException ex) {
			throw new D5IOException(ex);
		}
	}

	@Override
	public boolean hasEnd() {
		return true;
	}

	@Override
	public long getLength() throws D5IOException {
		try {
			return Files.size(s);
		} catch(IOException ex) {
			throw new D5IOException(ex);
		}
	}

	public long getTimestamp() throws D5IOException {
		try {
			return Files.getLastModifiedTime(s).toMillis();
		} catch(IOException ex) {
			throw new D5IOException(ex);
		}
	}

	public String getEtag() throws D5IOException {
		return "FETAG-" + s.toAbsolutePath().toString() + "-" +
								getTimestamp();
	}

}
