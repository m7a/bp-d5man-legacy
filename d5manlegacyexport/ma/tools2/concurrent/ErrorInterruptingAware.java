package ma.tools2.concurrent;

/**
 * An interface to be implemented by threads potentially notified by
 * StderrCacher.
 *
 * @version 1.0
 * @since Tools 2.1, 2015/10/03
 */
public interface ErrorInterruptingAware {

	/**
	 * Invoked before interrupt is called due to the StderrCacher.
	 */
	public void setInterruptDueToErrorFollows();

}
