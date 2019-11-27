package ma.d5man.lib.procxml;

public abstract class SubHandler<T extends BasicProcessingResult>
						extends ResultlessSubHandler {

	protected T r = null;

	/**
	 * After calling <code>super.reset(...)</code> a subclass should reset
	 * the object to be useful to process a new document.
	 */
	@SuppressWarnings("unchecked")
	protected void reset(BasicProcessingResult newProcResult) {
		r = (T)newProcResult;
	}

}
