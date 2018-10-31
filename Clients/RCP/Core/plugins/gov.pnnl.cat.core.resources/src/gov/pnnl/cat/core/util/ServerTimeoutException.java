package gov.pnnl.cat.core.util;

/**
 * If the http server responds with 502 or 504 (Bad Gateway, Gateway Timeout), we need to convert it
 * to a ServerTimeoutException so it can be handled correctly in the UI error handler.
 * TODO: we need to create an upload polling service and run uploads in the background so we can
 * mitigate proxy/firewall timeouts for huge uploads
 * @author D3K339
 *
 */
public class ServerTimeoutException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ServerTimeoutException(String message) {
    super(message);
  }

}
