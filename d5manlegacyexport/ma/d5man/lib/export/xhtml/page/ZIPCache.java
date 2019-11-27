package ma.d5man.lib.export.xhtml.page;

import java.util.HashMap;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.InputStream;
import java.io.IOException;
import ma.d5man.lib.lls.D5IOException;

public class ZIPCache implements AutoCloseable {

	private final HashMap<String,ZipFile> openFiles;

	public ZIPCache() {
		super();
		openFiles = new HashMap<String,ZipFile>();
	}

	InputStream queryInputStream(String query) throws D5IOException {
		int sep = query.indexOf('!');
		if(sep == -1)
			throw new D5IOException("Missing separator `!`");
		String zip = query.substring(0, sep);
		String res = query.substring(sep + 1);
		if(zip.length() == 0 || res.length() == 0)
			throw new D5IOException("Either no zip file or no " +
							"resource given.");
		try {
			return accessResource(zip, res);
		} catch(IOException ex) {
			throw D5IOException.wrapIn(ex);
		}
	}

	private InputStream accessResource(String zip, String res)
							throws IOException {
		ZipFile rz = openFiles.containsKey(zip)?  openFiles.get(zip):
					openFiles.put(zip, new ZipFile(zip));
		ZipEntry e = rz.getEntry(res);
		if(e == null)
			throw new D5IOException("Entry not found: " + res);
		return rz.getInputStream(e);
	}

	@Override
	public void close() throws IOException {
		for(ZipFile f: openFiles.values())
			f.close();
	}

}
