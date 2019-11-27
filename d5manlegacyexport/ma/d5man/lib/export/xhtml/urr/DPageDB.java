package ma.d5man.lib.export.xhtml.urr;

import java.util.*;
import java.io.IOException;
import java.io.Writer;
import ma.tools2.util.NotImplementedException;
import ma.d5man.lib.shr.JSONExportable;
import ma.d5man.lib.db.PageName;
import ma.d5man.lib.db.D5ManDBPageMeta;
import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.lls.D5Stream;

/**
 * Provides a dynamically generated JavaScript file containing a large JSON
 * object which stores all information necessary to be able to search the
 * DB using JavaScript (i.e. client-side technology) only.
 *
 * Format
 * <pre>
	{
		main: { section: [ name, name, ... ], section: ... },
		tag: { tag: [ { section: sec, name: name }, ... ], tag: ... }
	}
   </pre>
 *
 * WARNING: This class' methods are not fully generic because that would not
 * compile as a result of nested collections. Thus, we have partially dropped
 * the safety of type-controlled generics in favour of less redundant code.
 *
 * @version 1.0.1.1
 */
class DPageDB extends RamPage<D5ManDB> {

	DPageDB(D5ManDB d) throws IOException {
		super(d);
	}

	@Override
	String getName() {
		return "pagedb.js";
	}

	@Override
	void writePageTo(Writer w) throws IOException {
		w.write("var d5man_page_database = {\n");
		w.write("\tmain: {\n");
		writeAssocList(remap(), w);
		w.write("\t},\n\ttag: {\n");
		writeByTag(w);
		w.write("\t}\n};\n\n");
		w.write("if(typeof msq === \"object\")\n");
		w.write("\tmsq.callbackPageDBLoaded(d5man_page_database);\n");
	}

	private Map<Integer, Iterable> remap() {
		return new AbstractMap<Integer, Iterable>() {
			@Override
			public Set<Map.Entry<Integer, Iterable>> entrySet() {
				return new AbstractSet<Map.Entry<Integer,
								Iterable>>() {
					@Override
					public int size() {
						return d.a().size();
					}
					@Override
					public Iterator<Map.Entry<Integer,
							Iterable>> iterator() {
						return createIterator();
					}
				};
			}
		};
	}

	private Iterator<Map.Entry<Integer, Iterable>> createIterator() {
		final Iterator<Map.Entry<Integer, TreeMap<String,
			D5ManDBPageMeta>>> ci = d.a().entrySet().iterator();
		return new Iterator<Map.Entry<Integer, Iterable>>() {
			@Override
			public boolean hasNext() {
				return ci.hasNext();
			}
			@Override
			public Map.Entry<Integer, Iterable> next() {
				return createEntry(ci.next());
			}
			@Override
			public void remove() {
				ci.remove();
			}
		};
	}

	private static Map.Entry<Integer, Iterable> createEntry(final Map.Entry<
							Integer, TreeMap<String,
							D5ManDBPageMeta>> cci) {
		return new Map.Entry<Integer, Iterable>() {
			@Override
			public Integer getKey() {
				return cci.getKey();
			}
			@Override
			public Iterable getValue() {
				// TODO z hacky
				ArrayList<String> ret = new ArrayList<String>();
				for(D5ManDBPageMeta m: cci.getValue().values())
					ret.add(m.getName());
				return ret;
			}
			@Override
			public Iterable setValue(Iterable c) {
				throw new NotImplementedException();
			}
		};
	}

	private void writeAssocList(Map<?, Iterable> sec2n, Writer w)
							throws IOException {
		Iterator e = sec2n.entrySet().iterator();
		while(e.hasNext()) {
			Map.Entry ce = (Map.Entry)e.next();
			w.write("\t\t\"" + ce.getKey() + "\": [\n");
			writeListValues((Iterable)ce.getValue(), w);
			w.write("\t\t]");
			if(e.hasNext())
				w.write(",");
			w.write("\n");
		}
	}

	private void writeListValues(Iterable<?> names, Writer w)
							throws IOException {
		int count = 0;
		Iterator<?> n = names.iterator();
		if(n.hasNext()) {
			w.write("\t\t\t");
			while(n.hasNext()) {
				Object co = n.next();

				// TODO z very bad code
				if(co instanceof JSONExportable)
					w.write(((JSONExportable)co).
								exportJSON());
				else if(co instanceof String)
					w.write("\"" + co + "\"");
				else
					w.write(co.toString());

				if(n.hasNext()) {
					w.write(", ");
					if(++count == 3) {
						w.write("\n\t\t\t");
						count = 0;
					}
				}
			}
			w.write("\n");
		}
	}

	private void writeByTag(Writer w) throws IOException {
		Map<String, Iterable> tagAssoc = new HashMap<String,
								Iterable>();
		for(D5ManDBPageMeta m: d)
			for(String s: m.getTags())
				procMeta(tagAssoc, s, m);

		writeAssocList(tagAssoc, w);
	}

	// TODO z very bad code
	@SuppressWarnings("unchecked")
	private void procMeta(Map<String, Iterable> tagAssoc,
						String tag, PageName m) {
		HashSet pageList = (HashSet)tagAssoc.get(tag);
		if(pageList == null)
			tagAssoc.put(tag, pageList = new HashSet());
		pageList.add(m);
	}

}
