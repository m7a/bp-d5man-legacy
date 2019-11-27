package ma.d5man.lib.input;

import java.io.InputStream;

import ma.d5man.lib.procxml.ResultlessSubHandler;
import ma.d5man.lib.procxml.GeneralExportManager;
import ma.d5man.lib.lls.D5IOException;

public class XMLFromStdin {

	public static void proc(ResultlessSubHandler... h)
							throws D5IOException {
		proc(System.in, h);
	}

	private static void proc(InputStream in, ResultlessSubHandler... h)
							throws D5IOException {
		GivenResultlessXMLHandler hn = new GivenResultlessXMLHandler(h);
		GeneralExportManager<GivenResultlessXMLHandler> x =
			new GeneralExportManager<GivenResultlessXMLHandler>(hn);
		x.createProcResult(in);
	}

}
