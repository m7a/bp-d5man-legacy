package ma.d5man.lib.export.xhtml.page;

import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import ma.tools2.util.NotImplementedException;
import ma.tools2.util.BinaryOperations;

import ma.d5man.lib.db.D5ManDBPageMeta;
import ma.d5man.lib.db.Attachment;
import ma.d5man.lib.lls.DRS;
import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.shr.AbstractD5ManPage;
import ma.d5man.lib.export.proc.D5ManIOResolver;

// TODO EXPORT WORKER IMPORTS THIS DIRECTLY => THIS IS BY NO MEANS RELATED TO EXPORT/XHTML!
public class D5ManPage extends AbstractD5ManPage {

	private final D5ManIOResolver ioResolver;
	private final DRS assocDRS;
	private final D5ManIOResolver.Result mainQueryResult;
	private final HashMap<String,D5ManIOResolver.Result>
							attachmentQueryResults;
	private final HashMap<String,String> queryPathToAttachmentName;
	private final ZIPCache z;

	public D5ManPage(final D5ManDBPageMeta m, D5ManIOResolver ioResolver,
						ZIPCache z) throws IOException {
		super(m);
		this.ioResolver = ioResolver;
		mainQueryResult = ioResolver.resolve(m, null,
						D5ManIOResolver.Mode.READ);
		attachmentQueryResults = new HashMap<String,
						D5ManIOResolver.Result>();
		queryPathToAttachmentName = new HashMap<String,String>();
		this.z = z;
		for(String s: m)
			queryPathToAttachmentName.put(createFSPath("/", null,
									s), s);
		assocDRS = new DRS(this) {
			@Override
			public D5Stream openResource(String name)
							throws IOException {
				D5Stream prev = super.openResource(name);
				return prev == null? openAttachment(name): prev;
			}
			@Override
			public D5Stream openFirst() throws IOException {
				return openMain();
			}
			@Override
			public String getFirstResourceName() {
				return m.getFSName();
			}
			@Override
			public Iterable<String> getOtherResourceNames() {
				return queryPathToAttachmentName.keySet();
			}
		};
	}

	@Override
	public DRS access() throws D5IOException {
		return assocDRS;
	}

	private D5Stream openMain() {
		return new D5Stream() {
			@Override
			public InputStream openInputStreamRepresentation()
							throws D5IOException {
				return resultToInputStream(mainQueryResult);
			}
			@Override
			public long getTimestamp() throws D5IOException {
				return modified;
			}
			@Override
			public String getEtag() throws D5IOException {
				return makeGeneralEtag();
			}
			@Override
			public byte[] getMD5() throws D5IOException {
				return md5 == null? null: BinaryOperations.
					decodeHexString(new String(md5));
			}
		};
	}

	private D5Stream openAttachment(final String an) throws IOException {
		final String an2 = queryPathToAttachmentName.get(an);
		if(an2 == null)
			return null;

		final D5ManIOResolver.Result r;
		if(attachmentQueryResults.containsKey(an)) {
			r = attachmentQueryResults.get(an);
		} else {
			r = ioResolver.resolve(this, an2, D5ManIOResolver.Mode.
									READ);
			attachmentQueryResults.put(an, r);
		}
		return new D5Stream() {
			@Override
			public InputStream openInputStreamRepresentation()
							throws D5IOException {
				return resultToInputStream(r);
			}
			@Override
			public long getTimestamp() throws D5IOException {
				return getAttachment(an2).modified;
			}
			@Override
			public String getEtag() throws D5IOException {
				return makeGeneralEtag() + "/" + an;
			}
			@Override
			public byte[] getMD5() throws D5IOException {
				// TODO AN2 SEEMS TO BE CORRECT HERE (AN before) / RECHECK IF SERVER STILL WORKS AS EXPECTED...
				Attachment a = getAttachment(an2);
				return a.md5 == null? null: BinaryOperations.
					decodeHexString(new String(a.md5));
			}
		};
	}

	private InputStream resultToInputStream(D5ManIOResolver.Result r)
							throws D5IOException {
		try {
			switch(r.t) {
			case REALFILE: return new FileInputStream(r.arg);
			case ZIPFILE:  return z.queryInputStream(r.arg);
			case COMMAND:  return Runtime.getRuntime().exec(r.arg).
							getInputStream();
			default: throw new NotImplementedException(
						"Missing action for " + r.t);
			}
		} catch(IOException ex) {
			throw D5IOException.wrapIn(ex);
		}
	}

	private String makeGeneralEtag() {
		return getEtag();
	}

}
