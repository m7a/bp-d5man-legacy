package ma.d5man.lib.util;

import java.util.Iterator;

/** TODO potentially useful for tools lib? */
public class ConstantObjectIterator<T> implements Iterator<T> {

	private final Object[] values;
	private int idx;

	public ConstantObjectIterator(Object[] values) {
		super();
		this.values = values;
		idx = 0;
	}

	@Override
	public boolean hasNext() {
		return idx < values.length;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		return (T)values[idx++];
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
