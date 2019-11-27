package ma.d5man.lib.procxml;

import ma.d5man.lib.lls.D5IOException;

/**
 * Unfortunately, SAX does not offer any means of regularily terminating the
 * parsing process. See the following two URLs:
 *
 * http://stackoverflow.com/questions/2964315/java-sax-parser-how-to-manually-
 * 								break-parsing
 * http://stackoverflow.com/questions/1345293/how-to-stop-parsing-xml-document-
 * 							with-sax-at-any-time
 *
 * Therefore, we throw an exception to mark "`no need to continue parsing"' a
 * regular means of termination for example if we only extract metadata from
 * an XML input. Ideally, this also breaks reading from the input stream and
 * therefore terminates <code>d5man2xml</code> early.
 */
public class RegularTerminationException extends D5IOException {

	public RegularTerminationException() {
		super();
	}

	public RegularTerminationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public RegularTerminationException(Throwable cause) {
		super(cause);
	}

}
