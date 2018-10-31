package gov.pnnl.velo.dataset.views;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.SapphireEditorPagePart;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentOutline;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPagePart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.forms.widgets.TreeNode;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.velo.dataset.DatasetsPlugin;
import gov.pnnl.velo.dataset.service.DatasetService;
import gov.pnnl.velo.dataset.smartfolder.provider.MyDatasets;
import gov.pnnl.velo.dataset.util.DatasetConstants;
import gov.pnnl.velo.dataset.util.DatasetUtil;
import gov.pnnl.velo.model.ACE;
import gov.pnnl.velo.model.ACL;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.sapphire.dataset.DatasetMetadata;
import gov.pnnl.velo.sapphire.editor.VeloSapphireEditorForXml;
import gov.pnnl.velo.sapphire.osti.OSTIPublicationService;
import gov.pnnl.velo.util.VeloConstants;

public class StepsToPublish extends ViewPart implements IPartListener, IResourceEventListener {
  public static final String ID = StepsToPublish.class.getName();
  private FormToolkit toolkit;
  private ScrolledForm form;
  private Section section;
  private IFolder dataset;
  private boolean stepsViewInitialized;
  private boolean validMetadata;
  private String datasetSize = null;

  public StepsToPublish() {
    // TODO let this view listen for resource events and re-draw itself when the user has (for example) uploaded
    // more files, filled out more required fields, etc.
  }

  @Override
  public void createPartControl(Composite parent) {
    this.toolkit = new FormToolkit(Display.getCurrent());
    this.form = toolkit.createScrolledForm(parent);
    TableWrapLayout layout = new TableWrapLayout();
    layout.leftMargin = 10;
    layout.rightMargin = 10;
    form.getBody().setLayout(layout);
    // listen to perspective and AFTER dataset editor is opened, load this view's text so we can
    // examine required field state from sapphire to set message text.
    // loadFormContent();
    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(this);
    ResourcesPlugin.getResourceManager().addResourceEventListener(this);
  }

  private void loadFormContent() {

    if (dataset == null) {
      // section.setText("Steps for publishing a dataset");
      // section.setDescription("Please select a dataset");
      // TODO - minimize this part when dataset is not selected
      // TODO - SapphireEditor part to display similar message
      // TODO - update content of this viewpart when user browses a dataset

    } else {
      
//      if (section != null)
//        section.dispose();

      this.section = toolkit.createSection(form.getBody(), Section.DESCRIPTION);
      toolkit.createCompositeSeparator(section);
      FormText rtext = toolkit.createFormText(section, false);
      section.setClient(rtext);
      section.setText("Steps for publishing a dataset");
      section.setDescription("Follow the general guidence below in order to publish your dataset to OSTI.");
      loadFormText(rtext, toolkit, dataset);
      TableWrapData td = new TableWrapData();
      td.align = TableWrapData.FILL;
      td.grabHorizontal = true;
      section.setLayoutData(td);
      form.reflow(true);
    }
  }

  private void loadFormText(final FormText rtext, FormToolkit toolkit, IFolder dataset) {
    // load dataset state info to use to determine if step gets a checkmark, font colors, messages, etc.
    // IFolder dataset = DatasetUtil.getSelectedDatasetInActiveWindow();

    IResource dataFolder = ResourcesPlugin.getResourceManager().getResource(dataset.getPath().append("Data"));
    ArrayList<Long> stats = DatasetService.getInstance().getDatasetFileStats(dataFolder.getPropertyAsString(VeloConstants.PROP_UUID));

    String fileInfo = getFileInfo(stats);
    String fileInfoColor = "green";
    if (stats.get(0) == 0) {
      fileInfoColor = "red";
    }

    String draftDOIColor = "orange";
    String DOI = null;
    boolean DOIFinal = false;
    String draftDOIInfo = "Does not have DOI";
    if (dataset.hasAspect(DatasetConstants.ASPECT_DOI)) {
      DOI = dataset.getPropertyAsString(DatasetConstants.PROP_DOI);
      if (dataset.getPropertyAsString(DatasetConstants.PROP_DOI_STATE).equalsIgnoreCase(DatasetConstants.DOI_STATE_FINAL)) {
        draftDOIInfo = "FINAL: " + DOI;
        draftDOIColor = "black";
        DOIFinal = true;
      } else {
        draftDOIInfo = DOI;
        draftDOIColor = "green";
      }
    }

    rtext.addHyperlinkListener(new SapphireHyperLinkAdapter(rtext.getShell(), dataset.getPath()));

    rtext.setHyperlinkSettings(toolkit.getHyperlinkGroup());

    if (validMetadata)
      rtext.setImage("checkStatusDescribeData", DatasetsPlugin.getDefault().getImage(DatasetsPlugin.IMAGE_CHECKED, 16));

    if (stats.get(0) > 0) {
      rtext.setImage("checkStatusForUploadData", DatasetsPlugin.getDefault().getImage(DatasetsPlugin.IMAGE_CHECKED, 16));
    }

    if (DOI != null) {
      rtext.setImage("checkStatusForDraftDOI", DatasetsPlugin.getDefault().getImage(DatasetsPlugin.IMAGE_CHECKED, 16));
      if (DOIFinal) {
        rtext.setImage("checkStatusForPublish", DatasetsPlugin.getDefault().getImage(DatasetsPlugin.IMAGE_CHECKED, 16));
      }
    }

    rtext.setImage("help", DatasetsPlugin.getDefault().getImage(DatasetsPlugin.IMAGE_HELP, 16));

    createCompositeControl(rtext, toolkit);
    createControl(rtext, toolkit);

    byte[] bytes = null;
    InputStream is = null;
    try {
      is = StepsToPublish.class.getResourceAsStream("stepsToPublishTemplate.xml");
      bytes = IOUtils.toByteArray(is);
      // first parse xml to find/replace status text placeholders:
      String templateString = new String(bytes);
      // find/replace Step2Status
      if (validMetadata)
        templateString = templateString.replaceFirst("describeDataStatus", "<span color=\"green\">All mandatory fields complete</span>");
      else
        templateString = templateString.replaceFirst("describeDataStatus", "<span color=\"red\">Missing required metadata</span>");
      
      // find/replace Step3Status
      templateString = templateString.replaceFirst("uploadDataStatus", "<span color=\"" + fileInfoColor + "\">" + fileInfo + "</span>");
      
      
      // find/replace Step4Status
      templateString = templateString.replaceFirst("draftDOIStatus", "<span color=\"" + draftDOIColor + "\">" + draftDOIInfo + "</span>");
      if(DOIFinal){
        templateString = templateString.replace("<a href=\"draftDOI\">Get a draft DOI</a>", "Get a draft DOI");
      }

      rtext.setText(templateString, true, true);

      FormColors colors = toolkit.getColors();
      colors.createColor("red", colors.getSystemColor(SWT.COLOR_RED));
      rtext.setColor("red", colors.getColor("red"));

      colors.createColor("green", colors.getSystemColor(SWT.COLOR_DARK_GREEN));
      rtext.setColor("green", colors.getColor("green"));

      colors.createColor("orange", colors.getSystemColor(SWT.COLOR_DARK_YELLOW));
      rtext.setColor("orange", colors.getColor("orange"));

      colors.createColor("black", colors.getSystemColor(SWT.COLOR_BLACK));
      rtext.setColor("black", colors.getColor("black"));
    } catch (Exception e1) {
      e1.printStackTrace();
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e1) {
        }
      }
    }

    Menu menu = new Menu(rtext);
    final MenuItem item = new MenuItem(menu, SWT.PUSH);
    item.setText("&Copy");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        rtext.copy();
      }
    });
    // rtext.setMenu(menu);
    item.setEnabled(false);
    rtext.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        item.setEnabled(rtext.canCopy());
      }
    });

  }

  private String getFileInfo(ArrayList<Long> stats) {

    long numChildren = stats.get(0);
    long filesSize = stats.get(1);
    String fileInfo = "";
    if (filesSize == 0) {
      fileInfo = numChildren + " Files (0 KB)";
    } else {
      datasetSize = numChildren + " Files (";
      if (filesSize > 1000) {
        // display files size as MB if its more than 1 meg
        filesSize = filesSize / 1000;
        datasetSize += filesSize + " MB)";
      } else if (filesSize > 1000000) {
        // display files size as GB if its more than 1 gig
        filesSize = filesSize / 1000000;
        datasetSize += filesSize + " GB)";
      } else {
        datasetSize += filesSize + " KB)";
      }

      fileInfo = datasetSize + " uploaded";
    }
    return fileInfo;
  }

  private static void createControl(FormText ftext, FormToolkit toolkit) {
    TreeNode node = new TreeNode(ftext, SWT.NULL);
    toolkit.adapt(node, true, true);
    ftext.setControl("node", node);
  }

  private static void createCompositeControl(final FormText ftext, FormToolkit toolkit) {
    Composite comp = toolkit.createComposite(ftext);
    GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    comp.setLayout(layout);
    toolkit.createLabel(comp, "Sample text:");
    Text text = toolkit.createText(comp, "");
    text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Button browsebutton = toolkit.createButton(comp, "Browse...", SWT.PUSH);
    browsebutton.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent event) {
        // for some reason getSelection is returning false even though
        // the button was in fact selected
        // if (browsebutton.getSelection()) {
        // MessageDialog.openInformation(ftext.getShell(), "Eclipse
        // Forms",
        // "Browse button clicked");
        // }
        System.out.println("widgetSelected event on browse button");
      }

      public void widgetDefaultSelected(SelectionEvent event) {
      }
    });
    ftext.setControl("control1", comp);
    toolkit.paintBordersFor(comp);
  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub

  }

  @Override
  public void partActivated(IWorkbenchPart part) {
    if (stepsViewInitialized)
      return;

    if (part instanceof SapphireEditor) {
      SapphireEditor editor = (SapphireEditor) part;
      this.dataset = DatasetUtil.getSelectedDatasetInActiveWindow();
      evaluateMetadataCompleteness(editor);
      loadFormContent();
      stepsViewInitialized = true;
    }
  }

  private void evaluateMetadataCompleteness(SapphireEditor editor) {
    validMetadata = true;

    List<SapphireEditorPagePart> editorPageParts = editor.getEditorPageParts();
    System.out.println("number of parts: " + editorPageParts.size());

    partLoop: for (SapphireEditorPagePart editorPagePart : editorPageParts) {
      // TODO need to learn more about sapphire and see if we should look at each 'editor page part' or just a specific one
      if (editorPagePart instanceof MasterDetailsEditorPagePart) {
        MasterDetailsEditorPagePart detailsPart = (MasterDetailsEditorPagePart) editorPagePart;
        ProblemsTraversalServiceData problems = new ProblemsTraversalServiceData(detailsPart, new Listener() {
          public void handle(Event event) {
          }
        });

        for (MasterDetailsContentNodePart node : detailsPart.outline().getRoot().nodes().visible()) {
          MasterDetailsContentNodePart error = problems.findNextError(node);
          if (error != null) {
            validMetadata = false;
            break partLoop;
          }
        }

      }
    }
  }

  @Override
  public void partBroughtToTop(IWorkbenchPart part) {
    // no custom action
  }

  @Override
  public void partClosed(IWorkbenchPart part) {
    // no custom action
  }

  @Override
  public void partDeactivated(IWorkbenchPart part) {
    // no custom action
  }

  @Override
  public void partOpened(IWorkbenchPart part) {
    // no custom action
  }

  // methods for IResourceEventListener
  @Override
  public void onEvent(IBatchNotification events) {
    System.out.println("Event Fired");
    if (this.dataset == null)
      return;

    Iterator<IResourceEvent> it = events.getNonRedundantEvents();
    IResourceEvent event;

    boolean reloadView = false;
    boolean verifyMetadata = false;

    while (it.hasNext()) {
      event = it.next();
      if (dataset.getPath().isPrefixOf(event.getPath())) {
        CmsPath metadataFile = dataset.getPath().append("Metadata").append("dataset.xml");
        // check if the change is in dataset.xml file if so verify metadata completeness
        if (metadataFile.isPrefixOf(event.getPath()))
          verifyMetadata = true;
        reloadView = true;
        break;
      }

    }

    if (verifyMetadata) {
      Display.getDefault().asyncExec(new Runnable() {
        @Override
        public void run() {
          IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
          for (IEditorReference editorReference : editorReferences) {
            IEditorPart editor = editorReference.getEditor(false);
            if (editor instanceof SapphireEditor)
              evaluateMetadataCompleteness((SapphireEditor) editor);
          }
          //TODO - if the below if statement is within loadFormContent, before createSection line,  
          // toolkit.createSection(form.getBody(), Section.DESCRIPTION); gives a IllegalArgumentException 
          //because form object used as first param is in disposed state. Not sure why it happens. 
          //moved the section.dispose here and the exception disappeared !!!!????
          if(section!=null && !section.isDisposed()){
            section.dispose();
          }
          loadFormContent();
        }
      });
    } else {
      if (reloadView) {
        Display.getDefault().asyncExec(new Runnable() {
          @Override
          public void run() {
            if(section!=null && !section.isDisposed()){
              section.dispose();
            }
            loadFormContent();
          }
        });
      }
    }

  }

  @Override
  public void cacheCleared() {
    // no custom action
  }

  private final class SapphireHyperLinkAdapter extends HyperlinkAdapter implements IRunnableWithProgress {
    private final Shell parent;
    private final CmsPath datasetPath;
    
    
    private VeloSapphireEditorForXml editor;
    private SapphireEditorPagePart editorPart;
    private boolean isFinal;

    public SapphireHyperLinkAdapter(Shell parent, CmsPath path) {
      this.parent = parent;
      this.datasetPath = path;
    }

    public void linkActivated(HyperlinkEvent e) {
      String href = e.getHref().toString();
      if (href.startsWith("help_")) {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp("gov.pnnl.velo.datasets." + href.substring("help_".length()));
      } else if (href.equalsIgnoreCase("datasetMetadata") || href.equalsIgnoreCase("landingPage") || href.equalsIgnoreCase("ostiInfo") || href.equalsIgnoreCase("draftDOI") || href.equalsIgnoreCase("publish")) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IEditorReference[] editors = window.getActivePage().getEditorReferences();
        // TODO - can I assume this is the only editor in this perspective ?
        for (int i = 0; i < editors.length; i++) {
          if (editors[i].getEditor(false) instanceof SapphireEditor) {
            editors[i].getEditor(true);
            VeloSapphireEditorForXml veloXmlEditor = (VeloSapphireEditorForXml) editors[i].getEditor(true);
            SapphireEditorPagePart editorPagePart = veloXmlEditor.getEditorPagePart("Dataset Metadata");

            if (editorPagePart == null) {
              // this can happen when the dataset was last closed with source page open
              // in this case form page won't be loaded till user switches to that tab
              // currently form page would be SapphireEditor$DeferredPage.
              // Below line force the form page to be made active and hence converted from
              // SapphireEditor$DeferredPage to MasterDetailEditorPagePart
              veloXmlEditor.setActivePage(0);
              editorPagePart = veloXmlEditor.getEditorPagePart("Dataset Metadata");
              veloXmlEditor.getEditorPageParts();
            }

            if (href.equals("draftDOI")) {
              publish(parent, veloXmlEditor, editorPagePart, false);
            }else if(href.equals("publish")){
              MessageDialog.openInformation(parent, "Publish Dataset", "This feature is not supported yet");
              //publish(parent, veloXmlEditor, editorPagePart, true);
            }else {
              window.getActivePage().setPartState(editors[i], IWorkbenchPage.STATE_RESTORED);

              MasterDetailsContentOutline outline = ((MasterDetailsEditorPagePart) editorPagePart).outline();

              switch (href) {
                case "datasetMetadata":
                  // maps to the label of the nodes in tree
                  outline.setSelection("Dataset Metadata/Citation Information");
                  break;
                case "landingPage":
                  outline.setSelection("Landing Page Configuration");
                  break;
                case "ostiInfo":
                  outline.setSelection("OSTI Metadata");
                  break;
              }
            }
            break;

          } // end of if sapphire editor
        } // end of editors loop
      }  else if (href.equals("dataFolder")) {
        // ImportFilesWizard wizard = new ImportFilesWizard();
        // Object[] segments = dataset.getPath().getSegments().toArray();
        // ??? How to create a TreeSelection using the dataset cms path. below doesn't work!
        // wizard.init(PlatformUI.getWorkbench(), new TreeSelection(new TreePath(segments )));
        // // Instantiates the wizard container with the wizard and opens it
        // WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
        // dialog.create();
        // dialog.open();
      }
    }


    private void publish(Shell parent, VeloSapphireEditorForXml editor, SapphireEditorPagePart editorPart, boolean isFinal) {
      this.editor = editor;
      this.editorPart = editorPart;
      this.isFinal = isFinal;
      
      
      if (isFinal && datasetSize == null) {
        // no data to publish
        MessageDialog.openError(parent, "No data to publish", "There is no data to publish under the data folder");
        return;
      }

      // force save as the underylying form object would have all un saved changes and that object is parsed to publish to osti
      // if user closed the dataset.xml without saving changes then the file in velo would not have the same information
      // that was published.
      if (editor.isDirty()) {
        boolean save = MessageDialog.openConfirm(parent, "Save changes and publish?", "Metadata has unsaved changes and needs to be saved before publishing. Do you want to save and publish? ");
        if (save) {
          editor.doSave(null);
        } else {
          return;
        }
      }
      // after saving check if metadata has issues
      if (!validMetadata) {
        // no data to publish
        MessageDialog.openError(parent, "Incomplete/Invalid metadata", "Incomplete/Invalid metadata: Please fix the errors in metadata form before publishing.");
        return;
      }
      
      try {
        new ProgressMonitorDialog(parent).run(true, true, this);
      } catch (InvocationTargetException | InterruptedException e) {
        StatusUtil.handleStatus("Exception", e, StatusManager.SHOW);
      }
      
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      
      if(isFinal)
        monitor.beginTask("Publishing final version to OSTI",2);
      else
        monitor.beginTask("Publishing draft to OSTI",1);
      if(isFinal){
        monitor.subTask("Setting Permissions");
        ACE aceGuest = new ACE();
        HashSet<ACE> aces = new HashSet<ACE>();
        ACL currentAcl = ResourcesPlugin.getSecurityManager().getPermissions(datasetPath);
        ACL acl = new ACL();
        aceGuest.setAuthority("guest");
        aceGuest.setPermission("Consumer");
        aceGuest.setAccessStatus(ACE.ACCESS_STATUS_ALLOWED);
        aces.add(aceGuest);

        ACE aceOwner = new ACE();     
        aceOwner.setAuthority("admin");
        aceOwner.setPermission("Coordinator");
        aceOwner.setAccessStatus(ACE.ACCESS_STATUS_ALLOWED);      
        aces.add(aceOwner); 

        ACE aceEveryone = new ACE();      
        aceEveryone.setAuthority("EVERYONE");
        aceEveryone.setPermission("Consumer");
        aceEveryone.setAccessStatus(ACE.ACCESS_STATUS_ALLOWED);     
        aces.add(aceEveryone); 


        acl.setAces(aces.toArray(new ACE[aces.size()]));

        acl.setInheritPermissions(false);
        try{
          ResourcesPlugin.getSecurityManager().setPermissions(new ACL[]{acl});
        } catch (Throwable e) {
          StatusUtil.handleStatus("Error saving permissions.",
              e, StatusManager.SHOW);
        }
        monitor.worked(1);
        
      }
      
      monitor.subTask("Publishing to OSTI");
      DatasetMetadata root = (DatasetMetadata) editorPart.getModelElement().root();
      OSTIPublicationService.getInstance().publishToOSTI(datasetPath, datasetSize, root, isFinal);
       //TODO-  in case of exception 
      //reset permissions for that folder
      monitor.done();
    }
  }

}
