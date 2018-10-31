package vabc;

import org.w3c.dom.Node;

public interface IABCDataProvider {
  
	/**
	 * Return list of objects associated with the "type" attribute in xml file.
	 * @param dataType
	 * @return
	 */
	public Object[] getObjects(String dataType);

	/**
	 * Similar to getObject but for the attribute "disabled_items" in the xml file.
	 * @param dataType
	 * @return
	 */
	public Object[] getDisabledItems(String dataType);
	
	// Returns a nice UI label for an object
	public String getLabel(String objectIdentifier);

	/**
	 * Gets the Object associated with the identifier
	 * @param objectIdentifier
	 * @return
	 */
	public Object getObject(String objectIdentifier);
	public void removeObject(Object object); // If abc needs to remove a users object
  public void addObject(Object object); // If abc needs to copy an object
	
	// Returns an identifier for an object, used for data item's objectIdentifier
	public String getIdentifier(Object object);
	
	/**
	 * Determines if the specified xml element should be shown in the generated user interface.
	 * This method is a back door for cases where the xml rule sets are insufficient for example because
	 * the logic is based on information outside the generated UI.
	 * @param key - the key attribute for the ui item
	 * @return true if the ui item should be generated
	 */
	public boolean shouldShow(Node node);
	
	/**
	 * Returns the string value of a variable from a rule expression
	 * @param variable of the form $somename as provided in the rule expression
	 * @return
	 */
	//public String getRuleVariable(String variable);
	//public List<String> getRuleFunctions();
	
}
