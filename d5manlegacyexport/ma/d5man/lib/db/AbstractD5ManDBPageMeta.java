package ma.d5man.lib.db;

import java.util.*;
import ma.d5man.lib.util.ConstantIterator;
import ma.d5man.lib.Compliance;

public abstract class AbstractD5ManDBPageMeta extends CompliantPageName {

	static final int DATE_NOT_SET = -1;

	protected String     description;
	protected long       expires;      // can not expire is DATE_NOT_SET
	protected long       modified;
	protected String     lang;
	protected String     location;     // no redirect is encoded with null
	protected float      webPriority;
	protected Frequency  webFreq;

	private Set<String> tags;
	private final List<AbstractDownload> downloads;

	protected final Map<String, Attachment> attachments;

	protected AbstractD5ManDBPageMeta() {
		super();
		description     = null;
		expires         = DATE_NOT_SET;
		modified        = DATE_NOT_SET;
		lang            = null;
		compliance      = null;
		location        = null;
		md5             = null;
		webPriority     = 0.4f;              // sane default
		webFreq         = Frequency.MONTHLY; // sane default
		downloads       = new ArrayList<AbstractDownload>();
		attachments     = new HashMap<String, Attachment>();
	}

	/**
	 * This copy constructor enables subclasses to extend this class having
	 * an instance of this class as a parameter. It only performs a flat
	 * copy.
	 */
	protected AbstractD5ManDBPageMeta(AbstractD5ManDBPageMeta m) {
		super();
		description     = m.description;
		expires         = m.expires;
		modified        = m.modified;
		lang            = m.lang;
		compliance      = m.compliance;
		location        = m.location;
		md5             = m.md5;
		webPriority     = m.webPriority;
		webFreq         = m.webFreq;
		tags            = m.tags;
		attachments     = m.attachments;
		downloads       = m.downloads;
	}

	public Iterable<AbstractDownload> getDownloads() {
		return downloads;
	}

	protected void setTags(String tags) {
		setTags(tags.split(" "));
	}

	private void setTags(String[] tags) {
		this.tags = new HashSet<String>(Arrays.asList(tags));
	}

	void openTags() {
		tags = new HashSet<String>();
	}

	void addTag(String tag) {
		tags.add(tag);
	}

	/**
	 * Guranteed to return not-null even if there might not be any tags
	 * set and the tags field is null.
	 */
	public Set<String> getTags() {
		return (tags == null)? Collections.<String>emptySet(): tags;
	}

	public Attachment getAttachment(String name) {
		return attachments.get(name);
	}

	@Override
	public Iterator<String> iterator() {
		return attachments.keySet().iterator();
	}

	protected void addDownload(AbstractDownload d) {
		downloads.add(d);
	}

	protected void putAttachment(String name, Attachment a) {
		attachments.put(name, a);
	}

	protected String getEtag() {
		return "D5I-" + toString() + "-" + new String(md5);
	}

	public long getModified() {
		return modified;
	}

	public float getPriority() {
		return webPriority;
	}

	public Frequency getFreq() {
		return webFreq;
	}

	public String getDescription() {
		return description;
	}

	public String getLanguage() {
		return lang;
	}

	public static String multistr(String lang, String de, String en) {
		return (lang == null || lang.equals("de"))? de: en;
	}

	// TODO DEBUG ONLY
	public String toDebugString() {
		StringBuilder ret = new StringBuilder(super.toString());
		ret.append("\ndescription=");     ret.append(description);
		ret.append("\nexpires=");         ret.append(expires);
		ret.append("\nmodified=");        ret.append(modified);
		ret.append("\nlang=");            ret.append(lang);
		ret.append("\ncompliance=");      ret.append(compliance);
		ret.append("\nlocation=");        ret.append(location);
		ret.append("\nmd5=");             ret.append(new String(md5));
		ret.append("\nwebPriority=");     ret.append(webPriority);
		ret.append("\nwebFreq=");         ret.append(webFreq);
		ret.append("\ntags=");            al2s(ret, tags);
		ret.append("\ndownloads=");       al2s(ret, downloads);
		return ret.toString();
	}

	private static void al2s(StringBuilder ret, Collection<?> l) {
		ret.append(l == null? "(null)": Arrays.toString(l.toArray()));
	}

}
