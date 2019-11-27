package ma.d5man.lib.export.xhtml.urr;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import ma.d5man.lib.lls.MinimalResourceSet;
import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.lls.PathD5Stream;

class DFSOverrides implements MinimalResourceSet {

	private final Map<String,Path> fsOverrides;

	DFSOverrides(Path fLoc) throws IOException {
		super();
		fsOverrides = getAllFSOverrides(fLoc);
	}

	/** @return Relative to Real assoc. */
	private static Map<String,Path> getAllFSOverrides(final Path fLoc)
							throws IOException {
		final Map<String,Path> ret = new HashMap<String,Path>();
		Files.walkFileTree(fLoc,
				EnumSet.of(FileVisitOption.FOLLOW_LINKS),
				Integer.MAX_VALUE,
			new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path f,
							BasicFileAttributes att)
							throws IOException {
				ret.put(fLoc.relativize(f).toString(),
							f.toAbsolutePath());
				return FileVisitResult.CONTINUE;
			}
		});
		return ret;
	}

	@Override
	public D5Stream openResource(String name) throws IOException {
		return (fsOverrides.containsKey(name)? new PathD5Stream(
			fsOverrides.get(name)): null);
	}

	@Override
	public Iterable<String> getResourceNames() {
		return fsOverrides.keySet();
	}

}
