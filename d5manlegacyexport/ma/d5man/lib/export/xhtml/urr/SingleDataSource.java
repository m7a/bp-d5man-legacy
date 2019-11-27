package ma.d5man.lib.export.xhtml.urr;

import java.io.IOException;
import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.lls.MinimalResourceSet;
import ma.d5man.lib.util.ConstantIterable;

abstract class SingleDataSource implements MinimalResourceSet {

	SingleDataSource() {
		super();
	}

	abstract String getName();

	@Override
	public D5Stream openResource(String name) throws IOException {
		return name.equals(getName())? openResource(): null;
	}

	abstract D5Stream openResource() throws IOException;

	@Override
	public Iterable<String> getResourceNames() {
		return new ConstantIterable<String>(getName());
	}

}
