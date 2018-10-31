package abc.rules;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.fathzer.soft.javaluator.BracketPair;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.Function;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;
import com.fathzer.soft.javaluator.DoubleEvaluator.Style;

/**
 * An example of extending the DoubleEvaluator to add functions - in this case sqrt.
 * The ABCCalculationRule creates this for rule evaluation.
 * @author karen
 *
 */
public class ABCDoubleEvaluator extends DoubleEvaluator {
  
  /** Defines the new function (square root) that takes 1 argument.*/
  private static final Function SQRT = new Function("sqrt", 1);
  private static final Function[] FUNCTIONS = new Function[] {SQRT};
  public static final Parameters PARAMS;
  ABCRuleEvaluator functionProvider;
  
  static {
    // Gets the default DoubleEvaluator's parameters
    PARAMS = DoubleEvaluator.getDefaultParameters();
    // add the new sqrt function to these parameters
    PARAMS.addFunctions(Arrays.asList(FUNCTIONS));
  }
 
  public ABCDoubleEvaluator() {
    super(PARAMS);
    functionProvider = null;
  }

  public ABCDoubleEvaluator(Parameters parameters, ABCRuleEvaluator functionProvider) {
    super(parameters);
    this.functionProvider = functionProvider;
  }
  
  public boolean isFunction(String name) {
    //Collection<Function> functions = DoubleEvaluator.getDefaultParameters().getFunctions();
    Collection<Function> functions = PARAMS.getFunctions();
    for (Function f: functions ) {
      if (f.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }
  
  
  /**
   * This converts literal numeric values from the expression string.
   * It does not have anything to do with variables so probably nothing to do with units here.
   */
  @Override
  protected Double toValue(String literal, Object evaluationContext) {
    Double result = super.toValue(literal, evaluationContext);
    return result.doubleValue();
  }
  
  /* (non-Javadoc)
   * @see net.astesana.javaluator.AbstractEvaluator#evaluate(net.astesana.javaluator.Operator, java.util.Iterator)
   */
  @Override
  protected Double evaluate(Operator operator, Iterator<Double> operands, Object evaluationContext) {
    if (NEGATE.equals(operator) || NEGATE_HIGH.equals(operator)) {
      return -operands.next();
    } else if (MINUS.equals(operator)) {
      return operands.next() - operands.next();
    } else if (PLUS.equals(operator)) {
      return operands.next() + operands.next();
    } else if (MULTIPLY.equals(operator)) {
      return operands.next() * operands.next();
    } else if (DIVIDE.equals(operator)) {
      Double first = operands.next();
      Double second = operands.next();
      return first / second;
      //return operands.next() / operands.next();
    } else if (EXPONENT.equals(operator)) {
      return Math.pow(operands.next(),operands.next());
    } else if (MODULO.equals(operator)) {
      return operands.next() % operands.next();
    } else {
      return super.evaluate(operator, operands, evaluationContext);
    }
  }
  
  @Override
  protected Double evaluate(Function function, Iterator<Double> arguments, Object evaluationContext) {
    if (functionProvider != null) {
      // Get method
      Method[] methods = functionProvider.getClass().getMethods();
      Method customMethod=null;
      for (Method method: methods) {
        if (method.getName().equals(function.getName())) {
          // TOdo make sure arg count matchs
          customMethod = method;
          break;
        }
      }
      try {
        // Support 0-3 arguments only.  Not sure how to do this better but shouldn't need more anyway.
        // We'll assume arg types and return type match for now.  The rule will fail but the app will
        // not be harmed.
        Object object = null;
        if (function.getMinimumArgumentCount() == 0) {
          object = customMethod.invoke(functionProvider);
        } else if (function.getMinimumArgumentCount() == 1) {
          object = customMethod.invoke(functionProvider,arguments.next());
        } else if (function.getMinimumArgumentCount() == 2) {
          object = customMethod.invoke(functionProvider,arguments.next(), arguments.next());
        } else if (function.getMinimumArgumentCount() == 3) {
          object = customMethod.invoke(functionProvider,arguments.next(), arguments.next(),arguments.next());
        }
        // If no legit value is returned we want to have it fire off an exception
        return Double.parseDouble(object.toString());
      } catch (Exception e) {
        // IllegalArgumentException IllegalAccessException InvocationTargetException
        e.printStackTrace();
      }
    }

    if (function == SQRT) {
      // Implements the new function
      return Math.sqrt(arguments.next());
    } else {
      // If it's another function, pass it to DoubleEvaluator
      return super.evaluate(function, arguments, evaluationContext);
    }
  }
  

}
