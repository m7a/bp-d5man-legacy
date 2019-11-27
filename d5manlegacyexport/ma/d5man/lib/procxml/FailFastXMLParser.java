package ma.d5man.lib.procxml;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class FailFastXMLParser extends DefaultHandler {

	@Override
	public void warning(SAXParseException ex) throws SAXException {
		super.warning(ex);
		throw new SAXException("SAX generated a warning (see cause).",
									ex);
	}
	
	@Override
	public void error(SAXParseException ex) throws SAXException {
		super.error(ex);
		throw new SAXException("SAX error occurred (see cause).", ex);
	}
	
	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		super.fatalError(ex);
		throw new SAXException("Fatal SAX error occurred (see cause).",
									ex);
	}

}
