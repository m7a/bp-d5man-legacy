package ma.d5man.lib.export.xhtml;

import org.xml.sax.SAXException;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.db.AbstractD5ManDBPageMeta;
import ma.d5man.lib.procxml.XMLHandler;
import ma.d5man.lib.procxml.SubHandler;
import ma.d5man.lib.export.ExternalLanguageProcessors;

/**
 * Remember: This class supports only documents which contain a single page!
 *
 * @version 2.0
 */
class XHTMLHandlerMain extends XMLHandler<ProcessingResult> {

	private final SubHandler[] handlers;
	private AbstractD5ManDBPageMeta meta;

	private class SThisMeta extends SubHandler<ProcessingResult> {
		@Override
		public boolean endElement(String en) throws D5IOException,
								SAXException {
			// != null to make this largely optional
			if(en.equals("meta") && meta != null) {
				r.setModified(meta.getModified());
				TemplateDownloadBox.createDownloadBoxesFromMeta(
								r.body, meta);
			}
			return false;
		}
	}

	XHTMLHandlerMain(ExternalLanguageProcessors lang) {
		super();
		meta = null;
		ParagraphCoordinator parC = new ParagraphCoordinator();
		CodeCoordinator codeC = new CodeCoordinator(lang);
		handlers = new SubHandler[] {
			new SPage(parC),
			new SThisMeta(),
			new SMeta(parC),
			new STex(parC, codeC),
			new SBlock(parC),
			new SInline(parC),
			new SList(parC),
			new STable(parC),
			new SCode(parC, codeC),
			new SCDATA(parC),
			parC, codeC
		};
	}

	@Override
	protected ProcessingResult initProcessingResult() {
		return new ProcessingResult();
	}

	@Override
	protected SubHandler[] getHandlers() {
		return handlers;
	}

	void setDBMeta(AbstractD5ManDBPageMeta meta) {
		this.meta = meta;
	}

}
