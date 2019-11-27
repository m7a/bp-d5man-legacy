package ma.d5man.lib.procxml;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.d5man.lib.lls.D5IOException;

public abstract class ResultlessSubHandler {

	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		return false;
	}

	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		return false;
	}

	protected boolean cdata(String text) throws D5IOException,
								SAXException {
		return false;
	}

}
