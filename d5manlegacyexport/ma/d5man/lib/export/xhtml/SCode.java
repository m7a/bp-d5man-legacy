package ma.d5man.lib.export.xhtml;

import java.io.IOException;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.tools2.util.BufferUtils;
import ma.tools2.util.NotImplementedException;
import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.lls.DRS;
import ma.d5man.lib.lls.StringD5Stream;
import ma.d5man.lib.procxml.SubHandler;
import ma.d5man.lib.procxml.BasicProcessingResult;

class SCode extends SubHandler<ProcessingResult> {

	private final ParagraphCoordinator parC;
	private final CodeCoordinator codeC;

	private StringBuilder codebuf;

	SCode(ParagraphCoordinator parC, CodeCoordinator codeC) {
		super();
		this.parC  = parC;
		this.codeC = codeC;
	}

	@Override
	protected void reset(BasicProcessingResult r) {
		super.reset(r);
		codebuf = null;
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		switch(n) {
		case "codeblock": beginCodeBlock();  break;
		case "c":         beginCodeInline(); break;
		default:          return false;
		}
		return true;
	}

	void beginCodeBlock() {
		parC.exitPara(Para.OTHERBLOCK);
		if(codeC.hasLang())
			codebuf = new StringBuilder();
		else
			r.body.openRaw("pre", new String[][] { { "class",
								"code" } });
	}

	private void beginCodeInline() {
		parC.openPara();
		r.body.openInline("code");
	}

	@Override
	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		switch(n) {
		case "codeblock": endCodeBlock(); break;
		case "c":         r.body.close(); break;
		default:          return false;
		}
		return true;
	}

	void endCodeBlock() throws D5IOException {
		if(codebuf != null) {
			flushCodeBuf();
			codebuf = null;
		} else {
			r.body.close();
		}
		parC.setMode(Para.NOBLOCK);
	}

	private void flushCodeBuf() throws D5IOException {
		DRS result = codeC.proc.convert(new StringD5Stream(
					codebuf.toString()), codeC.getLang());
		addCSSResource(result);
		writeCodeBody(result);
	}

	private void addCSSResource(DRS result) throws D5IOException {
		String cssName = searchCSSResource(result);
		String cssPath = r.linkResource(cssName);
		try {
			r.addResource(cssName, result.openResource(cssName));
		} catch(IOException ex) {
			throw D5IOException.wrapIn(ex);
		}
		linkResourceInHead(cssPath);
	}

	private static String searchCSSResource(DRS drs) {
		for(String i: drs.getResourceNames())
			if(i.endsWith(".css"))
				return i;

		throw new NotImplementedException("Code w/o Stylesheet " +
					"detected. This is a program bug.");
	}

	private void linkResourceInHead(String cssPath) {
		r.head.beginInline("link");
		r.head.attribute("rel",  "stylesheet");
		r.head.attribute("type", "text/css");
		r.head.attribute("href", cssPath);
		r.head.empty();
	}

	private void writeCodeBody(DRS result) throws D5IOException {
		r.body.openBlock("div");
		r.body.setEscaping(false);
		try {
			r.body.content(BufferUtils.readfile(result.openResource(
					result.getFirstResourceName()).
					openInputStreamRepresentation()));
		} catch(D5IOException ex) {
			throw ex;
		} catch(IOException ex) {
			throw new D5IOException(ex);
		}
		r.body.setEscaping(true);
		r.body.close();
	}

	@Override
	protected boolean cdata(String text) throws D5IOException,
								SAXException {
		if(codebuf != null) {
			codebuf.append(text);
			return true;
		} else {
			return false;
		}
	}

}
