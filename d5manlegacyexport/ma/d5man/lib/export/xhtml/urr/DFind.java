package ma.d5man.lib.export.xhtml.urr;

import java.io.Writer;
import java.io.IOException;

import ma.d5man.lib.lls.MinimalResourceSet;
import ma.d5man.lib.shr.MaBufXHTML;

class DFind extends RamPage<Iterable<MinimalResourceSet>> {

	DFind(Iterable<MinimalResourceSet> dsAll) throws IOException {
		super(dsAll);
	}

	@Override
	String getName() {
		return "_find.xhtml";
	}

	@Override
	void writePageTo(Writer w) throws IOException {
		MaBufXHTML x = new MaBufXHTML();
		x.openHTML();
		x.openBody();
		x.openInline("title");
		x.content("Ma_Sys.ma D5Man Export: List of all files");
		x.close();
		x.openBlock("p");
		for(MinimalResourceSet s: d)
			for(String s2: s.getResourceNames())
				link(x, s2);
		x.close();
		x.endPage();
		w.write(x.toString());
	}

	private static void link(MaBufXHTML x, String s) {
		x.beginInline("a");
		x.attribute("href", s);
		x.open();
		x.content(s);
		x.close();
		x.newline();
	}

}
