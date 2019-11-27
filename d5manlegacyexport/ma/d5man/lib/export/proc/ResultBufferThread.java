package ma.d5man.lib.export.proc;

import java.io.*;
import ma.tools2.util.BufferUtils;
import ma.tools2.concurrent.ErrorInterruptingAware;

import ma.d5man.lib.lls.D5IOException;

public class ResultBufferThread extends Thread
					/*implements ErrorInterruptingAware*/{

	private final InputStream in;
	private final ByteArrayOutputStream result;

	private Exception miscEx;

	/** if interrupted due to error output on stderr */
	//private boolean hasError;

	public ResultBufferThread(InputStream in) {
		super("Result Buffer Thread for " +
					Thread.currentThread().toString());
		this.in  = in;
		result   = new ByteArrayOutputStream();
		miscEx   = null;
		//hasError = false;
	}

	@Override
	public void run() {
		try {
			BufferUtils.copy(in, result);
		// TODO z NEVER THROWN...
		//} catch(InterruptedException ex) {
		//	if(!hasError) // not announced => fatal
		//		miscEx = D5IOException.chain(miscEx, ex);
		} catch(IOException ex) {
			miscEx = D5IOException.chain(miscEx, ex);
		}
	}

	//@Override
	//public void setInterruptDueToErrorFollows() {
		//hasError = true;
	//}

	public Exception getException() {
		return miscEx;
	}

	public byte[] getData() {
		return result.toByteArray();
	}

}
