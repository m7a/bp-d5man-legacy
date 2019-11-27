package ma.d5man.lib.export;

import java.io.IOException;

import ma.d5man.lib.input.Conf;
import ma.d5man.lib.D5ManConstants;
import ma.d5man.lib.lls.*;

public class ExternalLanguageProcessors extends AbstractMultipleResourceOnDemand
					<ExternalLanguageProcessor, DRS> {

	public ExternalLanguageProcessors(Conf conf) {
		super(ExternalLanguageProcessor.class, conf);
	}

	public DRS convert(D5Stream in, String clang) throws D5IOException {
		return getFromSub(new Object[] { in, clang });
	}

	@Override
	protected DRS invoc(ExternalLanguageProcessor l, Object[] args)
							throws D5IOException {
		return l.convert((D5Stream)args[0], (String)args[1]);
	}

	@Override
	protected DRS makeDefaultResource() throws D5IOException {
		return null;
	}

}
