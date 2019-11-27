package ma.d5man.lib.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** TODO potentially useful for tools lib */
public class MergedIterable<T> implements Iterable<T> {

	private final Iterable<T>[] iters;

	// -> http://stackoverflow.com/questions/21132692/java-unchecked-
	// 		unchecked-generic-array-creation-for-varargs-parameter
	@SafeVarargs
	public MergedIterable(Iterable<T>... iters) {
		super();
		this.iters = iters;
	}

	@Override
	public Iterator<T> iterator() {
		ArrayList<Iterable<T>> siters =
				new ArrayList<Iterable<T>>(iters.length);
		for(Iterable<T> iter: iters)
			siters.add(iter);

		return merge(siters);
	}

	private static <T> Iterator<T> merge(final Iterable<Iterable<T>>
								siters) {
		return new Iterator<T>() {

			private Iterator<Iterable<T>> sub = siters.iterator();
			private Iterator<T> cur = null;

			@Override
			public boolean hasNext() {
				if(cur == null) {
					if(sub.hasNext())
						cur = sub.next().iterator();
					else
						return false;
				}
					
				while(!cur.hasNext() && sub.hasNext())
					cur = sub.next().iterator();

				return cur.hasNext();
			}
			@Override
			public T next() {
				if(hasNext())
					return cur.next();
				else
					throw new NoSuchElementException();
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

	}

}
