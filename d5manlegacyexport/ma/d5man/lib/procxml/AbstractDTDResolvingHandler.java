package ma.d5man.lib.procxml;

import java.io.IOException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class AbstractDTDResolvingHandler extends FailFastXMLParser {

	private final D5ManXMLEntityResolver r;

	AbstractDTDResolvingHandler() {
		super();
		r = new D5ManXMLEntityResolver();
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
					throws IOException, SAXException {
		return r.resolveEntity(publicId, systemId);
	}

}
