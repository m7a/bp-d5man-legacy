package ma.d5man.lib.util;

import java.util.Iterator;
import ma.tools2.util.NotImplementedException;

public abstract class NestedIterator<T, E> implements Iterator<T> {

	private final Iterator<E> oiter;

	private Iterator<T> iiter;

	protected NestedIterator(Iterator<E> oiter) {
		super();
		this.oiter = oiter;
		iiter = null;
	}

	@Override
	public boolean hasNext() {
		return (iiter != null && iiter.hasNext()) || oiter.hasNext();
	}

	@Override
	public T next() {
		if(iiter == null || !iiter.hasNext())
			iiter = convert(oiter.next());

		return iiter.next();
	}

	protected abstract Iterator<T> convert(E e);

	@Override
	public void remove() {
		throw new NotImplementedException();
	}
	
}
