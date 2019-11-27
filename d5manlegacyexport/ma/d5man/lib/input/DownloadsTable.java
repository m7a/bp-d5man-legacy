package ma.d5man.lib.input;

import java.util.Iterator;
import java.util.ArrayList;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.d5man.lib.db.StaticPageName;
import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.procxml.ResultlessSubHandler;

public class DownloadsTable extends ResultlessSubHandler
				implements Iterable<ReverseLinkedDownload> {

	private static enum Mode { PRE, IN, POST };

	private final ArrayList<ReverseLinkedDownload> downloads;
	private Mode mode;

	public DownloadsTable() {
		super();
		downloads = new ArrayList<ReverseLinkedDownload>();
		mode = Mode.PRE;
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		if(n.equals("downloads") && mode == Mode.PRE) {
			mode = Mode.IN;
			return true;
		} else if(n.equals("download") && mode == Mode.IN) {
			downloads.add(createDownload(att));
			return true;
		} else if(n.equals("pages") && mode == Mode.PRE) {
			mode = Mode.POST;
			return false; // others might want to process this, too
		} else {
			return false;
		}
	}

	private static ReverseLinkedDownload createDownload(Attributes a) {
		return new ReverseLinkedDownload(
			a.getValue("url"), a.getValue("name"),
			a.getValue("description"), a.getValue("version"),
			tol(a.getValue("size")), mt(tol(a.getValue("checked"))),
			a.getValue("sha256") == null? null:
					a.getValue("sha256").toCharArray(),
			new StaticPageName(Integer.parseInt(
						a.getValue("for_section")),
						a.getValue("for_name")));
	}

	private static long mt(long l) {
		return l > 0? l * 1000: l;
	}

	private static long tol(String in) {
		return in == null? -1: Long.parseLong(in);
	}

	@Override
	protected boolean endElement(String n)
					throws D5IOException, SAXException {
		if(n.equals("downloads") && mode == Mode.IN) {
			mode = Mode.POST;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Iterator<ReverseLinkedDownload> iterator() {
		return downloads.iterator();
	}

}
