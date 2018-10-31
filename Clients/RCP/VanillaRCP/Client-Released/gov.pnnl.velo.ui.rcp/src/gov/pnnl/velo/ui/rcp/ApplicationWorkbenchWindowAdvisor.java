package gov.pnnl.velo.ui.rcp;

import gov.pnnl.cat.ui.common.rcp.AbstractCatApplicationWorkbenchWindowAdvisor;

import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

public class ApplicationWorkbenchWindowAdvisor extends AbstractCatApplicationWorkbenchWindowAdvisor {

  public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
    super(configurer, "VeloExplorer");
  }

  public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
    super.createActionBarAdvisor(configurer);
    return new ApplicationActionBarAdvisor(configurer);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.common.rcp.AbstractCatApplicationWorkbenchWindowAdvisor#getPerspectiveIds()
   */
  @Override
  public String[] getPerspectiveIds() {
     return new String[]{
        "gov.pnnl.velo.uircp.perspectives.Dashboard",
        "gov.pnnl.cat.ui.rcp.perspectives.DataBrowser",
        "gov.pnnl.cat.ui.rcp.perspectives.search",
        // "gov.pnnl.cat.alerts.perspective", take off alerts perspective until we get the bugs out
         "org.eclipse.rse.ui.view.SystemPerspective", //TODO: fix dynamic bootstrap of remote systems
        "gov.pnnl.cat.ui.rcp.perspectives.userperspective",
        "gov.pnnl.cat.ui.rcp.perspectives.teamsperspective"
    };
  }

  @Override
  public void preWindowOpen() {
    super.preWindowOpen();
    IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

    // turn off perspective bar
    configurer.setShowPerspectiveBar(true);

    // dock perspectives on top left
    PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR,
        IWorkbenchPreferenceConstants.TOP_LEFT);

    // show text on perspective bar
    PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR,
        true);
    
    // turn off tool bar
    configurer.setShowCoolBar(false);


    // XXX We set the status line and progress indicator so that update
    // information can be shown there
    configurer.setShowStatusLine(true);
    configurer.setShowProgressIndicator(true);

    //      // dock perspectives on top right
    //      PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR,
    //          IWorkbenchPreferenceConstants.LEFT);
    //      
    //      // don't show text on perspective bar
    //      PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR,
    //          false);
  }

  //    @Override
  //    protected String getTitle(IPerspectiveDescriptor perspective) {
  //      String cifsMsg = "";
  //      String jmsMsg = "";
  //      if(!CIFSConnectionHelper.isCifsEnabled()) {
  //        cifsMsg = " - network drive disabled";
  //      }
  //      if(!NotificationManagerJMS.isJMS_ENABLED()) {
  //        jmsMsg = " - messaging disabled";
  //      }
  //      return "Velo [version: " + VeloRCPPlugin.getDefault().getBuildVersion() + "] "  + cifsMsg + jmsMsg;
  //    }


}
