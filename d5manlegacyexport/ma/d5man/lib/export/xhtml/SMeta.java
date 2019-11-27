package ma.d5man.lib.export.xhtml;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.procxml.SMetaOnly;

class SMeta extends SMetaOnly<ProcessingResult> {

	private final ParagraphCoordinator c;

	SMeta(ParagraphCoordinator c) {
		super();
		this.c = c;
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		if(super.startElement(n, att))
			return true;

		if(n.equals("raw_xml") && isActive()) {
			r.head.setEscaping(false);
			return true;
		}

		return false;
	}

	@Override
	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		if(super.endElement(n))
			return true;

		if(n.equals("raw_xml") && isActive()) {
			r.head.setEscaping(true);
			return true;
		}

		return false;
	}

	@Override
	protected boolean cdata(String text) throws D5IOException,
								SAXException {
		return isActive() && SCDATA.cdata(r.head, c, text);
	}

}
