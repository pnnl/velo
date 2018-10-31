package gov.pnnl.velo.tools.ui.abc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.w3c.dom.NodeList;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.ColorMap;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.FontMap;
import bibliothek.gui.dock.common.theme.ThemeMap;
import datamodel.Key;
import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.SWTUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.tools.Tool;
import gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior;
import gov.pnnl.velo.tools.ui.ToolUI;
import gov.pnnl.velo.tools.ui.abc.ProcessTree.What;
import gov.pnnl.velo.tools.ui.abc.ProcessTree.Where;
import gov.pnnl.velo.tools.ui.abc.panels.AbstractToolPanel;
import gov.pnnl.velo.tools.ui.abc.panels.PanelChangeListener;
import gov.pnnl.velo.tools.ui.abc.util.CommandManager;
import gov.pnnl.velo.tools.util.ToolUtils;
import vabc.ABCConstants;
import vabc.ABCDocument;
import vabc.ABCStyle;
import vabc.IABCActionProvider;
import vabc.IABCDataProvider;
import vabc.IABCErrorHandler;
import vabc.SwingUtils;

/**
 * First level of abstraction for a swing-based tool UI
 * 
 * This level is responsible for:
 * 
 * ->	Creating a Swing (for now) UI with a dock-able content
 * 
 * 			By default the UI will contain (from top to bottom)
 * 
 * 				menu bar (top)
 * 				button bar (top)
 * 				dock-able container with (by default) process tree, status tab and summary view?
 * 				status bar (bottom)
 * 
 * -> Listening for changes in the UI and binding them to the data model
 * 
 * -> Interactions with the server
 * 
 * 			Downloading necessary files and initializing the state of the AbstractToolPanels
 * 			Save button, which will save data model, upload files to the server and set meta data properties on the tool
 * 
 * @author port091
 */
public abstract class ToolUIABCDefault extends JFrame implements ToolUI, IABCErrorHandler {
  protected Logger logger = CatLogger.getLogger(ToolUIABCDefault.class);
  
  // keys for menus and actions
  public static final String MENU_FILE = "File";
  public static final String MENU_EDIT = "Edit";  
  public static final String ACTION_SAVE = "Save"; 
  public static final String ACTION_SAVE_AS = "Save As";   
  public static final String ACTION_CLOSE = "Close"; 
  public static final String ACTION_UNDO = "Undo"; 

  private static final long serialVersionUID = 1L;

  // Keep track of if the model has changed
  // since the tool was opened
  protected boolean dirty = false;

  // Keep track of actions in case
  // we need to enable/disable them
  protected Map<String, Action> actions;

  // Status list 
  private Map<String, List<String>> errorLog;
  private JList status;	

  // Access to the tool panels
  protected ProcessTree processTree;

  private CommandManager commandManager; // Observable

  protected File localWorkingDirectory;
  protected IFolder toolInstanceDir;
  protected Tool tool;
  
  
  protected ABCDocument abcdoc;

  protected IResourceManager resourceManager = CmsServiceLocator.getResourceManager();
  
  /**
   * Empty constructor
   */
  public ToolUIABCDefault() {

  }
  
  /**
   * TODO: return a file to configure the layout 
   * Each plugin should return this directly from their bundle
   * to avoid conflicts.
   * @return
   */
  protected abstract File getAbcConfigFile ();
  
  @Override
  public void initializeContext(Tool tool, List<IResource> selectedResources) {
    this.tool = tool;
    
    // default is to assume that only one resource will be selected when opening this tool, and
    // that selection is the tool instance folder
    if(selectedResources.size() > 0) {
      if(selectedResources.get(0) instanceof IFolder) {
        toolInstanceDir = (IFolder)selectedResources.get(0);
      }
      // create a local working dir
      try {
        this.localWorkingDirectory = ToolUtils.getToolWorkingDirectory(tool, selectedResources.get(0));   
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }  


    // Initialize anything that needs to
    // happen before loading the UI's
    initializeData();			

    commandManager = new CommandManager();

    initializeUI();

    PanelChangeListener panelListener = new PanelChangeListener() {     
      @Override
      public void panelChanged() {
        ToolUIABCDefault.this.panelChanged();        
      }
    };

    for(AbstractToolPanel toolPanel: processTree) {
      toolPanel.addPanelChangeListener(panelListener);
    }

  }
  
  public String getMimetype() {
    return tool.getMimetype();
  }
  
  /**
   * @return the toolInstanceDir
   */
  public IFolder getToolInstanceDir() {
    return toolInstanceDir;
  }

  /**
   * @return the tool
   */
  public Tool getTool() {
    return tool;
  }

  public File getLocalWorkingDirectory() {
    return localWorkingDirectory;
  }

  /**
   * *********************************** Interaction with Abstract Tool Methods
   */

  public void pushErrors(String key, List<String> errors) {
    if(errors != null) {
      if(errorLog.containsKey(key)) 
        setDirty(true); // Otherwise it's the initial time?
      errorLog.put(key, errors);	// Overrides previous errors
    }
    setErrors();	
  }

  public void clearErrors(String key) {
    if (errorLog.containsKey(key)) {
      errorLog.remove(key);
      pushErrors(null, null);
    }
  }

  public Map<String, List<String>> getErrors() {
    return new HashMap<String, List<String>>();
  }

  private void setErrors() {
    ((DefaultListModel) status.getModel()).clear();
    for (List<String> allErrors : errorLog.values()) {
      for (String error : allErrors) {
        if (error.isEmpty())
          continue;
        error.trim();
        ((DefaultListModel) status.getModel()).addElement(error);
      }
    }
  }

  /**
   * *********************************** Abstract Methods
   */

  /**
   * Responsible for making sure any needed data models
   * get initialized
   */
  protected void initializeData() {
    try {
      
      File xmlfile = getAbcConfigFile();
      if(xmlfile != null) {
        abcdoc = ABCDocument.load(xmlfile);
      }
      
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * TODO: Maybe this should be moved out of the abstract panel?
   * This is a summary view for the lower left hand corner.
   * @return
   */
  public abstract JPanel getSummaryView();

  /**
   * @return Return a generic map for generating the process tree view
   * The object can be:
   * 	Another map <String, Object>; which will create a new node in the tree under key
   *  An AbstractToolPanel; which will create a leaf element linked to the provided UI
   *  A string; which will create a leaf element linked to a new ABCToolPanel
   */
  public ProcessTree getProcessTree() {
    // Auto build based on abc components
    ProcessTree processTree = new ProcessTree();

    if(abcdoc != null)	{
      NodeList children = abcdoc.getDocument().getElementsByTagName(ABCConstants.Key.COMPONENT);		
      for(int i = 0; i < children.getLength(); i++) {
        if(children.item(i).getNodeName().equals(ABCConstants.Key.COMPONENT)) {
          String name = children.item(i).getAttributes().getNamedItem(ABCConstants.Key.NAME).getNodeValue();
          org.w3c.dom.Node group = children.item(i).getAttributes().getNamedItem(ABCConstants.Key.GROUP);	
          if(group != null) {
            if(processTree.indexOf(group.getNodeValue()) == -1) {
              processTree.add(What.NODE, group.getNodeValue(), Where.ROOT, null);
            }
            processTree.add(What.NEW_ABC_PANEL, new Key(name), Where.UNDER, group.getNodeValue());
          } else {
            processTree.add(What.NEW_ABC_PANEL, new Key(name), Where.ROOT, null);
          }
        }
      }	
    }
    return processTree;
  }

  /**
   * Default for the title is the name of the tool concatenated
   * with the name of the tool instance folder.
   * @return title of the tool for the frame
   */
  protected String getWindowTitle() {
    String title = tool.getName() + ": " + (toolInstanceDir != null ? toolInstanceDir.getPath().getName() : "");
    return title;
  }


  /**
   * Each tool will need to provide a map of custom menu items -
   * Save, save as, close, and undo will be provided by this class
   * @return
   */
  protected abstract Map<String, Object> getMenuItems();


  /**
   * Extract the files to save from your data model
   * @return
   */
  public Map<File, CmsPath> getFilesToSave() {
    return new HashMap<File, CmsPath>();
  }

  /**
   * Extract the properties to save from your data model
   * @return
   */
  public Map<String, String> getPropertiesToSave() {
    return new HashMap<String, String>();
  }

  protected void save() {
    try {
      setBusyCursor();
      Map<File, CmsPath> filesToSave = getFilesToSave(); 
      Map<String, String> propertiesToSave = getPropertiesToSave();
      SaveToolInstanceBehavior sti = tool.getSaveToolInstanceBehavior();
      if(sti != null) {
        sti.save(toolInstanceDir, this, filesToSave, propertiesToSave);
      }
    } catch(Throwable e) {
      ToolErrorHandler.handleError("Failed to save " + getTitle(), e, true, ToolUIABCDefault.this);

    } finally {
      removeBusyCursor();
    }
  }
  
  /**
   * Gives tools an easy way to set the busy cursor when executing
   * long-running operations.
   */
  public void setBusyCursor() {
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  public void removeBusyCursor() {
    this.setCursor(Cursor.getDefaultCursor());
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.ui.ToolUI#isDirty()
   */
  @Override
  public boolean isDirty() {
    return dirty;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.ui.ToolUI#setDirty(boolean)
   */
  @Override
  public void setDirty(final boolean dirty) {
    this.dirty = dirty;
    updateSaveButtonEnablement();  
  }
  
  /**
   * @return
   */
  @Override
  public File getToolLocalWorkingDir() {
    return localWorkingDirectory;
  }

  /**
   * @return
   */
  @Override
  public Tool getToolDefintion() {
    return tool;
  }

  /**
   * 
   */
  @Override
  public void bringToFront() {
    toFront();
  }

  
  private void updateSaveButtonEnablement() {
    if(actions != null) {
      final Action saveAction = actions.get(ACTION_SAVE);
      
      if (saveAction != null) {
        SwingUtilities.invokeLater(new Runnable() {
          
          @Override
          public void run() {
            SaveToolInstanceBehavior sti = tool.getSaveToolInstanceBehavior();
            saveAction.setEnabled(dirty && sti != null && sti.isSaveAllowed(toolInstanceDir));          
          }
        });
      }
    } 
  }

  /**
   * TODO: this needs to be linked with the CustomSaveAsBehavior so we don't have redundant code
   */
  protected void saveAs() {
    try {
      setBusyCursor();
      Map<File, CmsPath> filesToSave = getFilesToSave(); 
      Map<String, String> propertiesToSave = getPropertiesToSave();
      SaveToolInstanceBehavior sti = tool.getSaveToolInstanceBehavior();
      if(sti != null) {
        sti.saveAs(toolInstanceDir, this, filesToSave, propertiesToSave, false);
      }
    } catch(Throwable e) {
      ToolErrorHandler.handleError("Failed to save " + getTitle(), e, true, ToolUIABCDefault.this);

    } finally {
      removeBusyCursor();
    }
  } 
  
  /**
   * Used to change the context of this UI to point to a new tool instance dir.  Used when
   * performing a Save As.
   * @param newToolInstanceDir
   */
  @Override
  public void changeContext(IFolder newToolInstanceDir) {
    // need to reset any resources that are currently cached, as their paths have changed
    this.toolInstanceDir = newToolInstanceDir;
    setTitle(getWindowTitle()); 
  }

  /**
   * Subclasses can override if they want to do something special when the
   * tool closes.
   * @return TODO
   */
  public boolean close() {

    // if dirty, prompt to save
    boolean finishClosing = true;
    if (dirty) {
      SaveToolInstanceBehavior sti = tool.getSaveToolInstanceBehavior();
      if(sti != null) {
        int option = sti.promptForSaveOnClose(this);
        if(option == SaveToolInstanceBehavior.SAVE) {
          save();
        } else {
          finishClosing = false;
        }
      }
    }
    dispose();

    if (finishClosing) {
      dirty = false;
      dispose();
      
    } else {
      this.requestFocus();
    }
    
    return finishClosing;
  }

  protected void undo() {

//    // Undo has been called, we might want to set the dirty flag here
//    setDirty(true);
//
//    // Request an undo action from the command manager
//    Command previousCommand = commandManager.undoStackPeek();
//    if(previousCommand.isUndoable()) {
//      commandManager.undo();
//    }
//
//    if(previousCommand instanceof PanelChangedCommand) {
//      ((PanelChangedCommand)previousCommand).processUndo();		
//    }

  }

  protected void panelChanged() {
    setDirty(true);
    /* When to bind?
		AbstractToolPanel toolPanel = SwingUtils.getToolPanel(widget);
		if(toolPanel == null)
			return; // Widget change didn't come from an abstract tool panel, should we process it?

		System.out.println("Widget changed in tool panel: " + toolPanel.getKey());

		// Change occurred, set the dirty flag
		setDirty(true);

		// Get the parameters from the panel that changed, this assumes one receiver object
		// will be returned in the map
		Serializable receiver = null;
		Map<ParameterType, Object> parameters = toolPanel.getParameters();
		if(parameters != null) {
			for(ParameterType param: parameters.keySet()) {
				if(param.getType().equals(ParameterType.Type.SERALIZABLE)) {
					receiver = (Serializable)parameters.get(param);
					break;
				}
			}
		}

		// Create a panel change command and set the parameters form the UI
		PanelChangedCommand panelChange = new PanelChangedCommand(this, toolPanel, receiver);

		// When the command is executed the parameters will get bound to the data model
		commandManager.executeCommand(panelChange);
     */

  }
  
  public ABCDocument getDocument() {
    return abcdoc; // TODO: Make sure it's initialized?
  }
  
  public abstract IABCDataProvider getDataProvider();
  
  public abstract IABCActionProvider getActionProvider();
  
  /**
   * *********************************** User interface initialization
   */
  
  /**
   * Creates the main dock-able UI, protected in case 
   * implementing class wants to change it
   * @return
   */
  protected JPanel createDockableUI() {

    final int width = getWidth();
    int height = getHeight();

    final int leftPanelWidth = (int)(width* 0.2);
    int processTreeHeight = (int)(height * 0.4);
    final int formPanelHeight = (int)(height * 0.75);

    // Status messages
    status = new JList();
    errorLog = new HashMap<String, List<String>>();

    // Add the components to the container UI		
    CControl control = new CControl(this);
    control.getThemes().select(ThemeMap.KEY_ECLIPSE_THEME);
    // control.getThemes().select(ThemeMap.KEY_FLAT_THEME);

    ABCStyle style = ABCStyle.style();
    Map<String, Color> colors = new HashMap<String, Color>();
    colors.put(ColorMap.COLOR_KEY_TAB_BACKGROUND_SELECTED, style.getTabBackgroundSelectedColor());
    colors.put(ColorMap.COLOR_KEY_TAB_BACKGROUND_FOCUSED, style.getTabBackgroundFocusedColor());
    colors.put(ColorMap.COLOR_KEY_TAB_BACKGROUND, style.getTabBackgroundColor());
    colors.put(ColorMap.COLOR_KEY_TAB_FOREGROUND, style.getForegroundColor());
    colors.put(ColorMap.COLOR_KEY_TAB_FOREGROUND_SELECTED, style.getForegroundColor());
    colors.put(ColorMap.COLOR_KEY_TAB_FOREGROUND_FOCUSED, style.getForegroundColor());

    final CGrid grid = new CGrid(control);

    processTree = getProcessTree();		
    final ProcessTreeView processTreeView = new ProcessTreeView(processTree);

    boolean showTree = true;

    if(showTree) {
      processTreeView.getFonts().setFont(FontMap.FONT_KEY_TAB, ABCStyle.style().getDefaultFont());
      for(String key: colors.keySet())
        processTreeView.getColors().setColor(key, colors.get(key));
      grid.add(0, 0, leftPanelWidth, processTreeHeight, processTreeView); // x, y, width, height

      // Include the regions for model setup, a tree view of the data model for tool sets?
      JPanel summaryView = getSummaryView();
      if(summaryView != null) {
        DefaultSingleCDockable summaryDockable = new DefaultSingleCDockable("Summary", "Summary", summaryView);
        summaryDockable.getFonts().setFont(FontMap.FONT_KEY_TAB,  ABCStyle.style().getDefaultFont());
        for(String key: colors.keySet())
          summaryDockable.getColors().setColor(key, colors.get(key));
        grid.add(0, processTreeHeight, leftPanelWidth, height-processTreeHeight, summaryDockable);
      }
    }

    DefaultSingleCDockable firstDockable = null;
    for(DefaultSingleCDockable dockable: processTreeView) {

      System.out.println("Adding: " + dockable.getTitleText());
      dockable.getFonts().setFont(FontMap.FONT_KEY_TAB, ABCStyle.style().getDefaultFont());
      for(String key: colors.keySet())
        dockable.getColors().setColor(key, colors.get(key));
      grid.add(leftPanelWidth, 0, width-leftPanelWidth, formPanelHeight, dockable);
      
      if(firstDockable == null) {
        firstDockable = dockable;
        // Make sure the first dockable is selected - you have to do it right after it's added
        // or else the select seems to pick the last one
        grid.select(leftPanelWidth, 0, width-leftPanelWidth, formPanelHeight, dockable);
      }
    }		

    status.setModel(new DefaultListModel());
    status.setFont(style.getDefaultFont());
    status.setForeground(style.getForegroundColor());
    status.setSelectionModel(new DefaultListSelectionModel() {
      private static final long serialVersionUID = 1L;
      @Override
      public void setSelectionInterval(int index0, int index1) {
        super.setSelectionInterval(-1, -1);
      }
    });
    JScrollPane statusView = new JScrollPane(status);
    DefaultSingleCDockable statusDockable = new DefaultSingleCDockable("Status", "Status", statusView);	
    statusDockable.getFonts().setFont(FontMap.FONT_KEY_TAB, ABCStyle.style().getDefaultFont());
    for(String key: colors.keySet())
      statusDockable.getColors().setColor(key, colors.get(key));	
    grid.add(leftPanelWidth, formPanelHeight, width-leftPanelWidth, height-formPanelHeight, statusDockable);

    control.getContentArea().deploy(grid);		

    JPanel dockableArea = new JPanel();
    dockableArea.setLayout(new GridLayout(1, 1));
    dockableArea.add(control.getContentArea());		
    dockableArea.doLayout();

    return dockableArea;
  }
  
  private void centerOnEclipseShell() {
    
    // set location based on the location of the main swt shell
    Callable<Point> cb = new Callable<Point>() {

      @Override
      public Point call() throws Exception {
        Point location = SWTUtil.getCenteredDialogBounds(getWidth(), getHeight());
        return location;
      }
    };
    Point location = SWTUtil.blockingAsyncExec(cb);
    if(location != null) {
      setLocation(location.x, location.y);
    }

  }

  private void initializeUI() {

    setTitle(getWindowTitle()); 
    setSize(800, 600); // (svga 4:3)
    //setSize(1024, 768); // (xga)
    //setSize(1280, 720); // (hd)
    centerOnEclipseShell();
      
    // Get the menu items, all tools will get save, save as, close, and undo
    Map<String, Object> menuItems = getDefaultMenuItems();
    menuItems.putAll(getMenuItems()); // Additional menu items from the tools

    actions = new HashMap<String, Action>();
    putActionInMap(menuItems);

    // Create the menu and tool bars
    JMenuBar menuBar = new JMenuBar();
    for(String key : menuItems.keySet()) {
      if(!(menuItems.get(key) instanceof Map))
        continue;
      @SuppressWarnings("unchecked")
      Map<String, Object> menu = (Map<String, Object>)menuItems.get(key);
      JMenu subMenu = new JMenu(key);
      buildMenuBar(subMenu, menu);
      menuBar.add(subMenu);		
    }
    setJMenuBar(menuBar);

    JToolBar toolbar = new JToolBar();
    buildToolBar(toolbar, menuItems);
    //		toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
    toolbar.setFloatable(false);
    toolbar.setRollover(true);

    add(toolbar, BorderLayout.NORTH);		

    // Initialize the main dock-able component
    add(createDockableUI(), BorderLayout.CENTER);

    // Call initialize in all the tool panels
    for(AbstractToolPanel toolPanel: processTree) {
      if(toolPanel == null)
        continue;
      toolPanel.initializePanel(this);
    }

    // TODO: bottom button panel, what do we want to do with this?
    JToolBar statusBar = new JToolBar();
    statusBar.setPreferredSize(new Dimension(getWidth(), 20));
    add(statusBar, BorderLayout.SOUTH);
    statusBar.setFloatable(false); // Keep it at the bottom
    
    // Listen for window closing event so we can prompt for save if needed
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }        
    });

    // make sure save button is enabled properly
    updateSaveButtonEnablement();
    setVisible(true);
  }

  protected Map<String, Object> getDefaultMenuItems() {

    Map<String, Object> items = new LinkedHashMap<String, Object>();
    Map<String, Object> fileMenu =  new LinkedHashMap<String, Object>();
    Map<String, Object> editMenu =  new LinkedHashMap<String, Object>();

    items.put(MENU_FILE, fileMenu);
    items.put(MENU_EDIT, editMenu);
    
    Action saveAction = new AbstractAction(ACTION_SAVE, SwingUtils.getImageIcon("save", 16)) {
      private static final long serialVersionUID = 1L;
      @Override
      public void actionPerformed(ActionEvent e) {
        save();
      } 
    };
    saveAction.setEnabled(false);
    fileMenu.put(ACTION_SAVE, saveAction);

    fileMenu.put(ACTION_SAVE_AS, new AbstractAction(ACTION_SAVE_AS, SwingUtils.getImageIcon("save_as", 16)) {
      private static final long serialVersionUID = 1L;
      @Override
      public void actionPerformed(ActionEvent e) {
        saveAs();
      }		
    });

    AbstractAction closeAction = new AbstractAction(ACTION_CLOSE) {
      private static final long serialVersionUID = 1L;
      @Override
      public void actionPerformed(ActionEvent e) {
        close();
      }     
    };
    closeAction.putValue("ignoreOnToolbar", "true");
    fileMenu.put(ACTION_CLOSE, closeAction);

    editMenu.put(ACTION_UNDO, new AbstractAction(ACTION_UNDO, SwingUtils.getImageIcon("undo", 16)) {
      private static final long serialVersionUID = 1L;
      @Override
      public void actionPerformed(ActionEvent e) {
        undo();
      }			
    });

    return items;
  }

  @SuppressWarnings("unchecked")
  private void buildMenuBar(JMenu menu, Map<String, Object> menuItems) {
    for(String key : menuItems.keySet()) {
      Object value = menuItems.get(key);
      if(value instanceof AbstractAction) {
        menu.add((AbstractAction)value);
      } else if(value instanceof Map) {
        JMenu subMenu = new JMenu(key);
        buildMenuBar(subMenu, (Map<String, Object>)value);
        menu.add(new JSeparator());
        menu.add(subMenu);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void buildToolBar(JToolBar toolBar, Map<String, Object> menuItems) {
    
    for(String key : menuItems.keySet()) {
      Object value = menuItems.get(key);
      
      if(value instanceof AbstractAction) {
        if(((AbstractAction)value).getValue("ignoreOnToolbar") != null) {
          continue;
        }
        final AbstractAction action = (AbstractAction)value;
        final JButton button = SwingUtils.newButton(action);
        toolBar.add(button);
      
      } else if(value instanceof Map) {
        buildToolBar(toolBar, (Map<String, Object>)value);
        toolBar.addSeparator();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void putActionInMap(Map<String, Object> menuItems) {
    for(String key : menuItems.keySet()) {
      Object value = menuItems.get(key);
      if(value instanceof AbstractAction) {
        this.actions.put(key, (AbstractAction)value);
      } else if(value instanceof Map) {
        putActionInMap((Map<String, Object>)value);
      }
    }
  }
}
