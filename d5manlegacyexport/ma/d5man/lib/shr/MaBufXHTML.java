package ma.d5man.lib.shr;

import ma.tools2.xml.MaBufXML;

public class MaBufXHTML extends MaBufXML {

	public MaBufXHTML() {
		super(0);
		xmlLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		newline();

		xmlLine("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1 " +
					"plus MathML 2.0 plus SVG 1.1//EN\"");
		xmlLine("\t\t\"http://www.w3.org/2002/04/xhtml-math-svg/" +
						"xhtml-math-svg.dtd\">");
		newline();
	}

	public void openHTML() {
		beginBlock("html");
		attribute("xml:lang", "en");
		attribute("xmlns", "http://www.w3.org/1999/xhtml");
		open();
		openBlock("head");
	}

	public void openBody() {
		close(); // head
		openBlock("body");
	}

	public void endPage() {
		close(); // body
		close(); // html
	}

}
