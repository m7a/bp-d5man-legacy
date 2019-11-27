package ma.d5man.lib.procxml;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.d5man.lib.lls.D5IOException;

public class SPageDummy<T extends BasicProcessingResult> extends SubHandler<T> {

	private int pn;

	public SPageDummy() {
		super();
	}

	@Override
	protected void reset(BasicProcessingResult r) {
		super.reset(r);
		pn = 0;
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		if(n.equals("page"))
			return processPage(att.getValue("prefix"));
		else
			return false;
	}

	protected boolean processPage(String prefix) throws SAXException {
		if(pn++ > 0)
			throw new SAXException("Only one page per document " +
								"supported.");
		return true;
	}

	@Override
	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		if(n.equals("page"))
			return endPage();
		else
			return false;
	}

	protected boolean endPage() {
		//c.exitPara(Para.NOBLOCK);
		return true;
	}


	
}
