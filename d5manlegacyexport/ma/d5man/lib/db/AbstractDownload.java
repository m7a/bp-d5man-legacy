package ma.d5man.lib.db;

/**
 * Interface to make sure we can have some ``lazy'' download Objects
 * in form of references
 */
public interface AbstractDownload {
	
	public String getNameWOAccess();
	public Download access();

}
