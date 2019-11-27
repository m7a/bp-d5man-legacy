package ma.d5man.lib.export.xhtml.page;

import java.util.Iterator;
import ma.tools2.util.NotImplementedException;
import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.db.D5ManDBPageMeta;
import ma.d5man.lib.util.NestedIterator;

class ResourceNameIterator extends NestedIterator<String, D5ManDBPageMeta> {

	private static enum Mode { NAME, SRC, ATTACHMENT }

	ResourceNameIterator(D5ManDB db) {
		super(db.iterator());
	}

	@Override
	protected Iterator<String> convert(final D5ManDBPageMeta m) {
		final Iterator<String> aiter = m.iterator();
		return new Iterator<String>() {
			private Mode mode = Mode.NAME;
			@Override
			public boolean hasNext() {
				return mode != Mode.ATTACHMENT ||
								aiter.hasNext();
			}
			@Override
			public String next() {
				switch(mode) {
				case NAME:
					mode = Mode.SRC;
					return m.createFSPath("/", ".xhtml");
				case SRC:
					mode = Mode.ATTACHMENT;
					return m.createFSPath("/",
								"@src.xhtml");
				case ATTACHMENT:
					return m.createFSPath("/", "_att",
								aiter.next());
				default:
					throw new NotImplementedException(
						"Unknown mode: " + mode +
						" (likely program bug)"
					);
				}
			}
			@Override
			public void remove() {
				throw new NotImplementedException();
			}
		};
	}

}
