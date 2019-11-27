package ma.d5man.lib.export.xhtml;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.procxml.SubHandler;
import ma.d5man.lib.procxml.BasicProcessingResult;

class SBlock extends SubHandler<ProcessingResult> {

	private final ParagraphCoordinator c;

	private int seclevel;

	SBlock(ParagraphCoordinator c) {
		super();
		this.c = c;
	}

	@Override
	protected void reset(BasicProcessingResult r) {
		super.reset(r);
		seclevel = 1;
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		switch(n) {
		case "section": beginSection(att.getValue("title")); break;
		case "par":     c.exitPara(Para.NOBLOCK);            break;
		default:        return false;
		}
		return true;
	}

	private void beginSection(String title) {
		c.exitPara(Para.OTHERBLOCK);
		r.body.openBlock("h" + (++seclevel));
		r.body.content(title);
		r.body.close();
	}

	@Override
	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		switch(n) {
		case "section": seclevel--;   break;
		default:        return false;
		}
		return true;
	}

}
