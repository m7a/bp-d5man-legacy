package ma.d5man.lib.export.xhtml;

import ma.d5man.lib.procxml.SubHandler;
import ma.d5man.lib.procxml.BasicProcessingResult;
import ma.d5man.lib.export.ExternalLanguageProcessors;

class CodeCoordinator extends SubHandler<ProcessingResult> {

	private String lang;
	final ExternalLanguageProcessors proc;

	CodeCoordinator(ExternalLanguageProcessors proc) {
		super();
		this.proc = proc;
	}

	@Override
	protected void reset(BasicProcessingResult r) {
		super.reset(r);
		lang = null;
	}

	void setLang(String lang) {
		this.lang = lang;
	}

	boolean hasLang() {
		return lang != null;
	}

	String getLang() {
		return lang;
	}

}
