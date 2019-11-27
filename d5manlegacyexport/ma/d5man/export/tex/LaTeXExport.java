package ma.d5man.export.tex;

import java.util.HashMap;
import java.nio.file.Path;

import ma.d5man.lib.export.proc.real.D5Man2XMLProcess;
import ma.d5man.lib.shr.AbstractD5ManPage;
import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.input.Conf;
import ma.d5man.lib.lls.DRS;
import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.lls.MinimalResourceSet;

import ma.d5man.export.XSLTExport;

public class LaTeXExport extends XSLTExport {

	private static final String STYLESHEET = "d5manxml2tex.xsl";

	private final MediaConverter conv;
	private final StaticResourceSet srs;

	public LaTeXExport(D5Man2XMLProcess d2x, D5ManDB db,
				Conf conf, String exportConf) throws Exception {
		super(d2x, ".tex", LaTeXExport.class.
					getResourceAsStream(STYLESHEET));
		HashMap<String,String> ec = extractExportConf(exportConf);
		conv = new MediaConverter(conf.getMediaConverter());
		srs = new StaticResourceSet(db, conf.getCommonRes(), ec);
	}

	private static HashMap<String,String> extractExportConf(String str) {
		HashMap<String,String> ret = new HashMap<String,String>();
		if(str.length() == 0)
			return ret;
		String[] parts = str.split(",");
		for(String s: parts) {
			int pos = s.indexOf("=");
			if(pos == -1)
				throw new RuntimeException(
					"Invalid configuration String: \"" +
					str + "\". Elements need to " +
					"be formatted this way: k=v,k=v,..."
				);
			ret.put(s.substring(0, pos), s.substring(pos + 1));
		}
		return ret;
	}

	@Override
	public DRS createConversionResultResourceSet(AbstractD5ManPage p)
							throws D5IOException {
		return new ConversionResultWrapper(super.
				createConversionResultResourceSet(p), conv);
	}

	@Override
	protected MinimalResourceSet createStaticResourceSet() {
		return srs;
	}

}
