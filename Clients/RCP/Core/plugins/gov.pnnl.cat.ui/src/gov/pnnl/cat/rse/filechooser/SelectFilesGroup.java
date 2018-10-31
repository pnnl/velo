package gov.pnnl.cat.rse.filechooser;

import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.ui.view.SystemViewLabelAndContentProvider;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Section for wizard pages that lets user select from local or remote files and
 * shows a summary of selected files.
 * @author D3K339
 *
 */
public class SelectFilesGroup extends Composite {
  
  public static String ACTION_UPLOAD = "Upload";
  public static String ACTION_LINK = "Link";
  
  public static String TABLE_ITEM_KEY_HOST = "host";
  public static String TABLE_ITEM_KEY_ACTION = "action";  
  
  private Map<IHost, List<IRemoteFile>> selectedFiles;
  private Map<IHost, String> actions;
  
  // User can pass in special file name filter to be able to identify specific files from the selected files
  private Map<String, IRemoteFile> specialFiles;
  private Map<String, Pattern> patterns;
  
  private TableViewer filesSummaryTable;
  private Button selectFilesButton;
  private WizardPage wizardPage; // TODO: this should be changed to a listener so we can be used regardless of whether in wizard or not
  
  public SelectFilesGroup(Composite parent, int style, final WizardPage wizardPage) {
    super(parent, style);
    this.wizardPage = wizardPage;
    setLayout(new GridLayout(2, false));
    
    TableViewer tv = new TableViewer(this, SWT.BORDER);
    filesSummaryTable = tv;
    tv.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    tv.getTable().setHeaderVisible(true);

    Layout layout = tv.getTable().getLayout();
    
    TableViewerColumn tc1 = new TableViewerColumn(tv, SWT.LEFT);
    tc1.getColumn().setText("From Machine");
    tc1.getColumn().setWidth(220);
        
    TableViewerColumn tc2 = new TableViewerColumn(tv, SWT.LEFT);
    tc2.getColumn().setText("Total Size of Data");
    tc2.getColumn().setWidth(220);
    
    TableViewerColumn tc3 = new TableViewerColumn(tv, SWT.LEFT);
    tc3.getColumn().setText("Import Action");
    tc3.getColumn().setWidth(100);
    
    tv.getTable().addListener(SWT.MeasureItem, new Listener() {
      public void handleEvent(Event event) {
         // height cannot be per row so simply set
         event.height = 35;
      }
   });
    
    selectFilesButton = new Button(this, SWT.NONE);
    selectFilesButton.setText("Select files...");
    selectFilesButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

    this.selectFilesButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        FileSystemsFileChooser fileSelectionTree = new FileSystemsFileChooser(getShell(), false);
        Map<IHost, List<IRemoteFile>> selections = fileSelectionTree.run();
        
        // Clear the selections every time
        if(selections != null) {
          selectedFiles = selections;
          actions = new HashMap<IHost, String>();
          populateSelectedFileSummary();
        }
        // TODO: this should be changed to a listener so we can be used regardless of whether in wizard or not
        wizardPage.setPageComplete(wizardPage.isPageComplete());
      }
    });

  }

  public void setSpecialFileFilters(String[] regexes) {
    // create a pattern for each filter
    this.patterns = new HashMap<String, Pattern>();
    
    for(String regex : regexes) {
      Pattern pattern = Pattern.compile(regex);
      patterns.put(regex, pattern);
    }
    specialFiles = new HashMap<String, IRemoteFile>();
  }
  
  /**
   * For now I am assuming there will only be one file with the same "special" name in the selected file set.
   * Later we can change this to return a map of lists if needed
   * @return
   */
  public Map<String, IRemoteFile> getSpecialFiles() {
    return specialFiles;
  }
 

  public Map<IHost, List<IRemoteFile>> getSelectedFiles() {
    return selectedFiles;
  }
  
  public Map<IHost, String> getActions() {
    return actions;
  }

  private void populateSelectedFileSummary() {
    final Table table = filesSummaryTable.getTable();
    // first we need to remove all previous editors and table items
    // (note removing the table item from the table will NOT dispose of its editors :( )
    for(TableItem item : table.getItems()) {
      TableEditor editor = (TableEditor) item.getData("ComboEditor");
      editor.getEditor().dispose();
      editor.dispose();
    }
    table.removeAll();
    
    SystemViewLabelAndContentProvider labelProvider = new SystemViewLabelAndContentProvider();
    
    for(final IHost host : selectedFiles.keySet()) {
      final List<IRemoteFile> files = selectedFiles.get(host);
      
      if(files != null && files.size() > 0) {
        // machine name
        final TableItem tableItem = new TableItem (table, SWT.NONE);
        tableItem.setImage(labelProvider.getImage(host));
        tableItem.setText(0, host.getName()); 
        tableItem.setData(TABLE_ITEM_KEY_HOST, host);

        // total size
        // Need to run this in a progress monitor since it could be slow
        final String[] sizeSummary = new String[1];
        final SizeInfo[] sizeInfo = new SizeInfo[1];
        try {
          wizardPage.getWizard().getContainer().run(true, true, new IRunnableWithProgress(){
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
              monitor.beginTask("Counting selected files...", IProgressMonitor.UNKNOWN);
              SizeInfo sizes = getFileSizeSummary(host, files, monitor);
              sizeInfo[0] = sizes; 
              sizeSummary[0] = sizes.numFiles + " files / " + sizes.numFolders + " folders / " + FileUtils.byteCountToDisplaySize(sizes.bytes);
              
              if(monitor.isCanceled()) {
                sizeSummary[0] += " - count interrupted";
                // TODO: only allow link if they interrupt because it's probably really big
              }
            }
          });
        } catch (Exception e) {
          ToolErrorHandler.handleError("Failed to browse remote files.", e, true);
        } 
        
        tableItem.setText(1,sizeSummary[0]);
        
        // action
        TableEditor editor = new TableEditor(table);
        editor.grabHorizontal = true;
        editor.grabVertical = true;
        Composite composite = new Composite(table, SWT.None);
        composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        GridLayout layout = new GridLayout(1, true);
        layout.verticalSpacing = 5;
        composite.setLayout(layout);
        Combo combo = new Combo(composite, SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        String[] items;
        editor.setEditor(composite, tableItem, 2);
        combo.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            Combo combo = (Combo)e.getSource();
            tableItem.setText(2, combo.getText());
            tableItem.setData(TABLE_ITEM_KEY_ACTION, combo.getText());
            actions.put(host, combo.getText());
          }
        });
        // need to associate the combo editor with the table item so we can properly dispose of it before redrawing the table
        tableItem.setData("ComboEditor", editor);        
        
        if(host.getName().equalsIgnoreCase("local")) {
          items = new String[] {ACTION_UPLOAD};
          
        } else if (sizeInfo[0].bytes > 1000000000) {
          items = new String[] {ACTION_LINK};
          
        } else if(host.getSystemType().getId().contains("Globus")) {
          // TODO: for now, if we are using a globus endpoint, we can only link because
          // we don't have web service on Velo server yet that can ingest the files from 
          // the pic#dtn endpoint after transfer is complete
         items = new String[] {ACTION_LINK};
          
        } else {
          items = new String[] {ACTION_UPLOAD, ACTION_LINK};
        }
        
        combo.setItems(items);
        combo.select(0);
        actions.put(host, items[0]);

      }
    }    

  }
  
  private SizeInfo getFileSizeSummary(IHost host, List<IRemoteFile> files, IProgressMonitor monitor) {
    
    SizeInfo sizes = new SizeInfo(host, monitor);
    
    for(IRemoteFile file : files) {
      sizes.updateSize(file);
    }

    return sizes;
  }
  
  private class SizeInfo {
    Map<String, String> countedFiles = new HashMap<String, String>();
    IHost host;
    IRemoteFileSubSystem fss;
    IProgressMonitor monitor;
    
    public SizeInfo(IHost host, IProgressMonitor monitor) {
      IRemoteFileSubSystem fss = RSEUtils.getFileSubSystem(host);
      this.monitor = monitor;
    }
    
    int numFiles = 0;
    int numFolders = 0;
    long bytes = 0;
    
    public void updateSize(IRemoteFile file) {
      if(monitor.isCanceled()) {
        return;
      }
      String path = file.getAbsolutePath();
      
      if(countedFiles.containsKey(path)) {
        // skip this file because we already counted it
        return;
        
      } else {
        countedFiles.put(path, path);

        if(file.isDirectory()) {
          numFolders++;
          ISystemViewElementAdapter adapter = RSEUtils.getRseViewAdapter(file);
          for(Object child : adapter.getChildren((IAdaptable)file, monitor)) {
            if(monitor.isCanceled()) {
              return;
            }
            if(child instanceof IRemoteFile) {
              updateSize((IRemoteFile)child);
            } else {
              System.out.println(child.getClass().toString());
            }
          }
          
        } else {
          numFiles ++;
          bytes += file.getLength();
          
          //check to see if this is a special file
          for(String regex : patterns.keySet()) {
            Pattern pattern = patterns.get(regex);
            if(pattern.matcher(file.getName()).find()) {
              specialFiles.put(regex, file);
            }
          }
        }

      }
     
    }
    
  }
}
