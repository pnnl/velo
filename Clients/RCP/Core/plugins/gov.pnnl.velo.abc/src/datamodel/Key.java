package datamodel;

/**
 * One idea to get our keys organized a bit
 * 
 * abc classes will extract Parameters with keys set based on the abc key
 * 
 * But we probably don't want use that key in the UI
 * And it's not the key used by amanzi or agni, so lets add a tool key too?
 *
 *
 */
public class Key extends NamedItem implements Comparable<Object> {

	private static final long serialVersionUID = 1L;
	
	// Changes that can occur in a key (although key should never change, it is final...)
  public final static String ALIAS = NamedItem.ALIAS;
	public final static String KEY = "key";
	
	public final String key;
		
	public Key(String alias) {
		this(alias.toLowerCase().replaceAll(" ", "_"), alias);
	}

	public Key(String key, String alias) {
	  super(alias != null ? alias : capitalize(key.replaceAll("_", " ")));
		this.key = key != null ? key : alias.toLowerCase().replaceAll(" ", "_");
	}
	public String getKey() {
		return key;
	}
	
	/**
	 * Convenience function that converts a label (or alias) to key form using the default algorithm.
	 * This is intended to help with cases where we have mixed use of keys and labels as left over artifacts.
	 * @param label
	 * @return
	 */
	public static String toAlias(String label) {
		return label != null ? label.toLowerCase().replaceAll(" ", "_") : null;
	  
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public String toString() {
		return getAlias() == null ?  "" :  getAlias();
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof Key) {
			Key compareTo = (Key)object;
			return	compareTo.key.equalsIgnoreCase(key); 
		}
		if(object instanceof String) {
			String compareTo = (String)object;
			// Compare it against each thing, giving
			// order priority abcKey, toolKey, alias
			if(compareTo.equalsIgnoreCase(key))
				return true;
			//if(compareTo.equalsIgnoreCase(getAlias()))
			//	return true;
		}
		return false;
	}

	public int compareTo(Key key2) {
		return key.compareTo(key2.key);
	}
	
	public static String capitalize(String alias) {
		char[] capitalized = alias.toCharArray();
		for(int i = 0; i < alias.length(); i++) {
			if(i == 0 || capitalized[i - 1] == ' ')
				capitalized[i] = String.valueOf(capitalized[i]).toUpperCase().charAt(0);
		}
		return new String(capitalized);
	}

	@Override
	public int compareTo(Object object) {
		// Compare based on alias
		if(object instanceof Key) {
			Key compareTo = (Key)object;
			return	getAlias().compareTo(compareTo.getAlias());
		}
		if(object instanceof String) {
			String compareTo = (String)object;
			// Compare it against each thing, giving
			// order priority abcKey, toolKey, alias
			return getAlias().compareTo(compareTo);
		}
		// Can't do it :(
		return -1;
	}
}
