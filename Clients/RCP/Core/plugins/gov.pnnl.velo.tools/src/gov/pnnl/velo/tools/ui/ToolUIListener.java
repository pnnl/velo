package gov.pnnl.velo.tools.ui;

public interface ToolUIListener {
  
  /**
   * Tool UI has been created.
   * @param toolUI
   */
  public void toolCreated(ToolUI toolUI);
  
  // TODO: We may need to provide a toolChanged method so that external entities
  // can be notified when the tool changes in a consistent way (maybe
  // self-describing avro messages?)
  // public void toolChanged(ToolUI toolUI);
 
  /**
   * Tool UI has been closed.
   * @param toolUI
   */
  public void toolClosed(ToolUI toolUI);
  
  public void toolFailed(Throwable exception);

}
