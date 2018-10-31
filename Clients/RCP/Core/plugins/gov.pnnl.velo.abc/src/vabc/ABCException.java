package vabc;

/**
 * Class to denote internal exceptions that represent an error that should be reported to the user.  
 * If thrown, there is something wrong with the 
 * description of the UI.  This is typically a bug and the user should be directed to
 * report the problem.
 * 
 * @author karen
 *
 */
public class ABCException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6189256794578477387L;

	public ABCException(String message) {
		super(message);
	}

  public ABCException(String message, Throwable cause) {
    super(message, cause);
  }}
