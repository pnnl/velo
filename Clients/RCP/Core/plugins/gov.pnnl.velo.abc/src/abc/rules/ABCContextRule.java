package abc.rules;

//For the inner class


import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.Function;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;
import com.fathzer.soft.javaluator.StaticVariableSet;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import datamodel.DataItem;


public class ABCContextRule extends ABCExpressionRule {
  public static final String CONTEXTRULE_ENABLE = "enable";
  public static final String CONTEXTRULE_DISABLE = "disable";
  public static final String CONTEXTRULE_SHOW = "show";
  public static final String CONTEXTRULE_HIDE = "hide";
  public static final String CONTEXTRULE_WARN = "warn";
  public static final String CONTEXTRULE_SET = "set";

  String message;  // for action=warn

  String action;
  String value;  // for action=set only
  String cleanedExpression;  


  public ABCContextRule(String fieldKey, String expression, String trigger, String message, String action, String value, ABCRuleEvaluator functionProvider) {
    super(fieldKey, expression,trigger, functionProvider);
    this.message = message;
    this.action = action;
    this.value = value;
    this.cleanedExpression = cleanExpression(this.expression); // Clean it at the beginning for performance
  }

  /**
   * The javevaluator will pick out things like "or" even embedded in words.
   * To subvert this, the BooleanEvaluator delimits the keywords with '.'
   * We let the user skip this and automatically put them in so long as
   * they have whitespace.
   * Also no whitespace around allowed around comparators.
   */
  protected String cleanExpression(String expression) {
    String exp = expression;
    exp = exp.replaceAll("\\s*=\\s*","=");
    exp = exp.replaceAll("\\s*!=\\s*","!=");
    exp = exp.replaceAll("\\s*>=\\s*",">=");
    exp = exp.replaceAll("\\s*<=\\s*","<=");
    exp = exp.replaceAll("\\s*>\\s*",">");
    exp = exp.replaceAll("\\s*<\\s*","<");

    exp = exp.replace(" AND ",BooleanEvaluator.AND.getSymbol());
    exp = exp.replace(" and ",BooleanEvaluator.and.getSymbol());
    exp = exp.replace(" NOT ",BooleanEvaluator.NEGATE.getSymbol());
    exp = exp.replace(" not ",BooleanEvaluator.negate.getSymbol());
    exp = exp.replace(" OR ",BooleanEvaluator.OR.getSymbol());
    exp = exp.replace(" or ",BooleanEvaluator.or.getSymbol());
    return exp;

  }

  public boolean isFunction(String name) {
    if (functionProvider != null) {
      for (String func: functionProvider.getRuleFunctions()) {
        if (func.equals(name)) {
          return true;
        }
      }
    }
    return new BooleanEvaluator().isFunction(name);
  }

  public String getAction() {
    return action;
  }

  public String getValue() {
    return value;
  }

  public String getMessage() {
    return message;
  }

  @Override
  protected void parseTriggers(String expression) {
    
    // First lets parse this into individual expressions
    String delims = "(AND)|(OR)|(NOT)|(\\s+and\\s+)|(\\s+or\\s+)|(\\s+not\\s+)";
    String[] parts = expression.split(delims);
    // Now anything to the left of an operator is a trigger
    for (int idx=0; idx<parts.length; idx++) {
      String[] triggers = parts[idx].split("[\\(\\)<>!=]");
      String trigger = triggers[0].trim(); // only left side may be function, right side should be true/false
      if (trigger.isEmpty()) continue;
      char c = trigger.charAt(0);
      if (c == '$') {
        userVariables.add(trigger);
      } else if (isFunction(trigger)) {
        functions.add(trigger);
      } else if (c >='a' && c <= 'z' || c >='A' && c <= 'Z' || c=='_') {   // this weeds out numbers etc - sub optimal
        if (!triggerKeys.contains(trigger)) {
          triggerKeys.add(trigger);
        }
      }
      // Look for these anywhere.  Maybe should be doing that for functions too??
      // Maybe dead code??
//      for (String var: triggers) {
//        if (var.startsWith("$")) userVariables.add(var);
//      }
    }
  }

  @Override
  public String evaluate(Map<String,DataItem> variables)  {

    Parameters params = BooleanEvaluator.PARAMETERS;
    if (functionProvider != null) {
      List<String> functions = functionProvider.getRuleFunctions();
      Method[] methods = functionProvider.getClass().getMethods();
      for (Method method: methods) {
        if (functions.contains(method.getName())) {
          // TODO validate return type of string?
          int numArgs = method.getParameterTypes().length;
          Function f = new Function(method.getName(),numArgs);
          params.add(f);
        }
      }
    }

    StaticVariableSet<String> svariables = new StaticVariableSet<String>();
    for (String key: variables.keySet()) {
      DataItem p = variables.get(key);
      svariables.set(key, p.getValue());
    }


    //AbstractEvaluator<Boolean> evaluator = new TextAndNumericOperatorsEvaluator();
    BooleanEvaluator evaluator = new BooleanEvaluator(params,functionProvider);

    return evaluator.evaluate(cleanedExpression,svariables).toString();

  }

}
