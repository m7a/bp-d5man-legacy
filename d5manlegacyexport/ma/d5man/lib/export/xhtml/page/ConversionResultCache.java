package ma.d5man.lib.export.xhtml.page;

import java.util.Map;
import java.util.LinkedHashMap;

import ma.d5man.lib.db.StaticPageName;
import ma.d5man.lib.lls.DRS;

class ConversionResultCache extends LinkedHashMap<StaticPageName,DRS> {

	private static final int MAX_CACHED_ENTRIES = 1024;

	ConversionResultCache() {
		super();
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<StaticPageName,DRS> e) {
		return size() > MAX_CACHED_ENTRIES;
	}

	@Override
	public DRS put(StaticPageName k, DRS v) {
		synchronized(this) {
			return super.put(k, v);
		}
	}

	@Override
	public DRS get(Object k) {
		synchronized(this) {
			// put: to reorder correctly (thus the synchronization)
			if(containsKey(k)) {
				DRS d = remove(k);
				put((StaticPageName)k, d);
				return d;
			} else {
				return null;
			}
		}
	}

}
