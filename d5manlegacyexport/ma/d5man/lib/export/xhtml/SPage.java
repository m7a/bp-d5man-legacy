package ma.d5man.lib.export.xhtml;

import org.xml.sax.SAXException;
import ma.d5man.lib.procxml.SPageDummy;

class SPage extends SPageDummy<ProcessingResult> {

	private final ParagraphCoordinator c;

	SPage(ParagraphCoordinator c) {
		super();
		this.c = c;
	}

	@Override
	protected boolean processPage(String prefix) throws SAXException {
		boolean ret = super.processPage(prefix);
		if(ret)
			r.prefix = (prefix == null? "": prefix);
		return ret;
	}

	@Override
	protected boolean endPage() {
		c.exitPara(Para.NOBLOCK);
		return super.endPage();
	}

}
