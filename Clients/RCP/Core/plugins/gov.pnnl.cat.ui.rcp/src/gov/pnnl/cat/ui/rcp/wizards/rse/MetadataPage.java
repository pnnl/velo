package gov.pnnl.cat.ui.rcp.wizards.rse;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.cat.rse.filechooser.FileSystemsFileChooser;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.wizards.FileExportWizard;
import gov.pnnl.velo.model.CmsPath;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.statushandlers.StatusManager;

public class MetadataPage extends WizardPage {

  protected static Logger logger = CatLogger.getLogger(MetadataPage.class);
  private IWorkbenchWindow workbenchWindow;
  private Text globalMetaFileText;
  private IRemoteFile globalMetaFile;
  private Button browseGlobalMetaFileButton;
  private Composite body;
  
  // Protocols that support us bringing back a remote metadata file
  private String[] allowedHostTypes = new String[] {
      RSEUtils.SYSTEM_TYPE_SSH,
      RSEUtils.SYSTEM_TYPE_LOCAL,
      RSEUtils.SYSTEM_TYPE_SSH_PLUS_GLOBUS_ONLINE};
  
  private boolean visited = false;

  public MetadataPage(String pageName, IWorkbenchWindow workbenchWindow) {
    super(pageName);
    this.workbenchWindow = workbenchWindow;
  }

  @Override
  public void createControl(Composite parent) {
    this.body = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    layout.marginHeight = 10;
    layout.marginWidth = 12;
    layout.horizontalSpacing = 20;
    body.setLayout(layout);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    body.setLayoutData(gd);

    Label label = new Label(body, SWT.NONE);
    label.setText("Metadata");
    label.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 2, 1));
    FontData fontData = label.getFont().getFontData()[0];
    Font font = new Font(body.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
    label.setFont(font);
    
    
    CLabel info = new CLabel(body, SWT.WRAP);
    GridData d = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
    d.verticalAlignment = SWT.TOP;
    StringBuilder builder = new StringBuilder();
    builder.append("This file specifies global metadata that applies to the whole file set.\n");
    builder.append("Please download and fill out the supplied template:\n");
    //builder.append("Add additional rows with a property name and value to specify additional metadata.");   //for now only supporting defined props, no ad-hoc
    info.setLayoutData(d);
    info.setText(builder.toString());
    
    
 // Download Metadata Template File
    ImageHyperlink downloadTemplate = new ImageHyperlink(body, SWT.NONE);
    downloadTemplate.setText("Download Metadata Template File");
    downloadTemplate.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE));
    downloadTemplate.addHyperlinkListener(new HyperlinkAdapter() {
      public void linkActivated(HyperlinkEvent e) {
        // invoke file export wizard
        
        // Set the selection to the template
        IResource template = ResourcesPlugin.getResourceManager().getResource(((ImportFilesWizard)getWizard()).getMetadataTemplatePath());
        IStructuredSelection selection = new StructuredSelection(template);

        try {

          FileExportWizard wizard = new FileExportWizard(RCPUtil.getTreeRoot(), false);
          wizard.init(PlatformUI.getWorkbench(), selection);
          
          // Instantiates the wizard container with the wizard and opens it
          WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);      
          dialog.create();
          dialog.open();

        } catch (Throwable ex) {
          StatusUtil.handleStatus(
              "An unexpected error occurred! See the client log for details.",
              ex, StatusManager.SHOW);
        }

      }
    });   
    
    
    label = new Label(body, SWT.NONE);
//    label = new Label(body, SWT.NONE);
    
    
    
    this.globalMetaFileText = new Text(body, SWT.BORDER);
    gd = new GridData(SWT.FILL, SWT.NONE, true, false);
    globalMetaFileText.setLayoutData(gd);
    globalMetaFileText.setEditable(false);
    
    this.browseGlobalMetaFileButton = new Button(body,  SWT.NONE);
    gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
    gd.widthHint = 76;
    this.browseGlobalMetaFileButton.setLayoutData(gd);
    browseGlobalMetaFileButton.setText("Browse...");
    
    this.browseGlobalMetaFileButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        FileSystemsFileChooser fileSelectionTree = new FileSystemsFileChooser(getShell(), allowedHostTypes, false);
        Map<IHost, List<IRemoteFile>> selections = fileSelectionTree.run();
        // TODO: make this only allow one file to be chosen
        // for now we get the first one
        outerloop: for(IHost host : selections.keySet()) {

          for(IRemoteFile remoteFile : selections.get(host)) {
            globalMetaFile = remoteFile;
            globalMetaFileText.setText(remoteFile.getAbsolutePath());
            break outerloop;
          }
        }

        setPageComplete(isPageComplete());
      }
    });
    
    
    
    setControl(body);
    // don't show an error message right away.
    setErrorMessage(null);
  }

	@Override 
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		visited = true;
	}
  
  public void setMetaFile(IRemoteFile file) {
    globalMetaFileText.setText(file.getAbsolutePath());
    globalMetaFile = file;
    setPageComplete(isPageComplete());
  }

  
  public IRemoteFile getMetaFile() {
    return globalMetaFile;
  }

  @Override
  public boolean isPageComplete() {
    // metadata not required by default
    // TODO: should add a setting whether metadata is required or not
    return true;
  }

  
  
  
  
}