package ma.d5man.lib.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ma.d5man.lib.shr.AbstractD5ManPage;
import ma.d5man.lib.lls.*;
import ma.d5man.lib.j8.Function;

/**
 * This is a file export which converts the main resource (conv) of an
 * AbstractD5ManPage's DRS and makes D5Man-Paths for the main resource name
 * out of a simple resource name (therefore: structural).
 */
public abstract class StructuralConvExport implements FileExport {

	/**
	 * @param conv Conversion function to convert primary resource stream.
	 */
	public StructuralConvExport() {
		super();
	}

	@Override
	public MinimalResourceSet open(AbstractD5ManPage p)
							throws D5IOException {
		return p == null? createStaticResourceSet():
					createConversionResultResourceSet(p);
	}

	protected DRS createConversionResultResourceSet(final
							AbstractD5ManPage p)
							throws D5IOException {
		return new DRSWrapper(p.access()) {
			@Override
			public D5Stream openFirst() throws IOException {
				return apply(super.openFirst());
			}
			@Override
			public String getFirstResourceName() {
				return p.createFSPath(File.separator,
								getExtension());
			}
		};
	}

	/**
	 * Perform conversion. To support inheritance, work on the result of
	 * super.apply(d5s) instead of d5s whenever you are not extending
	 * StructuralConvExport directly.
	 */
	protected abstract D5Stream apply(D5Stream d5s);

	/**
	 * Do not compute inside this function, because it is invoked often.
	 *
	 * @return .xml for XML files or null for D5Man files and such.
	 */
	protected abstract String getExtension();

	/**
	 * @return
	 * 	potentially empty resource set to be opened if open(null)
	 * 	is called.
	 */
	protected abstract MinimalResourceSet createStaticResourceSet()
							throws D5IOException;

}
