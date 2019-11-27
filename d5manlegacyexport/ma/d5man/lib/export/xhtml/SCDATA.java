package ma.d5man.lib.export.xhtml;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.tools2.xml.MaBufXML;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.procxml.SubHandler;

class SCDATA extends SubHandler<ProcessingResult> {

	private final ParagraphCoordinator c;

	SCDATA(ParagraphCoordinator c) {
		super();
		this.c = c;
	}

	@Override
	protected boolean cdata(String text) throws D5IOException, SAXException {
		return cdata(r.body, c, text);
	}

	static boolean cdata(MaBufXML out, ParagraphCoordinator c, String text)
					throws D5IOException, SAXException {
		if(!out.isRaw() && !isSpaceOnly(text))
			c.openPara();
		out.content(text);
		return true;
	}

	private static boolean isSpaceOnly(String s) {
		return s.trim().length() == 0;
	}

}
