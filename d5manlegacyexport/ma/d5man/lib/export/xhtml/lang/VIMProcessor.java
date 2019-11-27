package ma.d5man.lib.export.xhtml.lang;

import java.io.IOException;
import java.nio.file.Paths;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.lls.DRS;
import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.input.Conf;
import ma.d5man.lib.export.ExternalLanguageProcessor;
import ma.d5man.lib.export.proc.real.VIMProcess;

public class VIMProcessor extends ExternalLanguageProcessor {

	private VIMProcess vim;

	public VIMProcessor() {
		super();
		vim = null;
	}

	@Override
	public void configure(Conf c) {
		vim = new VIMProcess(c.getVIM());
	}

	@Override
	public void open() throws IOException {
		if(vim == null)
			throw new IOException(
				"VIMProcessor needs to be configured with " +
				"the path to a VIM executable first."
			);

		if(!vim.isOpen())
			vim.open();

		super.open();
	}

	@Override
	public DRS convert(D5Stream in, String language) throws D5IOException {
		try {
			return vim.highlight(language, in);
		} catch(D5IOException ex) {
			throw ex;
		} catch(IOException ex) {
			throw new D5IOException(ex);
		}
	}

	@Override
	public void close() throws IOException {
		vim.close();
	}

}
