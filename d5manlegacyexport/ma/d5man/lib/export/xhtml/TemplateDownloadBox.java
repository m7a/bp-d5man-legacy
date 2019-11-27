package ma.d5man.lib.export.xhtml;

import java.util.Iterator;
import ma.tools2.xml.MaBufXML;

import ma.d5man.lib.shr.DateFormats;
import ma.d5man.lib.db.AbstractDownload;
import ma.d5man.lib.db.AbstractD5ManDBPageMeta;
import ma.d5man.lib.input.ReverseLinkedDownload;
import static ma.d5man.lib.db.AbstractD5ManDBPageMeta.*;

class TemplateDownloadBox {

	// TODO z XHTML GENERATION code too long and slightly redundant (methods too basic...)

	static void createDownloadBoxesFromMeta(MaBufXML body,
						AbstractD5ManDBPageMeta meta) {
		Iterator<AbstractDownload> d = meta.getDownloads().iterator();
		if(d.hasNext()) {
			body.beginBlock("div");
			body.attribute("id", "page_downloads");
			body.open();
			while(d.hasNext())
				// TODO z it is not quite obvious that we require this sort of downloads here... introduce some guard or documentation for this. #4
				makeBox(body, (ReverseLinkedDownload)d.next(),
									meta);
			body.close();
		}
	}

	private static void makeBox(MaBufXML body, ReverseLinkedDownload d,
						AbstractD5ManDBPageMeta meta) {
		body.beginBlock("table");
		body.attribute("class", (d.link.getSection() ==
			meta.getSection()?  "": "foreign_") + "download_box");
		body.attribute("summary", d.description + " download details");
		body.open();
		body.openInline("caption");
		body.content(d.description);
		body.close();
		body.openBlock("tbody");
		if(d.version != null)
			infoline(body, "Version", d.version);
		if(d.size != -1)
			infoline(body, multistr(meta.getLanguage(),
						"Größe [KiB]", "Size [KiB]"),
						String.valueOf(d.size));
		if(d.sha256 != null) {
			infolinePre(body, "SHA256");
			body.attribute("class", "download_sha256");
			body.open();
			body.content(new String(d.sha256));
			infolinePost(body);
		}
		if(d.checked != -1)
			infoline(body, multistr(meta.getLanguage(),
					"Abgerufen", "Checked"),
					DateFormats.formatHuman(d.checked));

		if(d.link.getSection() != meta.getSection() ||
				!d.link.getName().equals(meta.getName())) {
			infolinePre(body, "Seite");
			body.open();
			body.beginInline("a");
			// TODO z HACKY
			body.attribute("href", ProcessingResult.linkPage("../",
					d.link.getSection(), d.link.getName()));
			body.open();
			body.content(d.link.toString());
			body.close();
			infolinePost(body);
		}

		body.openInline("tr");
		body.openInline("th");
		body.beginInline("a");
		body.attribute("href", d.url);
		body.open();
		body.content("Download");
		body.close();
		body.close();
		body.beginInline("td");
		body.attribute("class", "download_url");
		body.open();
		body.beginInline("a");
		body.attribute("href", d.url);
		body.open();
		body.content(d.url);
		body.close(); // a
		body.close(); // td
		body.close(); // tr
		body.newline();

		body.close();
		body.close();
	}

	private static void infoline(MaBufXML body, String k, String v) {
		infolinePre(body, k);
		body.open();
		body.content(v);
		infolinePost(body);
	}

	private static void infolinePre(MaBufXML body, String k) {
		body.openInline("tr");
		body.openInline("th");
		body.content(k);
		body.close();
		body.beginInline("td");
	}

	private static void infolinePost(MaBufXML body) {
		body.close();
		body.close();
		body.newline();
	}

}
