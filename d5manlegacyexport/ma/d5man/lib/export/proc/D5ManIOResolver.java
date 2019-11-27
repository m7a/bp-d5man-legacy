package ma.d5man.lib.export.proc;

import java.io.IOException;

import ma.d5man.lib.db.PageName;

public interface D5ManIOResolver {

	public static enum Mode { READ, WRITE, CREATE }

	public static class Result {

		public static enum Type {

			REALFILE, ZIPFILE, COMMAND;

			public static Type fromString(String str) {
				for(Type t: Type.class.getEnumConstants())
					if(t.name().equals(str))
						return t;

				throw new IllegalArgumentException("Unknown " +
								"type: " + str);
			}
		};

		public final Type t;
		public final String arg;

		public Result(Type t, String arg) {
			super();
			this.t   = t;
			this.arg = arg;
		}

	}

	/**
	 * @param page
	 * 	needs to be D5ManDBPageMeta if Mode is CREATE
	 * 	(compliance field becomes relevant)
	 */
	public Result resolve(PageName page, String attachment, Mode m)
							throws IOException;

}
