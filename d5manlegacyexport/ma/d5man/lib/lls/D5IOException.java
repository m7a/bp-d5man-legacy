package ma.d5man.lib.lls;

import java.io.IOException;

import ma.tools2.util.ErrorInfo;

public class D5IOException extends IOException {

	private final String detailedErrorMessage;

	public D5IOException() {
		super();
		detailedErrorMessage = null;
	}

	public D5IOException(String msg) {
		this(msg, null, null);
	}

	public D5IOException(String msg, String detailedMsg) {
		this(msg, null, detailedMsg);
	}

	public D5IOException(Throwable t) {
		this(null, t, null);
	}

	public D5IOException(String msg, Throwable t) {
		this(msg, t, null);
	}

	public D5IOException(String msg, Throwable t, String detailedMsg) {
		super(msg, t);
		detailedErrorMessage = detailedMsg;
	}

	public static D5IOException wrapIn(Exception ex) {
		return (ex instanceof D5IOException)? (D5IOException)ex:
							new D5IOException(ex);
	}

	public boolean hasDetailedErrorMessage() {
		return detailedErrorMessage != null;
	}

	public String getDetailedErrorMessage() {
		return detailedErrorMessage;
	}

	public static ErrorInfo.ExtendedMessageGenerator
						getExtendedMessageGenerator() {
		return new ErrorInfo.ExtendedMessageGenerator() {
			@Override
			public String[] getExtendedInfo(Throwable t) {
				if(t instanceof D5IOException) {
					D5IOException s = (D5IOException)t;
					if(s.hasDetailedErrorMessage())
						return s.detailedErrorMessage.
								split("\n");
				}
				return null;
			}
		};
	}

	public static <T extends Exception> T chain(T current, T other) {
		if(current == null) {
			return other;
		} else {
			if(other != null)
				current.addSuppressed(other);
			return current;
		}
	}

}
