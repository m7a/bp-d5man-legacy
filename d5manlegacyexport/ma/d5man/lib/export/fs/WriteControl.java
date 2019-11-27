package ma.d5man.lib.export.fs;

import java.io.IOException;
import java.nio.file.Path;

import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.j8.Consumer;

/** <code>accept(Path)</code> is invoked after successfully writing a file.  */
public interface WriteControl extends Consumer<Path> {

	/** @return true if this stream should be written to dest */
	boolean decideOnWriting(D5Stream stream, Path dest) throws IOException;

}
