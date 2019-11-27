package ma.tools2.concurrent;

import java.io.InputStream;
import java.io.IOException;

/**
 * This Thread continously reads fron the given input stream and ignores any
 * data. Potential errors can be queried using failIfErrorsArePresent()
 *
 * @version 1.1
 */
public class VoidStream extends Thread {

	private final InputStream in;
	private IOException fail;
	private boolean interrupted;

	public VoidStream(InputStream in) {
		super();
		this.in     = in;
		fail        = null;
		interrupted = false;
	}

	public void run() {
		try {
			while(in.read() != -1 && !interrupted)
				;
			in.close();
		} catch(IOException ex) {
			if(!interrupted)
				this.fail = ex;
		}
	}

	public void failIfErrorsArePresent() throws IOException {
		if(fail != null)
			throw new IOException(fail);
	}

	public void interrupt() {
		this.interrupted = true;
		super.interrupt();
		try {
			in.close();
		} catch(IOException ex) {
			// fail = ex;
		}
	}

}
