package ma.d5man.lib.lls;

import java.lang.reflect.Array;
import java.io.IOException;

import ma.d5man.lib.shr.Configurable;
import ma.d5man.lib.input.Conf;
import ma.d5man.lib.D5ManConstants;

public abstract class AbstractMultipleResourceOnDemand<T
			extends ResourceOnDemand, R> extends ResourceOnDemand {

	private final Class<T> cls;
	private final Conf conf;
	private T[] subResources;

	protected AbstractMultipleResourceOnDemand(Class<T> cls, Conf conf) {
		super();
		this.cls     = cls;
		this.conf    = conf;
		subResources = null;
	}

	@Override
	public void open() throws IOException {
		String[] res =
			D5ManConstants.REGISTERED_RESOURCE_ON_DEMAND_CLASSES;

		initializeSubResourcesArray(res.length);
		for(int i = 0; i < res.length; i++)
			createSubResource(i, res[i]);

		super.open();
	}

	@SuppressWarnings("unchecked")
	private void initializeSubResourcesArray(int len) {
		// -> http://stackoverflow.com/questions/529085/how-to-create-a-
		// 					generic-array-in-java
		subResources = (T[])Array.newInstance(cls, len);
	}

	private void createSubResource(int i, String clsn) throws IOException {
		Object x;
		try {
			x = Class.forName(clsn).getConstructor().newInstance();
		} catch(Exception ex) {
			throw new IOException(ex);
		}

		checkPotentialSubResource(x);
		setSubResource(i, x);

		((Configurable)subResources[i]).configure(conf);
	}

	private void checkPotentialSubResource(Object x) throws IOException {
		if(!cls.isInstance(x))
			throw new IOException("Failed to interpret " + x +
								" as " + cls);
		if(!(x instanceof Configurable))
			System.err.println("WARNING: " + x +
						" is not configurable.");
	}

	@SuppressWarnings("unchecked")
	private void setSubResource(int idx, Object x) {
		// -> http://stackoverflow.com/questions/12886769/java-compiler-
		// 		error-not-making-any-sense-identifier-expected
		subResources[idx] = (T)x;
	}

	protected R getFromSub(Object[] parameters) throws D5IOException {
		R ret;
		for(T r: subResources) {
			need(r);
			if((ret = invoc(r, parameters)) != null)
				return ret;
		}
		return makeDefaultResource();
	}

	protected abstract R invoc(T r, Object[] parameters)
							throws D5IOException;

	protected abstract R makeDefaultResource() throws D5IOException;
	
	private static void need(ResourceOnDemand r) throws D5IOException {
		if(!r.isOpen()) {
			try {
				r.open();
			} catch(D5IOException ex) {
				throw ex;
			} catch(IOException ex) {
				throw new D5IOException(ex);
			}
		}
	}

	@Override
	public void close() throws IOException {
		for(ResourceOnDemand r: subResources)
			if(r.isOpen())
				r.close();
	}

}
