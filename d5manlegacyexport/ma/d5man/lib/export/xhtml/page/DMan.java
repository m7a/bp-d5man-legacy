package ma.d5man.lib.export.xhtml.page;

import java.util.Iterator;
import java.io.IOException;

import ma.d5man.lib.lls.*;
import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.db.D5ManDBPageMeta;
import ma.d5man.lib.db.StaticPageName;
import ma.d5man.lib.export.xhtml.XHTMLExport;
import ma.d5man.lib.export.proc.D5ManIOResolver;
import ma.d5man.lib.util.MergedIterable;

public class DMan implements MinimalResourceSet, AutoCloseable {

	private final D5ManDB db;
	private final D5ManIOResolver ioResolver;
	private final ZIPCache z;
	private final ConversionResultCache cache;

	private XHTMLExport x;

	public DMan(D5ManDB db, D5ManIOResolver ioResolver) {
		super();
		this.db         = db;
		this.ioResolver = ioResolver;
		x               = null;
		z               = new ZIPCache();
		cache           = new ConversionResultCache();
	}

	public void setXHTMLExport(XHTMLExport x) {
		this.x = x;
	}

	public boolean hasXHTMLExport() {
		return x != null;
	}

	@Override
	public D5Stream openResource(String name) throws IOException {
		DRS d = getDRS(name);
		return d == null? null: d.openResource(name);
	}

	private DRS getDRS(String name) throws IOException {
		String[] parts = name.split("/");
		return parts.length > 3 || parts.length < 2? null:
								getDRS(parts);
	}

	private DRS getDRS(String[] parts) throws IOException {
		int sec;
		try {
			sec = Integer.parseInt(parts[0]);
		} catch(NumberFormatException ex) {
			return null;
		}
		if(parts[1].length() < 5 || sec <= 0 || sec >= 100)
			return null;
		StaticPageName pn = new StaticPageName(sec,
						extractPageName(parts[1]));
		return cache.containsKey(pn)? cache.get(pn): acquireDRS(pn);
	}

	private static String extractPageName(String fnodn)
							throws D5IOException {
		if(fnodn.endsWith("_att")) {
			fnodn = fnodn.substring(0, fnodn.length() - 4);
		} else if(fnodn.endsWith(".xhtml")) {
			fnodn = fnodn.substring(0, fnodn.length() - 6);
			if(fnodn.endsWith("@src"))
				fnodn = fnodn.substring(0, fnodn.length() - 4);
		} else {
			throw new D5IOException("Misformatted page name: " +
									fnodn);
		}
		return fnodn;
	}

	private DRS acquireDRS(StaticPageName pn) throws IOException {
		if(!db.a().containsKey(pn.getSection()))
			return null;
		D5ManDBPageMeta m = db.a().get(pn.getSection()).get(
								pn.getName());
		if(m == null)
			return null;
		DRS ret = x.createConversionResultResourceSet(new D5ManPage(m,
								ioResolver, z));
		cache.put(pn, ret);
		return ret;
	}

	/**
	 * Warning: This implementation generally does not return all
	 * resource names but only those which can be known without
	 * processing the respecitive pages.
	 */
	@Override
	public Iterable<String> getResourceNames() {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new ResourceNameIterator(db);
			}
		};
	}

	@Override
	public void close() throws IOException {
		z.close();
	}

}
