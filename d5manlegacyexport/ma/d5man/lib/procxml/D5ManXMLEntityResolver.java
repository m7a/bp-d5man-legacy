package ma.d5man.lib.procxml;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.net.URI;
import org.xml.sax.*;
import ma.d5man.lib.D5ManConstants;

public class D5ManXMLEntityResolver implements EntityResolver {

	public D5ManXMLEntityResolver() {
		super();
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
					throws IOException, SAXException {
		return resolveEntityS(publicId, systemId);
	}

	public static InputSource resolveEntityS(String publicId,
			String systemId) throws IOException, SAXException {
		checkId(publicId);

		URI uri      = constructURI(systemId);
		Path dtdPath = Paths.get(uri);
		String name  = dtdPath.getFileName().toString();

		return createDTDFromName(name);
	}

	private static void checkId(String publicId) throws SAXException {
		if(publicId != null)
			throw new SAXException("This parser does not know " +
				"public resources (should normally not be " +
				"necessary... wrong XML file?)");
	}

	private static URI constructURI(String systemId) throws SAXException {
		try {
			return new URI(systemId);
		} catch(Exception ex) {
			throw new SAXException("Failed to construct URI " +
					"from \"" + systemId + "\".", ex);
		}
	}

	public static InputSource createDTDFromName(String name)
					throws IOException, SAXException {
		for(String s: D5ManConstants.XML_DTD_NAMES)
			if(name.equals(s))
				return createDTDSource(s);
		throw new SAXException(
			"This parser can only load predefined DTDs. Other " +
			"DTDs are not supported and should not be necessary. " +
			"If you need this feature, go to " +
			AbstractDTDResolvingHandler.class.
			getCanonicalName() + " and add code as necessary."
		);
	}

	private static InputSource createDTDSource(String name)
							throws IOException {
		return new InputSource(AbstractDTDResolvingHandler.class.
			getResourceAsStream(D5ManConstants.XML_DTD_RES_PREFIX +
			name));
	}

}
