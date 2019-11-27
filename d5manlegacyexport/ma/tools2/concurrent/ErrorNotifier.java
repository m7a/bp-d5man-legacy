package ma.tools2.concurrent;

import ma.tools2.util.ErrorInfo;

/**
 * Thread used to notify other threads of error accumulation in StderrCacher.
 * This is only used by StderrCacher internally.
 *
 * @version 1.1
 */
class ErrorNotifier extends Thread {

	private final int ERROR_ACUMULATION_TIMEOUT = 500;

	private final StderrCacher c;
	private long sleepTill;

	ErrorNotifier(StderrCacher c) {
		super("Error Notifier for " + Thread.currentThread().
								toString());
		this.c = c;
		reInitSleepTill();
	}

	void reInitSleepTill() {
		sleepTill = System.currentTimeMillis() +
						ERROR_ACUMULATION_TIMEOUT;
	}

	@Override
	public void run() {
		try {
			while(!isInterrupted() &&
					c.interruptOnErrors != null && work())
				;
		} catch(InterruptedException ex) {
			c.hasError = true;
			c.stderr.append("\n\nError Notifier " +
						"interrupted\n");
			c.stderr.append(ErrorInfo.getStackTrace(ex));
		}
	}

	private boolean work() throws InterruptedException {
		long sleep = sleepTill - System.currentTimeMillis();
		if(sleep > 0)
			sleep(sleep);
		if(c.interruptOnErrors != null && System.currentTimeMillis()
								>= sleepTill) {
			performNotification();
			return false;
		}
		return true;
	}

	private void performNotification() {
		if(c.interruptOnErrors instanceof ErrorInterruptingAware)
			((ErrorInterruptingAware)c.interruptOnErrors).
						setInterruptDueToErrorFollows();
		c.interruptOnErrors.interrupt();
	}

}
