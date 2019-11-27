package ma.d5man.export;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ma.d5man.lib.db.D5ManDBPageMeta;
import ma.d5man.lib.db.D5ManDB;

/**
 * The enqueuing is performed by an extra thread in order to be able to
 * terminate this action from inside a worker thread
 */
public class LivingQueue extends Thread {

	private static final int QUEUE_LENGTH_FACTOR = 4;

	private final D5ManDB db;
	private final int nworkers;

	final BlockingQueue<D5ManDBPageMeta> allPages;

	private long havePut;
	private boolean alive;

	LivingQueue(D5ManDB db, int nworkers) {
		super("Living Queue");
		this.db       = db;
		this.nworkers = nworkers;
		alive         = false;
		havePut       = 0;
		allPages      = new ArrayBlockingQueue<D5ManDBPageMeta>(
				(int)Math.max(1, Math.min(db.getSize(),
				(long)(nworkers * QUEUE_LENGTH_FACTOR))));
	}

	@Override
	public void run() {
		alive = true;
		try {
			for(D5ManDBPageMeta m: db) {
				if(!alive || isInterrupted())
					break;
				allPages.put(m);
				havePut++;
			}
			for(int i = 0; i < nworkers; i++)
				allPages.put(new TerminatorPage());
		} catch(InterruptedException ex) {
			// only happens in error cases thus the message is OK
			// to display but generally non-fatal by itself
			ex.printStackTrace();
		}
		alive = false;
	}

	@Override
	public void interrupt() {
		alive = false;
		super.interrupt();
	}

	long getTotalElements() {
		return db.getSize();
	}

	long getQueuedElements() {
		return havePut;
	}

}
