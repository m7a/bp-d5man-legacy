package ma.d5man.lib.lls;

import java.io.IOException;

import ma.d5man.lib.util.MergedIterable;

public class MergedResourceSet implements MinimalResourceSet {

	protected final MinimalResourceSet[] subSets;

	public MergedResourceSet(MinimalResourceSet... subSets) {
		super();
		this.subSets = subSets;
	}

	@Override
	public D5Stream openResource(String name) throws IOException {
		D5Stream ret;
		for(MinimalResourceSet s: subSets)
			if((ret = access(s).openResource(name)) != null)
				return ret;
		return null;
	}

	/** May be overriden to hide resource on demand properties */
	protected MinimalResourceSet access(MinimalResourceSet s)
							throws IOException {
		return s;
	}

	// unfortunately necessary otherwise generic array creation
	@SuppressWarnings("unchecked")
	@Override
	public Iterable<String> getResourceNames() {
		Iterable<String>[] subIters = new Iterable[subSets.length];
		for(int i = 0; i < subSets.length; i++)
			subIters[i] = subSets[i].getResourceNames();
		return new MergedIterable<String>(subIters);
	}

}
