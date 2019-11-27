package ma.d5man.lib.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** TODO potentially useful for tools lib? */
public class ConstantIterable<T> implements Iterable<T> {

	private final T[] values;

	@SafeVarargs
	public ConstantIterable(T... values) {
		super();
		this.values = values;
	}

	@Override
	public Iterator<T> iterator() {
		return new ConstantIterator<T>(values);
	}

}
