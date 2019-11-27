package ma.d5man.lib.export.xhtml;

import java.io.IOException;
import java.io.InputStream;

import ma.tools2.util.BufferUtils;

import ma.d5man.lib.util.MergedIterable;
import ma.d5man.lib.util.ConstantIterable;
import ma.d5man.lib.lls.*;

class XHTMLDRS extends DRSWrapper {

	private final XHTMLTemplate tpl;
	private final ProcessingResult result;
	private final ProcessingResult src;

	XHTMLDRS(XHTMLTemplate tpl, DRS xml, ProcessingResult result,
							ProcessingResult src) {
		super(xml);
		this.tpl = tpl;
		this.result = result;
		this.src    = src;
	}

	@Override
	public D5Stream openResource(String name) throws IOException {
		D5Stream s;
		if((s = super.openResource(name)) != null ||
					(s = result.openResource(name)) != null)
			return s;
		else if(name.equals(src.getAbsoluteLink()))
			return wrapIntoD5Stream(src);
		else
			return src.openResource(name);
	}

	@Override
	public D5Stream openFirst() throws IOException {
		return wrapIntoD5Stream(result);
	}

	private D5Stream wrapIntoD5Stream(final ProcessingResult result)
							throws IOException {
		return new D5StreamWrapper(sub.openFirst()) {
			@Override
			public long getTimestamp() throws D5IOException {
				return Math.max(super.getTimestamp(), tpl.mod);
			}
			@Override
			public String getEtag() throws D5IOException {
				return super.getEtag() + "-TP-" + (tpl.mod
									/ 1000);
			}
			@Override
			public InputStream openInputStreamRepresentation()
							throws D5IOException {
				return useTemplate(result);
			}
		};
	}

	@Override
	public String getFirstResourceName() {
		return result.getAbsoluteLink();
	}

	@Override
	public Iterable<String> getOtherResourceNames() {
		return new MergedIterable<String>(
			super.getOtherResourceNames(),
			src.getResourceNames(),
			new ConstantIterable<String>(src.getAbsoluteLink()),
			result.getResourceNames()
		);
	}

	private InputStream useTemplate(ProcessingResult result)
							throws D5IOException {
		XHTMLTemplateValues tv = new XHTMLTemplateValues(tpl, result);
		tv.setAddToHead(result.head.toString());
		tv.setBody(result.body.toString());
		tv.setSurroundings(result.prefix, result.suffix);
		tv.setCopyright(result.getCopyright());
		return BufferUtils.writefile(tv.apply());
	}

}
