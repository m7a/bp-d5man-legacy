package ma.tools2.xml;

import java.util.Deque;
import java.util.ArrayDeque;

public class MaBufXML {

	private static final int TW = 8;

	/** Rem: We won't get 80 anyway... */
	private static final int WRAP_LIMIT = 80;

	private static enum MaBufXMLMode {
		BLOCK, INLINE, RAW
	}

	private static class MaBufXMLElem {
		final MaBufXMLMode m;
		final String name;
		MaBufXMLElem(MaBufXMLMode m, String name) {
			this.m    = m;
			this.name = name;
		}
	}

	private final StringBuilder o;
	private final Deque<MaBufXMLElem> stack;
	private final StringBuilder holdback;
	private boolean escapingEnabled;
	private boolean holdbackEnabled;
	private int currentIndentation;
	private int currentPos;
	private boolean lastWasSpace;

	public MaBufXML(int initialIndentation) {
		super();
		o                  = new StringBuilder();
		stack              = new ArrayDeque<MaBufXMLElem>();
		holdback           = new StringBuilder();
		escapingEnabled    = true;
		holdbackEnabled    = false;
		currentIndentation = initialIndentation;
		currentPos         = initialIndentation * TW;
		lastWasSpace       = currentIndentation > 0;
	}

	public void beginBlock(String name) {
		stack.push(new MaBufXMLElem(MaBufXMLMode.BLOCK, name));
		autoIndent();
		lowlevelBeginTag(name);
	}

	private void autoIndent() {
		newline();
		indent();
	}

	public void newline() {
		if(currentPos != 0)
			putchar('\n');
	}

	private void putchar(char val) {
		switch(val) {
		case '\n': currentPos = 0;   break;
		case '\t': currentPos += TW; break;
		default:   currentPos++;     break;
		}

		lastWasSpace = isSpace(val);

		if(holdbackEnabled)
			holdback.append(val);
		else
			o.append(val);
	}

	private static boolean isSpace(char c) {
		return c == ' ' || c == '\t' || c == '\n';
	}

	private void indent() {
		for(int i = 0; i < currentIndentation; i++)
			putchar('\t');

		assert currentPos == TW * currentIndentation;
	}

	private void lowlevelBeginTag(String name) {
		putchar('<');
		puts(name);
	}

	private void puts(String s) {
		char[] data = s.toCharArray();
		for(char c: data)
			putchar(c);
	}

	public void beginInline(String name) {
		if(lastWasSpace)
			autoIndent();
		stack.push(new MaBufXMLElem(MaBufXMLMode.INLINE, name));
		lowlevelBeginTag(name);
	}

	public void beginRaw(String name) {
		stack.push(new MaBufXMLElem(MaBufXMLMode.RAW, name));
		lowlevelBeginTag(name);
	}

	public void attribute(String k, String v) {
		if(!lastWasSpace)
			putchar(' ');
		puts(k);
		puts("=\"");
		contentDirect(v, true);
		putchar('"');
	}

	public void open() {
		putchar('>');
		if(stack.peek().m == MaBufXMLMode.BLOCK) {
			currentIndentation++;
			newline();
		}
	}

	public void empty() {
		MaBufXMLMode m = stack.pop().m;
		puts("/>");
		if(m == MaBufXMLMode.BLOCK)
			newline();
	}

	public void openBlock(String name) {
		openBlock(name, new String[0][0]);
	}

	public void openBlock(String name, String[][] kv) {
		beginBlock(name);
		openKV(kv);
	}

	private void openKV(String[][] kv) {
		for(String[] entry: kv)
			attribute(entry[0], entry[1]);
		open();
	}

	public void openInline(String name) {
		openInline(name, new String[0][0]);
	}

	public void openInline(String name, String[][] kv) {
		beginInline(name);
		openKV(kv);
	}

	public void openRaw(String name) {
		openRaw(name, new String[0][0]);
	}

	public void openRaw(String name, String[][] kv) {
		beginRaw(name);
		openKV(kv);
	}

	public void close() {
		if(stack.isEmpty())
			throw new RuntimeException("Can not close element " +
							"beyond top level.");
		MaBufXMLElem e = stack.pop();
		if(e.m == MaBufXMLMode.BLOCK) {
			assert currentIndentation > 0;
			currentIndentation--;
			autoIndent();
		}
		puts("</");
		puts(e.name);
		putchar('>');
		if(e.m == MaBufXMLMode.BLOCK)
			newline();
	}

	public void xmlLine(String line) {
		autoIndent();
		puts(line);
	}

	public void content(String cnt) {
		contentDirect(cnt, isRaw());
	}

	private void contentDirect(String cnt, boolean israw) {
		char[] data = cnt.toCharArray();

		if(!israw && currentPos == 0)
			autoIndent();

		for(char c: data)
			contentChar(c, israw);
	}

	private void contentChar(char c, boolean israw) {
		if(escapingEnabled) {
			switch(c) {
			case '<': puts("&lt;");   return;
			case '>': puts("&gt;");   return;
			case '"': puts("&quot;"); return;
			case '&': puts("&amp;");  return;
			}
		}

		if(!israw && isSpace(c)) {
			if(currentPos >= WRAP_LIMIT) {
				newline();
				autoIndent();
			} else if(!lastWasSpace) {
				putchar(' ');
			}
		} else {
			putchar(c);
		}
	}

	public boolean isHoldback() {
		return holdbackEnabled;
	}

	public void setHoldback(boolean holdback) {
		holdbackEnabled = holdback;
	}

	public StringBuilder getHoldback() {
		return holdback;
	}

	public void flushHoldback() {
		assert !holdbackEnabled;
		boolean bakesc = escapingEnabled;
		escapingEnabled = false;
		content(holdback.toString());
		holdback.setLength(0); /* hack / there is no clear() method */
		escapingEnabled = bakesc;
	}

	public void setEscaping(boolean esc) {
		escapingEnabled = esc;
	}

	public String toString() {
		return o.toString();
	}

	public boolean isRaw() {
		return !escapingEnabled || (!stack.isEmpty() &&
					stack.peek().m == MaBufXMLMode.RAW);
	}

}
