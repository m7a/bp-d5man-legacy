package ma.d5man.export;

import java.nio.file.Path;
import java.nio.file.Paths;

import ma.tools2.util.ErrorInfo;

import ma.d5man.lib.lls.D5IOException;

public class Main {

	private final String type;
	private final Path dest;
	private final String conf;
	private final boolean useTable;

	private boolean validArgs;

	private Main(String[] args) {
		super();

		String type      = null;
		String dest      = ".";
		String conf      = "";
		boolean useTable = false;

		validArgs = false;

		for(int i = 0; i < args.length; i += 2) {
			if(args[i].length() < 2) {
				System.err.println("Argument too short: " +
								args[i]);
				continue;
			}
			char copt = args[i].charAt(1);
			switch(copt) {
			case 'm': type     = args[i + 1]; break;
			case 'o': dest     = args[i + 1]; break;
			case 'c': conf     = args[i + 1]; break;
			case 't': useTable = true; i--;   break;
			default:  System.err.println("Unknown option: " + copt);
			}
		}

		if(type == null)
			System.err.println("Mode argument -m mandatory.");
		else
			validArgs = true;

		this.type     = type;
		this.dest     = Paths.get(dest).toAbsolutePath();
		this.conf     = conf;
		this.useTable = useTable;
	}

	private void run() throws Exception {
		if(!validArgs) {
			System.err.println();
			help();
			System.exit(1);
			return;
		}

		D5ManExport export = D5ManExport.createFromStdin();
		if(useTable)
			export.initTable(dest);

		try {
			switch(type) {
			case "xml":   export.exportXML(dest);             break;
			case "xhtml": export.exportXHTML(dest);           break;
			case "latex": export.exportLaTeX(dest, conf);     break;
			case "xslt":
				export.exportXSLT(dest, Paths.get(conf)); break;
			default:
				System.err.println("Unknown export mode: " +
									type);
				System.exit(1);
			}
		} finally {
			export.saveTable();
		}
	}

	public static void main(String[] args) {
		try {
			banner();
			if(args.length < 2)
				help();
			else
				new Main(args).run();
		} catch(Exception ex) {
			System.err.println();
			System.err.println("Export failed.");
			System.err.println(ErrorInfo.getStackTrace(ex,
				D5IOException.getExtendedMessageGenerator()));
			System.exit(1);
		}
	}

	private static void banner() {
		System.out.println("Ma_Sys.ma D5Man LEGACY Export 1.0.0, " +
					"Copyright (c) 2015, 2019 Ma_Sys.ma.");
		System.out.println("For further info send an e-mail to " +
					"Ma_Sys.ma@web.de.");
		System.out.println();
	}

	private static void help() {
		System.out.println("USAGE d5manexport -m mode " +
					"[-o destination] [-c conf] [-t]");
		System.out.println();
		System.out.println("Pipe `d5manquery -p -q [query] -o xml` " +
					"into this application to supply page");
		System.out.println("metadata and general configuration.");
		System.out.println();
		System.out.println("mode");
		System.out.println("        xml    Raw XML export " +
					"(debug and interfacing only).");
		System.out.println("        xhtml  To perform XHTML export.");
		System.out.println("        latex  To perform LaTeX export.");
		System.out.println("               " +
			"Configure require_toc=1 to always generate TOC.");
		System.out.println("        xslt   Use XSLT sheet from conf.");
		System.out.println("destination");
		System.out.println("        Give target directory. Existence " +
						"required. Default ist PWD");
		System.out.println("conf");
		System.out.println("        Configuration information " +
								"(see mode)");
		System.out.println("-t");
		System.out.println("        Enables Checksum table.");
	}

}
