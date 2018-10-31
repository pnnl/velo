package gov.pnnl.cat.rse.filechooser;


import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.cat.ui.preferences.PreferenceUtils;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.files.ui.view.SystemRemoteFileSelectionInputProvider;
import org.eclipse.rse.internal.ui.view.SystemViewDecoratingStyledCellLabelProvider;
import org.eclipse.rse.internal.ui.view.SystemViewLabelAndContentProvider;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Uses the CheckboxTreeViewer to allow multiple selection of files and folders for a given
 * file system.
 * 
 * http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/DemonstratesCheckboxTreeViewer.htm
 * 
 */
public class FileSystemComposite extends Composite implements ISystemResourceChangeListener {

  // Columns in the file browser
  public final static int FILE_NAME = 0;
  public final static int DATE_MODIFIED = 1;
  public final static int FILE_SIZE = 2;
  public final static int KIND = 3;

  private Text rootDir;
  private String rootDirPath = null;
  private CheckboxTreeSystemView fileSystemContentsViewer;
  private IHost host;
  private SystemViewLabelAndContentProvider treeContentProvider;
  private Collection expandedTreeNodes = new HashSet();
  private Map<Object, Boolean> expandedNodes = new HashMap<Object, Boolean>();
  private Shell shell;
  private boolean foldersOnly = true;

  /**
   * @param parentShell
   * @param defaultDirectory
   */
  public FileSystemComposite (Composite parent, IHost host, boolean foldersOnly) {
    super(parent, SWT.NONE);
    this.host = host;
    this.foldersOnly = foldersOnly;
    this.shell = parent.getShell();
    createContents();
  }

  protected void createContents() {
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    setLayout(layout);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    setLayoutData(gridData);

    Label label = new Label(this, SWT.NONE);
    label.setText("Go to directory");
    rootDir = new Text(this, SWT.BORDER);
    GridData rootData = new GridData(SWT.FILL, SWT.NONE, true, false);
    rootDir.setLayoutData(rootData);
    rootDir.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        rootDir.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        IRemoteFileSubSystem fss = RSEUtils.getFileSubSystem(host);
        IRemoteFile remoteFile;
        try {
          remoteFile = fss.getRemoteFileObject(rootDir.getText(), new NullProgressMonitor());
          if(remoteFile != null && remoteFile.exists()) {
            rootDir.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
            expandToPath(remoteFile);
            rootDirPath = rootDir.getText();
          }
        } catch (SystemMessageException ex) {
          ToolErrorHandler.handleError("Failed to find remote file.", ex, true);
        }    
      }
    });

    fileSystemContentsViewer = createFileSystemContentsTable(this);
    fileSystemContentsViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

    IRemoteFileSubSystem fileSS = RemoteFileUtility.getFileSubSystem(host);
    if(fileSS != null && fileSS.isConnected()) {
      connectionInitialized();
    }
    
    RSECorePlugin.getTheSystemRegistry().addSystemResourceChangeListener(this);
    
    
    /* IMPORTANT: Dispose the listeners */
    addListener(SWT.Dispose, new Listener() {
      @Override
      public void handleEvent(Event event) {
        // remove our system listener
        // apparently, dispose() is not called recursively on widgets in the hierarchy, so we have to add
        // dispose listener to hook into dispose event
        RSECorePlugin.getTheSystemRegistry().removeSystemResourceChangeListener(FileSystemComposite.this);
      }
    });
  }

  /* (non-Javadoc)
   * @see org.eclipse.rse.core.events.ISystemResourceChangeListener#systemResourceChanged(org.eclipse.rse.core.events.ISystemResourceChangeEvent)
   */
  @Override
  public void systemResourceChanged(ISystemResourceChangeEvent event) {
    if(event.getParent() instanceof FileServiceSubSystem) {
      FileServiceSubSystem fss = (FileServiceSubSystem)event.getParent();
      if(fss.getHost().equals(host)) {
        if(event.getType() == ISystemResourceChangeEvents.EVENT_ADD_FILTER_REFERENCE) {
          // For now if we get a system resource change event, let's just refresh this view
          fileSystemContentsViewer.refresh();              
        }
      }
    } 
  }
  
  /**
   * This is called after a connection is established with the host, so we can initialize host variables
   */
  public void connectionInitialized() {
    // look up the default directory from preferences per file system name
    
    String defaultDirectory = PreferenceUtils.getLastBrowsedPath(host.getName());
    if(defaultDirectory != null) {
      rootDir.setText(defaultDirectory);

    } else {
      // get the home folder from the file system
      IFileServiceSubSystem fss = RSEUtils.getFileSubSystem(host);
      rootDir.setText(fss.getFileService().getUserHome().getAbsolutePath());        
    }
  }

  public List<IRemoteFile> getCheckedElements() {
    Object[] selectedObjects = fileSystemContentsViewer.getCheckedElements();
    List<IRemoteFile> files = new ArrayList<IRemoteFile>();
    for(Object obj : selectedObjects) {
      if(obj instanceof IRemoteFile) {
        files.add((IRemoteFile)obj);
      }
    }
    
    // We assume that when we get the checked elements, we should save the last browsed path
    // for each host
    if(files.size() > 0) {
      // pick the first folder
      IRemoteFile selectedFile = files.get(0);
      String path;
      if(selectedFile.isDirectory()) {
        path = selectedFile.getAbsolutePath();
      } else {
        path = selectedFile.getParentPath();
      }
      PreferenceUtils.setLastBrowsedFile(host.getName(), path);
    
    } else if(rootDirPath != null) {
      PreferenceUtils.setLastBrowsedFile(host.getName(), rootDirPath);

    } 
    return files;
  }

  protected CheckboxTreeSystemView createFileSystemContentsTable(Composite parent) {

    SystemRemoteFileSelectionInputProvider inputProvider = new SystemRemoteFileSelectionInputProvider(host);
    treeContentProvider = new SystemViewLabelAndContentProvider();
    ILabelDecorator decorator = null;
    if (PlatformUI.isWorkbenchRunning()) {
      IWorkbench wb = PlatformUI.getWorkbench();
      decorator = wb.getDecoratorManager().getLabelDecorator();
    }
    // Create the tree viewer to display the file tree
    final CheckboxTreeSystemView tv = new CheckboxTreeSystemView(shell,
        parent, SWT.CHECK | SWT.BORDER, inputProvider, null, host, foldersOnly);
    //tv.getTree().setLinesVisible(true);
    tv.getTree().setHeaderVisible(true);

    tv.setContentProvider(treeContentProvider);
    final FileTreeLabelProvider labelProvider = new FileTreeLabelProvider(treeContentProvider);    
    tv.setLabelProvider(new SystemViewDecoratingStyledCellLabelProvider(labelProvider, decorator));


    String[] headings = new String[]{"Name", "Date Modified", "Size", "Kind"};
    int[] width = new int[]{340, 120, 80, 120};
    for(int i = 0; i < headings.length; i++) {
      TreeViewerColumn column = new TreeViewerColumn(tv, SWT.LEFT);
      column.getColumn().setAlignment(SWT.LEFT);
      column.getColumn().setText(headings[i]);
      column.getColumn().setWidth(width[i]);
      column.setLabelProvider(new CellLabelProvider() {
        @Override
        public void update(ViewerCell cell) {         
          cell.setText(labelProvider.getColumnText(cell.getElement(), cell.getColumnIndex()));
          Image image = labelProvider.getColumnImage(cell.getElement(), cell.getColumnIndex());
          if(image != null && !image.isDisposed()) {
            cell.setImage(image);
          } else if(image != null) {
            System.out.println("Trying to set disposed image");
          }
        }
      });
    }

    // When user checks/unchecks a checkbox in the tree, check/uncheck all its children
    // TODO: this isn't working right for remote folders because of deferred content provider returning "Pending..." for the contents,
    // which it can't find in the subtree, so it doesn't check children correctly
    // Look at org.eclipse.rse.internal.importexport.files.RemoteImportWizardPage1 for an example of using a a ResourceTreeAndListGroup
    // with input and content provider using MinimizedFileSystemElement to see if data has been loaded or not
    tv.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        Object treeElement = event.getElement();
        if(treeElement instanceof IRemoteFile) {
          IRemoteFile remoteFile = (IRemoteFile) event.getElement();
          if (!remoteFile.isRoot()) {        
            boolean checked = event.getChecked();
    
            tv.setSubtreeChecked(event.getElement(), checked);
            
            if(checked == false) {
              // make sure parent hierarcy is not checked since not all of its children are checked
              // TODO: should we display this as grayed box? - see ResourceTreeAndListGroup
              Object parent = treeContentProvider.getParent(event.getElement());
              while(parent != null && tv.getChecked(parent)) {
                tv.setChecked(parent, false);
                //tv.setGrayed(parent, true);
                parent = treeContentProvider.getParent(parent);
              }
            }
            
          } else {
            tv.setChecked(event.getElement(), false);
          }
        } else {
          tv.setChecked(event.getElement(), false);
        }
      }
    });
    
    tv.getTree().addTreeListener(new TreeListener() {
      
      @Override
      public void treeExpanded(TreeEvent e) {
        // if this is the first time we have expanded the node, then we need to push the selection do the children
        // we need to do this in case the remote items are already in the cache
        Object item = e.item.getData();
        if(expandedNodes.get(item) == null) {
          boolean checked = tv.getChecked(item);
          for(Object child : treeContentProvider.getChildren(item)) {
            tv.setChecked(child, checked);
          }
          expandedNodes.put(item, true);
        }
        
      }
      
      @Override
      public void treeCollapsed(TreeEvent e) {
      }
    });
    
    return tv;
  }

  public String getRootDirPath() {
    return rootDirPath;
  }

  
  private boolean expandToPath(Object selection) {

    ISystemViewElementAdapter adapter = RSEUtils.getRseViewAdapter(selection);

    if (adapter != null)
    {
      Object parent = adapter.getParent(selection);
      ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
      ISubSystem ss = adapter.getSubSystem(selection);
      IHost connection = ss.getHost();

      List filterRefs = registry.findFilterReferencesFor(selection, ss, false);

      if (filterRefs.size() > 0)
      {
        ISystemFilterReference ref = (ISystemFilterReference)filterRefs.get(0);
        fileSystemContentsViewer.expandTo(ref, selection);

        return true;
      }
      else
      {
        if (expandToPath(parent))
        {
          fileSystemContentsViewer.expandTo(parent, selection);
          return true;
        }       
      }
    }
    return false;
  }

  
}


/**
 * This class provides the labels for the file tree
 */

class FileTreeLabelProvider implements ITableLabelProvider, IStyledLabelProvider {

  private Map<String, Image> fileImages;
  private static String DIRECTORY = "directory";
  private static String UNKNOWN = "unknown";
  private SystemViewLabelAndContentProvider remoteSystemsLabelProvider;

  /**
   * Constructs a FileTreeLabelProvider
   */
  public FileTreeLabelProvider(SystemViewLabelAndContentProvider remoteSystemsLabelProvider) {
    fileImages = new HashMap<String, Image>();
    this.remoteSystemsLabelProvider = remoteSystemsLabelProvider;
  }

  /**
   * Gets the image to display for a node in the tree
   * 
   * @param arg0
   *            the node
   * @return Image
   */
  public Image getColumnImage(Object element, int columnIndex) {
    // See if we can't assign an image to the incoming object
    switch(columnIndex) {
      case FileSystemComposite.FILE_NAME: {
        return remoteSystemsLabelProvider.getColumnImage(element, columnIndex);
      }
    }

    return null;
  }

  /**
   * Gets the text to display for a node in the tree
   * 
   * @param arg0
   *            the node
   * @return String
   */
  public String getColumnText(Object element, int columnIndex) {
    // Get the name of the file
    // See if we can't assign an image to the incoming object
    switch(columnIndex) {
      case FileSystemComposite.FILE_NAME: {
        return remoteSystemsLabelProvider.getColumnText(element, columnIndex);
        
      } case FileSystemComposite.DATE_MODIFIED: {
        if(element instanceof IRemoteFile) {
          IRemoteFile file = (IRemoteFile)element;
          return new SimpleDateFormat().format(new Date(file.getLastModified()));
        }
        break;
        
      } case FileSystemComposite.FILE_SIZE: {
        if(element instanceof IRemoteFile) {
          IRemoteFile file = (IRemoteFile) element;
          double length = file.getLength();
          if(file.isDirectory()) {
            return "--";
          }
          String[] units = new String[]{"bytes", "KB", "MB", "GB"};
          int label = 0;
          while(length/1024 > 10 && label < 3) {
            label++;
            length /= 1024;
          }
          return new DecimalFormat("#.##").format(length) + " " + units[label];
        }
        break;
        
      } case FileSystemComposite.KIND:
        if(element instanceof IRemoteFile) {
          IRemoteFile file = (IRemoteFile) element;
          return URLConnection.guessContentTypeFromName(file.getName());
        }
        break;

    }
    return "";
  }

  /**
   * Adds a listener to this label provider
   * 
   * @param arg0
   *            the listener
   */
  public void addListener(ILabelProviderListener listener) {
    remoteSystemsLabelProvider.addListener(listener);
  }

  /**
   * Called when this LabelProvider is being disposed
   */
  public void dispose() {
    // Dispose the images
    for(Image image: fileImages.values())
      image.dispose();
  }

  /**
   * Returns whether changes to the specified property on the specified
   * element would affect the label for the element
   * 
   * @param arg0
   *            the element
   * @param arg1
   *            the property
   * @return boolean
   */
  public boolean isLabelProperty(Object element, String property) {
    return remoteSystemsLabelProvider.isLabelProperty(element, property);
  }

  /**
   * Removes the listener
   * 
   * @param arg0
   *            the listener to remove
   */
  public void removeListener(ILabelProviderListener listener) {
    remoteSystemsLabelProvider.removeListener(listener);
  }

  @Override
  public StyledString getStyledText(Object element) {
    return remoteSystemsLabelProvider.getStyledText(element);
  }

  @Override
  public Image getImage(Object element) {
    return remoteSystemsLabelProvider.getImage(element);
  }

}

