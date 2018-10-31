/**
 * 
 */
package gov.pnnl.cat.core.resources.security;


/**
 * @author zoe
 *
 */
public interface ILoginListener{
  
  /**
   * Method userLoggedIn
   * @param loginEvent event
   */
  public void userLoggedIn(String userId);
}
