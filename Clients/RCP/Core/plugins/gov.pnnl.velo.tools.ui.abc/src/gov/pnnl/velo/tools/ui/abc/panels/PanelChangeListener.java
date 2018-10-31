package gov.pnnl.velo.tools.ui.abc.panels;

public interface PanelChangeListener {
  
  // TODO: flush out what info needs to be passed in the panel changed event
  // for now we are only using it to detect if the tool is dirty
  public void panelChanged();

}
