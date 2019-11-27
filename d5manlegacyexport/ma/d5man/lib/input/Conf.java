package ma.d5man.lib.input;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.procxml.ResultlessSubHandler;

public class Conf extends ResultlessSubHandler {

	private Path       commonRes;
	private Path       vim;
	private Path       ioResolver;
	private Path       dbSync;
	private Path       d5man2xml;
	private String[]   mediaConverter;
	private Path       complianceB;
	private List<Path> ioRoots;

	public Conf() {
		super();
		commonRes      = null;
		vim            = null;
		mediaConverter = null;
		complianceB    = null;
		ioRoots        = null;
	}

	@Override
	protected boolean startElement(String n, Attributes att)
					throws D5IOException, SAXException {
		if(n.equals("conf")) {
			commonRes      = Paths.get(att.getValue("common_res"));
			vim            = Paths.get(att.getValue("vim"));
			dbSync         = Paths.get(att.getValue("db_sync"));
			ioResolver     = Paths.get(att.getValue("io_resolver"));
			d5man2xml      = Paths.get(att.getValue("d5man2xml"));
			mediaConverter = att.getValue("media_converter").
								split(" ");
			complianceB    = Paths.get(att.getValue("io_compl_b")).
							toAbsolutePath();
		} else if(n.equals("io_roots")) {
			ioRoots        = new ArrayList<Path>();
		} else if(n.equals("io_root")) {
			ioRoots.add(Paths.get(att.getValue("v")).
							toAbsolutePath());
		} else {
			return false;
		}
		return true;
	}

	public Path getCommonRes() {
		return commonRes;
	}

	public Path getVIM() {
		return vim;
	}

	public Path getIOResolver() {
		return ioResolver;
	}

	public Path getD5Man2XML() {
		return d5man2xml;
	}

	public Path getDBSync() {
		return dbSync;
	}

	public String[] getMediaConverter() {
		return mediaConverter;
	}

	public Path getComplianceB() {
		return complianceB;
	}

	public Path[] getRoots() {
		return ioRoots.toArray(new Path[ioRoots.size()]);
	}

}
