package ma.d5man.export;

import java.util.ArrayList;
import java.nio.file.Path;

import ma.d5man.lib.j8.Consumer;
import ma.d5man.lib.db.D5ManDBPageMeta;
import ma.d5man.lib.export.FileExport;
import ma.d5man.lib.export.fs.WriteControl;
import ma.d5man.lib.export.proc.D5ManIOResolver;
import ma.d5man.lib.export.xhtml.page.D5ManPage;
import ma.d5man.lib.export.xhtml.page.ZIPCache;

public class ExportWorker extends Thread {

	private static final int EXCEPTION_LIMIT = 20;

	// shared
	private final boolean first;
	private final Path dest;
	private final LivingQueue queue;
	private final D5ManIOResolver resolver;
	private final WriteControl wctrl;

	// thread specific
	private final FileExport assocExport;
	private final ZIPCache zip;
	private final ArrayList<Exception> ebuf;

	private long processed;
	private boolean alive;

	ExportWorker(Path dest, FileExport export, LivingQueue queue,
				D5ManIOResolver resolver, ChecksumTable tbl,
				int i, Consumer<Path> fileCreationListener) {
		super("Export Worker " + i);
		this.first    = (i == 0);
		this.dest     = dest;
		this.queue    = queue;
		this.resolver = resolver;
		wctrl         = new ChangedWriteControl((tbl == null?
						new AlwaysWriteControl(): tbl),
						fileCreationListener);
		assocExport   = export;
		zip           = new ZIPCache();
		alive         = false;
		ebuf          = new ArrayList<Exception>();
		processed     = 0;
	}

	@Override
	public void run() {
		alive = true;

		runExport(first);
		while(alive && !isInterrupted())
			runExport(false);

		// If a worker terminates while the queue still contains item,
		// there is some sort of fatal error and we need to terminate
		// leaving some elements unprocessed
		D5ManDBPageMeta pmeta = queue.allPages.peek();
		if(pmeta != null && !(pmeta instanceof TerminatorPage))
			queue.interrupt();

		alive = false;
	}

	private void runExport(boolean first) {
		D5ManDBPageMeta cmeta = null;
		try {
			// make sure static resources are exported
			// TODO z CODE LAYOUT A BIT DIRTY...
			if(first) {
				processSub(null);
			} else {
				try {
					cmeta = queue.allPages.take();
					// poison pill pattern
					if(cmeta instanceof TerminatorPage)
						alive = false;
					else
						process(cmeta);
				} catch(InterruptedException ex) {
					if(alive)
						ex.printStackTrace();
					alive = false;
				}
			}
		} catch(Exception ex) {
			Exception e2 = new Exception("Exception occurred " +
				"during export. Last known page to be " +
				"tried to be exported is: " + cmeta, ex);
			ebuf.add(e2);
			if(ebuf.size() > EXCEPTION_LIMIT) {
				ebuf.add(new Exception("EXCEPTION LIMIT " +
					EXCEPTION_LIMIT + " EXCEEDED. " +
					"OPERATION HALTED."));
				alive = false;
			}
		}
	}

	private void process(D5ManDBPageMeta page) throws Exception {
		processSub(new D5ManPage(page, resolver, zip));
		processed++;
	}

	private void processSub(D5ManPage pg) throws Exception {
		ma.d5man.lib.export.fs.SinglePageExport.syncToDisk(dest,
						assocExport.open(pg), wctrl);
	}

	@Override
	public void interrupt() {
		alive = false;
		super.interrupt();
	}

	long getProcessedElements() {
		return processed;
	}

	long getErrors() {
		return ebuf.size();
	}

	void term() throws Exception {
		join();

		try {
			zip.close();
		} catch(Exception ex) {
			ebuf.add(ex);
		}

		Exception fail = null;
		for(Exception e: ebuf) {
			if(fail == null)
				fail = e;
			else
				fail.addSuppressed(e);
		}

		if(fail != null)
			throw fail;
	}

}
