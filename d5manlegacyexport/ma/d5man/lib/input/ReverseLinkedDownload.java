package ma.d5man.lib.input;

import ma.d5man.lib.db.Download;
import ma.d5man.lib.db.PageName;

// TODO z THIS IS INTENDED TO BE THE DEFAULT => OTHER CLASSES IN BETWEEN SHOULD BE REDUCED AND THIS ONE SHOULD GO TO DB WHERE IT IS CORRECT #4
public class ReverseLinkedDownload extends Download {

	public final PageName link;

	public ReverseLinkedDownload(String url, String name,
				String description, String version, long size,
				long checked, char[] sha256, PageName link) {
		super(url, name, description, version, size, checked, sha256);
		this.link = link;
	}

}
