package ma.d5man.lib.export.xhtml;

import java.io.IOException;
import org.xml.sax.SAXException;

import ma.tools2.util.BufferUtils;

import ma.d5man.lib.lls.DRS;
import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.db.AbstractD5ManDBPageMeta;
import ma.d5man.lib.procxml.GeneralExportManager;
import ma.d5man.lib.export.ExternalLanguageProcessors;

class XHTMLExportManager extends GeneralExportManager<XHTMLHandlerMain> {

	private final ExternalLanguageProcessors lang;
	private final XHTMLTemplate tpl;

	XHTMLExportManager(ExternalLanguageProcessors lang,
					XHTMLTemplate tpl) throws IOException {
		super(new XHTMLHandlerMain(lang));
		this.lang = lang;
		this.tpl  = tpl;
	}

	DRS open(final DRS xml, D5Stream pageSrc, AbstractD5ManDBPageMeta meta)
							throws D5IOException {
		handler.setDBMeta(meta);
		process(xml);
		ProcessingResult r = handler.getCurrentProcessingResult();
		ProcessingResult src = highlightPageSrc(pageSrc, r);
		return new XHTMLDRS(tpl, xml, r, src);
	}

	private ProcessingResult highlightPageSrc(D5Stream src,
				ProcessingResult orig) throws D5IOException {
		ProcessingResult ret = new ProcessingResult(orig);
		ret.suffix = "@src";
		ParagraphCoordinator pc = new ParagraphCoordinator();
		CodeCoordinator cc = new CodeCoordinator(lang);
		pc.reset(ret);
		cc.reset(ret);
		cc.setLang("masysma_d5man");
		SCode code = new SCode(pc, cc);
		code.reset(ret);
		code.beginCodeBlock();
		try {
			code.cdata(BufferUtils.readfile(
					src.openInputStreamRepresentation()));
		} catch(Exception ex) {
			throw D5IOException.wrapIn(ex);
		}
		code.endCodeBlock();
		return ret;
	}

}
