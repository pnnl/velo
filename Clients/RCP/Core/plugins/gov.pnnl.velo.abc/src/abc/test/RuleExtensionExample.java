package abc.test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.Function;
import com.fathzer.soft.javaluator.Parameters;

import abc.rules.ABCContextRule;
import abc.rules.ABCExpressionRule;
import abc.rules.ABCRuleEvaluator;
import abc.rules.BooleanEvaluator;
import vabc.ABCConstants;
import vabc.ABCDocument;

/**
 * This is a self contained example of how to extend exisitng rule classes to add your
 * own functions - typically for consistency checking. Normally these would be separate classes
 * in which case the static final functions would be defined in the evaluator class.
 * @author karen
 *
 */
public class RuleExtensionExample extends ABCRuleEvaluator {

  // These should be in the MyBooleanEvaluator if it weren't an inner class
  private static final Function IS_IJK_MATERIAL = new Function("isIjkMaterial", 1);
  private static final Function[] FUNCTIONS = new Function[] {IS_IJK_MATERIAL};
  
  protected static final Parameters MYPARAMS;
  
  static {
    // Gets the default DoubleEvaluator's parameters
    MYPARAMS = BooleanEvaluator.PARAMETERS;
    // add the new sqrt function to these parameters
    MYPARAMS.addFunctions(Arrays.asList(FUNCTIONS));
  }

  public RuleExtensionExample(ABCDocument abcdoc) {
    super(abcdoc);
  }
  
  /**
   * Parse XML Node into a rule.
   * @param node
   * @return
   */
  @Override
  public ABCExpressionRule parseRule(Node node) {
    String fieldKey = getAttrValue(node, "key");
    String action = getAttrValue(node, "action");


    String expression;
    NodeList children = node.getChildNodes();
    for (int idx=0; idx<children.getLength(); idx++) {
      Node child = children.item(idx);
      if (child.getNodeName().equals("expression")) {
        expression = child.getTextContent();
        return new MyContextRule(fieldKey.trim(), expression.trim(), "", action);
      }
    }
    return null;
  }
  
  public class MyContextRule extends ABCContextRule {
    

      public MyContextRule(String fieldKey, String expression, String message, String action) {
        super(fieldKey, expression, null, message, action, null, null);
      }
      
      public boolean isFunction(String name) {
        return new MyBooleanEvaluator().isFunction(name);
      }
  }
  
    
  public class MyBooleanEvaluator extends BooleanEvaluator {
       public MyBooleanEvaluator() {
         super(MYPARAMS,null);
       }
       
       @Override
       public boolean isFunction(String name) {
         Collection<Function> functions = MYPARAMS.getFunctions();
         for (Function f: functions ) {
           if (f.getName().equals(name)) {
             return true;
           }
         }
         return false;
       }
       
       @Override
       protected Boolean evaluate(Function function, Iterator<Boolean> arguments, Object evaluationContext) {
         if (function == IS_IJK_MATERIAL) {
           System.out.println("Would evaluate IS_IJK_MATERIAL here... but returning true");
           // Implements the new function
           // You should call arguments.next() for however many arguments your function has (and is declared to have)
           System.out.println("my args is"+arguments.next()); // eat it
           return true;
         } else {
           // If it's another function, pass it to DoubleEvaluator
           return super.evaluate(function, arguments, evaluationContext);
         }
       }
    
  }

}
