package gov.pnnl.velo.tools.ui.abc.panels;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import datamodel.Key;
import gov.pnnl.velo.tools.ui.abc.ToolUIABCDefault;


public abstract class AbstractToolPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	protected Key identifier;
	protected List<PanelChangeListener> listeners = new ArrayList<PanelChangeListener>();

	public AbstractToolPanel(String title) {		
		this(new Key(title));
	}

	public AbstractToolPanel(Key key) {		
		this.identifier = key;
	}
	
	public Key getKey() {
		return identifier;
	}
	
	/**
	 * Initialize the contents of this panel
	 * Pass in the parent tool, in case the panel needs it
	 * 
	 * @param tool
	 */
	public abstract void initializePanel(ToolUIABCDefault tool);
	
	/**
	 * Initialize the state of the panel based
	 * @return
	 */
	//public abstract void setParameters(Map<ParameterType, Object> parameters);

	/**
	 * Extract the parameters from the panel
	 * @return
	 */
	//public abstract Map<ParameterType, Object> getParameters();

	/**
	 * Returns true if the user has filled out everything necessary in this panel
	 * The component listener is responsible for showing missing content to the user ?
	 * Keeping this in here for now so we can draw the icon in the process model
	 * based on the status of the tab
	 * @return
	 */
	public abstract boolean isComplete(); 

	/**
	 * Returns true if there is an error on this panel,
	 * The component listener is responsible for showing the errors to the user ?	 
	 * Keeping this in here for now so we can draw the icon in the process model
	 * based on the status of the tab
	 * @return 
	 */
	public abstract boolean hasErrors();

  /**
   * TODO: we may need to send a change event
   */
  protected void notifyChangeListeners() {
    // Send the batch notification to all the listeners
    ArrayList<PanelChangeListener> listenersCopy; 
    synchronized(this.listeners){
      listenersCopy = new ArrayList<PanelChangeListener>(this.listeners);
    }
    for (int i = 0; i < listenersCopy.size(); i++) {
      listenersCopy.get(i).panelChanged();;
    }    
  }
	
	public void addPanelChangeListener(PanelChangeListener listener) {
    synchronized(this.listeners) {
      // remove it first
      this.listeners.remove(listener);
      this.listeners.add(listener);
    }
	}
	
	public void removePanelChangeListener(PanelChangeListener listener) {
    synchronized (this.listeners) {
      this.listeners.remove(listener);
    }
	}

}
