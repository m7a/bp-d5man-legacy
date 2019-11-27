package ma.d5man.lib.export.xhtml.lang;

import java.io.IOException;
import java.io.InputStream;

import ma.tools2.util.BufferUtils;
import ma.tools2.util.NotImplementedException;

import ma.d5man.lib.input.Conf;
import ma.d5man.lib.export.ExternalLanguageProcessor;
import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.lls.DRS;

/**
 * Processes languages <code>MATH</code> and <code>MATH_BLOCK</code> which
 * represent LaTeX-style math expressions.
 *
 * This class has been stripped of its original functionality as to allow
 * compilation w/o external libraries.
 */
public class MathProcessor extends ExternalLanguageProcessor {

	/** <code>date +%s</code> * 1000 */
	private static final long CREATION = 1420298945000l;

	public MathProcessor() {
		super();
	}

	public void open() throws IOException {
		super.open();
	}

	public DRS convert(D5Stream in, String language) throws D5IOException {
		boolean isblk = language.equals("MATH_BLOCK");
		if(isblk || language.equals("MATH"))
			return convert(in, isblk);
		else
			return null;
	}

	private DRS convert(final D5Stream in, final boolean isblk) {
		return new DRS() {
			@Override
			public String getFirstResourceName() {
				return "DEFAULT";
			}
			@Override
			public D5Stream openFirst() throws IOException {
				return convert2(in, isblk);
			}
		};
	}

	private D5Stream convert2(final D5Stream in, final boolean isblk) {
		return new D5Stream() {
			@Override
			public InputStream openInputStreamRepresentation()
							throws D5IOException {
				return openInputStream(in, isblk);
			}
			@Override
			public long getTimestamp() throws D5IOException {
				return Math.max(CREATION, in.getTimestamp());
			}
			@Override
			public String getEtag() throws D5IOException {
				return in.getEtag() + "-MATH-" + CREATION;
			}
		};
	}

	private InputStream openInputStream(D5Stream in, boolean isblk)
							throws D5IOException {
		try {
			return BufferUtils.writefile(convert(BufferUtils.
				readfile(in.openInputStreamRepresentation()),
				isblk));
		} catch(D5IOException ex) {
			throw ex;
		} catch(IOException ex) {
			throw new D5IOException(ex);
		}
	}

	private String convert(String data, boolean isblk) throws IOException {
		throw new RuntimeException("Implementation removed from legacy distribution. Use old mdvl-d5man package for a version including this functionality.");
	}

	@Override
	public void close() throws IOException {
		// N_REQ IIUC
	}

	@Override
	public void configure(Conf c) {
		// N_REQ
	}

}
