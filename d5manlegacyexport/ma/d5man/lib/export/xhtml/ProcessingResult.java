package ma.d5man.lib.export.xhtml;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

import ma.tools2.xml.MaBufXML;

import ma.d5man.lib.Compliance;
import ma.d5man.lib.db.*;
import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.lls.MinimalResourceSet;
import ma.d5man.lib.shr.DateFormats;
import ma.d5man.lib.input.ReverseLinkedDownload;
import ma.d5man.lib.procxml.BasicProcessingResult;

// TODO THIS CONSISTENTLY USES File.separator but that might result in chaos on Windows systems? We want to use this for URLs so Fiel.separator is no good? -> currently used inconsistently -> find policy
public class ProcessingResult extends BasicProcessingResult
						implements MinimalResourceSet {

	String suffix;
	String prefix;

	final MaBufXML head;
	final MaBufXML body;

	private final Map<String,D5Stream> res;

	ProcessingResult() {
		super();
		prefix    = null;
		suffix    = "";
		head      = new MaBufXML(2);
		body      = new MaBufXML(3);
		res       = new HashMap<String,D5Stream>();
	}

	/** Same metadata, but no content */ 
	ProcessingResult(ProcessingResult src) {
		super(src);
		suffix    = src.suffix;
		prefix    = src.prefix;
		head      = new MaBufXML(2);
		body      = new MaBufXML(3);
		res       = new HashMap<String,D5Stream>();
	}

	// TODO z LINKS ARE NOT GOOD HERE BUT ALSO NOT GOOD AT ANY OTHER LOCATION...???

	String linkResource(String resourceFile) {
		return prefix + PageName.createFSPath(getSection(), getName(),
			File.separator, XHTMLTemplate.EXT, resourceFile);
	}

	String linkPage(int section, String name) {
		return linkPage(prefix, section, name);
	}

	static String linkPage(String prefix, int section, String name) {
		return prefix + PageName.createFSPath(section, name,
					File.separator, XHTMLTemplate.EXT);
	}

	void addResource(String name, D5Stream d5s) {
		res.put(PageName.createFSPath(getSection(), getName(),
					File.separator, null, name), d5s);
	}

	void setModified(long modified) {
		this.modified = modified;
	}

	@Override
	public D5Stream openResource(String name) throws IOException {
		return res.get(name);
	}

	@Override
	public Iterable<String> getResourceNames() {
		return res.keySet();
	}

	String getAbsoluteLink() {
		return PageName.createFSPath(getSection(), getName(),
				File.separator, suffix + XHTMLTemplate.EXT);
	}

}
