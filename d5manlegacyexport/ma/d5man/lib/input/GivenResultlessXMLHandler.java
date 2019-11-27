package ma.d5man.lib.input;

import ma.d5man.lib.procxml.ResultlessSubHandler;
import ma.d5man.lib.procxml.ResultlessXMLHandler;

class GivenResultlessXMLHandler extends ResultlessXMLHandler {

	private final ResultlessSubHandler[] handlers;

	GivenResultlessXMLHandler(ResultlessSubHandler[] handlers) {
		super();
		this.handlers = handlers;
	}
	
	@Override
	protected ResultlessSubHandler[] getHandlers() {
		return handlers;
	}

}
