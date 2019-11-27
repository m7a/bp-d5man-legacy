package ma.d5man.lib.export.xhtml.urr;

import java.io.Writer;
import java.io.IOException;

import ma.tools2.util.StringUtils;
import ma.tools2.xml.MaBufXML;

import ma.d5man.lib.Compliance;
import ma.d5man.lib.util.ConstantIterable;
import ma.d5man.lib.db.AbstractD5ManDBPageMeta;
import ma.d5man.lib.db.Frequency;
import ma.d5man.lib.export.xhtml.ProcessingResult;
import ma.d5man.lib.export.xhtml.XHTMLTemplate;
import ma.d5man.lib.export.xhtml.XHTMLTemplateValues;
import ma.d5man.lib.input.DownloadsTable;
import ma.d5man.lib.input.ReverseLinkedDownload;

class DDownloads extends RamPage<TableAndTemplate> {

	private static class DownloadMetadata extends AbstractD5ManDBPageMeta {
		private DownloadMetadata() {
			super();
			description = "Downloads";
			modified = System.currentTimeMillis();
			lang = "de";
			compliance = Compliance.PUBLIC;
			webPriority = 0.4f;
			webFreq = Frequency.MONTHLY;
			setTags("masysma downloads");
		}
		@Override
		public int getSection() {
			return 31;
		}
		@Override
		public String getName() {
			return "web/downloads";
		}
	}

	DDownloads(DownloadsTable t, XHTMLTemplate x) throws IOException {
		super(new TableAndTemplate(t, x));
	}

	@Override
	String getName() {
		return "31/web_downloads.xhtml";
	}

	@Override
	void writePageTo(Writer w) throws IOException {
		MaBufXML main = new MaBufXML(3);
		createText(main);
		main.openBlock("table");
		createHeader(main);
		main.openBlock("tbody");
		for(ReverseLinkedDownload d2: d.t)
			addDownload(main, d2);
		main.close();
		main.close();

		XHTMLTemplateValues tv = new XHTMLTemplateValues(d.x,
							new DownloadMetadata());
		tv.setBody(main.toString());
		tv.setSurroundings("../", "");
		tv.setCopyright("Copyright (c) 2015 Ma_Sys.ma. For further " +
				"info send an e-mail to Ma_Sys.ma@web.de.");
		w.write(tv.apply());
	}

	private static void createText(MaBufXML b) {
		b.openBlock("p");
		b.content(
			"Auf dieser Seite finden Sie alle Downloads, die von " +
			"dieser Webseite aus verlinkt sind. MDVL Packages " +
			"sind leider momentan weder als Repository, noch als " +
			"Download verfügbar."
		);
		b.close();
	}

	private static void createHeader(MaBufXML b) {
		td(b, "Ma_Sys.ma Downloads", "caption");
		b.newline();
		b.openBlock("thead");
		b.openBlock("tr");
		th(b, "Seite");
		th(b, "Download");
		th(b, "Version");
		th(b, "Größe [KiB]");
		th(b, "SHA256");
		th(b, "Link");
		b.close();
		b.close();
	}

	private static void addDownload(MaBufXML b, ReverseLinkedDownload d) {
		b.openInline("tr");

		// TODO z ideally could use linking facilities of processing results... (also below)
		tda(b, "../" + d.link.createFSPath("/", ".xhtml"),
							d.link.toString());
		td(b, d.description);
		td(b, d.version);
		td(b, d.size == -1? "N/A": String.valueOf(d.size));
		b.beginInline("td");
		b.attribute("class", "download_sha256");
		b.open();
		b.content(d.sha256 == null? "N/A": new String(d.sha256));
		b.close();
		tda(b, d.url, "Download");

		b.close();
		b.newline();
	}

	private static void tda(MaBufXML b, String target, String title) {
		b.openInline("td");
		b.beginInline("a");
		b.attribute("href", target);
		b.open();
		b.content(title);
		b.close();
		b.close();
	}

	private static void td(MaBufXML b, String v) {
		td(b, v, "td");
	}

	private static void th(MaBufXML b, String v) {
		td(b, v, "th");
	}

	private static void td(MaBufXML b, String v, String el) {
		b.openInline(el);
		b.content(v);
		b.close();
	}

}
