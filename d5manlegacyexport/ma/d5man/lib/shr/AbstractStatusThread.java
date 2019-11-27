package ma.d5man.lib.shr;

import ma.tools2.util.StringUtils;

public abstract class AbstractStatusThread extends Thread {

	public static final int STATUS_PRINT_DELAY = 500;

	private final Object monitor;

	private int plen;
	private String slin;
	private boolean terminated;
	private boolean holdBack;

	protected AbstractStatusThread() {
		super();
		monitor = new Object();
		plen = 0;
		slin = "";
		terminated = false;
		holdBack = false;
	}

	protected abstract String getStatusLine();

	@Override
	public void interrupt() {
		super.interrupt();
		terminated = true;
	}

	@Override
	public boolean isInterrupted() {
		return super.isInterrupted() || terminated;
	}

	@Override
	public void run() {
		try {
			while(!isInterrupted()) {
				synchronized(monitor) {
					plen = astatus(plen);
				}
				sleep(STATUS_PRINT_DELAY);
			}
		} catch(InterruptedException ex) {
			// ignore as we always interrupt this thread when
			// we are ready
		}
		plen = astatus(plen);
		System.out.println();
		terminated = true;
	}

	private int astatus(int plen) {
		String bsp = mkclear(plen);
		slin = "STAT " + getStatusLine();
		plen = slin.length();
		if(!holdBack) {
			System.out.print(bsp + slin);
			System.out.flush();
		}
		return plen;
	}

	private String mkclear(int plen) {
		// TODO TEST THIS ON WINDOWS (FIRST VARIANT IS PROBABLY MORE PORTABLE, SECOND VARIANT WORKS FOR CHANGING LINE LENGTHS)
		//return StringUtils.repeat('\b', plen);
		return "\r" + StringUtils.repeat(' ', plen) + "\r";
	}

	protected void holdBack() {
		synchronized(monitor) {
			System.out.print(mkclear(plen));
			plen = 0;
			holdBack = true;
		}
	}

	protected void release() {
		synchronized(monitor) {
			System.out.print(slin);
			plen = slin.length();
			holdBack = false;
		}
	}

}
