package abc.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;

import com.fathzer.soft.javaluator.Function;

import vabc.ABCConstants;
import vabc.IABCDataProvider;
import vabc.ABCDocument;
import vabc.IABCErrorHandler;
import vabc.ABCException;
import vabc.ABCConstants.Key;
import datamodel.DataItem;
import abc.containers.ABC;
import abc.containers.ABCComponent;
import abc.units.ABCUnitFactory;
import abc.validation.ABCDataItem;
import abc.validation.ABCDoubleItem;
import abc.validation.ABCIntegerItem;



/**
 * This class parses rules from XML, evaluates them, and takes action on the UI.
 * To add custom rules: 
 *    ABCExpressionRule
 *    Override parseRules
 *    Override evaluate methods
 * @author karen
 *
 */
public class ABCRuleEvaluator {
  
  protected Map<String, Set<DataItem>>  delayedEventQueue ; 

  protected Map<String, List<ABCExpressionRule>>  triggerMap; 

	// if true all rules evaluate as soon as event occurs. if false, caller must forcibly 
	// request rule evaluation. For apps that use an apply CB and only pertains across ABC components
  protected boolean autoEvaluate = true;
  
  protected boolean debug = false;

	// Used to look for cylces in rules so we can break the cycle.
	protected Set<String> ruleTrace = new HashSet<String>();

	// For providing variables of the form $name from rule expressions
	protected IABCDataProvider dataHandler;
	
  protected ABCDocument abcdoc;

  public ABCRuleEvaluator(ABCDocument abcdoc) {
    delayedEventQueue = new HashMap<String,Set<DataItem>>();
    triggerMap = new HashMap<String,List<ABCExpressionRule>>();
    this.abcdoc = abcdoc;
  }
  
  public void setABCDocument(ABCDocument abcdoc) {
    this.abcdoc = abcdoc;
  }
  
  public boolean isTrigger(String key) {
    return triggerMap.get(key) != null;
  }

  public boolean isTarget(String key) {
    for (String trigger: triggerMap.keySet()) {
      List<ABCExpressionRule> thisrules = triggerMap.get(trigger);
      if (thisrules != null) {
        for (ABCExpressionRule rule: thisrules) {
          if (rule.getFieldKeys() != null && rule.getFieldKeys().contains(key)) {
            return true;
          }
          
        }
      }
    }
    return false;
  }


  public Set<String> getKeys() {
    return triggerMap.keySet();
  }
  
  public void setAutoEvaluate(boolean autoEvaluate) {
    this.autoEvaluate = autoEvaluate;
  }
  public boolean isAutoEvaluate() {
    return this.autoEvaluate;
  }
  
  public void setDataHandler(IABCDataProvider handler) {
    this.dataHandler = handler;
  }


  /**
   *  Add key to stack to avoid circular rules
   * @param key
   */
	void traceTrigger(String key) {
		ruleTrace.add(key);
	}
  /**
   *  Remove key to stack to avoid circular rules
   * @param key
   */
	void removeTraceTrigger(String key) {
		ruleTrace.remove(key);
	}
	boolean isCircular(String key) {
		if (ruleTrace.contains(key))
			return true;
		return false;
	}

	/**
	 * Gets rules associated with a "trigger" aka variables in an expression
	 * @param key
	 * @return
	 */
  public List<ABCExpressionRule> getRules(String key) {
    List<ABCExpressionRule> rules = triggerMap.get(key);
    if (rules == null) {
      rules = triggerMap.get(key.replace(" ","_"));

    }
    return rules;
  }

	/**
	 * Gets rules associated with a "target" aka the key attribute of the rule
	 * @param key
	 * @return
	 */
  public List<ABCExpressionRule> getRulesForTarget(String key) {
    // May want to make a map or other fast access data structure to find rules 
    // this way now that the design has changed
    List<ABCExpressionRule> rules = new ArrayList<ABCExpressionRule>();
    for (String trigger: triggerMap.keySet()) {
      List<ABCExpressionRule> thisrules = triggerMap.get(trigger);
      if (thisrules != null) {
        for (ABCExpressionRule rule: thisrules) {
          if (rule.getFieldKeys() != null && rule.getFieldKeys().contains(key)) {
            rules.add(rule);
          }
          
        }
      }
    }
    return rules;

  }

  public List<ABCExpressionRule> getRules() {
    List<ABCExpressionRule> rules = new ArrayList<ABCExpressionRule>();
    for (String key: triggerMap.keySet()) {
      rules.addAll(triggerMap.get(key));
    }
    return rules;
  }

  /**
   * Parse XML Node into a rule.
   * Subclasses can implement this to provide their own rules
   * @param node
   * @return
   */
  public ABCExpressionRule parseRule(Node node) {
    if (node.getNodeName().equals(ABCConstants.Key.CALCULATION)) {
      return parseCalculation(node);
    } else if (node.getNodeName().equals(ABCConstants.Key.LOGICAL_EXPRESSION)) {
      return parseContextRule(node);
    }
    return null;

  }

  public void addRule(ABCExpressionRule rule) {
    if (validateRule(rule)) {
      List<String> triggers = rule.getTriggerKeys();
      if (rule.hasExternalTriggers()) {
        // If we have an expresssio with variables or functions, triggering them is difficult.
        // This is my attempt to avoid the trigger= attribute being required of the user. Every
        // target will have itself added as a trigger in this case.
        triggers.addAll(rule.getFieldKeys());
      }
      for (String trigger: triggers) {
        List<ABCExpressionRule> rules = triggerMap.get(trigger);
        if (rules == null) {
          rules = new ArrayList<ABCExpressionRule>();
        }
        rules.add(rule);
        triggerMap.put(trigger, rules);
      }
    }
  }
  
  public boolean validateRule(ABCExpressionRule rule) {
    if (rule.getFieldKeys() == null) return true; // No way to validate these - maybe validate triggers?

    if (rule != null) {
      boolean okToAdd = true;
      for (String key: rule.getFieldKeys()) {
        if (!abcdoc.containsKey(key)) {
          okToAdd = false;
          System.err.println("WARNING: Rule field "+key+" not found.  Discarding rule.");
        }
      }
      // These may be hard to do especially if we end up adding functions.
      for (String key: rule.getTriggerKeys()) {
        if (!abcdoc.containsKey(key) && !getRuleFunctions().contains(key) && !key.startsWith("$")) {
          okToAdd = false;
          System.err.println("WARNING: Rule key "+key+" not found.  Discarding rule.");
        }
      }
      return okToAdd;
    }
    return false;
  }

  protected String getAttrValue(Node node, String key) {

    if (node.hasAttributes()) {
      NamedNodeMap attrs = node.getAttributes();
      Node match = attrs.getNamedItem(key);
      if (match != null)
        return ((Attr)match).getValue().trim();
    }
    return null;
  }

  protected ABCExpressionRule parseCalculation(Node node) {
    String fieldKey = getAttrValue(node, "key");
    String expression = getAttrValue(node, "expression");
    String trigger = getAttrValue(node, "trigger");
    return new ABCCalculationRule(fieldKey, expression, trigger, this);

  }

  protected ABCExpressionRule parseContextRule(Node node) {
    String fieldKey = getAttrValue(node, "key");
    String action = getAttrValue(node, "action");
    String value = getAttrValue(node, "value");
    String trigger = getAttrValue(node,"trigger");

    String expression="", message="";
    NodeList children = node.getChildNodes();
    for (int idx=0; idx<children.getLength(); idx++) {
      Node child = children.item(idx);
      if (child.getNodeName().equals("expression")) {
        expression = child.getTextContent().trim();
      } else if (child.getNodeName().equals("message")) {
        message = child.getTextContent().trim();
      }
    }
    if (!expression.isEmpty())
      return new ABCContextRule(fieldKey, expression, trigger, message, action, value, this);
    return null;
  }

  /**
   * When a field is created, we want to evaluate it immediately against any rules we have for it and evaluateTarget does that.
   */
	public void evaluateTarget(DataItem targetdi) {
	  evaluateTargetViaTriggers(targetdi);

	  // Block out the original approach of evaluating targets directly until the api is more consistent in its approach.
	  if (false) {
	    evaluateTarget(targetdi);
	  }
	}
	
	 /**
   * When a field is created, we want to evaluate it immediately against any rules we have for it and evaluateTarget does that.
   */
	public void realEvaluateTarget(DataItem targetdi) {
	  // Block out the approach of evaluating targets directly until the api is more consistent in its approach.
	  List<ABCExpressionRule> rules = getRulesForTarget(targetdi.getUnlinkedKey());
	  if (rules != null) {
	    for (ABCExpressionRule rule: rules) {

	      evaluateTarget(rule, targetdi);

	    }
	  }
	}
	
	public void evaluateTarget(ABCExpressionRule rule, DataItem targetdi) {
	  if (debug) System.out.println("\n***Evaluating target directly \""+ rule.getExpression() +"\" for target" + targetdi.getKey().getKey() + targetdi.getValue());
	  ABCComponent context = abcdoc.getEvaluationContext(targetdi);         
	  //if (context == null) System.err.println("Unable to find context of shouldObserve "+targetdi.getKey().getKey());
	  Map<String,DataItem> ruleVariables = abcdoc.getRuleExpressionVariables(rule, targetdi, context);

	  ruleVariables.putAll(getRuleUserVariables(rule));  // For the case where a user supplies a variable

	  if (rule instanceof ABCCalculationRule) {
	    // Setting the target if its an ABCDataItem does NOT work.
	    // So find the real data item target
	    DataItem realtarget = abcdoc.getTarget(targetdi.getKey().getKey(),context);
	    evaluateCalcuationRule((ABCCalculationRule)rule, realtarget, ruleVariables, context);
	    //evaluateCalcuationRule((ABCCalculationRule)rule, targetdi, ruleVariables, context);
	  } else if (rule instanceof ABCContextRule) {
	    ABC ruleTop = abcdoc.findABC(targetdi.getKey().getKey());  // This is the ABC object that contains the triggering component
	    ABC provider =  (ruleTop instanceof ABC) ? (ABC)ruleTop: null;
	    evaluateContextRule((ABCContextRule)rule, targetdi.getKey().getKey(), ruleVariables, context, provider);

	  }

	}
	
	
	/**
	 * Evaluate a target (via a create/add) by processing its triggers.
	 * This is an alternative way to handle create and rule processing.  The problem with evaluating targets directly is 
	 * that some targets can be key'd items like tabs that won't actually have an ABC item to process.
	 * @param targetdi
	 */
	public void evaluateTargetViaTriggers(DataItem targetdi) {
	  List<ABCExpressionRule> rules = getRulesForTarget(targetdi.getUnlinkedKey());
	  if (rules != null) {
	    for (ABCExpressionRule rule: rules) {
	      if (rule.hasExternalTriggers()) {
	        // This is a hack to get by.  If there are functions or variables, the GHU there probably aren't also
	        // choice menu variables.
	        evaluateTarget(rule, targetdi);
	        continue;
	      }
        List<String> triggers = rule.getTriggerKeys();
        
        for (String trigger: triggers) {
          DataItem triggerdi = null;
	        ABCComponent context = abcdoc.getEvaluationContext(targetdi);	        
	        // If the target has the :uuid appeneded, we MAY need to append the same to find the trigger
	        // Try that first at least to see if its an object in the same expandedlist
	        String []parts = targetdi.getKey().getKey().split(":");
	        if (parts.length>1) triggerdi = abcdoc.getDataItem(trigger+":"+parts[1], context);
          if (triggerdi == null) triggerdi = abcdoc.getDataItem(trigger, context);

          if (triggerdi != null) evaluateTrigger(triggerdi,true);
          
        }

	    }
	  }
	}

	/**
	 * Forwards call to the main evaluateTrigger passing in the flag that controls whether or not cross component rules
	 * should be evaluated immediately.
	 * @param triggerdi
	 */
	public void evaluateTrigger(DataItem triggerdi ) {
	  evaluateTrigger(triggerdi,autoEvaluate);
	}

	/**
	 * 
	 * @param triggerdi
	 * @param evaluateAll if false, only evaluate triggers with targets on same window
	 */
	public void evaluateTrigger(DataItem triggerdi, boolean evaluateAll) {
		ABC triggerTop = abcdoc.findABC(triggerdi.getKey().getKey());  // This is the ABC object that contains the triggering component
		if (triggerTop == null) {
		  return; // Launched from tool sets?
		//	throw new ABCException("Oh Oh - can't find containing ABC: " +triggerdi.getKey().getKey());
		}

		// Get all the rules associated with this trigger.
		// An example of multiple rules would be many fields disabled/enabled based on the value of a choice menu
		List<ABCExpressionRule> rules = getRules(triggerdi.getUnlinkedKey());

		if(rules == null) {
			return; // No rules on this component
		}

    ABCComponent context = abcdoc.getEvaluationContext(triggerdi);


		for (int idx=0; idx<rules.size(); idx++) {

			ABCExpressionRule rule = rules.get(idx);
			
			// We evaluate the rule for each field.  This is necessary since rules aren't necessarily applied
			// immediately across components (ie apply buttons).  This could be optimized so that the expression
			// is only evaluated one time though.
			for (String fieldKey: rule.getFieldKeys())  {
			  
			  // Need to create the unique key used internally for sets and lists
			  if(triggerdi.getLinkedObject() != null) {
			    fieldKey += ABCConstants.Key.UUID_SEPERATOR + triggerdi.getLinkedObject();
			  }


			  // TODO Want target to be able to be null some day for rules that don't specify actions on a field
				DataItem target = abcdoc.getTarget(fieldKey,context);
				if (target == null) {
				  // Ohoh in this case we have a field but no target.  Might be a choice menu so I'll great a bugus
				}


				// If we've been evaluating this already, we have a cycle so don't keep going
				if (target != null && isCircular(target.getKey().getKey())) continue;
			  
				// Ignore rules that Span ABC components if we aren't in autoEvaluate mode
				ABC ruleTop = abcdoc.findABC(fieldKey);  // This is the ABC object that contains the triggering component
				if (ruleTop != triggerTop && ruleTop != null) {
					if (!evaluateAll) {
					  queueEvent(triggerTop, triggerdi);
					  continue;
					}
				}

	      // Add to stack for detecting/breaking cycles in rules.
		    traceTrigger(triggerdi.getKey().getKey());

		    if (debug) System.out.println("\n***Evaluating expression \""+ rule.getExpression() +"\" with trigger: " + 
		                            triggerdi.getKey().getKey()+"->" + triggerdi.getValue() + " in " + triggerTop.getName() +
		                            " for field " +fieldKey);

	      Map<String,DataItem> ruleVariables = abcdoc.getRuleExpressionVariables(rule, triggerdi,context);
	      ruleVariables.putAll(getRuleUserVariables(rule));
	      	      
				if (rule instanceof ABCCalculationRule) {
	        evaluateCalcuationRule((ABCCalculationRule)rule, target, ruleVariables, context);

				} else if (rule instanceof ABCContextRule) {
				  ABC provider =  (ruleTop instanceof ABC) ? (ABC)ruleTop: null;
	        evaluateContextRule((ABCContextRule)rule, fieldKey, ruleVariables, context, provider);
				}
		   removeTraceTrigger(triggerdi.getKey().getKey());
			}
		}		
	}

	protected boolean evaluateContextRule(ABCContextRule rule, String targetName, Map<String,DataItem> ruleVariables, ABCComponent context, ABC provider) {
	  String result = "false";  // If exception occurs, consider it false
	  try { 
	    result = rule.evaluate(ruleVariables);
	    //System.out.println("rule eval is: "+result);
	  } catch (NumberFormatException nex) {
	    // This happens if the user types in a non numeric value
	    // If we can not call rules in this case, then pass the exception on...
	    // But we break because we don't want to update the UI (or do we???)
	    return false;
	  } catch (Exception ex) {
	    ex.printStackTrace();
	    System.err.println("Rule warning: "+ex.getMessage());
	    // It may not be possible to ensure that all DataItems exist at this time...
	    //throw new ABCException("Exception evaluating rule: "+rule.getFieldKey(), ex);
	  }
	  boolean bresult = Boolean.valueOf(result);


  	if (debug) System.out.println("Rule evaluated to: "+result);
	  if (rule.getAction().equals(ABCContextRule.CONTEXTRULE_ENABLE)) {
	    abcdoc.enable(targetName,bresult,context);
	  } else if (rule.getAction().equals(ABCContextRule.CONTEXTRULE_DISABLE)) {
	    abcdoc.enable(targetName,!bresult,context);
	  } else if (rule.getAction().equals(ABCContextRule.CONTEXTRULE_SHOW)) {
	    abcdoc.show(targetName,bresult,context);
	  } else if (rule.getAction().equals(ABCContextRule.CONTEXTRULE_HIDE)) {
	    abcdoc.show(targetName,!bresult,context);
	  } else if (rule.getAction().equals(ABCContextRule.CONTEXTRULE_SET)) {
	    if (bresult) {
	      abcdoc.setValue(targetName, rule.getValue(), context);
	    }
	  } else if (rule.getAction().equals(ABCContextRule.CONTEXTRULE_WARN)) {		
	    if(provider != null) {
	      if (bresult) {
	        List<String> msg = new ArrayList<String>();
	        msg.add(rule.getMessage());
	        provider.pushErrors(targetName.split(ABCConstants.Key.UUID_SEPERATOR)[0]+rule.getId(), msg);
	       // System.out.println("pushError"+targetName.split(ABCConstants.Key.UUID_SEPERATOR)[0]+rule.getId());
	      } else {
	        provider.clearErrors(targetName.split(ABCConstants.Key.UUID_SEPERATOR)[0]+rule.getId());
	      //  System.out.println("clarError"+targetName.split(ABCConstants.Key.UUID_SEPERATOR)[0]+rule.getId());
	      }
	    } else {
	      return false;
	    }
	  }

	  return true;
	}
	
	protected boolean evaluateCalcuationRule(ABCCalculationRule rule, DataItem target, Map<String,DataItem> ruleVariables, ABCComponent context) {
	  if (rule instanceof ABCCalculationRule) {
	    String result = null;
	    try { 
	      result = rule.evaluate(ruleVariables);
	      if (debug) System.out.println("Calculation evaluated to "+result);
	    } catch (NumberFormatException nex) {
	      // This happens if the user types in a non numeric value
	      // If we can not call rules in this case, then pass the exception on...
	      // But we break because we don't want to update the UI (or do we???)
	      return false;  
	    } catch (java.lang.IllegalArgumentException iaex) {
	      // Maybe not craeated yet
	      return false;  
	    } catch (Exception ex) {
	      throw new ABCException("Exception calculating rule: "+ex.getMessage() + target.getKey().getKey(), ex);
	    }
	    if (result.isEmpty()) return false;

	    if (target != null) { 
	      if (target instanceof ABCDoubleItem || target instanceof ABCIntegerItem ) {
	        ABCDataItem abcp = (ABCDataItem)target;
	        double value = ABCUnitFactory.getABCUnits().convertValue(result, target.getUnit(), abcp.getDefaultUnit()).asDouble();  
	        target.setValue(Double.toString(value), rule);
	      } else {
	        target.setValue(result, rule);
	      }
	    } else {
	      //throw new ABCException("Unable to find field to set: "+target.getKey().getKey());
	      System.err.println("Unable to find field to set: "+target.getKey().getKey());
	    }
	  }
	  return true;

	}
	
	protected Map<String,DataItem> getRuleUserVariables(ABCExpressionRule rule) {
	  Map<String,DataItem> vars = new HashMap<String,DataItem>();
	  if (dataHandler != null) {
	    for (String var: rule.getUserVariables()) {
	      String value = getRuleVariable(var);  //null; //dataHandler.getRuleVariable(var);
	      if (value != null) {
	        DataItem di = new DataItem(var,value);
	        vars.put(di.getKey().getKey(), di);
	      }
	    }
	    
	  }
	  return vars;

	}

	/**
	 * Keep track of events that have delayed processing.
	 * They must be tied to the ABC component so that they can be executed per component
	 * @param triggerTop
	 * @param triggerdi
	 */
	private void queueEvent(ABC triggerTop, DataItem triggerdi) {
	  Set<DataItem> list = delayedEventQueue.get(""+triggerTop.hashCode());
	  if (list == null) {
	    list = new HashSet<DataItem>();
	    delayedEventQueue.put(""+triggerTop.hashCode(), list);
	  }
	  if (list.contains(triggerdi)) {
	    System.out.println("remvoing duplicate");
	    list.remove(triggerdi);
	  }
	  System.out.println("Adding to queue"+triggerdi.getKey().getKey());
	  list.add(triggerdi);
	}
	
	public void processQueuedEvents(ABC component) {
	  Set<DataItem> dataitems = delayedEventQueue.get(""+component.hashCode());
	  if (dataitems != null) {
	    for (DataItem di: dataitems) {
	      evaluateTrigger(di, true);
	    }
	    dataitems.clear();
	    
	  }
	  
	}
	public void clearQueuedEvents(ABC component) {
	  Set<DataItem> dataitems = delayedEventQueue.get(""+component.hashCode());
	  if (dataitems != null) {
	    dataitems.clear();
	  }
	}


  protected void dump() {
    for (String key: triggerMap.keySet()) {
      List<ABCExpressionRule> rules = triggerMap.get(key);
      for (ABCExpressionRule rule: rules) {
        for(String fieldKey : rule.getFieldKeys()) {
          System.out.println("Field: "+ fieldKey + " triggers on " + key);
        }

      }
    }

  }
  
  /**
   * Returns the string value of a variable from a rule expression.
   * The type of the variable will be interpretted by the class in which it appears.  For example,
   * an ABCCaluculationRule will attempt to interpret it as a double.
   * @param variable of the form $somename as provided in the rule expression
   * @return
   */
  public String getRuleVariable(String variable) {
     return null;
  }

  /**
   * Subclasses should provide their own version of this static function.
   * Include the functions in this implementation to get standard ABCFunctions
   * e.g. include functions=ABCRuleEvaluator.getRuleFunctions()
   */
  public List<String> getRuleFunctions() {
    List<String> functions = new ArrayList<String>();
    functions.add("test");
    return functions;
  }

  
  public String test() {
    if (dataHandler != null) {
      return "5";
    }
    return "8";
  }

}
