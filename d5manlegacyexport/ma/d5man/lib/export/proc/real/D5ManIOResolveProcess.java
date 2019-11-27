package ma.d5man.lib.export.proc.real;

import java.io.*;
import java.nio.file.Path;
import static java.nio.charset.StandardCharsets.UTF_8;

import ma.d5man.lib.lls.D5IOException;
import ma.d5man.lib.db.AbstractD5ManDBPageMeta;
import ma.d5man.lib.db.PageName;
import ma.d5man.lib.export.proc.*;

public class D5ManIOResolveProcess extends AbstractConverterProcess
						implements D5ManIOResolver {

	private BufferedReader queryResults;
	private OutputStream queryPipe;
	private Exception storedError;

	public D5ManIOResolveProcess(Path executable) {
		super(executable);
		storedError = null;
	}

	@Override
	public void open() throws IOException {
		super.open();
		queryResults = new BufferedReader(new InputStreamReader(
							proc.getInputStream()));
		queryPipe    = proc.getOutputStream();
	}

	@Override
	public Result resolve(PageName page, String attachment, Mode m)
							throws IOException {
		validateInput(page, attachment, m);
		String answer = resolveCritical(page, attachment, m);

		int pos = answer.indexOf(',');
		if(pos == -1)
			throw new IOException("Misformatted result line: " +
									answer);

		String type = answer.substring(0, pos);
		String arg  = answer.substring(pos + 1);
		if(type.equals("ERROR"))
			throw new IOException("d5manioresolve error: " + arg + 
					" (for page=" + page.toString() +
					", attachment=" + attachment +
					", mode=" + m + ")");

		return new Result(Result.Type.fromString(type), arg);
	}

	private void validateInput(PageName page, String attachment, Mode m)
							throws D5IOException {
		String err = page.validate();
		if(err != null)
			throw new D5IOException("PageName invalid: " + err);
		if(m == null)
			throw new NullPointerException("Mode may not be null.");
	}

	private synchronized String resolveCritical(final PageName page,
					final String attachment, final Mode m)
					throws IOException {
		if(storedError != null)
			throw new IOException("Failure due to previous error.",
								storedError);

		final byte[] query = mkquery(page, attachment, m);

		Thread sender = new Thread() {
			@Override
			public void run() {
				try {
					queryPipe.write(query);
					queryPipe.flush();
				} catch(IOException ex) {
					storedError = ex;
				}
			}
		};
		sender.start();
		String answer;
		try {
			answer = queryResults.readLine();
		} finally {
			// TODO z Enhancement Consider this: We have a newly created process w/ some error line at the begin of output. In this case, we have read the previous reror message using readLine() and now wait for a write operation to complete which might never be the case as we do not read... As this problem only occurrs whenever some internal buffer is excceeded, this is not a real issue but to be kept in mind.
			try {
				sender.join();
			} catch(InterruptedException ex) {
				// This thread is never intrrupted
				// => this exception is fatal
				throw new IOException(ex);
			}
		}
		if(storedError != null)
			throw new IOException("Exception in sub thread. " +
				"Got result: " + answer + " for query " +
				new String(query, UTF_8).replace("\n", "\\n")
				+ " (discarded due " +
				"to IOException).", storedError);

		return answer;
	}

	private static byte[] mkquery(PageName page, String attachment,
						Mode m) throws IOException {
		String query = m.name() + "," + page.getSection() + "," +
								page.getName();

		if(m == Mode.CREATE) {
			if(!(page instanceof AbstractD5ManDBPageMeta))
				throw new IOException("Page Meta " + page +
					" incomplete. Need at least " +
					"AbstractD5ManDBPageMeta (for " +
					"compliance field). This error " +
					"indicates an internal program error " +
					"and thus most likely a bug.");
			query += "," + ((AbstractD5ManDBPageMeta)page).
						getCompliance().toString();
		}

		if(attachment != null)
			query += "," + attachment;

		query += "\n";
		return query.getBytes(UTF_8);
	}

	@Override
	public void close() throws IOException {
		queryPipe.close();
		queryResults.close();
		super.close();
	}

}
