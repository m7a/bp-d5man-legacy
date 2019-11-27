package ma.d5man.lib.shr;

import ma.tools2.util.BinaryOperations;
import ma.d5man.lib.db.Attachment;
import ma.d5man.lib.db.AbstractD5ManDBPageMeta;
import ma.d5man.lib.db.D5ManDBPageMeta;
import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.lls.DRS;

public abstract class AbstractD5ManPage extends D5ManDBPageMeta {

	public AbstractD5ManPage(AbstractD5ManDBPageMeta meta) {
		super(meta);
	}

	/** Warning: This only creates a non-functional mockup object */
	public AbstractD5ManPage() {
		super(-1, "NONEXISTENT_ABSTRACT_D5_MAN_PAGE");
	}

	public abstract DRS access() throws D5IOException;

}
