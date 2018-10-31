package abc.validation;

import java.util.ArrayList;
import java.util.List;

import vabc.ABCConstants;
import vabc.ABCConstants.Key;
import vabc.NodeWrapper;
import datamodel.DataItem;

public class ABCStringItem extends ABCDataItem {

	private static final long serialVersionUID = 1L;

	private String regex;
	private boolean listItem;
	
	public ABCStringItem(NodeWrapper nodeWrapper, DataItem dataItem) {
		super(nodeWrapper, dataItem);
		this.listItem = nodeWrapper == null;
    initialize(nodeWrapper);
	}

	@Override
	public void initialize(NodeWrapper nodeWrapper) {

	  if(nodeWrapper != null) {
	    regex = nodeWrapper.getString(Key.REGEX);	 
	  }	  
	}

	@Override
	public List<String> validate() {
		
	  
		List<String> errors = new ArrayList<String>();
		if(listItem)
		  return errors;
		
		if(isRequired() && (getValue() == null || getValue().isEmpty() || getValue().equals(ABCConstants.Key.NOT_SET))) {
			errors.add("Error: " +  getAlias()  + " is a required parameter, the value cannot be empty");
			return errors;
		}
		
		if(isFile())
		  return errors; // Nothing to validate?
		
		if(regex != null) {
			if(getValue() != null && !getValue().matches(regex)) {
				errors.add("Error: " +  getAlias()  + " does not match expression: " + regex);
			}
		}		
		
		if(isRequired()) {
			if(getValue().equalsIgnoreCase(ABCConstants.Key.NOT_SET))
				errors.add("Error: " +  getAlias()  + " is a required parameter, the value cannot be 'not set'");
		}
		
		return errors;
		
	}

	
}
