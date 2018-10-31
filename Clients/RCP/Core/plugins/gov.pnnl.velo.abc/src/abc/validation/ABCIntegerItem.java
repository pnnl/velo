package abc.validation;

import java.util.ArrayList;
import java.util.List;

import vabc.ABCConstants.Key;
import vabc.NodeWrapper;
import abc.units.ABCUnitFactory;
import datamodel.DataItem;


public class ABCIntegerItem extends ABCDataItem {

	private static final long serialVersionUID = 1L;
	
	private Integer absoluteMin;
	private Integer absoluteMax;
	private Integer suggestedMin;
	private Integer suggestedMax;
	
	public ABCIntegerItem(NodeWrapper nodeWrapper, DataItem dataItem) {
	  super(nodeWrapper, dataItem);
    initialize(nodeWrapper);
	}

	@Override
	public void initialize(NodeWrapper nodeWrapper) {

	  absoluteMin = nodeWrapper.getInteger(Key.ABSOLUTE_MIN);
	  absoluteMax = nodeWrapper.getInteger(Key.ABSOLUTE_MAX);
	  suggestedMin = nodeWrapper.getInteger(Key.SUGGESTED_MIN);
	  suggestedMax = nodeWrapper.getInteger(Key.SUGGESTED_MAX);

	}


  @Override
  public boolean isDefaultValue() {
    if(getDefaultValue() == null || getValue() == null)
      return false; // No default set or no value to check
    try {
      double value = Integer.parseInt(getValue());        
      if(getDefaultUnit() != null && !getDefaultUnit().isEmpty() &&
          getUnit() != null && !getUnit().isEmpty() && !getUnit().equals(getDefaultUnit())) {
        try {
          value = ABCUnitFactory.getABCUnits().convertValue(getValue(), getUnit(), getDefaultUnit()).asDouble();          
          // Convert to the default unit and compare agains the default value          
        } catch (Exception ex) {
          System.err.println("Another unit issue: " +getUnit() + " " + getDefaultUnit());
        }
      }      
      return (Double.compare(value, Integer.parseInt(getDefaultValue())) == 0);      
    } catch (NumberFormatException e) {
      return false;
    }    
  }

	@Override
	public List<String> validate() {

		List<String> errors = new ArrayList<String>();
		
		if(isRequired() && (getValue() == null || getValue().isEmpty())) {
			errors.add("Error: " + getAlias() + " is a required parameter, the value cannot be empty");
		}

		if(getValue() == null || getValue().isEmpty() || isFile())
			return errors;
		
		try {
			int value = Integer.parseInt(getValue());
			
			if(getDefaultUnit() != null) {
				value = ABCUnitFactory.getABCUnits().convertValue(getValue(), getUnit(), getDefaultUnit()).asInteger();
			}

			// Validate bounds
			if(absoluteMin != null && value < absoluteMin) {
				errors.add("Error: " +  getAlias()  + " must be greater than " + absoluteMin + ", current value is " + value);
			}

			if(absoluteMax != null && value > absoluteMax) {
				errors.add("Error: " +  getAlias()  + " must be less than " + absoluteMax + ", current value is " + value);
			}

			if(suggestedMin != null && value < suggestedMin) {
				errors.add("Warning: the suggested minimum value for " +  getAlias()  + " is " + suggestedMin + ", current value is " + value);
			}

			if(suggestedMax != null && value > suggestedMax) {
				errors.add("Warning: the suggested maximum value for " +  getAlias()  + " is " + suggestedMax + ", current value is " + value);
			}
		
		} catch (NumberFormatException e) {
			  errors.add("Error: " +  getAlias()  + " is expecting a numerical value, current value is " + getValue());
		}
		
		return errors;
	}

}
