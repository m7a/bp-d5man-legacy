package ma.d5man.lib.export.xhtml.urr;

import java.util.*;
import java.text.SimpleDateFormat;
import java.io.Writer;
import java.io.IOException;

import ma.tools2.xml.MaBufXML;

import ma.d5man.lib.shr.DateFormats;
import ma.d5man.lib.db.D5ManDBPageMeta;
import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.D5ManConstants;

class DSitemap extends RamPage<D5ManDB> {

	DSitemap(D5ManDB d) throws IOException {
		super(d);
	}

	@Override
	String getName() {
		return "sitemap.xml";
	}

	@Override
	void writePageTo(Writer w) throws IOException {
		MaBufXML buf = new MaBufXML(0);
		populate(buf);
		w.write(buf.toString());
	}

	private void populate(MaBufXML buf) {
		openSitemap(buf);
		for(D5ManDBPageMeta m: d)
			addPage(buf, m);
		buf.close();
	}

	private static void openSitemap(MaBufXML buf) {
		buf.xmlLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		buf.newline();
		buf.beginBlock("urlset");
		buf.attribute("xmlns",
				"http://www.sitemaps.org/schemas/sitemap/0.9");
		buf.open();
	}

	private static void addPage(MaBufXML buf, D5ManDBPageMeta m) {
		addPageSub(buf, m, ".xhtml", 1.0f);
		// sources get 1/10 of normal pages for priority (we generally
		// do not want them to appear in search results...)
		addPageSub(buf, m, "@src.xhtml", 0.1f);
	}

	private static void addPageSub(MaBufXML buf, D5ManDBPageMeta m,
					String suffix, float priofactor) {
		buf.openBlock("url");
		// TODO z USE LINK FACILITY LIKE IN ProcessingResult (MERGE THESE FUNCTIONS TO A NEW LOCATION POTENTIALLY THEY COULD BE ADDED TO SOME SHARED XHTMLExportPageProcessing OR PageName even but NOT PROCESSING RESULT!)
		bcnl(buf, "loc", D5ManConstants.BASE + "/" +
						m.createFSPath("/", suffix));
		bcnl(buf, "lastmod", DateFormats.formatSitemap(
							m.getModified()));
		bcnl(buf, "priority", String.valueOf(m.getPriority() *
								priofactor));
		bcnl(buf, "changefreq", m.getFreq().toString());
		buf.close();
	}

	private static void bcnl(MaBufXML buf, String k, String v) {
		buf.openInline(k);
		buf.content(v);
		buf.close();
		buf.newline();
	}

}
