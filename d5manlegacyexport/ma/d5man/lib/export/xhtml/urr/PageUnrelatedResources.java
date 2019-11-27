package ma.d5man.lib.export.xhtml.urr;

import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Path;

import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.export.xhtml.page.DMan;
import ma.d5man.lib.export.xhtml.XHTMLTemplate;
import ma.d5man.lib.input.DownloadsTable;
import ma.d5man.lib.util.ConstantIterable;
import ma.d5man.lib.util.MergedIterable;
import ma.d5man.lib.lls.MinimalResourceSet;
import ma.d5man.lib.lls.MergedResourceSet;
import ma.d5man.lib.lls.ResourceOnDemand;

public class PageUnrelatedResources extends MergedResourceSet implements
								AutoCloseable {

	public PageUnrelatedResources(Path fsOverride, D5ManDB db, DMan man,
					DownloadsTable tbl, XHTMLTemplate tpl)
					throws IOException {
		super(createSubResources(fsOverride, man, db, tbl, tpl));
	}

	private static MinimalResourceSet[] createSubResources(Path fsOverride,
				DMan man, D5ManDB db, DownloadsTable tbl,
				XHTMLTemplate tpl) throws IOException {
		ArrayList<MinimalResourceSet> subRes =
					new ArrayList<MinimalResourceSet>();
		subRes.add(new DFSOverrides(fsOverride));

		if(db != null) {
			subRes.add(new DPageDB(db));
			subRes.add(new DSitemap(db));
			subRes.add(new DHtaccess(db));
		}

		if(tbl != null)
			subRes.add(new DDownloads(tbl, tpl));

		subRes.add(new DFind(man == null? subRes:
			new MergedIterable<MinimalResourceSet>(
				new ConstantIterable<MinimalResourceSet>(man),
				subRes)));

		// drain to array
		MinimalResourceSet[] subResources = new MinimalResourceSet[
								subRes.size()];
		subRes.toArray(subResources);
		return subResources;
	}

	@Override
	protected MinimalResourceSet access(MinimalResourceSet s)
							throws IOException {
		MinimalResourceSet t = super.access(s);
		if(t instanceof ResourceOnDemand)
			((ResourceOnDemand)t).open();
		return t;
	}

	@Override
	public void close() throws IOException {
		try {
			for(MinimalResourceSet s: subSets)
				if(s instanceof AutoCloseable)
					((AutoCloseable)s).close();
		} catch(IOException ex) {
			throw ex;
		} catch(Exception ex) {
			throw new IOException(ex);
		}
	}

}
