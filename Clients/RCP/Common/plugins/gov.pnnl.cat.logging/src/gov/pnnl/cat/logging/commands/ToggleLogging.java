package gov.pnnl.cat.logging.commands;

import gov.pnnl.cat.logging.CatLogger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Toggle logging on or off.  Later we can pop up a dialog so users can pick which 
 * commonents to log in debug mode...
 * @author D3K339
 *
 */
public class ToggleLogging extends AbstractHandler {
  public static final String ID = ToggleLogging.class.getName();
  
  @SuppressWarnings("unused")
  private static Logger logger = CatLogger.getLogger(ToggleLogging.class);

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    
    Level currentLevel = Logger.getLogger("out").getLevel();
    Level newLevel = Level.WARN;
    if(currentLevel == null || !currentLevel.equals(Level.DEBUG)) {
      newLevel = Level.DEBUG;
    }
    Logger.getLogger("out").setLevel(newLevel);
    return null;
  }

}
