package ma.d5man.lib.export.proc;

import ma.d5man.lib.lls.D5IOException;

public class IOInterruptedException extends D5IOException {

	public IOInterruptedException(Throwable cause) {
		super("Unexcepted InterruptedException in thread.", cause);
	}

}
