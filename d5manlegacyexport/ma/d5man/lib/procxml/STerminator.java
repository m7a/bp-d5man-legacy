package ma.d5man.lib.procxml;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import ma.d5man.lib.lls.D5IOException;

public class STerminator extends SubHandler<BasicProcessingResult> {

	public STerminator() {
		super();
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		if(!n.equals("d5man")) // ignore page start
			terminate();
		return true;
	}

	@Override
	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		terminate();
		return true;
	}

	@Override
	protected boolean cdata(String text) throws D5IOException,
								SAXException {
		if(text.replace('\n', ' ').trim().length() == 0)
			return true; // do not term if nothing real
		terminate();
		return true;
	}

	private void terminate() throws RegularTerminationException {
		throw new RegularTerminationException();
	}

}
