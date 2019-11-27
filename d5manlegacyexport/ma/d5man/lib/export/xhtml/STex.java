package ma.d5man.lib.export.xhtml;

import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.tools2.util.BufferUtils;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.lls.StringD5Stream;
import ma.d5man.lib.procxml.SubHandler;
import ma.d5man.lib.procxml.BasicProcessingResult;

class STex extends SubHandler<ProcessingResult> {

	private final CodeCoordinator c;
	private final ParagraphCoordinator c2;

	private StringBuilder tex;

	STex(ParagraphCoordinator c2, CodeCoordinator c) {
		super();
		this.c = c;
		this.c2 = c2;
	}

	@Override
	protected void reset(BasicProcessingResult r) {
		super.reset(r);
		tex = null;
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		switch(n) {
		case "tex":
		case "math": tex = new StringBuilder(); return true;
		default:                                return false;
		}
	}

	@Override
	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		switch(n) {
		case "tex":  return endTex();
		case "math": return endMath();
		default:     return false;
		}
	}

	private boolean endTex() throws D5IOException {
		String data = tex.toString().trim();
		tex = null;

		if(data.startsWith("\\img{"))
			pragmaImage(data);
		else if(data.startsWith("\\code{"))
			c.setLang(data.substring(6, data.length() - 1));
		else
			return false;

		return true;
	}

	private void pragmaImage(String data) throws D5IOException {
		int sep = data.indexOf("}");
		if(sep == -1)
			throw new RuntimeException("Syntax error in image " +
								"pragma");
		else
			addImage(data.substring(5, sep), data.substring(sep + 2,
							data.length() - 1));
	}

	private void addImage(String file, String title) throws D5IOException {
		r.body.openBlock("p", new String[][] { { "class", "figure" } });
		r.body.beginInline("img");
		r.body.attribute("src", r.linkResource(resolveImage(file)));
		r.body.attribute("alt", title);
		r.body.empty();
		r.body.beginInline("br");
		r.body.empty();
		r.body.content(unescapeTex(title));
		r.body.close();
	}

	private String resolveImage(String file) throws D5IOException {
		int sep = file.lastIndexOf('.');
		if(sep != -1 && couldBeExtension(file.substring(sep + 1)))
			return file; // assume there is an extension.

		String fileE = null;

		// Attempt to resolve by startsW&remove yields ext
		for(String s: r) {
			if(s.startsWith(file)) {
				String rem = s.substring(file.length() + 1);
				if(couldBeExtension(rem))
					// prefer other than PDF for websites!
					if(fileE == null ||
							fileE.endsWith(".pdf"))
						fileE = s;
			}
		}
		if(fileE == null)
			throw new D5IOException("Could not Resolve image " +
						"name for \"" + file + "\"");
		else
			return fileE;
	}

	private static boolean couldBeExtension(String part) {
		int len = part.length();
		return len == 3 || len == 4;
	}

	private static String unescapeTex(String str) {
		// TODO ASTAT INCOMPLETE
		return str.replace("\\_", "_");
	}

	private boolean endMath() throws SAXException, D5IOException {
		try {
			D5Stream result = c.proc.convert(new StringD5Stream(
					tex.toString()), "MATH").openFirst();
			c2.openPara();
			r.body.xmlLine(BufferUtils.readfile(result.
					openInputStreamRepresentation()));
		} catch(D5IOException ex) {
			throw ex;
		} catch(IOException ex) {
			throw new D5IOException(ex);
		} finally {
			tex = null;
		}
		return true;
	}

	@Override
	protected boolean cdata(String text) {
		if(tex == null) {
			return false;
		} else {
			tex.append(text);
			return true;
		}
	}

}
