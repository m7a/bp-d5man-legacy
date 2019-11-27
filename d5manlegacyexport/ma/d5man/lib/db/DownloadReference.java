package ma.d5man.lib.db;

public class DownloadReference implements AbstractDownload {

	public final PageName pageReference;
	private final String name;

	public DownloadReference(PageName pn, String name) {
		super();
		pageReference = pn;
		this.name     = name;
	}

	@Override
	public String getNameWOAccess() {
		return name;
	}

	@Override
	public Download access() {
		throw new ma.tools2.util.NotImplementedException(
			"Dereferencing Download Reference not possible in " +
			"libd5manexport.");
	}

}
