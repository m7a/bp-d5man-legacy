package ma.d5man.lib.db;

import java.util.*;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.tools2.util.NotImplementedException;
import ma.d5man.lib.Compliance;
import ma.d5man.lib.input.ReverseLinkedDownload;
import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.util.NestedIterator;
import ma.d5man.lib.procxml.ResultlessSubHandler;

public class D5ManDB extends ResultlessSubHandler
					implements Iterable<D5ManDBPageMeta> {

	private final TreeMap<Integer, TreeMap<String, D5ManDBPageMeta>> cnt;

	private boolean active;
	private D5ManDBPageMeta current;
	private long nelem;

	public D5ManDB() {
		super();
		cnt  = new TreeMap<Integer, TreeMap<String, D5ManDBPageMeta>>();
		current = null;
		active  = false;
		nelem   = 0;
	}

	@Override
	public boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		if(n.equals("pages")) {
			active = true;
			return true;
		} else if(active) {
			switch(n) {
			case "page":       processPage(att);       return true;
			case "tags":       current.openTags();     return true;
			case "tag":        current.addTag(att.getValue("v"));
			                                           return true;
			case "attachment": processAttachment(att); return true;
			case "download":   processDownload(att);   return true;
			default:                                   return false;
			}
		}
		return false;
	}

	private void processPage(Attributes att) {
		current = new D5ManDBPageMeta(Integer.parseInt(att.getValue(
					"section")), att.getValue("name"));
		// TODO IS GETTING DESCRIPTION NECESSARY?
		//current.description = 
		current.expires = att.getValue("expires") != null?
					Long.parseLong(att.getValue("expires")):
					AbstractD5ManDBPageMeta.DATE_NOT_SET;
		current.modified = Long.parseLong(att.getValue("modified")) *
									1000;
		current.lang = att.getValue("lang");
		current.compliance = Compliance.fromString(
						att.getValue("compliance"));
		current.location = att.getValue("location");
		current.md5 = att.getValue("md5") != null?
					att.getValue("md5").toCharArray(): null;
		current.webPriority = Float.parseFloat(att.getValue(
							"web_priority"));
		current.webFreq = Frequency.fromString(att.getValue(
								"web_freq"));
		nelem++;
		accessSection(current.getSection()).put(current.getFSName(),
								current);
	}

	private TreeMap<String, D5ManDBPageMeta> accessSection(int section) {
		if(cnt.containsKey(section)) {
			return cnt.get(section);
		} else {
			TreeMap<String, D5ManDBPageMeta> map =
					new TreeMap<String, D5ManDBPageMeta>();
			cnt.put(section, map);
			return map;
		}
	}

	private void processAttachment(Attributes att) {
		current.putAttachment(att.getValue("name"),
			new Attachment(Long.parseLong(att.getValue("modified")),
				att.getValue("md5") == null? null:
				att.getValue("md5").toCharArray()));
	}

	private void processDownload(Attributes att) {
		current.addDownload(new ReverseLinkedDownload(
			att.getValue("url"),
			att.getValue("name"),
			att.getValue("description"),
			att.getValue("version"),
			att.getValue("size") == null? -1:
					Long.parseLong(att.getValue("size")),
			att.getValue("checked") == null? -1:
				Long.parseLong(att.getValue("checked")) * 1000,
			att.getValue("sha256") == null? null:
					att.getValue("sha256").toCharArray(),
			new StaticPageName(Integer.parseInt(
					att.getValue("for_section")),
					att.getValue("for_name"))
		));
	}

	@Override
	public boolean endElement(String n) throws D5IOException, SAXException {
		if(active) {
			switch(n) {
			case "pages": active = false; return true;
			case "page":  current = null; return true;
			default:                      return false;
			}
		}
		return false;
	}

	public TreeMap<Integer, TreeMap<String, D5ManDBPageMeta>> a() {
		return cnt;
	}

	@Override
	public Iterator<D5ManDBPageMeta> iterator() {
		return new NestedIterator<D5ManDBPageMeta,TreeMap<String,
				D5ManDBPageMeta>>(cnt.values().iterator()) {
			@Override
			protected Iterator<D5ManDBPageMeta> convert(
					TreeMap<String, D5ManDBPageMeta> e) {
				return e.values().iterator();
			}
		};
	}

	public long getSize() {
		return nelem;
	}

	public AbstractD5ManDBPageMeta get(PageName pn) {
		return cnt.containsKey(pn.getSection())?
			cnt.get(pn.getSection()).get(pn.getFSName()): null;
	}

}
