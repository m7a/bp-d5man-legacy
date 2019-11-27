package ma.d5man.export.tex;

import java.util.Arrays;
import java.util.StringTokenizer;
import ma.tools2.util.HexUtils;

/**
 * Derived from ma.tools.gui.HexViewer from Tools 2.
 *
 * @author Linux-Fan, Ma_Sys.ma
 */
class XT2HXV {

	static String work(byte[] data) {
		String binary = formatBytesHexadecimal(data);
		String textv = formatBytesAsText(data);
		StringTokenizer lines1 = new StringTokenizer(binary, "\n");
		StringTokenizer lines2 = new StringTokenizer(textv, "\n");
		StringBuilder output = new StringBuilder();
		while(lines1.hasMoreElements()) {
			String cline = lines1.nextToken();
			output.append(cline);
			if(!lines1.hasMoreElements()) {
				char[] spc = new char[80 - cline.length()];
				Arrays.fill(spc, ' ');
				output.append(new String(spc));
			}
			output.append(lines2.nextToken());
			output.append('\n');
		}
		
		return output.toString();
	}

	private static String formatBytesHexadecimal(byte[] data_bytes) {
		StringBuilder data_hex = new StringBuilder();
		for(int i = 0, chars = 0; i < data_bytes.length; i++,
								chars += 4) {
			if((chars % 80) == 0 && i != 0)
				data_hex.append('\n');
			data_hex.append(' ');
			data_hex.append(HexUtils.formatAsHex(data_bytes[i]));
			data_hex.append(' ');
		}
		
		return data_hex.toString();
	}

	private static String formatBytesAsText(byte[] data_bytes) {
		StringBuilder data_text = new StringBuilder(" ");
		for(int i = 0; i < data_bytes.length; i++) {
			if((i % 20) == 0 && i != 0) {
				data_text.append(" \n ");
			}
			if(Character.isLetterOrDigit(data_bytes[i])) {
				char character = (char)data_bytes[i];
				if(character == '\n' || character == '\r' ||
							character == '\t') {
					data_text.append(' ');
				} else {
					data_text.append(character);
				}
			} else {
				data_text.append('.');
			}
		}
		return data_text.toString();
	}

}
