package vabc;


/**
 * If you want to use custom actions in the xml, you will need
 * to provide this class to the abc constructors
 * @author port091
 *
 */
public interface IABCActionProvider {
  
  public IABCAction getCustomAction(String key);

}
