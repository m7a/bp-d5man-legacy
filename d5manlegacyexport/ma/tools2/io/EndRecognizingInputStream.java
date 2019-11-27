package ma.tools2.io;

import java.util.Arrays;
import java.io.InputStream;
import java.io.IOException;

/**
 * Allows you to create a filter stream which ends once the underlying stream
 * has sent a specific terminator byte string.
 *
 * @since Tools 2.1, 2015/10/03
 * @author Linux-Fan, Ma_Sys.ma
 * @version 1.0.0.0
 */
public class EndRecognizingInputStream extends InputStream {

	private final InputStream src;
	private final byte[] terminator;

	private IOException failure;
	private int bpos;
	private byte[] last;

	public EndRecognizingInputStream(InputStream src, byte[] terminator) {
		this.src        = src;
		this.terminator = terminator;
		last            = new byte[terminator.length];
		failure         = null;
		bpos            = -1;
		Arrays.fill(last, (byte)0);
	}

	@Override
	public int read() throws IOException {
		if(failure != null)
			throw failure;

		if(last == null)
			return -1; // end of stream

		int next = src.read();
		if(next == -1) {
			try {
				src.close();
			} catch(IOException ex) {
				failure = ex;
				throw failure;
			}
			failure = new IOException("Unexpected end of stream.");
			throw failure;
		}

		last[bpos = ((bpos + 1) % last.length)] = (byte)next;

		if(isEnd()) {
			last = null;
			return -1;
		}

		return next;
	}

	private boolean isEnd() {
		// the start is one behind me
		// consider the pattern to be TEST and the input to be XTEST
		// v        v        v        v        v    v      (cursor)
		//  0000 -> X000 -> XT00 -> XTE0 -> XTES -> TTES   (data)
		int j = (bpos + 1) % last.length;
		for(int i = 0; i < last.length; i++) {
			if(last[j] != terminator[i])
				return false;
			j = (j + 1) % last.length;
		}
		return true;
	}

	@Override
	public void close() throws IOException {
		if(last != null) {
			try {
				while(read() != -1)
					;
			} catch(IOException ex) {
				// Ignore errors which occur if the output is
				// not wanted anyway.
			}
		}
		// src deliberately not closed!
		super.close();
	}

}
