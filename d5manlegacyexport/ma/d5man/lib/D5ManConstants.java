package ma.d5man.lib;

public class D5ManConstants {

	/* TODO HOW SHALL WE HANDLE THIS */
	public static String[] REGISTERED_RESOURCE_ON_DEMAND_CLASSES = {
		"ma.d5man.lib.export.xhtml.lang.MathProcessor",
		"ma.d5man.lib.export.xhtml.lang.VIMProcessor"
	};

	public static final String XML_DTD_RES_PREFIX = "/ma/d5man/lib/dtd/";

	/** order matters: index 0 is <code>d5manxml.dtd</code> */
	public static final String[] XML_DTD_NAMES = { "d5manxml.dtd",
							"d5manqueryxml.dtd" };

	/* TODO USE OR ? */
	public static final String XHTML_TPL = "template.xml";

	// TODO MAKE THIS CONFIGURABLE VIA SETTINGS (CONFIGURATION AFFECTS ma.d5man.export.xhtml.ProcessingResult) / DSitemap
	public static final String BASE = "http://masysma.lima-city.de";

	public static final String HASH_ATTACHMENTS = "md5";
	public static final String HASH_PAGES       = "md5";

}
