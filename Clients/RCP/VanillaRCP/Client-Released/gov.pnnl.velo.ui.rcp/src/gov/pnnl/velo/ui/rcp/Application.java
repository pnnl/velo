package gov.pnnl.velo.ui.rcp;

import gov.pnnl.cat.ui.common.rcp.AbstractCatApplicationWorkbenchAdvisor;
import gov.pnnl.cat.ui.common.rcp.AbstractVeloApplication;

/**
 * This class controls all aspects of the application's execution
 */
public class Application extends AbstractVeloApplication{

  @Override
  protected boolean showGovernmentWarningNotice() {
    return true;
  }

  @Override
  protected AbstractCatApplicationWorkbenchAdvisor getWorkbenchAdvisor() {
    return new ApplicationWorkbenchAdvisor();
  }
  
}
