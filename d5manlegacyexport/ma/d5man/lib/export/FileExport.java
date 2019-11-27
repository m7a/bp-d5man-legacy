package ma.d5man.lib.export;

import ma.d5man.lib.shr.AbstractD5ManPage;
import ma.d5man.lib.lls.MinimalResourceSet;
import ma.d5man.lib.lls.D5IOException;

/**
 * Represents a FileExport which exports <code>D5ManPage</code>s to any number
 * of result files including generated resources, attachments etc.
 *
 * @author Linux-Fan, Ma_Sys.ma &lt;<code>Ma_Sys.ma@web.de</code>&gt;
 * @version 1.1.0.1
 */
public interface FileExport {

	/**
	 * @param page
	 * 	Page to export or <code>null</code> to return all the additional
	 * 	and static resources (w/o main resource)
	 */
	public MinimalResourceSet open(AbstractD5ManPage page)
							throws D5IOException;

}
