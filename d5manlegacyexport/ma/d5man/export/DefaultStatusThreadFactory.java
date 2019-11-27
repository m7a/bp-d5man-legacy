package ma.d5man.export;

import ma.d5man.lib.j8.BiFunction;

class DefaultStatusThreadFactory implements BiFunction<LivingQueue,
						ExportWorker[], Thread> {

	@Override
	public Thread apply(LivingQueue q, ExportWorker[] w) {
		return new StatusThread(q, w);
	}

}
