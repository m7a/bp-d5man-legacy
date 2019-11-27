package ma.d5man.lib.export.xhtml;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.procxml.SubHandler;

class STable extends SubHandler<ProcessingResult> {

	private final ParagraphCoordinator c;

	private boolean tbltr;

	STable(ParagraphCoordinator c) {
		super();
		this.c = c;
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		switch(n) {
		case "table": beginTable(att.getValue("title")); break;
		case "tf":    beginTableField();                 break;
		case "tnl":   tableNewline();                    break;
		case "tms":   tableMidSep();                     break;
		default:      return false;
		}
		return true;
	}

	private void beginTable(String title) {
		c.exitPara(Para.OTHERBLOCK);
		r.body.openBlock("table");
		if(title != null) {
			r.body.openBlock("caption");
			r.body.content(title);
			r.body.close();
		}
		r.body.setHoldback(true);
		r.body.openBlock("thead");
		tbltr = false;
	}

	private void beginTableField() {
		if(!tbltr) {
			r.body.openBlock("tr");
			tbltr = true;
		}
		r.body.openBlock("td");
	}

	private void tableNewline() {
		if(tbltr) {
			r.body.close();
			tbltr = false;
		}
	}

	private void tableMidSep() {
		assert !tbltr;
		if(r.body.isHoldback()) {
			r.body.close();
			r.body.setHoldback(false);
			replaceInHoldback("td>", "th>");
			r.body.flushHoldback();
			r.body.openBlock("tbody");
		}
	}

	private void replaceInHoldback(String a, String b) {
		StringBuilder hb = r.body.getHoldback();
		String rep = hb.toString().replace(a, b);
		hb.replace(0, rep.length(), rep);
	}

	@Override
	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		switch(n) {
		case "table": endTable();     break;
		case "tf":    r.body.close(); break;
		default:      return false;
		}
		return true;
	}

	private void endTable() {
		r.body.close();
		if(r.body.isHoldback()) {
			r.body.setHoldback(false);
			replaceInHoldback("thead>", "tbody>");
			r.body.flushHoldback();
		}
		r.body.close();
		c.setMode(Para.NOBLOCK);
	}

}
