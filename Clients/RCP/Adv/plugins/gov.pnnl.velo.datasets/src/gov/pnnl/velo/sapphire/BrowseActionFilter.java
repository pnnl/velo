package gov.pnnl.velo.sapphire;

import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandlerFilter;

public class BrowseActionFilter extends SapphireActionHandlerFilter {
  @Override
  public boolean check(SapphireActionHandler handler) {
    return (!handler.getAction().getId().equals("Sapphire.Browse.Calendar"));
  }
}