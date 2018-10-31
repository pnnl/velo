package gov.pnnl.velo.tools.behavior.mgr;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.velo.tools.ToolManager;

public class ToolMgrBehavior {
  
  protected ToolManager toolManager;
  
  public ToolMgrBehavior() {
    // look up the tool manager from the bean container so we only have one copy
    toolManager = (ToolManager)ResourcesPlugin.getBean("toolManager");
  }

}
