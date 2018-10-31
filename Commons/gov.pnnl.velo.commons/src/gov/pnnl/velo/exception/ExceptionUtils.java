package gov.pnnl.velo.exception;

import java.io.PrintWriter;
import java.io.StringWriter;


public class ExceptionUtils {

  public static String getFullStackTrace(final Throwable throwable) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw, true);
    throwable.printStackTrace(pw);
    return sw.getBuffer().toString();
  }
  
}
