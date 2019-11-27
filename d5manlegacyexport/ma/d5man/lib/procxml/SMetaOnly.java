package ma.d5man.lib.procxml;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.d5man.lib.lls.D5IOException;

public class SMetaOnly<T extends BasicProcessingResult> extends SubHandler<T> {

	private boolean active;

	public SMetaOnly() {
		super();
	}

	protected boolean isActive() {
		return active;
	}

	@Override
	protected void reset(BasicProcessingResult r) {
		super.reset(r);
		active = false;
	}
	
	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		switch(n) {
		case "meta":    active = true; return true;
		case "kv":      kv(att);       return true;
		default:                       return false;
		}
	}

	private void kv(Attributes att) {
		assert active;
		r.setFromHeaderKV(att.getValue("k"), att.getValue("v"));
	}

	@Override
	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		switch(n) {
		case "kv":   return true; // consume
		case "meta": active = false; return true;
		default:     return false;
		}
	}

}
