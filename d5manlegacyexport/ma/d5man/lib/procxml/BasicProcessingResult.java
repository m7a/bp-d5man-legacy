package ma.d5man.lib.procxml;

import java.util.List;
import java.util.ArrayList;

import ma.d5man.lib.Compliance;
import ma.d5man.lib.db.*;

public class BasicProcessingResult extends AbstractD5ManDBPageMeta {

	private int section;
	private String name;
	protected String copyright;

	private ProcDownload cDownload;

	public BasicProcessingResult() {
		super();
		section   = -1;
		name      = null;
		copyright = null;
		cDownload = null;
	}

	protected BasicProcessingResult(BasicProcessingResult r) {
		super(r);
		section   = r.section;
		name      = r.name;
		copyright = r.copyright;
	}

	@Override
	public int getSection() {
		return section;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getCopyright() {
		return copyright;
	}

	void setFromHeaderKV(String k, String v) {
		if(processDownload(k, v))
			return;
		switch(k) {
		case "name":        name        = v;                   break;
		case "section":     section     = Integer.parseInt(v); break;
		case "description": description = v;                   break;
		case "tags":        setTags(v);                        break;
		case "lang":        lang        = v;                   break;
		case "compliance":  compliance  = Compliance.fromString(v);
									break;
		case "copyright":   copyright   = v;                   break;
		case "attachments": setAttachmentNames(v.split(" "));  break;
		}
	}

	private void setAttachmentNames(String[] names) {
		for(String s: names)
			putAttachment(s, null);
	}

	/** @return true if it was processed as a download field. */
	private boolean processDownload(String k, String v) {
		int dID = -1;
		int klen1 = k.length() - 1;
		char lastChr = k.charAt(klen1);
		if(Character.isDigit(lastChr))
			dID = Integer.valueOf(lastChr);
		
		return processDownload(dID, k.substring(0, klen1), v);
	}

	private boolean processDownload(int dID, String k, String v) {
		switch(k) {
		case "dref":     addDownloadReference(v);                 break;
		case "download": ad(dID).name        = v;                 break;
		case "ddescr":   ad(dID).description = v;                 break;
		case "dlink":    ad(dID).url         = v;                 break;
		case "dsize":    ad(dID).size        = Long.parseLong(v); break;
		case "dver":     ad(dID).version     = v;                 break;
		case "dchcksm":  ad(dID).sha256      = v.toCharArray();   break;
		default:         return false;
		}
		return true;
	}

	/** access download by id */
	private ProcDownload ad(int dID) {
		if(cDownload != null && cDownload.downloadID == dID)
			return cDownload;

		cDownload = new ProcDownload(dID);
		addDownload(cDownload);
		return cDownload;
	}

	private void addDownloadReference(String reference) {
		int sep = reference.indexOf('/');
		addDownload(new DownloadReference(
			StaticPageName.fromNameSection(reference.substring(0,
									sep)),
			reference.substring(sep + 1)
		));
	}

}
