package vabc;

public interface IABCObserver {

	/**
	 * Observers that implement this interface will be reference when deciding on
	 * whether or not a collections or recursive collections child DataItems will
	 * inherit the collections observer.
	 * 
	 * This is used then the collections expand dynamically at runtime
	 * 
	 * @param if the observer should observe the given key
	 * @return
	 */
	public abstract boolean shouldObserve(String key);
	
}
