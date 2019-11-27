package ma.tools2.concurrent; // initially Copied from JMBB

import java.io.*;
import ma.tools2.util.ErrorInfo;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A thread which collects error information from Processes.
 *
 * @version 1.1
 */
public class StderrCacher extends Thread { // TODO UPDATE TOOLS LIB W/ ERROR CACHER AND NOTIFIER

	private final Process p;

	final StringBuffer stderr;

	Thread interruptOnErrors;
	boolean hasError;

	private ErrorNotifier notifier;

	public StderrCacher(Process p) {
		super("StderrCacher for " + p);
		this.p            = p;
		stderr            = new StringBuffer();
		hasError          = false;
		interruptOnErrors = null;
		notifier          = null;
	}

	@Override
	public void run() {
		try {
			runFailable();
		} catch(IOException ex) {
			hasError = true;
			stderr.append("\n\nStderr Cacher lost connection\n\n");
			stderr.append(ErrorInfo.getStackTrace(ex));
		} 
	}

	private void runFailable() throws IOException {
		InputStream err = p.getErrorStream();
		try(BufferedReader pErr = new BufferedReader(
					new InputStreamReader(err, UTF_8))) {
			String line;
			while((line = pErr.readLine()) != null &&
							!isInterrupted()) {
				hasError = true;
				stderr.append(line);
				stderr.append('\n');
				performErrorNotification();
			}
		}
	}

	private void performErrorNotification() {
		if(interruptOnErrors != null && interruptOnErrors.isAlive()) {
			if(notifier == null || !notifier.isAlive()) {
				notifier = new ErrorNotifier(this);
				notifier.start();
			} else {
				notifier.reInitSleepTill();
			}
		}
	}

	public void setInterruptOnErrors(Thread t) {
		// TODO z RACE CONDITION: BETWEEN SETTING INTERRUPT ON ERRORS AND INVOKING ERROR NITIFICATION IT COULD HAVE ALBREADY BEEN INVOKED...
		interruptOnErrors = t;
		if(hasError)
			performErrorNotification();
	}

	public boolean hasOutput() {
		return hasError;
	}

	public String getOutput() {
		return stderr.toString();
	}

}
