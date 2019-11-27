package ma.d5man.lib.procxml;

import java.util.Map;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.d5man.lib.lls.D5IOException;

public abstract class ResultlessXMLHandler extends AbstractDTDResolvingHandler {

	static interface SubInvoc {
		boolean run(ResultlessSubHandler h) throws SAXException,
								D5IOException;
	}

	/* Additional information in case of errors */
	private final StringBuilder currentCharacters;
	private       String        currentElement;
	private       Attributes    currentAttributes;

	protected ResultlessXMLHandler() {
		super();
		currentCharacters = new StringBuilder();
	}

	/**
	 * Implementing classes must always return the very same objects when
	 * this is invoked because otherwise they are re-created for each event!
	 */
	protected abstract ResultlessSubHandler[] getHandlers();

	void forEachHandler(SubInvoc i) throws SAXException {
		try {
			for(ResultlessSubHandler h: getHandlers())
				if(i.run(h))
					return;
		} catch(Exception ex) {
			StringBuilder info = new StringBuilder("Current " +
					"processing information: element=");
			info.append(currentElement == null? "<null>":
							currentElement);
			info.append(", attributes=<");
			if(currentAttributes == null) {
				info.append("null");
			} else {
				for(int j = 0; j < currentAttributes.
							getLength(); j++) {
					info.append(currentAttributes.
								getQName(j));
					info.append('=');
					info.append(currentAttributes.
								getValue(j));
					info.append(',');
				}
			}
			info.append("> characters=<");
			info.append(currentCharacters);
			info.append('>');
			throw new SAXException(info.toString(), ex);
		}
	}

	@Override
	public void startElement(String u, String ln, final String qn,
				final Attributes att) throws SAXException {
		currentCharacters.setLength(0);
		currentElement    = qn;
		currentAttributes = att;
		forEachHandler(new SubInvoc() {
			public boolean run(ResultlessSubHandler h)
					throws SAXException, D5IOException {
				return h.startElement(qn, att);
			}
		});
	}

	@Override
	public void endElement(String u, String ln, final String qn)
							throws SAXException {
		forEachHandler(new SubInvoc() {
			public boolean run(ResultlessSubHandler h)
					throws SAXException, D5IOException {
				return h.endElement(qn);
			}
		});
		currentCharacters.setLength(0);
		currentElement    = null;
		currentAttributes = null;
	}

	@Override
	public void characters(char[] c, int s0, int len) throws SAXException {
		final String str = new String(c, s0, len);
		currentCharacters.append(str);
		forEachHandler(new SubInvoc() {
			public boolean run(ResultlessSubHandler h)
					throws SAXException, D5IOException {
				return h.cdata(str);
			}
		});
	}

}
