package ma.d5man.lib.export.xhtml.urr;

import java.util.TreeSet;
import java.io.File;
import java.io.IOException;

import ma.tools2.util.BinaryOperations;
import ma.d5man.lib.lls.MinimalResourceSet;
import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.lls.StringD5Stream;
import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.db.D5ManDBPageMeta;

class DHtaccess implements MinimalResourceSet {

	/** <code>echo $(($(date "+%s") * 1000))L</code> */
	private static final long HTACCESS_TIMESTAMP = 1410209729000L;

	private final TreeSet<String> paths;

	DHtaccess(D5ManDB db) {
		super();
		paths = new TreeSet<String>();
		for(Integer i: db.a().keySet()) {
			paths.add(i + "/.htaccess");
			// Newly all pages have attachments (d5man syntax
			// highlighting css file) so this is no longer correct:
			//	if(m.iterator().hasNext()) // has attachment(s)
			for(D5ManDBPageMeta m: db.a().get(i).values())
				paths.add(m.createFSPath(File.separator,
							null, ".htaccess"));
		}
	}

	@Override
	public D5Stream openResource(String name) throws IOException {
		return paths.contains(name)? createAt(name): null;
	}

	// TODO z RE-CREATED FOR EVERY QUERY => A BIT HEAVY ON RESOURCES
	private static D5Stream createAt(String path) {
		final StringBuilder o = new StringBuilder(
			"Deny from all\n" +
			"ErrorDocument 404 /31/web_404.xhtml\n\n" +
			"php_flag short_open_tag off\n" +
			"php_value default_charset UTF-8\n" +
			"php_flag display_startup_errors off\n" +
			"php_flag display_errors off\n" +
			"php_flag html_errors off\n" +
			"php_flag log_errors on\n" +
			"php_value output_buffering 4096\n" +
			"php_value error_log /home/webpages/lima-city" +
				"/masysma/html/internal/data/php.txt\n\n"
		);
		
		if(path.endsWith("_att/.htaccess"))
			o.append(
				"<Files *>\n" +
				"\tOrder deny,allow\n" +
				"\tAllow from all\n" +
				"</Files>\n"
			);
		else
			o.append(
				"AddType application/x-httpd-php .xhtml\n" +
				"AddCharset UTF-8 .xhtml\n" +
				"php_value default_mimetype " +
						"\"application/xhtml+xml\"\n" +
				"\n\n" +
				"<Files *.xhtml>\n" +
				"\tOrder deny,allow\n" +
				"\tAllow from all\n" +
				"</Files>\n"
			);

		return new StringD5Stream(o.toString(), HTACCESS_TIMESTAMP); 
	}

	@Override
	public Iterable<String> getResourceNames() {
		return paths;
	}

}
