package ma.d5man.lib.lls;

import java.io.IOException;

public interface MinimalResourceSet {

	/** @return <code>null</code> for not responsible/not found */
	public D5Stream openResource(String name) throws IOException;

	public Iterable<String> getResourceNames();

}
