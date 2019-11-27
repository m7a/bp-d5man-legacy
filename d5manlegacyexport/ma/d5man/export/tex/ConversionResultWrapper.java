package ma.d5man.export.tex;

import java.io.IOException;

import java.util.*;
import ma.d5man.lib.lls.*;

class ConversionResultWrapper extends DRSWrapper {

	private final DRS                 normal;
	private final MediaConverter      conv;
	private final List<String>        newResourceSet;
	private final Map<String, String> svg2pdf;

	ConversionResultWrapper(DRS normal, MediaConverter conv) {
		super(normal);
		this.normal    = normal;
		this.conv      = conv;
		newResourceSet = new ArrayList<String>();
		svg2pdf        = new HashMap<String, String>();
		transformResourceSetNames(normal.getOtherResourceNames(),
						newResourceSet, svg2pdf);
	}

	private static void transformResourceSetNames(Iterable<String> names,
						List<String> newRes,
						Map<String, String> svg2pdf) {
		Set<String> currentNames = new HashSet<String>();
		for(String s: names)
			currentNames.add(s);
		for(String s: currentNames) {
			if(s.endsWith(".svg")) {
				String pdfName = s.substring(0, s.length() - 4)
								+ ".pdf";
				if(!currentNames.contains(pdfName))
					svg2pdf.put(pdfName, s);
				newRes.add(pdfName);
			} else {
				newRes.add(s);
			}
		}
	}

	@Override
	public D5Stream openResource(String name) throws IOException {
		try {
			return svg2pdf.containsKey(name)?
					conv.convertSVG2PDF(super.openResource(
							svg2pdf.get(name))):
					super.openResource(name);
		} catch(InterruptedException ex) {
			throw new D5IOException(ex);
		}
	}

	@Override
	public Iterable<String> getOtherResourceNames() {
		return newResourceSet;
	}

}
