package ma.d5man.lib.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** TODO potentially useful for tools lib? */
public class ConstantObjectIterable<T> implements Iterable<T> {

	private final Object[] values;

	@SafeVarargs
	public ConstantObjectIterable(Object... values) {
		super();
		this.values = values;
	}

	@Override
	public Iterator<T> iterator() {
		return new ConstantObjectIterator<T>(values);
	}

}
