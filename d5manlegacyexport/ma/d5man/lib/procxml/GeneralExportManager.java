package ma.d5man.lib.procxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.xml.parsers.SAXParser;
import org.xml.sax.InputSource;

import static java.nio.charset.StandardCharsets.UTF_8;

import ma.d5man.lib.lls.DRS;
import ma.d5man.lib.lls.D5IOException;

import ma.tools2.util.BufferUtils;
import ma.tools2.xml.ErrorAwareXMLParser;

public class GeneralExportManager<T extends ResultlessXMLHandler> {

	private final SAXParser parser;

	protected final T handler;

	public GeneralExportManager(T handler) throws D5IOException {
		super();
		this.handler = handler;
		parser = createParser();
	}

	private static SAXParser createParser() throws D5IOException {
		try {
			return ErrorAwareXMLParser.createParser();
		} catch(Exception ex) {
			throw new D5IOException(ex);
		}
	}

	/** Get result via <code>handler.getCurrentProcessingResult()</code> */
	public void process(DRS xml) throws D5IOException {
		try {
			createProcResult(xml.openFirst().
					openInputStreamRepresentation());
		} catch(IOException ex) {
			throw D5IOException.wrapIn(ex);
		}
	}

	/**
	 * This method is synchronized in order not to use <code>parser</code>
	 * multiple times simultaneously which also enables us to share
	 * <code>handler</code>.
	 */
	public synchronized void createProcResult(InputStream raw)
							throws D5IOException {
		InputStreamReader r = new InputStreamReader(raw, UTF_8);
		InputSource s = new InputSource(r);
		try {
			parser.parse(s, handler);
		} catch(RegularTerminationException ex) {
			regularTermination(r);
		} catch(D5IOException ex) {
			throw ex;
		} catch(Exception ex) {
			handleGenericException(ex, r);
		} finally {
			try {
				r.close();
			} catch(IOException ex) {
				throw D5IOException.wrapIn(ex);
			}
		}
	}

	private static void regularTermination(InputStreamReader r)
							throws D5IOException {
		try {
			// TODO z a bit idiotic... but works
			char[] tmpbuf = new char[BufferUtils.DEFAULT_BUFFER];
			while(r.read(tmpbuf, 0, tmpbuf.length) == tmpbuf.length)
				;
			tmpbuf = null; // explicitely delete buffer
		} catch(IOException ex) {
			// This could indicate a syntax error. Thus we
			// return it! For other IOExceptions the argumentation
			// is simply that we want to be warned about corrupted
			// (unreadable) pages as early as possible
			throw D5IOException.wrapIn(ex);
		}
	}

	private static void handleGenericException(Exception ex,
				InputStreamReader r) throws D5IOException {
		if(ex.getCause() != null) {
			if(ex.getCause() instanceof RegularTerminationException)
				regularTermination(r);
			else if(ex.getCause() instanceof D5IOException)
				throw (D5IOException)ex.getCause();
			else
				throw new D5IOException(ex);
		} else {
			throw new D5IOException(ex);
		}
	}

}
