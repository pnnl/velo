package gov.pnnl.velo.tools.behavior;

import gov.pnnl.velo.tools.Tool;

/**
 * Base class for behavior that is centered around a Tool
 * @author d3k339
 *
 */
public interface ToolBehavior {
  
  /**
   * Set the tool that will influence this behavior.
   * We provide it as a setter method instead of in constructor
   * so we can declaratively specify tool classes.
   * @param tool
   */
  public void setTool(Tool tool);

}
