package ma.d5man.export;

import java.io.IOException;
import java.nio.file.Path;
import ma.d5man.lib.export.fs.WriteControl;
import ma.d5man.lib.lls.D5Stream;

class AlwaysWriteControl implements WriteControl {

	@Override
	public boolean decideOnWriting(D5Stream stream, Path dest)
							throws IOException {
		return true;
	}

	@Override
	public void accept(Path path) {
		// NOP
	}

}
