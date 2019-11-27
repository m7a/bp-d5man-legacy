package ma.d5man.lib.export.proc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ma.tools2.util.BufferUtils;
import ma.d5man.lib.lls.D5IOException;

public class BGCopyThread extends Thread {

	private final InputStream in;
	private final OutputStream out;
	private final byte[] term;

	private IOException storedError;

	/**
	 * @param in Is closed once copying is complete.
	 * @param term Give terminator symbols to be sent at the end or null to
	 * 		close stream.
	 */
	public BGCopyThread(InputStream in, OutputStream out, byte... term) {
		super("BG Copy Thread for " +
					Thread.currentThread().toString());
		this.in     = in;
		this.term   = term;
		this.out    = out;
		storedError = null;
	}

	@Override
	public void run() {
		try {
			BufferUtils.copy(in, out);
			if(term != null) {
				out.write(term);
				out.flush();
			} else {
				out.close();
			}
		} catch(IOException ex) {
			storedError = ex;
		} finally {
			try {
				in.close();
			} catch(IOException ex) {
				storedError = D5IOException.chain(storedError,
									ex);
			}
		}
	}

	public boolean hasError() {
		return storedError != null;
	}

	public IOException getError() {
		return storedError;
	}

}
