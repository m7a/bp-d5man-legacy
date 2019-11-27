package ma.d5man.lib.export;

import ma.d5man.lib.lls.*;
import ma.d5man.lib.shr.Configurable;

public abstract class ExternalLanguageProcessor extends ResourceOnDemand
						implements Configurable {

	/** @return null if not responsible */
	public abstract DRS convert(D5Stream in, String language)
							throws D5IOException;

}
