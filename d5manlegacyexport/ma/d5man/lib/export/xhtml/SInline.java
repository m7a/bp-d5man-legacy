package ma.d5man.lib.export.xhtml;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.tools2.util.NotImplementedException;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.procxml.SubHandler;

class SInline extends SubHandler<ProcessingResult> {

	private final ParagraphCoordinator c;

	SInline(ParagraphCoordinator c) {
		super();
		this.c = c;
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		switch(n) {
		case "raw_xml": r.body.setEscaping(false);       break;
		case "q":       openQuote(att.getValue("lang")); break;
		case "exp":     exp(att);                        break;
		case "em":      beginEmphasis();                 break;
		case "link":    link(att);                       break;
		case "sym":     sym(att);                        break;
		case "key":     shortcutKey(att.getValue("x"));  break;
		default:        return false;
		}
		return true;
	}

	private void openQuote(String lang) {
		c.openPara();
		r.body.openInline("q", new String[][] { { "xml:lang", lang } });
	}

	private void exp(Attributes att) {
		r.body.content(att.getValue("base"));
		r.body.openInline("sup");
		r.body.content(att.getValue("exponent"));
		r.body.close();
	}

	private void beginEmphasis() {
		c.openPara();
		r.body.openInline("em");
	}

	private void link(Attributes att) {
		String title   = att.getValue("title");
		String section = att.getValue("section");
		String url     = att.getValue("url");

		c.openPara();
		r.body.openInline("a", new String[][] { {
				"href", section == null? url:
				r.linkPage(Integer.parseInt(section), title)
			} });

		if(title == null || title.equals("url")) {
			r.body.content(url.trim());
		} else {
			r.body.content(title.trim());
			if(section != null)
				r.body.content("(" + section.trim() + ")");
		}

		r.body.close();
	}

	private void sym(Attributes att) {
		String raw = att.getValue("raw");
		c.openPara();
		if(att.getValue("v").equals("exclamation"))
			dangerSymbol();
		else if(raw == null)
			throw new NotImplementedException("Symbols by name " +
					"only are currently not implemented.");
		else
			r.body.content(raw);
	}

	private void dangerSymbol() {
		r.body.setEscaping(false);
		r.body.openInline("span", new String[][] { { "class",
							"sym_exclamation" } });
		r.body.content("&#x26a0;");
		r.body.close();
		r.body.setEscaping(true);
	}

	private void shortcutKey(String x) {
		c.openPara();
		r.body.openInline("kbd");
		r.body.content(x);
		r.body.close();
	}

	@Override
	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		switch(n) {
		case "raw_xml": r.body.setEscaping(true); break;
		case "q":
		case "em":      r.body.close();           break;
		default:        return false;
		}
		return true;
	}

}
