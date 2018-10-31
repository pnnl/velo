package abc.rules;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.BracketPair;
import com.fathzer.soft.javaluator.Function;
import com.fathzer.soft.javaluator.Operator;
import com.fathzer.soft.javaluator.Parameters;
import com.fathzer.soft.javaluator.StaticVariableSet;

/**
 * An evaluator that can evaluate complex logical expressions such as:
 *   transport=On AND xx<5 etc
 *   
 * Not sure that the numeric comparators are very useful.
 *   
 * @author karen
 *
 */
public class BooleanEvaluator extends AbstractEvaluator<Boolean> {
  private ABCRuleEvaluator functionProvider;
  
  // A keyword used in the xml to check for an empty/blank field.
  private static String EMPTY_FIELD_CONSTANT = "empty";

  /** Defines the new function (square root) that takes 1 argument.*/
  // This was a test of functions in expressions.
  private static final Function IS2D = new Function("is2D", 1);

  // The javevaluator will pick out words as tokens even if surrounded by white space so
  // subvert this by using the old fortran hack.  That means that the caller of this method
  // can either have users use this notation or patch it before sending it in
  /** The negate unary operator. */
  public final static Operator NEGATE = new Operator(".NOT.", 1, Operator.Associativity.RIGHT, 3);
  public final static Operator negate = new Operator(".not.", 1, Operator.Associativity.RIGHT, 3);  // Can't use this do to conflict with not_set use in akuna
  /** The logical AND operator. */
  public static final Operator AND = new Operator(".AND.", 2, Operator.Associativity.LEFT, 2);
  public static final Operator and = new Operator(".and.", 2, Operator.Associativity.LEFT, 2);
  /** The logical OR operator. */
  public final static Operator OR = new Operator(".OR.", 2, Operator.Associativity.LEFT, 1);
  public final static Operator or = new Operator(".or.", 2, Operator.Associativity.LEFT, 1);

  public static final Parameters PARAMETERS;
  static {
    // Create the evaluator's parameters
    PARAMETERS = new Parameters();
    // Add the supported operators
    PARAMETERS.add(AND);
    PARAMETERS.add(OR);
    PARAMETERS.add(NEGATE);
    // These end up causing parsing issues in the underlying library (I think)
    PARAMETERS.add(or);
    PARAMETERS.add(and);
    PARAMETERS.add(negate);
    PARAMETERS.addFunctionBracket(BracketPair.PARENTHESES);
    PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);

    PARAMETERS.add(IS2D);
  }

  public BooleanEvaluator() {
    super(PARAMETERS);
    functionProvider = null;
  }

  public BooleanEvaluator(Parameters parameters, ABCRuleEvaluator functionProvider) {
    super(parameters);
    this.functionProvider = functionProvider;
  }

  public boolean isFunction(String name) {
    //Collection<Function> functions = DoubleEvaluator.getDefaultParameters().getFunctions();
    Collection<Function> functions = PARAMETERS.getFunctions();
    for (Function f: functions ) {
      if (f.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Evaluate an expression such as xx=yy x<4....
   * TODO Currently = and != are string comparisons and the others are numeric (double). 
   */
  @Override
  protected Boolean toValue(String literal, Object evaluationContext) {
    StaticVariableSet variables = (StaticVariableSet)evaluationContext;
    int index = literal.indexOf("<=");
    if (index>=0) {
      String lhs = literal.substring(0, index);
      String rhs = literal.substring(index+2);

      double rval = 0, lval=0;
      if (variables.get(rhs) != null)
         rval = Double.parseDouble((String)variables.get(rhs));
      else
         rval = Double.parseDouble(rhs);
      if (variables.get(lhs) != null)
         lval = Double.parseDouble((String)variables.get(lhs));
      else
         lval = Double.parseDouble(lhs);

      if (lval <= rval)
        return new Boolean(true);
      return new Boolean(false);
    }

    index = literal.indexOf(">=");
    if (index>=0) {
      String lhs = literal.substring(0, index);
      String rhs = literal.substring(index+2);
      double rval = 0, lval=0;
      if (variables.get(rhs) != null)
         rval = Double.parseDouble((String)variables.get(rhs));
      else
         rval = Double.parseDouble(rhs);
      if (variables.get(lhs) != null)
         lval = Double.parseDouble((String)variables.get(lhs));
      else
         lval = Double.parseDouble(lhs);
      if (lval >= rval)
        return new Boolean(true);
      return new Boolean(false);
    }

    index = literal.indexOf(">");
    if (index>=0) {
      String lhs = literal.substring(0, index);
      String rhs = literal.substring(index+1);
      double rval = 0, lval=0;
      if (variables.get(rhs) != null)
         rval = Double.parseDouble((String)variables.get(rhs));
      else
         rval = Double.parseDouble(rhs);
      if (variables.get(lhs) != null)
         lval = Double.parseDouble((String)variables.get(lhs));
      else
         lval = Double.parseDouble(lhs);

      if (lval > rval)
        return new Boolean(true);
      return new Boolean(false);
    }

    index = literal.indexOf("<");
    if (index>=0) {
      String lhs = literal.substring(0, index);
      String rhs = literal.substring(index+1);
      double rval = 0, lval=0;
      if (variables.get(rhs) != null)
         rval = Double.parseDouble((String)variables.get(rhs));
      else
         rval = Double.parseDouble(rhs);
      if (variables.get(lhs) != null)
         lval = Double.parseDouble((String)variables.get(lhs));
      else
         lval = Double.parseDouble(lhs);
      if (lval > rval)
        return new Boolean(true);
      return new Boolean(false);
    }
    
    // For = and !=, support keyword null for checking for empty fields

    index = literal.indexOf("!=");
    if (index>=0) {
      String variableName = literal.substring(0, index);
      String variable = (String) variables.get(variableName);
      if (variable == null || variable.isEmpty()) variable=EMPTY_FIELD_CONSTANT;
      String value = literal.substring(index+2);
      // TODO distinguish between numeric and string comparison
      return !value.equalsIgnoreCase(variable);
    }

    index = literal.indexOf('=');
    if (index>=0) {
      String variableName = literal.substring(0, index);
      String variable = (String) variables.get(variableName);
      if (variable == null || variable.isEmpty()) variable=EMPTY_FIELD_CONSTANT;
      String value = literal.substring(index+1);
      // TODO distinguish between numeric and string comparison
      return value.equalsIgnoreCase(variable);
    }

    return Boolean.valueOf(literal);
  }

  @Override
  protected Boolean evaluate(Operator operator,
      Iterator<Boolean> operands, Object evaluationContext) {
    if (operator == NEGATE) {
      return !operands.next();
    } else if (operator == OR || operator.equals(or)) {
      Boolean o1 = operands.next();
      Boolean o2 = operands.next();
      return o1 || o2;
    } else if (operator == AND || operator.equals(and)) {
      Boolean o1 = operands.next();
      Boolean o2 = operands.next();
      return o1 && o2;
    } else {
      return super.evaluate(operator, operands, evaluationContext);
    }
  }

  /*
  @Override
  protected Iterator<String> tokenize(String expression) {
    return Arrays.asList(expression.split("\\s")).iterator();
  }
  */
  
  @Override
  protected Boolean evaluate(Function function, Iterator<Boolean> arguments, Object evaluationContext) {
    
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
        Boolean b = Boolean.parseBoolean(object.toString());
        return new Boolean(b);
      } catch (Exception e) {
        // IllegalArgumentException IllegalAccessException InvocationTargetException
        e.printStackTrace();
      }
    }

    if (function == IS2D) {
      // Implements the new function
      return true;
    } else {
      // If it's another function, pass it to DoubleEvaluator
      return super.evaluate(function, arguments, evaluationContext);
    }
  }

}
