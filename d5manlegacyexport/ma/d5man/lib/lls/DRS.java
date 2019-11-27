package ma.d5man.lib.lls; // Low level storage

import java.io.IOException;

import ma.tools2.util.NotImplementedException;

import ma.d5man.lib.shr.AbstractD5ManPage;
import ma.d5man.lib.util.ConstantIterable;
import ma.d5man.lib.util.MergedIterable;

/**
 * Represents a Distingiusihed Resource Set, a resource set which specifies the
 * first element in a specific manner. This is transparent to the user of such
 * objects and only intereseting for the programmer of a DRS.
 */
public abstract class DRS implements MinimalResourceSet {

	protected final AbstractD5ManPage assocPage;

	protected DRS() {
		super();
		assocPage = null;
	}

	protected DRS(AbstractD5ManPage assocPage) {
		super();
		this.assocPage = assocPage;
	}

	@Override
	public D5Stream openResource(String name) throws IOException {
		return name.equals(getFirstResourceName())? openFirst(): null;
	}

	/**
	 * Do not ever override this because this does not play well w/
	 * <code>DRSWrapper</code> which is commonly used within this project.
	 */
	@Override
	public final Iterable<String> getResourceNames() {
		return new MergedIterable<String>(
			new ConstantIterable<String>(getFirstResourceName()),
			getOtherResourceNames()
		);
	}

	public abstract D5Stream openFirst() throws IOException;

	public abstract String getFirstResourceName();

	public AbstractD5ManPage getAssocPage() {
		return assocPage;
	}

	/** Generally not really public but needed by some implementation */
	public Iterable<String> getOtherResourceNames() {
		return new ConstantIterable<String>();
	}

}
