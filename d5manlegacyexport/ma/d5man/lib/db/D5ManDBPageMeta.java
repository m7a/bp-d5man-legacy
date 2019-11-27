package ma.d5man.lib.db;

/**
 * Represents all metadata which are stored within the database including those
 * not directly in the same table like tags.
 *
 * @version 1.0
 * @since 2014
 */
public class D5ManDBPageMeta extends AbstractD5ManDBPageMeta {

	private final int section;
	private final String name;

	protected D5ManDBPageMeta(int section, String name) {
		super();
		this.section = section;
		this.name    = name;
	}

	protected D5ManDBPageMeta(AbstractD5ManDBPageMeta m) {
		super(m);
		section = m.getSection();
		name    = m.getName();
	}

	@Override
	public int getSection() {
		return section;
	}

	@Override
	public String getName() {
		return name;
	}

}
