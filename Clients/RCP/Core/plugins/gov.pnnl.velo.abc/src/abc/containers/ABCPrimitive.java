package abc.containers;

import java.awt.Component;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import datamodel.DataItem;
import datamodel.Key;
import datamodel.collections.DataItemMap;


/**
 * In case we want to replace the table back end,
 * new UI's need to implement this interface 
 * 
 * @author port091
 *
 */
public interface ABCPrimitive {

	// Create the UI from the node
	public Component initialize(Node node);
	
	// Rebuild from scratch?
	public void rebuild();
	
	// Clear user data, replace with defaults
	public void setDefaults();
	
	// Extract any errors in the UI
	public Map<String, List<String>> getErrors();

	// UI modifiers, key can be null
	public boolean show(Key key, boolean shouldShow);
	public boolean enable(Key key, boolean shouldEnable);
  public boolean readOnly(Key key, boolean isReadOnly);
	public boolean select(Key key, boolean shouldSelect);
	public boolean exists(Key key);
	public DataItem find(Key key);
	
	public boolean isActive(Key key); // is enabled and is showing
	public boolean isSelected(Key key); // is currently selected, get selected?	
	
	// Primitives will have only collections
	public DataItemMap getData();
		
	// For transfering focus when tabbing
	public void transferFocusToFirst();
	public void transferFocusToLast();

	
}
