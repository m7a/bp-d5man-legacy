package ma.d5man.export;

import java.util.Arrays;

import ma.d5man.lib.shr.AbstractStatusThread;

public class StatusThread extends AbstractStatusThread {

	protected final LivingQueue    q;
	protected final ExportWorker[] workers;

	protected StatusThread(LivingQueue queue, ExportWorker[] workers) {
		super();
		q            = queue;
		this.workers = workers;
	}

	@Override
	protected String getStatusLine() {
		int ew = (int)Math.max(1, Math.ceil(
					Math.log10(q.getTotalElements())));
		int tw = (int)Math.max(1, Math.ceil(
					Math.log10(workers.length)));
		int fw = (int)Math.max(1, Math.ceil(
					Math.log10(workers.length * 20)));
		int alive = 0;
		long coll = 0;
		long err = 0;
		for(ExportWorker w: workers) {
			if(w.isAlive())
				alive++;
			coll += w.getProcessedElements();
			err += w.getErrors();
		}
		return String.format(
			"%" + ew + "d/%" + ew + "d  %2.2f%%  ACT %" + tw +
				"d/%" + tw + "d  PROC %" + ew + "d  E %" + fw +
				"d  %2.2f%%",
			q.getQueuedElements(), q.getTotalElements(),
			(double)q.getQueuedElements() * 100 /
							q.getTotalElements(),
			alive, workers.length, coll, err,
			(double)coll * 100 / q.getTotalElements()
		);
	}

}
