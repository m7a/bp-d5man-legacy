package ma.d5man.lib.export.proc;

import java.io.IOException;
import java.nio.file.Path;

import ma.d5man.lib.lls.ResourceOnDemand;

public abstract class AbstractConverterProcess extends ResourceOnDemand
						implements AutoCloseable {

	private final String executable;

	protected Process proc;

	protected AbstractConverterProcess(Path executable) {
		super();
		this.executable = executable.toAbsolutePath().toString();
	}

	/**
	 * Override this calling <code>super.open()</code> first in order to
	 * attach streams etc.
	 */
	public void open() throws IOException {
		String[] invoc = getDefaultParameters();
		invoc[0] = executable;
		ProcessBuilder pb = new ProcessBuilder(invoc);
		configureProcessBuilder(pb);
		proc = pb.start();
		super.open();
	}

	/**
	 * Override this in order to redirect streams as necessary before the
	 * actual process is being created.
	 */
	protected void configureProcessBuilder(ProcessBuilder pb) {
		// Only implemented by subclasses
	}

	/**
	 * Index 0 must be <code>null</code> because it is replaced by the
	 * executable name/path. By default, this returns an array with a single
	 * element which means no parameter is present.
	 */
	protected String[] getDefaultParameters() {
		return new String[1];
	}

	/**
	 * Override this calling <code>super.close()</code> afterwards in order
	 * to ensure the process is terminated.
	 */
	@Override
	public void close() throws IOException {
		try {
			proc.waitFor();
		} catch(Exception ex) {
			proc.destroy();
			throw new IOException("Failed to wait for process " +
							"termination.", ex);
		}	
	}

}
