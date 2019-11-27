package ma.d5man.lib.lls;

import java.io.IOException;

/**
 * Represents a resource which can be opened on demand in order not to create
 * unnecessary objects if they are not used.
 *
 * @version 1.0
 * @since 2014
 */
public abstract class ResourceOnDemand implements AutoCloseable {

	private boolean open;

	protected ResourceOnDemand() {
		super();
		this.open = false;
	}

	/** Needs to be overriden and called by subclasses. */
	public void open() throws IOException {
		this.open = true;
	}

	public boolean isOpen() {
		return open;
	}

	@Override
	public abstract void close() throws IOException;

}
