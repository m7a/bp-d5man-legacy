package ma.d5man.lib.shr;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

/**
 * That much of a singleton should be allowed :)
 */
public class DateFormats {

	private static final SimpleDateFormat FMT_HUMAN = new SimpleDateFormat(
							"dd.MM.yyyy HH:mm:ss");

	private static final SimpleDateFormat FMT_HTTP = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");

	// -> http://www.w3.org/TR/NOTE-datetime
	// -> http://stackoverflow.com/questions/4099862/convert-java-
	// 			date-into-xml-date-format-and-vice-versa
	private static final SimpleDateFormat FMT_SITEMAP =
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	static {
		FMT_HTTP.setTimeZone(TimeZone.getTimeZone("UTC"));
		FMT_HUMAN.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
		FMT_SITEMAP.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private DateFormats() {
		super(); // N_INVOC
	}

	// TODO z this is intended to be available via conf settings
	/*
	public static void applyTimezone(String tz) {
		FMT_HUMAN.setTimeZone(TimeZone.getTimeZone(tz));
	}
	*/

	public static String formatHuman(long date) {
		synchronized(FMT_HUMAN) {
			return FMT_HUMAN.format(new Date(date));
		}
	}

	public static String formatHTTP(long date) {
		synchronized(FMT_HTTP) {
			return FMT_HTTP.format(new Date(date));
		}
	}

	public static String formatSitemap(long date) {
		synchronized(FMT_SITEMAP) {
			return FMT_SITEMAP.format(new Date(date));
		}
	}

}
