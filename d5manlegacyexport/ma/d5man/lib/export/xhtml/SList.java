package ma.d5man.lib.export.xhtml;

import java.util.Deque;
import java.util.ArrayDeque;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import ma.tools2.util.NotImplementedException;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.procxml.SubHandler;
import ma.d5man.lib.procxml.BasicProcessingResult;

class SList extends SubHandler<ProcessingResult> {

	private final ParagraphCoordinator c;

	private boolean lopen;
	private Deque<String> ltype;

	SList(ParagraphCoordinator c) {
		super();
		this.c = c;
	}

	@Override
	protected void reset(BasicProcessingResult r) {	
		super.reset(r);
		lopen = false;
		ltype = new ArrayDeque<String>();
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		switch(n) {
		case "lg":   beginListGroup();                break;
		case "list": beginList(att.getValue("type")); break;
		case "lt":   r.body.openBlock("dt");          break;
		case "i":    beginListItem();                 break;
		default:     return false;
		}
		return true;
	}

	private void beginListGroup() {
		c.exitPara(Para.OTHERBLOCK);
		r.body.openBlock("dl");
	}

	private void beginList(String type) {
		if(ltype.isEmpty())
			r.body.openBlock("dd");
		ltype.push(type);
		openBlockForListItem(type);
	}

	private void beginListItem() {
		if(!ltype.peek().equals("desc"))
			r.body.openBlock("li");
	}

	private void openBlockForListItem(String ltype) {
		switch(ltype) {
		case "pro":  r.body.openBlock("ul", new String[][] { { "class",
							"list_pro" } }); break;
		case "con":  r.body.openBlock("ul", new String[][] { { "class",
							"list_con" } }); break;
		case "num":  r.body.openBlock("ol");                      break;
		case "ul":   r.body.openBlock("ul");                      break;
		case "desc": /* nothing */                                break;
		default:     throw new NotImplementedException();
		}
	}

	@Override
	protected boolean endElement(String n) throws D5IOException,
								SAXException {
		switch(n) {
		case "lg":   endListGroup(); break;
		case "list": endList();      break;
		case "lt":   r.body.close(); break;
		case "i":    endListItem();  break;
		default:     return false;
		}
		return true;
	}

	private void endListItem() {
		if(!ltype.peek().equals("desc"))
			r.body.close();
	}

	private void endList() {
		if(!ltype.pop().equals("desc"))
			r.body.close();
		if(ltype.isEmpty())
			r.body.close();
	}

	private void endListGroup() {
		r.body.close();
		c.setMode(Para.NOBLOCK);
	}

}
