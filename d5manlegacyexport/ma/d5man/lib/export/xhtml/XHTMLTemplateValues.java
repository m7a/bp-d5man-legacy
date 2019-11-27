package ma.d5man.lib.export.xhtml;

import java.net.URLEncoder;

import ma.d5man.lib.D5ManConstants;
import ma.d5man.lib.shr.DateFormats;
import ma.d5man.lib.db.PageName;
import ma.d5man.lib.db.AbstractD5ManDBPageMeta;

import static ma.d5man.lib.export.xhtml.XHTMLTemplate.*;

public class XHTMLTemplateValues {

	private final XHTMLTemplate t;
	private final AbstractD5ManDBPageMeta meta;

	private String addToHead;
	private String body;
	private String prefix;
	private String suffix;
	private String copyright;

	public XHTMLTemplateValues(XHTMLTemplate t,
					AbstractD5ManDBPageMeta meta) {
		super();
		this.t    = t;
		this.meta = meta;
		addToHead = "";
		body      = "";
		prefix    = "";
		suffix    = "";
		copyright = "";
	}

	public void setAddToHead(String str) {
		addToHead = str;
	}

	public void setBody(String str) {
		body = str;
	}

	public void setSurroundings(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public void setCopyright(String copyright) {
		if(copyright != null)
			this.copyright = copyright;
	}

	public String apply() {
		return t.applyTemplate(createEntityReplacementTable());
	}

	private String[][] createEntityReplacementTable() {
		String[][] drep = createEntityReplacementTableData();
		String[][] mrep = XHTMLTemplate.
			createEntityReplacementTableMenu(meta.getName());
		String[][] ret = new String[drep.length + mrep.length][];
		int y = 0;
		for(String[] s: drep)
			ret[y++] = s;
		for(String[] s: mrep)
			ret[y++] = s;
		return ret;
	}

	private String[][] createEntityReplacementTableData() {
		return new String[][] {
			{ "title",                  meta.getDescription() },
			{ "pre",                    prefix },
			{ "tags_meta",              tagsMeta(meta.getTags()) },
			{ "additional_header_data", addToHead },
			{ "page",                   meta.getName() },
			{ "section", String.valueOf(meta.getSection()) },
			{ "lang",                   meta.getLanguage() },
			{ "lang_alt", altlang(meta.getLanguage()) },
			{ "content",                body },
			{ "tags_html", tagsHTML(prefix, meta.getTags()) },
			{ "compliance", meta.getCompliance().toString() },
			{ "page_uri_urlencoded",    getURI(meta, suffix) },
			{ "copyright",              copyright },
			{ "display_src",            makeDisplaySrc() },
			{ "cnt_attrib",             getCntAttrib(suffix) },
			{ "mod_http", DateFormats.formatHTTP(Math.max(t.mod,
							meta.getModified())) },
			{ "mod_human", DateFormats.formatHuman(
							meta.getModified()) },
			// suffix always empty to unify @src and no @src.
			{ "canonical",              getURIRaw(meta, "") },
		};
	}

	// TODO z WHERE TO PUT THIS METHOD?
	public static String getURI(PageName p, String suffix) {
		try {
			return URLEncoder.encode(getURIRaw(p, suffix), "UTF-8");
		} catch(Exception ex) {
			ex.printStackTrace();
			return "ERROR";
		}
	}

	private static String getURIRaw(PageName p, String suffix) {
		return D5ManConstants.BASE + "/" + PageName.createFSPath(
				p.getSection(), p.getName(), "/", suffix + EXT);
	}

	// TODO z HACKY
	private String makeDisplaySrc() {
		return meta.getName().equals("web/downloads")? "":
					getDisplaySrc(prefix, meta.getSection(),
							meta.getName(), suffix);
	}

}
