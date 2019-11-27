package ma.d5man.export;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

import ma.d5man.lib.db.D5ManDB;
import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.input.Conf;
import ma.d5man.lib.input.DownloadsTable;
import ma.d5man.lib.input.XMLFromStdin;
import ma.d5man.lib.j8.*;
import ma.d5man.lib.export.FileExport;
import ma.d5man.lib.export.XMLExport;
import ma.d5man.lib.export.xhtml.XHTMLExport;
import ma.d5man.lib.export.xhtml.XHTMLTemplate;
import ma.d5man.lib.export.proc.real.D5Man2XMLProcess;

import ma.d5man.export.tex.LaTeXExport;

public class D5ManExport {

	private static interface ExportCreator {
		FileExport create(D5Man2XMLProcess proc) throws Exception;
	}

	// it should be enough to optimize these values
	// THREADS_PER_CPU >= (all the others)
	// otherwise, unused processes are created
	private static final float THREADS_PER_CPU           = 2.0f;
	private static final float PROC_IO_RESOLVERS_PER_CPU = 0.25f;
	// D5Man2XML is only a minor part of the calculation time and we can
	// thus create much fewer processes
	private static final float PROC_D5MAN2XML_PER_CPU    = 0.15f;
	// with what we calculate... a bit more conservative than the reality
	private static final float TABLE_THREADS             = 1.5f;

	public  final Conf           c;
	private final DownloadsTable t;
	public  final D5ManDB        d;
	private final int            cpus;

	private BiFunction<LivingQueue, ExportWorker[], Thread>
							statusThreadFactory;

	private ChecksumTable tbl;

	D5ManExport(Conf c, DownloadsTable t, D5ManDB d) {
		super();
		this.c = c;
		this.t = t;
		this.d = d;
		tbl    = null;
		cpus   = Runtime.getRuntime().availableProcessors();
		statusThreadFactory = new DefaultStatusThreadFactory();
	}

	public static D5ManExport createFromStdin() throws D5IOException {
		Conf c           = new Conf();
		DownloadsTable t = new DownloadsTable();
		D5ManDB d        = new D5ManDB();
		XMLFromStdin.proc(c, t, d);
		return new D5ManExport(c, t, d);
	}

	public void setStatusThreadFactory(BiFunction<LivingQueue,
						ExportWorker[], Thread> fact) {
		statusThreadFactory = fact;
	}

	int cpuMul(float factor) {
		return (int)Math.max(1, Math.min((long)Math.ceil(cpus * factor),
								d.getSize()));
	}

	void initTable(Path dest) throws IOException {
		tbl = new ChecksumTable(dest, cpuMul(TABLE_THREADS));
	}

	public void exportXML(Path dest) throws Exception {
		exportXML(dest, new DefaultFileCreationHandlerFactory());
	}

	public void exportXML(Path dest, Supplier<Consumer<Path>> f)
							throws Exception {
		exportD5Man2XMLNeeding(dest, f, new ExportCreator() {
			@Override
			public FileExport create(D5Man2XMLProcess d2x)
							throws Exception {
				return new XMLExport(d2x);
			}
		});
	}

	private void exportD5Man2XMLNeeding(Path dest,
					Supplier<Consumer<Path>> f,
					ExportCreator cr) throws Exception {
		try(MultipleD5Man2XMLProcesses d2x =
						new MultipleD5Man2XMLProcesses(
						c.getD5Man2XML(), cpuMul(
						PROC_D5MAN2XML_PER_CPU))) {
			FileExport[] export = new FileExport[cpuMul(
						THREADS_PER_CPU)];
			int j = 0;
			for(int i = 0; i < export.length; i++) {
				export[i] = cr.create(d2x.procs[j++]);
				if(j == d2x.procs.length)
					j = 0; // wrap around
			}
			runMultiFileExport(dest, f, export);
		}
	}

	private void runMultiFileExport(Path dest,
			Supplier<Consumer<Path>> fileCreationHandlerFactory,
			FileExport[] export) throws Exception {
		new MultiFileExport(c.getIOResolver(), dest, d, export,
			cpuMul(PROC_IO_RESOLVERS_PER_CPU), tbl,
			statusThreadFactory, fileCreationHandlerFactory).run();
	}

	public void exportXHTML(Path dest) throws Exception {
		exportXHTML(dest, new DefaultFileCreationHandlerFactory());
	}

	public void exportXHTML(Path dest, Supplier<Consumer<Path>> f)
							throws Exception {
		XHTMLTemplate tpl = new XHTMLTemplate(c.getCommonRes().
					resolve("website_fs_override").
					resolve("template.xml"));
		XHTMLExport[] export = new XHTMLExport[cpuMul(THREADS_PER_CPU)];

		for(int i = 0; i < export.length; i++)
			export[i] = new XHTMLExport(c, null, tpl, d, t);

		try {
			runMultiFileExport(dest, f, export);
		} finally {
			for(XHTMLExport x: export)
				x.close();
		}
	}

	public void exportLaTeX(Path dest, String conf) throws Exception {
		exportLaTeX(dest, new DefaultFileCreationHandlerFactory(),
									conf);
	}

	public void exportLaTeX(Path dest, Supplier<Consumer<Path>> f,
					final String conf) throws Exception {
		exportD5Man2XMLNeeding(dest, f, new ExportCreator() {
			@Override
			public FileExport create(D5Man2XMLProcess d2x)
							throws Exception {
				return new LaTeXExport(d2x, d, c, conf);
			}
		});
	}

	public void exportXSLT(Path dest, final Path xsltFile)
							throws Exception {
		exportD5Man2XMLNeeding(dest,
					new DefaultFileCreationHandlerFactory(),
					new ExportCreator() {
			@Override
			public FileExport create(D5Man2XMLProcess d2x)
							throws Exception {
				// TODO HACK .txt is not satisfying
				// TODO z IS THE STREAM CLOSED CORRECTLY?
				return new XSLTExport(d2x, ".txt",
					Files.newInputStream(xsltFile));
			}
		});
	}

	void saveTable() throws IOException {
		if(tbl != null)
			tbl.save();
	}

}
