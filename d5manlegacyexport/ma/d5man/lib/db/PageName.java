package ma.d5man.lib.db;

import java.util.Iterator;
import java.util.NoSuchElementException;
import ma.d5man.lib.util.ConstantIterable;
import ma.d5man.lib.shr.JSONExportable;
import ma.tools2.util.NotImplementedException;

public abstract class PageName implements Iterable<String>, JSONExportable {

	protected PageName() {
		super();
	}

	public abstract int getSection();
	public abstract String getName();

	public String getFSName() {
		return getFSName(getName());
	}

	private static String getFSName(String name) {
		return name.replace('/', '_');
	}

	public String createFSPath(String sep) {
		return createFSPath(sep, null, null);
	}

	/** @return D5I-Path for ext = null */
	public String createFSPath(String sep, String ext) {
		return createFSPath(sep, ext == null? ".d5i": ext, null);
	}

	public String createFSPath(String sep, String ext, String res) {
		return createFSPath(getSection(), getName(), sep, ext, res);
	}

	public static String createFSPath(int section, String name, String sep,
								String ext) {
		return createFSPath(section, name, sep, ext, null);
	}

	public static String createFSPath(int section, String name, String sep,
						String ext, String res) {
		return section + sep + getFSName(name) +
					(res == null? ext: "_att" + sep + res);
	}

	public String validate() {
		String msg =
			(((getSection() == -1)?
				"Section -1 is invalid.":
				"") +
			" " +
			((getName() == null)?
				"Name `null` is invalid.":
				"")).trim();
		return msg.length() == 0? null: msg;
	}

	@Override
	public String toString() {
		return getName() + "(" + getSection() + ")";
	}

	@Override
	public String exportJSON() {
		return "{ section: " + getSection() +
					", name: \"" + getName() + "\" }";
	}

	/**
	 * Intended to be overriden by subclasses to enable iteration over
	 * attachments
	 */
	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return false;
			}
			@Override
			public String next() {
				throw new NoSuchElementException();
			}
			@Override
			public void remove() {
				throw new NotImplementedException();
			}
		};
	}

}
