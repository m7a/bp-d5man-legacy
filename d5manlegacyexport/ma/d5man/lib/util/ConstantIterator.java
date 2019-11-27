package ma.d5man.lib.util;

import java.util.Iterator;

/** TODO potentially useful for tools lib? */
public class ConstantIterator<T> implements Iterator<T> {

	private final T[] values;
	private int idx;

	public ConstantIterator(T[] values) {
		super();
		this.values = values;
		idx = 0;
	}

	@Override
	public boolean hasNext() {
		return idx < values.length;
	}

	@Override
	public T next() {
		return values[idx++];
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
