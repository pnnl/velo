package vabc;

import java.util.List;

public interface IABCErrorHandler {
	
	public void pushErrors(String key, List<String> errors);
	public void clearErrors(String key);	
	
}
