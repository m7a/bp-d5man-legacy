package ma.d5man.export.tex;

import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.db.D5ManDBPageMeta;
import ma.d5man.lib.lls.StringD5Stream;

class TemplateIncludingMainFile extends StringD5Stream {

	TemplateIncludingMainFile(D5ManDB db, boolean requireToc) {
		super(generateTemplateIncludingMainFile(db, requireToc));
	}

	private static String generateTemplateIncludingMainFile(D5ManDB db,
							boolean requireToc) {
		StringBuilder ret = new StringBuilder();
		if(db.getSize() == 1 && !requireToc)
			ret.append("\\def\\notoc{1}\n"); // single page: no ToC
		ret.append("\\input{template.tex}\n");
		int prevsec = db.a().size() == 1?
					db.a().keySet().iterator().next(): -1;
		for(D5ManDBPageMeta m: db) {
			if(prevsec != m.getSection()) {
				prevsec = m.getSection();
				ret.append("\\dsectionseparator{");
				ret.append(prevsec);
				ret.append("}\n");
			}
			ret.append("\\input{");
			ret.append(m.createFSPath("/", ".tex"));
			ret.append("}\n");
		}
		ret.append("\\end{document}\n");
		return ret.toString();
	}

}
