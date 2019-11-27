package ma.d5man.lib.export.fs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import ma.d5man.lib.lls.MinimalResourceSet;
import ma.d5man.lib.lls.D5Stream;

public class SinglePageExport {

	private static final String EDESCR = "Exception occurred during " +
		"directory creation. This can either be the cause of " +
		"other problems (which are then soon to follow) or just " +
		"the occurrence of a race condition resulting from " +
		"multithreaded directory creation in potentially the same " +
		"places. As long as there are only exceptions of this type, " +
		"they can most likely safely be ignored.";

	public static void syncToDisk(Path dir, MinimalResourceSet data,
					WriteControl wctrl) throws IOException {
		for(String s: data.getResourceNames()) {
			D5Stream stream = data.openResource(s);
			Path dest = dir.resolve(s).toAbsolutePath();
			if(wctrl.decideOnWriting(stream, dest)) {
				requireDirectory(dest.getParent());
				try(InputStream in = stream.
						openInputStreamRepresentation()
						) {
					Files.copy(in, dest, StandardCopyOption.
							REPLACE_EXISTING);
				}
				wctrl.accept(dest);
			}
		}
	}

	private static void requireDirectory(Path path) {
		try {
			if(!Files.exists(path))
				Files.createDirectories(path);
		} catch(IOException ex) {
			System.err.println(EDESCR);
			ex.printStackTrace();
		}
	}

}
