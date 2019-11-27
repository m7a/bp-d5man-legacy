package ma.d5man.export;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

class AllFailErrorHandler implements ErrorHandler, ErrorListener {

	@Override
	public void warning(TransformerException exception)
						throws TransformerException {
		throw new TransformerException(
				"Transformer generated a warning", exception);
	}

	@Override
	public void fatalError(TransformerException exception)
						throws TransformerException {
		throw new TransformerException("Transformer generated a " +	
						"fatal error", exception);
	}

	@Override
	public void error(TransformerException exception)
						throws TransformerException {
		throw new TransformerException("Transformer generated an " +
						"error", exception);
	}

	@Override
	public void warning(SAXParseException ex) throws SAXException {
		throw new SAXException("SAX generated a warning", ex);
	}
	
	@Override
	public void error(SAXParseException ex) throws SAXException {
		throw new SAXException("SAX generated an error", ex);
	}

	@Override
	public void fatalError(SAXParseException ex) throws SAXException {
		throw new SAXException("SAX generated a fatal error", ex);
	}

}
