package gov.pnnl.cat.rse.filechooser;


import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.ui.view.SystemViewLabelAndContentProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.messages.SystemMessageLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Allow users to browse and choose files from all registered file systems (local and remote).
 * Uses the CheckboxTreeViewer to allow multiple selection of files and folders.
 * 
 * http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/DemonstratesCheckboxTreeViewer.htm
 * 
 */
public class FileSystemsFileChooser extends ApplicationWindow implements ISystemMessageLine, ISystemResourceChangeListener {

  private List<IHost> hosts;
  private Map<IHost, List<IRemoteFile>> fileSelections;
  private Map<IHost, FileSystemComposite> fileSystemComposites;
  private Point position;
  private StackLayout stackLayout;
  private Composite fileSystemContentsPanel;
  private TableViewer fsTable;
  private String[] hostTypeFilter;

  protected SystemMessageLine fMessageLine;
  protected SystemMessage pendingMessage, pendingErrorMessage;
  private boolean foldersOnly;
  
  public FileSystemsFileChooser (Shell parentShell, boolean foldersOnly) {
    this(parentShell, null, foldersOnly);
  }
  
  public FileSystemsFileChooser (Shell parentShell, String[] hostTypeFilter, boolean foldersOnly) {
    super(parentShell);
    this.foldersOnly = foldersOnly;
    this.setShellStyle(SWT.MODELESS |SWT.CLOSE|SWT.RESIZE|SWT.MIN|SWT.MAX);

    if(parentShell != null) {
      position = parentShell.getLocation();
      position = new Point(parentShell.getLocation().x + 200, parentShell.getLocation().y+100);
    } else {
      Monitor primary = Display.getDefault().getPrimaryMonitor();
      Rectangle bounds = primary.getBounds();
      int x = bounds.x + (bounds.width -740) / 2;
      int y = bounds.y + (bounds.height - 480) / 2;
      position = new Point(x, y);
    }
    
    this.hostTypeFilter = hostTypeFilter;
  }

  /**
   * Runs the application
   */
  public Map<IHost, List<IRemoteFile>> run() {

    // Don't return from open() until window closes
    setBlockOnOpen(true);

    // Open the main window
    open();
    
    return fileSelections;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.window.ApplicationWindow#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);

    shell.setLocation(position);
    // Set the title bar text and the size
    shell.setText("Select Files");
    shell.setSize(840, 480);
  }
  
  private List<IHost> getHosts() {
    List<IHost> hosts = new ArrayList<IHost>();
    for(IHost host : RSEUtils.getRegisteredHosts()) {
      // we have to ignore this special host used for globus online
      if(!host.getName().equalsIgnoreCase("globus online server")) {
        
        // now we have to filter host type
        if(hostTypeFilter == null) {
          // no filter so add all of them
          hosts.add(host);

        } else {
          // only add ones with the specific types specified
          boolean match = false;       
          for(String systemType : this.hostTypeFilter) {
            if(host.getSystemType().getId().equalsIgnoreCase(systemType)) {
              match = true;
              break;
            }
          }
          if(match) {
            hosts.add(host);
          }
        }
      }
    }
    return hosts;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    // load file systems
    hosts = getHosts();

    final Composite composite = new Composite(parent, SWT.NONE);

    // Two columns, grab all space
    composite.setLayout(new GridLayout(2, false));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    // Create file systems list on the left
    fsTable = createFileSystemsTable(composite);
    GridData data = new GridData(SWT.LEFT, SWT.FILL, false, true);
    data.heightHint = 200;
    data.widthHint = 180;
    fsTable.getTable().setLayoutData(data);

    // Create stack of file system contents views to the right
    fileSystemContentsPanel = createFileSystemContentsPanel(composite);
    fileSystemContentsPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    // Add button bar on the bottom
    Composite buttonBar = createButtonBar(composite);
    buttonBar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 2, 1));
    
    RSECorePlugin.getTheSystemRegistry().addSystemResourceChangeListener(this);

    return composite;
  }
  

  /* (non-Javadoc)
   * @see org.eclipse.rse.core.events.ISystemResourceChangeListener#systemResourceChanged(org.eclipse.rse.core.events.ISystemResourceChangeEvent)
   */
  @Override
  public void systemResourceChanged(ISystemResourceChangeEvent event) {
    if(event.getType() == ISystemResourceChangeEvents.EVENT_ADD_RELATIVE && event.getSource() instanceof IHost) {
      
      // refresh - TODO: replace this with table input and content provider
      hosts = getHosts();
      fsTable.getTable().removeAll();
      SystemViewLabelAndContentProvider labelProvider = new SystemViewLabelAndContentProvider();

      for(IHost host : hosts) {
        TableItem tableItem = new TableItem (fsTable.getTable(), SWT.NONE);
        tableItem.setImage(labelProvider.getImage(host));
        tableItem.setText(host.getName()); 
        tableItem.setData(host);
      }
      
      IHost host = (IHost)event.getSource();
      FileSystemComposite fsc = new FileSystemComposite(fileSystemContentsPanel, host, foldersOnly);
      fileSystemComposites.put(host, fsc);

    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.window.ApplicationWindow#close()
   */
  @Override
  public boolean close() {
    boolean closed = super.close();
    if(closed) {
      RSECorePlugin.getTheSystemRegistry().removeSystemResourceChangeListener(this);
    }
    return closed;
  }

  protected Composite createFileSystemContentsPanel(Composite parent) {
    fileSystemComposites = new HashMap<IHost, FileSystemComposite>();

    // create the composite that the pages will share
    final Composite fileSystemContentsPanel = new Composite(parent, SWT.NONE);
    stackLayout = new StackLayout();
    stackLayout.marginHeight = 0;
    fileSystemContentsPanel.setLayout(stackLayout);

    // iterate through file systems
    for (IHost host : hosts) {
      FileSystemComposite fsc = new FileSystemComposite(fileSystemContentsPanel, host, foldersOnly);
      fileSystemComposites.put(host, fsc);
    }
    stackLayout.topControl = fileSystemComposites.get(hosts.get(0));

    return fileSystemContentsPanel;
  }

  protected Composite createButtonBar(Composite parent) {
    Composite buttonBar = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(2, true);
    layout.marginHeight = 0;
    buttonBar.setLayout(layout);

    Button loadButton = new Button(buttonBar, SWT.NONE);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
    gd.widthHint = 75;
    loadButton.setLayoutData(gd);
    loadButton.setText("Select");
    loadButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        System.out.println("Select button clicked");  

        // Return the users selections
        fileSelections = new HashMap<IHost, List<IRemoteFile>>();
        for(IHost host : fileSystemComposites.keySet()) {
          FileSystemComposite fsc = fileSystemComposites.get(host);
          fileSelections.put(host, fsc.getCheckedElements());
        }
        close();
      }
    });

    Button cancelButton = new Button(buttonBar, SWT.NONE);
    gd = new GridData(SWT.FILL, SWT.FILL, false, false);
    gd.widthHint = 75;
    cancelButton.setLayoutData(gd);
    cancelButton.setText("Cancel");
    cancelButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        System.out.println("Cancel button clicked; closing shell");
        close();
      }
    });

    return buttonBar;
  }
  
  protected void createTablePopupMenu(final Table table) {
    
    final Menu headerMenu = new Menu(getShell(), SWT.POP_UP);
    // Request 
    MenuItem item = new MenuItem(headerMenu, SWT.NONE);
    item.setText("Request Machine Registration");
    item.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        // send an email to the mailing list to request a new machine to be registered
      }
    });
    
    // create new connection
    item = new MenuItem(headerMenu, SWT.NONE);
    item.setText("Register Machine (advanced users only)");
    item.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        IWizardDescriptor descriptor = PlatformUI.getWorkbench()
            .getNewWizardRegistry().findWizard("org.eclipse.rse.ui.newWizards.connection");

        try {
          // Then if we have a wizard, open it.
          if (descriptor != null) {
            IWorkbenchWizard wizard = descriptor.createWizard();
            wizard.init(PlatformUI.getWorkbench(), null);
            WizardDialog wd = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
            wd.setTitle(wizard.getWindowTitle());
            wd.open();
          }
        } catch (CoreException e) {
          ToolErrorHandler.handleError("Failed to launch wizard.", e, true);
        }
      }
    });
    
    
    table.addListener(SWT.MenuDetect, new Listener() {
      @Override
      public void handleEvent(Event event) {
        Point pt = getShell().getDisplay().map(null, table, new Point(event.x, event.y));
        Rectangle clientArea = table.getClientArea();
        boolean header = clientArea.y <= pt.y && pt.y < (clientArea.y + table.getHeaderHeight());
        table.setMenu(headerMenu);
      }
    });
    
    /* IMPORTANT: Dispose the menus (only the current menu, set with setMenu(), will be automatically disposed) */
    table.addListener(SWT.Dispose, new Listener() {
      @Override
      public void handleEvent(Event event) {
        headerMenu.dispose();
      }
    });

  }

  protected TableViewer createFileSystemsTable(Composite parent) {

    final TableViewer tableViewer = new TableViewer(parent, SWT.NO_FOCUS | SWT.NO_SCROLL | SWT.SINGLE | SWT.BORDER | SWT.SINGLE | SWT.NO_BACKGROUND);
    Table table = tableViewer.getTable();
    table.setLinesVisible (false);
    table.setHeaderVisible (true);

    TableViewerColumn column = new TableViewerColumn (tableViewer, SWT.BORDER | SWT.NO_FOCUS | SWT.FILL | SWT.NO_BACKGROUND);
    column.getColumn().setText ("Machines:");
    column.getColumn().setWidth(200);

    // create popup menu for header
    //createTablePopupMenu(table);
   
    SystemViewLabelAndContentProvider labelProvider = new SystemViewLabelAndContentProvider();
    for(IHost host : hosts) {
      TableItem tableItem = new TableItem (table, SWT.NONE);
      tableItem.setImage(labelProvider.getImage(host));
      tableItem.setText(0, host.getName()); 
      tableItem.setData(host);
    }

    table.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        TableItem[] selection = tableViewer.getTable().getSelection();
        // there will only be one selection since we set it to single
        for(TableItem item : selection) {
          final IHost host = (IHost)item.getData();
          connectToFileSystem(host, Display.getCurrent());
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
      }
    });

    table.select(0);
    table.setEnabled(true); 
    return tableViewer;
  }

  private void connectToFileSystem(final IHost host, final Display display) {
    IProgressService service = PlatformUI.getWorkbench().getProgressService();
    try {
      service.run(true, false, new IRunnableWithProgress(){
        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {    
          boolean connected = false;
          try {
            // make sure the host is connected
            IRemoteFileSubSystem fileSS = RSEUtils.getFileSubSystem(host);
            //ISubSystem fileSS = host.getConnectorServices()[0].getSubSystems()[0];
            if(!fileSS.isConnected()) {
              monitor.beginTask("Connecting to server " + host.getName(), IProgressMonitor.UNKNOWN);
              fileSS.connect(new NullProgressMonitor(), false);
              connected = true;
              monitor.done();
            }
          } catch (Exception ex) {
            ToolErrorHandler.handleError("Failed to connect to server: " + host.getName(), ex, true);
          }

          final boolean connectedFinal = connected;

          display.asyncExec(new Runnable() {          
            @Override
            public void run() {
              stackLayout.topControl = fileSystemComposites.get(host);
              if(connectedFinal) {
                fileSystemComposites.get(host).connectionInitialized();
              }
              fileSystemContentsPanel.layout();              
            }
          });
        }
      });
    } catch (Exception e) {
      ToolErrorHandler.handleError("Failed to connect to server: " + host.getName(), e, true);
    } 

  }

  // -----------------------------
  // ISystemMessageLine METHODS...
  // -----------------------------
  /**
   * Clears the currently displayed error message and redisplayes
   * the message which was active before the error message was set.
   */
  public void clearErrorMessage()
  {
    if (fMessageLine != null)
      fMessageLine.clearErrorMessage();
  }
  /**
   * Clears the currently displayed message.
   */
  public void clearMessage()
  {
    if (fMessageLine != null)
      fMessageLine.clearMessage();
  }
  /**
   * Get the currently displayed error text.
   * @return The error message. If no error message is displayed <code>null</code> is returned.
   */
  public String getErrorMessage()
  {
    if (fMessageLine != null)
      return fMessageLine.getErrorMessage();
    else
      return null;
  }
  /**
   * Get the currently displayed error text.
   * @return The error message. If no error message is displayed <code>null</code> is returned.
   */
  public SystemMessage getSystemErrorMessage()
  {
    if (fMessageLine != null)
      return fMessageLine.getSystemErrorMessage();
    else
      return null;
  }
  /**
   * Get the currently displayed message.
   * @return The message. If no message is displayed <code>null<code> is returned.
   */
  public String getMessage()
  {
    if (fMessageLine != null)
      return fMessageLine.getMessage();
    else
      return null;
  }
  /**
   * Display the given error message. A currently displayed message
   * is saved and will be redisplayed when the error message is cleared.
   */
  public void setErrorMessage(String message)
  {
    if (fMessageLine != null)
      fMessageLine.setErrorMessage(message);
    else
      SystemMessageDialog.displayErrorMessage(getShell(),message);
  }

  /**
   * Display the given error message. A currently displayed message
   * is saved and will be redisplayed when the error message is cleared.
   */
  public void setErrorMessage(SystemMessage message)
  {
    if (fMessageLine != null)
    {
      if (message != null)
        fMessageLine.setErrorMessage(message);
      else
        fMessageLine.clearErrorMessage();
    }
    else //if (message != null)
    {
      //(new SystemMessageDialog(getShell(),message)).open();
      pendingErrorMessage = message;
    }
  }
  /**
   * Set the message text. If the message line currently displays an error,
   * the message is stored and will be shown after a call to clearErrorMessage
   */
  public void setMessage(String message)
  {
    if (fMessageLine != null)
    {
      if (message != null)
        fMessageLine.setMessage(message);
      else
        fMessageLine.clearMessage();
    }
  }

  /**
   *If the message line currently displays an error,
   * the message is stored and will be shown after a call to clearErrorMessage
   */
  public void setMessage(SystemMessage message)
  {
    if (fMessageLine != null)
      fMessageLine.setMessage(message);
    else if (message != null)
      //(new SystemMessageDialog(getShell(),message)).open();
      pendingMessage = message;
  }


  /**
   * Convenience method to set an error message from an exception
   */
  public void setErrorMessage(Throwable exc)
  {
    if (fMessageLine != null)
      fMessageLine.setErrorMessage(exc);
    else
    {
      SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_UNEXPECTED);
      msg.makeSubstitution(exc);
      (new SystemMessageDialog(getShell(),msg)).open();
    }
  }


  /**
   * The application entry point
   * 
   * @param args
   *            the command line arguments
   */
  public static void main(String[] args) {
    new FileSystemsFileChooser(null, false).run();
  }
}