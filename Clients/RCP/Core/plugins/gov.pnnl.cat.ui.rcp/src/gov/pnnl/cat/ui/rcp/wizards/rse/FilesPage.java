package gov.pnnl.cat.ui.rcp.wizards.rse;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.rse.filechooser.SelectFilesGroup;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.wizards.ResourceTreeDialog;
import gov.pnnl.velo.model.ACE;
import gov.pnnl.velo.model.CmsPath;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author D3K339
 *
 */
public abstract class FilesPage extends WizardPage {

	//  private static final String[] PUBLISH_ITEMS = {"Now", "In 6 months", "In 1 year"};
	protected static Logger logger = CatLogger.getLogger(FilesPage.class);
	
  private IWorkbenchWindow workbenchWindow;
  protected Text destinationText;
  protected IStructuredSelection selection;
  protected IResource containerFolder;
  protected boolean validFolder;
  protected Object catTreeRoot = null;
  protected boolean showFiles = true;
  
  private Composite body;
	private SelectFilesGroup selectFilesGroup;
	private boolean visited = false;
	
	CLabel info;

	public FilesPage(String pageName, String description, IWorkbenchWindow workbenchWindow, IStructuredSelection selection) {
    super(pageName);
    this.workbenchWindow = workbenchWindow;

    this.selection = selection;
    setTitle("File Chooser");
    setDescription(description);
	}

	public void createControl(Composite parent) {

		this.body = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 10;
		layout.marginWidth = 16;
		layout.horizontalSpacing = 16;
		layout.verticalSpacing = 12;
		body.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		body.setLayoutData(gd);

		// Destination
    final Label destinationLabel = new Label(body, SWT.BOLD);
    destinationLabel.setText("Destination:");

    destinationText = new Text(body, SWT.BORDER);
    destinationText.setEditable(false);
    destinationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    final ResourceTreeDialog catResourceDialog = new ResourceTreeDialog(this.workbenchWindow.getShell(), catTreeRoot, showFiles, false);

    final Button browseButton = new Button(body, SWT.NONE);
    browseButton.setLayoutData(new GridData(76, SWT.DEFAULT));
    browseButton.setText("Br&owse...");
    browseButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        try {
          IResourceManager mgr = ResourcesPlugin.getResourceManager();
          IResource selResource = null;
          //if a folder is already selected, use it to start the browse tree
          if (validFolder) {
            selResource = mgr.getResource(getDestinationFolder());
          } else {
            //else init the tree to just the users home folder.
            selResource = mgr.getHomeFolder();
          }

          if (selResource != null) {
            catResourceDialog.setInitialSelection(selResource);
          }
        } catch (Exception ex) {
          //EZLogger.logError(ex, "Could not open resource chooser dialog.");
          logger.error("Could not open resource chooser dialog",ex);
        }
        if (catResourceDialog.open() == Dialog.OK && catResourceDialog.getSelectedResource() != null) {
          containerFolder = catResourceDialog.getSelectedResource();
          destinationText.setText(containerFolder.getPath().toDisplayString());
          setPageComplete(isPageComplete());
        }
      }
    });

    ACE allUsers = new ACE("GROUP_EVERYONE", "Consumer", ACE.ACCESS_STATUS_ALLOWED);
		ACE currentUser = new ACE(((ImportFilesWizard)getWizard()).getACL().getOwner(), "Coordinator", ACE.ACCESS_STATUS_ALLOWED);
    ACE[] aces = {allUsers, currentUser};
    ((ImportFilesWizard)getWizard()).getACL().setAces(aces); 
  			
		// Add a table here that lists the selected files
    Label filesToImport = new Label(body, SWT.NONE);
    filesToImport.setText("Files to Import:");
    gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
    filesToImport.setLayoutData(gd);
    
		selectFilesGroup = new SelectFilesGroup(body, SWT.NONE, this);
    gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    gd.horizontalSpan = 3;
		selectFilesGroup.setLayoutData(gd);
		selectFilesGroup.setSpecialFileFilters(
		    new String[]{ImportFilesWizard.METADATA_FILENAME_REGEX});
	
		initialize();
		setControl(body);
		// don't show an error message right away.
		setErrorMessage(null);
	
	}
	
  /**
   * Method getDestination.
   * @return String
   */
  public String getDestination() {
    return destinationText.getText();
  }
  
  /**
   * Method getDestinationFolder.
   * @return CmsPath
   */
  public CmsPath getDestinationFolder() {
    return new CmsPath(getDestination());
  }
	
  private void initialize() {
    if (selection != null && selection.isEmpty() == false
        && selection instanceof IStructuredSelection) {
      IStructuredSelection ssel = (IStructuredSelection) selection;
      if (ssel.size() > 1)
        return;
      IResource resource = RCPUtil.getResource(ssel.getFirstElement());
      IFolder container = null;
      try {
        if(resource instanceof ILinkedResource){
          resource = ((ILinkedResource)resource).getTarget();
        }
        
        if (resource instanceof IFile) {
          container = (IFolder) resource.getParent();
        }else if (resource instanceof IFolder) {
          container = (IFolder) resource;
        }
      } catch (ResourceException e) {
        // TODO Auto-generated catch block
        logger.error(e);
      }
     
      if(container != null){
        this.destinationText.setText(container.getPath().toDisplayString());
        this.validFolder = true;
        this.containerFolder = container;
      }
    }
  }


	private class BalloonMouseTip {

		private ToolTip tip;
		private StyledText widget;

		public BalloonMouseTip(StyledText widget, String tipText) {

			tip = new ToolTip(body.getShell(), SWT.BALLOON);
			tip.setMessage(tipText);
			this.widget = widget;
			widget.setCaret(null);

			// Make the styled text look like a label
			widget.setCursor(new Cursor(body.getDisplay(), SWT.CURSOR_ARROW));

			Listener openListener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					StyledText widget = BalloonMouseTip.this.widget;
					Point loc = widget.toDisplay(widget.getLocation());
					tip.setLocation(loc.x + widget.getSize().x - 40, loc.y - 90);
					tip.setVisible(true);
				}
			};
			Listener closeListener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					tip.setVisible(false);
				}
			};
			widget.addListener(SWT.MouseHover, openListener);
			widget.addListener(SWT.MouseExit, closeListener);
			widget.addListener(SWT.MouseDown, openListener);
		}

	}
	
  /**
   * @return the catTreeRoot
   */
  public Object getCatTreeRoot() {
    return catTreeRoot;
  }

  /**
   * @param catTreeRoot the catTreeRoot to set
   */
  public void setCatTreeRoot(Object catTreeRoot) {
    this.catTreeRoot = catTreeRoot;
  }
	
	@Override 
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		visited = true;
	}

	public Map<IHost, String> getImportActions() {
	  return selectFilesGroup.getActions();
	}

	public Map<IHost, List<IRemoteFile>> getSelectedFiles() {
		return selectFilesGroup.getSelectedFiles();
	}
		
	public IRemoteFile getMetaDataFile() {
		return selectFilesGroup.getSpecialFiles().get(ImportFilesWizard.METADATA_FILENAME_REGEX);
	}


	@Override
	public boolean isPageComplete() {
		if(!visited) {
			return false;
		}
		boolean pageComplete = true;
		
		boolean filesFound = (selectFilesGroup.getSelectedFiles() != null && !selectFilesGroup.getSelectedFiles().isEmpty());  

		if(this.getDestination().length() == 0){
      setErrorMessage("Destination must be specified");
      pageComplete = false;
    } else if(!filesFound) {
		  setErrorMessage("No files selected...");
		  pageComplete = false;
    } else{
      String msg = validateSelectedFiles();
      if(msg != null) {
        setErrorMessage(msg);
        pageComplete = false;
      } else {
        setErrorMessage(null);
      }
    }

		
		return pageComplete;
	}

  /**
   * Child classes need to validate if the selected file(s) and destination folder(s) are
   * ok.
  
   * @return false if selection is not valid */
  protected abstract String validateSelectedFiles();
}