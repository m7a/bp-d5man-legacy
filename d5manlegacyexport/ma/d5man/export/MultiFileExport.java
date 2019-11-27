package ma.d5man.export;

import java.io.IOException;
import java.nio.file.Path;

import ma.d5man.lib.j8.*;
import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.export.FileExport;

class MultiFileExport {

	private final ChecksumTable tbl;
	private final LivingQueue queue;
	private final ExportWorker[] workers;
	private final MultipleD5ManIOResolveProcesses resolvers;
	private final Thread status;

	MultiFileExport(Path resolver, Path dest, D5ManDB db,
			FileExport[] exports, int numResolvers,
			ChecksumTable tbl, BiFunction<LivingQueue,
			ExportWorker[], Thread> statusThreadFactory,
			Supplier<Consumer<Path>> fileCreationHandlerFactory)
			throws IOException {
		super();
		this.tbl  = tbl;
		queue     = new LivingQueue(db, exports.length);
		resolvers = new MultipleD5ManIOResolveProcesses(resolver,
								numResolvers);
		workers   = new ExportWorker[exports.length];
		status    = statusThreadFactory.apply(queue, workers);
		int j = 0;
		for(int i = 0; i < workers.length; i++) {
			workers[i] = new ExportWorker(dest, exports[i], queue,
					resolvers.procs[j++], tbl, i,
					fileCreationHandlerFactory.get());
			if(j == resolvers.procs.length)
				j = 0;
		}
	}

	void run() throws Exception {
		queue.start();
		status.start();

		for(ExportWorker w: workers)
			w.start();

		Exception fail = null;

		for(ExportWorker w: workers) {
			try {
				w.term(); // join and terminate
			} catch(Exception ex) {
				// collect exceptions
				if(fail == null)
					fail = ex;
				else
					fail.addSuppressed(ex);
			}
		}

		status.interrupt();

		if(queue.isAlive())
			queue.interrupt();

		try {
			queue.join();
		} catch(InterruptedException ex) {
			// only in error cases thus message OK although harmless
			ex.printStackTrace();
		}

		try {
			resolvers.close();
		} catch(Exception ex) {
			if(fail == null)
				fail = ex;
			else
				fail.addSuppressed(ex);
		}

		if(fail != null)
			throw fail;
	}

}
