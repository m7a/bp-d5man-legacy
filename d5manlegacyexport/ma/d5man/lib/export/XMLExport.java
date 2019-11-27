package ma.d5man.lib.export;

import java.io.IOException;

import ma.d5man.lib.D5ManConstants;
import ma.d5man.lib.lls.*;
import ma.d5man.lib.export.proc.real.D5Man2XMLProcess;

public class XMLExport extends StructuralConvExport {

	protected D5Man2XMLProcess conv;

	/** @param conv Converter Process which is never closed here. */
	public XMLExport(D5Man2XMLProcess conv) {
		super();
		this.conv = conv;
	}

	@Override
	protected String getExtension() {
		return ".xml";
	}

	@Override
	protected D5Stream apply(D5Stream d5s) {
		return conv.apply(d5s);
	}

	@Override
	protected MinimalResourceSet createStaticResourceSet() {
		return new DRS() {
			@Override
			public D5Stream openFirst() throws IOException {
				return new ResourceD5Stream(
					D5ManConstants.XML_DTD_RES_PREFIX +
					D5ManConstants.XML_DTD_NAMES[0]);
			}
			@Override
			public String getFirstResourceName() {
				return D5ManConstants.XML_DTD_NAMES[0];
			}
		};
	}

}
