package ma.d5man.export.tex;

import java.util.Map;
import java.io.IOException;
import java.nio.file.Path;

import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.util.ConstantIterable;
import ma.d5man.lib.lls.*;

class StaticResourceSet implements MinimalResourceSet {

	private final D5ManDB db;
	private final Path common;
	private final Map<String,String> econf;

	private D5Stream main;

	StaticResourceSet(D5ManDB db, Path common, Map<String,String> econf) {
		super();
		this.db     = db;
		this.common = common;
		this.econf  = econf;
		main        = null;
	}

	@Override
	public D5Stream openResource(String name) throws IOException {
		switch(name) {
		case "main.tex":        return accessMain();
		case "template.tex":    return af("template.tex");
		case "logo_v2.pdf":     return af("logo_v2.pdf");
		case "masysmaicon.pdf": return af("masysmaicon.pdf");
		case "Makefile":        return af("makefile_tex.mk");
		default:                return null;
		}
	}

	private D5Stream af(String fn) {
		return new PathD5Stream(common.resolve(fn));
	}

	@Override
	public Iterable<String> getResourceNames() {
		return new ConstantIterable<String>(
			"main.tex", "template.tex", "Makefile",
			"masysmaicon.pdf", "logo_v2.pdf"
		);
	}

	private D5Stream accessMain() {
		if(main == null)
			main = new TemplateIncludingMainFile(db,
					econf.containsKey("require_toc") &&
					econf.get("require_toc").equals("1"));
		return main;
	}

}
