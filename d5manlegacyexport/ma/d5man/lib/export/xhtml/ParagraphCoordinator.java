package ma.d5man.lib.export.xhtml;

import ma.d5man.lib.procxml.BasicProcessingResult;
import ma.d5man.lib.procxml.SubHandler;

class ParagraphCoordinator extends SubHandler<ProcessingResult> {

	private Para mode;

	ParagraphCoordinator() {
		super();
	}

	@Override
	protected void reset(BasicProcessingResult r) {
		super.reset(r);
		mode = Para.NOBLOCK;
	}

	void setMode(Para mode) {
		this.mode = mode;
	}

	void openPara() {
		if(mode == Para.NOBLOCK) {
			r.body.openBlock("p");
			mode = Para.PARABLOCK;
		}
	}

	void exitPara(Para to) {
		if(mode == Para.PARABLOCK)
			r.body.close();
		mode = to;
	}

}
