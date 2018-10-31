package abc.rules;

import datamodel.DataItem;
import abc.units.ABCUnitFactory;
import abc.validation.ABCDataItem;
import abc.validation.ABCDoubleItem;
import abc.validation.ABCIntegerItem;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.Function;
import com.fathzer.soft.javaluator.Parameters;
import com.fathzer.soft.javaluator.StaticVariableSet;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;



/**
 * A rule that provides a numerical calculation expression
 * I subclassed this so we could do things like add validation that the expression is numerical
 * 
 * @author karen
 *
 */
public class ABCCalculationRule extends ABCExpressionRule {
	
	public ABCCalculationRule(String fieldKey, String expression, String trigger, ABCRuleEvaluator functionProvider) {
		 super(fieldKey, expression, trigger, functionProvider);
	}

	@Override
	protected void parseTriggers(String expression) {
		// Start with supporting *,/,-,+,^.  Can add more later
		String delims = "[+\\-*/\\^ ()]";
		String[] parts = expression.split(delims);
		
		for (int idx=0; idx<parts.length; idx++) {
			if (parts[idx].length() == 0) continue;
			parts[idx] = parts[idx].trim();
			char c = parts[idx].charAt(0);
			if (c =='$') {
			  userVariables.add(parts[idx]);
			} else if (isFunction(parts[idx])) {
			  functions.add(parts[idx]);
			} else if (c >='a' && c <= 'z' || c >='A' && c <= 'Z' || c=='_') {   // this weeds out numbers etc - sub optimal
			  if (isFunction(parts[idx])) 
			    continue;
			  if (!triggerKeys.contains(parts[idx])) triggerKeys.add(parts[idx]);
			}
		}
	}
	
	/**
	 * Determine if a string literal token is a function name.
	 * if false is returned, the string will be considered to be a key for a UI XML object.
	 * @param token
	 * @return
	 */
	public boolean isFunction(String token) {
	  if (functionProvider != null) {
	    for (String func: functionProvider.getRuleFunctions()) {
	      if (func.equals(token)) {
	        return true;
	      }
	    }
	  }
	   return new ABCDoubleEvaluator().isFunction(token);
	}

	/**
	 * Overrides in order to convert units.
	 */
	@Override
	public String evaluate(Map<String,DataItem> variables)  {
	  Parameters params = DoubleEvaluator.getDefaultParameters();
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

	  
		ABCDoubleEvaluator eval = new ABCDoubleEvaluator(params,functionProvider);
		StaticVariableSet<Double> dvariables = new StaticVariableSet<Double>();
		
		// Transfer variable data to their data structure.  Units have to be converted on numeric fields
		for (String key: variables.keySet()) {
			DataItem p = variables.get(key);
			if (p.getValue() == null) return ""; //continue;
			if (p instanceof ABCDoubleItem || p instanceof ABCIntegerItem ) {
			  ABCDataItem abcp = (ABCDataItem)p;
			  double value = ABCUnitFactory.getABCUnits().convertValue(p.getValue(), p.getUnit(), abcp.getDefaultUnit()).asDouble();	
			   dvariables.set(key, value);
			} else {
			   dvariables.set(key, Double.parseDouble(p.getValue()));
			}
		}

		Double result = eval.evaluate(expression, dvariables);
		return result.toString();
	}

}
