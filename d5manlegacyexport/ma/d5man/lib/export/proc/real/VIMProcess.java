package ma.d5man.lib.export.proc.real;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.nio.file.Path;
import static java.nio.charset.StandardCharsets.UTF_8;

import ma.tools2.util.BufferUtils;
import ma.tools2.util.NotImplementedException;
import ma.tools2.concurrent.VoidStream;
import ma.d5man.lib.util.ConstantIterable;
import ma.d5man.lib.export.proc.AbstractConverterProcess;
import ma.d5man.lib.lls.DRS;
import ma.d5man.lib.lls.D5Stream;
import ma.d5man.lib.lls.StringD5Stream;

public class VIMProcess extends AbstractConverterProcess {

	private enum LineMode { PRE_STYLE, IN_STYLE, PRE_HTML, IN_HTML, END }

	private VoidStream vimStdoutDevNullPipe;
	private OutputStream vimStdinControl;
	private BufferedReader vimStderrData;

	public VIMProcess(Path executable) {
		super(executable);
	}

	@Override
	protected String[] getDefaultParameters() {
		// alt. use "xterm"
		return new String[] { null, "-nT", "rxvt-unicode" };
	}

	@Override
	public void open() throws IOException {
		super.open();
		vimStdoutDevNullPipe = new VoidStream(proc.getInputStream());
		vimStdinControl = proc.getOutputStream();
		vimStderrData = new BufferedReader(new InputStreamReader(
						proc.getErrorStream(), UTF_8));
		initVim();
	}

	private void initVim() throws IOException {
		vimStdoutDevNullPipe.start();
		immediatelySend(":set nonumber\n" +
				":let g:html_ignore_folding = 1\n" +
				":let g:html_number_lines = 0\n" +
				":let g:html_use_xhtml = 1\n");
		ping();
	}

	/**
	 * Attemts to read/write some test data in order to ensure the process
	 * connection is up. This clears superflous warnings as well.
	 */
	private void ping() throws IOException {
		// We do not need to fully parse the result and just let the
		// created lines be deleted by the GC.
		highlightRawHTML("text", new StringD5Stream("Test"));
	}

	private void immediatelySend(String data) throws IOException {
		vimStdinControl.write(data.getBytes(UTF_8));
		vimStdinControl.flush();
	}

	public DRS highlight(String lang, D5Stream src) throws IOException {
		return buildResult(highlightRawHTML(lang, src), lang);
	}

	/** Critical Method / This accesses the underlying VIM instance */
	private synchronized String[] highlightRawHTML(String lang,
					D5Stream src) throws IOException {
		sendData(lang, src);
		String[] responseRaw = getRawResponse();
		clearData();
		return responseRaw;
	}

	private void sendData(String lang, D5Stream src) throws IOException {
		immediatelySend("i");
		BufferUtils.copy(src.openInputStreamRepresentation(),
							vimStdinControl);
		immediatelySend("\033:setlocal filetype=" + lang +
					"\ngg:TOhtml\n:set encoding=utf-8\n" +
					":set nonumber\n:w !cat 1>&2\n");
	}

	private String[] getRawResponse() throws IOException {
		ArrayList<String> response = new ArrayList<String>();

		String line;
		// We do not rely on tailing </html> because it may not
		// have a newline afterwards and therefore not be flushed
		while((line = vimStderrData.readLine()) != null &&
						!line.equals("</html>"))
			response.add(line);

		return response.toArray(new String[response.size()]);
	}

	private void clearData() throws IOException {
		immediatelySend("ZQggdG");
	}

	private DRS buildResult(String[] lines, String lang) {
		StringBuilder css = new StringBuilder();
		StringBuilder html = new StringBuilder();
		LineMode m = LineMode.PRE_STYLE;
		Pattern p = Pattern.compile("<a href=\"(.*)\\)\">(.*)\\)</a>");
		for(String i: lines) {
			switch(m) {
			case PRE_STYLE:
				if(i.equals("<style type=\"text/css\">"))
					m = LineMode.IN_STYLE;
				break;
			case IN_STYLE:
				if(i.equals("</style>")) {
					m = LineMode.PRE_HTML;
				} else if(!i.startsWith("body {") &&
						!i.startsWith("* {") &&
						!i.startsWith("pre {")) {
					// Skip too generic elements
					css.append(i);
					css.append('\n');
				}
				break;
			case PRE_HTML:
				// To support multiple VIM versions we use <body
				// old versions: <body>
				// new versions: <body onload=...
				if(i.startsWith("<body"))
					m = LineMode.IN_HTML;
				break;
			case IN_HTML:
				if(i.equals("</body>")) {
					m = LineMode.END;
				} else if(i.equals(
						"<pre id='vimCodeElement'>")) {
					html.append("<pre>\n");
				} else {
					// TODO A BIT HACKY
					// -> http://stackoverflow.com/
					//    questions/2890700/backreferences-
					//    syntax-in-replacement-strings-
					//    why-dollar-sign
					html.append(p.matcher(i).replaceAll(
						"<a href=\"$1\">$2</a>)"));
					html.append('\n');
				}
				break;
			default:
				throw new NotImplementedException();
			}
			if(m == LineMode.END)
				break;
		}
		return encodeResult(html.toString(), css.toString(), lang);
	}

	private static DRS encodeResult(final String html, final String css,
								String lang) {
		final String htmlName = lang + ".html";
		final String cssName = lang + ".css";
		return new DRS() {
			@Override
			public D5Stream openResource(String name)
							throws IOException {
				D5Stream ret = super.openResource(name);
				return ret == null? new StringD5Stream(css):
									ret;
			}
			@Override
			public D5Stream openFirst() {
				return new StringD5Stream(html);
			}
			@Override
			public String getFirstResourceName() {
				return htmlName;
			}
			@Override
			public Iterable<String> getOtherResourceNames() {
				return new ConstantIterable<String>(cssName);
			}
		};
	}

	@Override
	public void close() throws IOException {
		try {
			vimStdoutDevNullPipe.interrupt();
			immediatelySend("ZQ");
			vimStdinControl.close();
			vimStderrData.close();
			vimStdoutDevNullPipe.join();
		} catch(InterruptedException ex) {
			throw new IOException(ex);
		} finally {
			super.close();
		}
		vimStdoutDevNullPipe.failIfErrorsArePresent();
	}

}
