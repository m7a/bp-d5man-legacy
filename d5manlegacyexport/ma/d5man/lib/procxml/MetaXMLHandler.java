package ma.d5man.lib.procxml;

import ma.d5man.lib.db.D5ManDB;

public abstract class MetaXMLHandler<T extends BasicProcessingResult>
							extends XMLHandler<T> {

	private final SubHandler[] handlers;

	public MetaXMLHandler(SubHandler endHandler) {
		super();
		handlers = new SubHandler[] {
			new SPageDummy<T>(),
			new SMetaOnly<T>(),
			endHandler,
		};
	}

	@Override
	protected SubHandler[] getHandlers() {
		return handlers;
	}

}
