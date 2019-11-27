package ma.d5man.lib.procxml;

import org.xml.sax.SAXException;
import ma.d5man.lib.lls.D5IOException;

public abstract class XMLHandler<T extends BasicProcessingResult>
						extends ResultlessXMLHandler {

	private T result;

	protected XMLHandler() {
		super();
	}

	@Override
	public void startDocument() throws SAXException {
		result = initProcessingResult();
		forEachHandler(new SubInvoc() {
			public boolean run(ResultlessSubHandler h)
					throws SAXException, D5IOException {
				((SubHandler)h).reset(result);
				return false;
			}
		});
	}

	protected abstract T initProcessingResult();

	public T getCurrentProcessingResult() {
		return result;
	}

}
