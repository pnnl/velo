package abc.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import datamodel.DataItem;
//import org.akuna.common.newvizui.datamodel.Parameter;

import com.fathzer.soft.javaluator.StaticVariableSet;

public class ABCExpressionRule {
	
	//String fieldKey;           // field that has the rule on it
	protected List<String> fieldKeys;
	protected List<String> triggerKeys = new ArrayList<String>();  // fields that trigger the rule evaluation
	protected List<String> userVariables = new ArrayList<String>(); // for expression variables of the form $somename
	protected List<String> functions = new ArrayList<String>(); // for functions
	String expression;         // the expression that will be evaluted
	protected ABCRuleEvaluator functionProvider; // for custom functions 
	protected String uuid;

	public ABCExpressionRule(String fieldKey, String expression, String trigger, ABCRuleEvaluator functionProvider) {
	  uuid = UUID.randomUUID().toString();
	  // keys can be null with new custom functions and triggers
	  if (fieldKey != null) {
	    String[] keys = fieldKey.split(",");
	    this.fieldKeys = new ArrayList<String>();
	    for (int idx=0; idx<keys.length; idx++)
	      fieldKeys.add(keys[idx].trim());
	  }

		// We use single = sign but don't force user to remember if they are a programmer
		this.expression = expression.replaceAll("==", "=");  
		//TODO call validate and throw runtime exception if error?
		this.functionProvider = functionProvider;
		parseTriggers(expression);
		if (trigger != null) {
		   String[] extraTriggers = trigger.split(",");
		   for (int idx=0; idx<extraTriggers.length; idx++) {
		     triggerKeys.add(extraTriggers[idx]);
		   }
		}
	}
	
	
	public String getId() {
	  return uuid;
	}
	
	public List<String> getTriggerKeys() {
		return triggerKeys;
	}
	
	public List<String> getFieldKeys() {
		return fieldKeys;
	}

	public List<String> getUserVariables() {
		return userVariables;
	}
	
	public String getExpression() {
		return expression;
	}
	
	/**
	 * Parse expressions to extract the triggers.
	 * The trigger keys will be the left side of operators such as =,>,>=,!=
	 * @param expression
	 */
	protected void parseTriggers(String expression) {
		System.err.println("Warning: default parseTriggers does nothing: " + expression);
	}
	
	/**
	 * Subclasses can validate based on the type of rule and syntax expected....
	 * @return
	 */
	public boolean validateExpression() {
		return true;
	}
	
	/**
	 * Evaluate the rule given the variables provided.
	 * TODO change to throw exception if variables don't match??
	 * Change parameter to DataItem
	 * TODO return DataItem??
	 * @param variables
	 * @return
	 */
	public String evaluate(Map<String,DataItem> variables) {
		return "TBD";
	}
	
	/**
	 * Returns true if the rule has variables and functions that effectively mean the rule has to be evaluated directly.
	 * @return
	 */
	public boolean hasExternalTriggers() {
	  return userVariables.size() > 0 || functions.size() > 0;
	}
	  
}
