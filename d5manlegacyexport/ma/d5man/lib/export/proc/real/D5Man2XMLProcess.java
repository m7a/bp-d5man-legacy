package ma.d5man.lib.export.proc.real;

import java.io.*;
import java.nio.file.Path;

import ma.tools2.concurrent.*;
import ma.tools2.io.EndRecognizingInputStream;
import ma.d5man.lib.lls.*;
import ma.d5man.lib.export.proc.*;
import ma.d5man.lib.j8.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

public class D5Man2XMLProcess extends AbstractConverterProcess
				implements Function<D5Stream, D5Stream> {

	/** Minimal but valid D5Man page */
	private static final String SAMPLE_PAGE =
				"-------------------------------" +
				"---------------------------------------" +
				"[ Meta ]--\n\nk\t\tv\n\n";

	private static final String RESULT_PREFIX =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
			"<!DOCTYPE page SYSTEM \"d5manxml.dtd\">\n\n";

	private static final byte[] END_OF_PAGE = "\n\t</page>\n".
								getBytes(UTF_8);

	private static final String FAIL_DISCARD = "Exception occurred while " +
			"reading from subprocess `d5man2xml`. The detailed " +
			"message contains the last 4KiB of data which have " +
			"been discarded.";

	private final Object monitor;

	private volatile boolean isOpening;

	private InputStream allConvResults;
	private StderrCacher errorCacher;
	private OutputStream queryPipe;

	public D5Man2XMLProcess(Path executable) {
		super(executable);
		monitor        = new Object();
		allConvResults = null;
		errorCacher    = null;
		queryPipe      = null;
		isOpening      = false;
	}

	@Override
	public void open() throws IOException {
		isOpening = true;
		super.open();
		allConvResults = proc.getInputStream();
		errorCacher    = new StderrCacher(proc);
		queryPipe      = proc.getOutputStream();
		errorCacher.start();
		performInitialSampleRequest();
		isOpening = false;
	}

	/**
	 * This method is used to make sure no additional header data is
	 * printed. Also, it enhances the plausability that the system is
	 * working correctly if a basic input can be parsed.
	 */
	private void performInitialSampleRequest() throws IOException {
		VoidStream s = new VoidStream(openInputStreamRepresentationExt(
			new ByteArrayInputStream(SAMPLE_PAGE.getBytes(UTF_8))));
		s.start();
		try {
			s.join();
		} catch(InterruptedException ex) {
			throw new IOInterruptedException(ex);
		}
		s.failIfErrorsArePresent();
	}

	/**
	 * Convert.
	 *
	 * @return converted D5Stream
	 */
	@Override
	public D5Stream apply(final D5Stream in) {
		return new D5StreamWrapper(in) {
			@Override
			public InputStream openInputStreamRepresentation()
							throws D5IOException {
				synchronized(monitor) {
					return openInputStreamRepresentationExt(
						in.openInputStreamRepresentation
									());
				}
			}
			@Override
			public boolean hasEnd() {
				return false;
			}
			@Override
			public byte[] getMD5() {
				return null;
			}
		};
	}

	private InputStream openInputStreamRepresentationExt(InputStream in)
							throws D5IOException {
		new BGCopyThread(in, queryPipe, (byte)0).start();
		try {
			return new SequenceInputStream(
				new ByteArrayInputStream(RESULT_PREFIX.
							getBytes(UTF_8)),
				getResult()
			);
		} catch(Exception ex) {
			throw D5IOException.wrapIn(ex);
		}
	}

	// TODO THIS METHOD (AND THUS THE FILE) IS TOO LONG...

	// We perform a complete copy to memory in order to enhance stability
	// and simplicity (UNIX style...). A more ``lazy evaulation''-like
	// approach has caused a lot of difficulties in the past.
	private InputStream getResult() throws Exception {
		ResultBufferThread rt = new ResultBufferThread(
						new EndRecognizingInputStream(
						allConvResults, END_OF_PAGE));
		rt.start();
		errorCacher.setInterruptOnErrors(rt);
		try {
			rt.join();
		} catch(InterruptedException ex) {
			// normally caused by error cacher => ignore here
		}
		errorCacher.setInterruptOnErrors(null);

		Exception failure = null;
		if(errorCacher.hasOutput()) {
			try {
				// Wait for error message to accumulate
				errorCacher.join(1000);
			} catch(InterruptedException ex) {
				// wayne
			}
			failure = new D5IOException("Failure in subprocess " +
				"`d5man2xml`.", errorCacher.getOutput());
		}

		Exception miscEx = rt.getException();
		if(miscEx != null) {
			failure = D5IOException.chain(failure,
					new D5IOException(FAIL_DISCARD, miscEx,
					c4K(new String(rt.getData(), UTF_8))));
		}

		if(failure == null)
			return new ByteArrayInputStream(rt.getData());

		// Attempt to restart subprocess
		if(isOpening) {
			failure = D5IOException.chain(failure,
					new D5IOException("Nested restart. " +
							"Operation halted."));
		} else {
			try {
				close();
			} catch(IOException ex) {
				failure = D5IOException.chain(failure,
					new D5IOException("Restart failed " +
					"to close old process.", ex));
			}
			try {
				open();
			} catch(IOException ex) {
				failure = D5IOException.chain(failure,
					new D5IOException("Failed to restart " +
					"process", ex));
			}
		}

		throw failure;
	}

	private static String c4K(String s) {
		int len = s.length();
		return len <= 4096? s: s.substring(len - 4096);
	}

	@Override
	public void close() throws IOException {
		errorCacher.interrupt();
		queryPipe.close();
		allConvResults.close();
		super.close();
	}

}
