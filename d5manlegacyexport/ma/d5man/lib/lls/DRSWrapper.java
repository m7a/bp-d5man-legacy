package ma.d5man.lib.lls;

import java.io.IOException;

import ma.d5man.lib.util.MergedIterable;

/**
 * This class is intended to be able to override some methods of an existing
 * result object. This view, however, must be used with great care: In realty,
 * this class only delegates most method calls to the original object and
 * therefore ``intercepts'' communication w/ the original object. This means
 * that a method you override here, is not called indirectly from the object
 * you gave as parameter but only accessible form this very class.
 */
public abstract class DRSWrapper extends DRS {

	protected final DRS sub;

	protected DRSWrapper(DRS sub) {
		super(sub.getAssocPage());
		this.sub = sub;
	}

	/**
	 * If you need to override this, do it in this one and only correct
	 * way.
	 *
	 * <pre>
	 *	D5Stream s = super.openResource(name);
	 * 	if(s == null)
	 *		return ...
	 * 	else
	 *		return s;
	 * </pre>
	 *
	 * Although it might look equivalent, do not change the order of these
	 * invocations because `super.openResource(name)` does nothing else than
	 * ask the original `sub` parameter object to open this resource. Your
	 * overriden methods from this class are _not_ used in that case.
	 */
	@Override
	public D5Stream openResource(String name) throws IOException {
		D5Stream s = super.openResource(name);
		return s == null? sub.openResource(name): s;
	}

	@Override
	public D5Stream openFirst() throws IOException {
		return sub.openFirst();
	}

	@Override
	public String getFirstResourceName() {
		return sub.getFirstResourceName();
	}

	@Override
	public Iterable<String> getOtherResourceNames() {
		return sub.getOtherResourceNames();
	}

}
