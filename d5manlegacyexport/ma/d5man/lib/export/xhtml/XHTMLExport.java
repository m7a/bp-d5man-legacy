package ma.d5man.lib.export.xhtml;

import java.io.IOException;

import ma.d5man.lib.D5ManConstants;
import ma.d5man.lib.util.MergedIterable;
import ma.d5man.lib.db.PageName;
import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.lls.*;
import ma.d5man.lib.shr.AbstractD5ManPage;
import ma.d5man.lib.input.Conf;
import ma.d5man.lib.input.DownloadsTable;
import ma.d5man.lib.export.XMLExport;
import ma.d5man.lib.export.proc.real.D5Man2XMLProcess;
import ma.d5man.lib.export.proc.D5ManIOResolver;
import ma.d5man.lib.export.xhtml.urr.PageUnrelatedResources;
import ma.d5man.lib.export.xhtml.page.DMan;
import ma.d5man.lib.export.ExternalLanguageProcessors;

public class XHTMLExport extends XMLExport implements AutoCloseable {

	private final DMan man;
	private final PageUnrelatedResources pageUnrelatedRes;
	private final ExternalLanguageProcessors lang;
	private final XHTMLExportManager mgr;

	/**
	 * @param conf Configuration (<code>website_fs_override</code> and
	 *				external language processor creation)
	 * @param resolver Optional e.g. <code>D5ManIOResolveProcess</code>
	 * 				(nees to be open already)
	 * @param db Optional DB (for sitemap generation etc.)
	 * @param dtbl Optional downloads table (for page generation)
	 */
	public XHTMLExport(Conf conf, D5ManIOResolver resolver,
				XHTMLTemplate tpl, D5ManDB db,
				DownloadsTable dtbl) throws IOException {
		super(new D5Man2XMLProcess(conf.getD5Man2XML()));
		conv.open();
		// save memory if resource set view not intended.
		man = resolver == null? null: new DMan(db, resolver);
		pageUnrelatedRes = new PageUnrelatedResources(
				conf.getCommonRes().resolve(
				"website_fs_override"), db, man, dtbl, tpl);
		lang = new ExternalLanguageProcessors(conf);
		mgr = new XHTMLExportManager(lang, tpl);
		lang.open();
	}

	@Override
	public DRS createConversionResultResourceSet(AbstractD5ManPage p)
							throws D5IOException {
		DRS pdrs = p.access();
		D5Stream r;
		try {
			r = pdrs.openResource(pdrs.getFirstResourceName());
		} catch(IOException ex) {
			throw D5IOException.wrapIn(ex);
		}
		return mgr.open(super.createConversionResultResourceSet(p), r,
									p);
	}

	@Override
	public MinimalResourceSet createStaticResourceSet() {
		// previously this merged w/ super.createStaticResourceSet() but
		// it does not really seem to be apt to export a DTD which is
		// not even applicable...
		return pageUnrelatedRes;
	}

	public MinimalResourceSet createResourceSetView() throws IOException {
		if(!man.hasXHTMLExport())
			man.setXHTMLExport(this);
		return new MergedResourceSet(createStaticResourceSet(), man);
	}

	@Override
	public void close() throws IOException {
		if(man != null)
			man.close();
		lang.close();
		conv.close();
		pageUnrelatedRes.close();
	}

}
