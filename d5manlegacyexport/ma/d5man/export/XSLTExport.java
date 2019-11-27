package ma.d5man.export;

import java.io.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.*;

import ma.tools2.xml.ErrorAwareXMLParser;
import ma.tools2.util.NotImplementedException;

import ma.d5man.lib.lls.*;
import ma.d5man.lib.procxml.D5ManXMLEntityResolver;
import ma.d5man.lib.shr.AbstractD5ManPage;
import ma.d5man.lib.export.proc.real.D5Man2XMLProcess;
import ma.d5man.lib.export.XMLExport;

public class XSLTExport extends XMLExport {

	private final String resultSuffix;

	private final XMLReader xmlReader;
	private final Transformer transformer;

	public XSLTExport(D5Man2XMLProcess d2x, String resultSuffix,
				InputStream stylesheet) throws Exception {
		super(d2x);
		this.resultSuffix = resultSuffix;
		// -> http://stackoverflow.com/questions/1572808/java-xml-xslt-
		// 					prevent-dtd-validation
		xmlReader = ErrorAwareXMLParser.createParser().getXMLReader();
		xmlReader.setEntityResolver(new D5ManXMLEntityResolver());
		AllFailErrorHandler handler = new AllFailErrorHandler();
		xmlReader.setErrorHandler(handler);
		TransformerFactory fact = TransformerFactory.newInstance();
		fact.setErrorListener(handler);
		transformer = fact.newTransformer(new StreamSource(stylesheet));
		transformer.setErrorListener(handler);
	}

	@Override
	protected String getExtension() {
		return resultSuffix;
	}

	@Override
	protected D5Stream apply(D5Stream in) {
		return new D5StreamWrapper(super.apply(in)) {
			@Override
			public InputStream openInputStreamRepresentation()
							throws D5IOException {
				InputStream i =
					super.openInputStreamRepresentation();
				try {
					return transformStream(i);
				} catch(Exception ex) {
					throw D5IOException.wrapIn(ex);
				}
			}
			@Override
			public boolean hasEnd() {
				return false;
			}
			@Override
			public byte[] getMD5() throws D5IOException {
				return null;
			}
			@Override
			public long getLength() throws D5IOException {
				throw new NotImplementedException();
			}
		};
	}

	private InputStream transformStream(InputStream in) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		transformer.transform(new SAXSource(xmlReader,
				new InputSource(in)), new StreamResult(out));
		return new ByteArrayInputStream(out.toByteArray());
	}

}
