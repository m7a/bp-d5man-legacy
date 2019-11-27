package ma.d5man.lib.export.xhtml;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Iterator;
import ma.tools2.util.BufferUtils;

import ma.d5man.lib.util.MergedIterable;
import ma.d5man.lib.util.ConstantIterable;
import ma.d5man.lib.shr.DateFormats;
import ma.d5man.lib.lls.*;

import static ma.d5man.lib.db.AbstractD5ManDBPageMeta.*;

public class XHTMLTemplate {

	static final String EXT = ".xhtml";

	private static final String[] MENU_ENTRIES = {
		"main", "about", "news", "mdvl", "programs", "mods",
		"thoughts", "tests", "stories", "downloads", "experiments"
	};

	private final String cnt;

	final long mod;

	public XHTMLTemplate(Path loc) throws IOException {
		super();
		cnt = BufferUtils.readfile(Files.newInputStream(loc));
		mod = Files.getLastModifiedTime(loc).toMillis();
	}

	String applyTemplate(String[][] tbl) {
		String cnt = this.cnt;
		for(String[] i: tbl) {
			try {
				cnt = cnt.replace("&d5man_" + i[0] + ";", i[1]);
			} catch(NullPointerException ex) {
				throw new RuntimeException("i[0]=" +
						i[0] + ", i[1]=" + i[1], ex);
			}
		}
		return cnt;
	}

	// independent methods which conceptually belong to the template

	static String getCntAttrib(String suffix) {
		return suffix.equals("@src")? " class=\"whole_page_source\"":
									"";
	}

	static String tagsMeta(Iterable<String> tags) {
		StringBuilder ret = new StringBuilder();
		Iterator<String> iter = tags.iterator();
		while(iter.hasNext()) {
			ret.append(iter.next());
			if(iter.hasNext())
				ret.append(',');
		}
		return ret.toString();
	}

	static String tagsHTML(String prefix, Iterable<String> tags) {
		StringBuilder ret = new StringBuilder();
		for(String i: tags) {
			ret.append("<a href=\"");
			ret.append(prefix);
			ret.append("31/web_search.xhtml?tag=");
			ret.append(i);
			ret.append("\">");
			ret.append(i);
			ret.append("</a> ");
		}
		return ret.toString();
	}

	static String altlang(String lang) {
		return multistr(lang, "Sprache: Deutsch", "Language: English");
	}

	static String[][] createEntityReplacementTableMenu(String pageName) {
		String[][] ret = new String[MENU_ENTRIES.length][2];
		for(int i = 0; i < MENU_ENTRIES.length; i++) {
			ret[i][0] = "mcls_" + MENU_ENTRIES[i];
			ret[i][1] = ismenrel(pageName, MENU_ENTRIES[i])?
								"sel": "n_sel";
		}
		return ret;
	}

	private static boolean ismenrel(String pageName, String minn) {
		int pos = pageName.indexOf('/');
		if(pos != -1) {
			String pre = pageName.substring(0, pos);
			if(pre.equals("overview") || pre.equals("web"))
				return pageName.substring(pos + 1).equals(minn);
		}
		return false;
	}

	static String getDisplaySrc(String prefix, int section, String name,
								String suffix) {
		String[] f = new String[2];
		if(suffix.equals("@src")) {
			f[0] = "<a href=\"" + ProcessingResult.linkPage(prefix,
						section, name) + "\">%s</a>";
			f[1] = "%s";
		} else {
			f[0] = "%s";
			// TODO HACKY
			f[1] = "<a href=\"" + ProcessingResult.linkPage(prefix,
					section, name + "@src") + "\">%s</a>";
		}
		return String.format(f[0] + " " + f[1], "WEB/XHTML", "SRC/D5I");
	}

}
