package gov.pnnl.velo.core.util;

public class EmailConstants {
	private static String toEmail = "";
	private static String fromEmailDomain = "velo.org";

	public static String getToEmail() {
		return toEmail;
	}

	public static void setToEmail(String toEmail) {
		EmailConstants.toEmail = toEmail;
	}

	public static String getFromEmailDomain() {
		return fromEmailDomain;
	}

	public static void setFromEmailDomain(String fromEmailDomain) {
		EmailConstants.fromEmailDomain = fromEmailDomain;
	}

}
