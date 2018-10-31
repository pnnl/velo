/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.ui.rcp.views.preview;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.TransformData;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.cat.ui.rcp.util.FormLayoutFactory;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.utils.CatUIUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import nu.psnet.quickimage.widgets.QuickImageCanvas;

import org.apache.log4j.Logger;
import org.apache.turbine.util.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.texteditor.FindReplaceAction;

/**
 */
public class FilePreviewPage implements IPreviewPage {

  private Composite control;
  private IResource resource;
  private IViewPart viewPart;

  private StackLayout stackLayout;

  private TextViewer bodyTextViewer;
  InputStream imageContent = null;     
  StringBuffer textContent = null;
  private FormToolkit toolkit;

  private Composite viewerBody;
  private Cursor cursor = null;
  private boolean wait = false;

  private final static String MSG_NO_RAW_TEXT = "The plain text extraction has not yet been executed for this node.  If the text does not appear within a few minutes, contact your CAT administrator.";

  // This list has been updated to reflect the same image extensions recognized by Alfresco
  // Note that it is a subset of the image formats recognized by Alfresco because SWT's ImageData class only supports jpeg, bmp, gif, tiff, and ico.
  private final static String[] imageExtensions = { "jpg", "jpeg", "jpe", "bmp", "png", "gif", "tiff", "tif", "ico" };

  private Label sizeLabel;

  //private StyledText nameValue;
  //private StyledText nameLabel;

  private Text sizeValue;

  private Label separator;

  private QuickImageCanvas viewer;

  private Composite header;


  private UpdatePreviewJob previewJob;

  // this is for displaying image preview
  private Composite imageComp;
  
  // this is for displaying text preview
  private Composite textComp;
  
  // this is for displaying remote link
  private Composite remoteLinkComp;

  // Remote link fields
  private Text remoteLinkHost;
  private Hyperlink remoteLinkPath;
  private Text remoteLinkTitle;
  private Text remoteLinkDescription;
  
  private Composite parent;

  protected static Logger logger = CatLogger.getLogger(FilePreviewPage.class);

  private final static int MAX_LENGTH = 1024 * 1024 * 10;


  protected static final String EXTENSION_POINT = "gov.pnnl.cat.ui.rcp.thumbnailedFileExtension";
  protected static final String ATTRIBUTE = "fileExtensionsWithThumbnails";

  //more extensions of files that might have thumbnails contributed from other plugins using our extension point
  protected static List<String> thumbnailExtenstionsContributions = new ArrayList<String>();

  static {
    loadThumbnailExtensions();
  }

  private static void loadThumbnailExtensions() {

    try {
      // look up all the extensions for the StyledWorkbenchLabelProvider extension point
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

      for (IConfigurationElement configurationElement : elements) {
        String extensionsAttr = configurationElement.getAttribute(ATTRIBUTE);
        String[] extensions = extensionsAttr.split(",");
        for (String ext : extensions) {
          thumbnailExtenstionsContributions.add(ext.trim());
        }
      }


    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load custom styled label extension points.", e);
    }
  }

  /**
   * Create the PageBookView Page
   */
  public FilePreviewPage() {
    super();
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.preview.IPreviewPage#createControl(java.awt.Composite, gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public void createControl(Composite parent, IResource resource, IViewPart viewPart) {
    this.parent = parent;
    this.resource = resource;
    this.viewPart = viewPart;
    control = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.numColumns = 1;
    layout.makeColumnsEqualWidth = true;
    control.setLayout(layout);
    control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    control.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

    createPreviewHeaderInfo();
    createPreviewBody();
    initializeBodyTextViewer();

    setTopControl(textComp);

    populateContent();
  }

  /**
   * Creates the header info for the Preview View (ie. File Name, File Size)
   */
  public void createPreviewHeaderInfo() {
    header = new Composite(control, SWT.NONE);
    final GridLayout gridLayout = new GridLayout();
    gridLayout.verticalSpacing = 0;
    gridLayout.numColumns = 2;
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.horizontalSpacing = 0;

    header.setLayout(gridLayout);
    header.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    header.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

    //    nameLabel = new StyledText(header, SWT.FULL_SELECTION);
    //    nameLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    //    nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    //    nameLabel.setText("Name:");
    //    
    //    Color blue = SWTResourceManager.getColor(new RGB(25, 76, 127));
    //    StyleRange styleRange = new StyleRange();
    //    styleRange.start = 0;
    //    styleRange.length = 5;
    //    styleRange.fontStyle = SWT.BOLD;
    //    styleRange.foreground = blue;
    //    nameLabel.setStyleRange(styleRange);
    //   
    //    nameValue =  new StyledText(header, SWT.FULL_SELECTION );
    //    nameValue.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    //    nameValue.setEditable(false);
    //    nameValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    //    nameValue.setForeground(blue);

    sizeLabel = new Label(header, SWT.NONE);
    sizeLabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    sizeLabel.setText("Size:");
    sizeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    sizeValue = new Text(header, SWT.NONE);
    sizeValue.setEditable(false);
    sizeValue.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    sizeValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

    separator = new Label(header, SWT.SEPARATOR | SWT.HORIZONTAL);
    separator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
  }   

  @Override
  public void dispose() {
    toolkit.dispose();
  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub

  }

  /**
   * Method createImageViewerContextMenu.
   * @param canvas QuickImageCanvas
   */
  public void createImageViewerContextMenu(final QuickImageCanvas canvas) {
    Menu menu = new Menu(canvas); 
    MenuItem item = new MenuItem(menu,SWT.PUSH); 
    item.setText("Zoom in");
    item.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        canvas.zoomIn();
      }
    });
    item = new MenuItem(menu,SWT.PUSH);
    item.setText("Zoom out");
    item.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        canvas.zoomOut();
      }
    });
    item = new MenuItem(menu,SWT.PUSH);
    item.setText("Zoom to fit");
    item.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        canvas.zoomFit();
      }
    });
    item = new MenuItem(menu,SWT.PUSH);
    item.setText("Zoom to 100%");
    item.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        canvas.zoomOriginal();
      }
    });
    canvas.setMenu(menu);

  }

  /**
   * Creates the Context Menu for the body text of the Preview View.
   * 
   * @param bodyText
   *          The StyledText widget to which the menu listeners will apply to
   */
  public void createPreviewContextMenu(final StyledText bodyText) {
    final Menu menu = new Menu(bodyText);

    final MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
    copyItem.setText("Copy");
    copyItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        bodyText.copy();
      }
    });
    // Set to disabled by default
    copyItem.setEnabled(false);

    MenuItem selectAllItem = new MenuItem(menu, SWT.PUSH);
    selectAllItem.setText("Select All");
    selectAllItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        bodyText.selectAll();
      }
    });

    new MenuItem(menu, SWT.SEPARATOR);

    final MenuItem findItem = new MenuItem(menu, SWT.NONE);
    findItem.setText("Find...");
    findItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("gov.pnnl.cat.ui.rcp.views.preview.PreviewView2");
        FindReplaceAction find = new FindReplaceAction(resourceBundle, "find_replace_action", Display.getCurrent().getActiveShell(), bodyTextViewer.getFindReplaceTarget());
        find.run();
      }
    });

    new MenuItem(menu, SWT.SEPARATOR);

    final MenuItem wordWrapItem = new MenuItem(menu, SWT.CHECK);
    wordWrapItem.setText("Word Wrap");
    wordWrapItem.setSelection(true);
    bodyText.setWordWrap(true);
    wordWrapItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        bodyText.setWordWrap(wordWrapItem.getSelection());
      }
    });

    bodyText.addMenuDetectListener(new MenuDetectListener() {
      @Override
      public void menuDetected(MenuDetectEvent e) {
        // When the context menu is opened...see if the
        // "Copy" menu item should be enabled
        copyItem.setEnabled(bodyText.getSelectionText().length() > 0);
      }
    });

    bodyText.setMenu(menu);
  }

  /**
   * Creates the main body composite for the Preview View (ie. viewerComp for Image Previews and messageComp for text previews
   */
  private void createPreviewBody() {
    // ViewerBody composite switches between texts and images given the resource
    viewerBody = new Composite(control, SWT.WRAP);
    stackLayout = new StackLayout();
    viewerBody.setLayout(stackLayout);
    viewerBody.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    viewerBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    remoteLinkComp = new Composite(viewerBody, SWT.NONE);
    remoteLinkComp.setLayout(new GridLayout());
    remoteLinkComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    remoteLinkComp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    createRemoteLinkSection(remoteLinkComp);

    textComp = new Composite(viewerBody, SWT.NONE);
    textComp.setLayout(new GridLayout());
    textComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    textComp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

    // Displays the text
    bodyTextViewer = new CatTextViewer(textComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
    bodyTextViewer.setDocument(new Document());
    bodyTextViewer.activatePlugins();

    imageComp = new Composite(viewerBody, SWT.NONE);
    imageComp.setLayout(new GridLayout());
    imageComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    imageComp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

    viewer = new QuickImageCanvas(imageComp, SWT.NONE);
    try {
      String iconsdir = FileLocator.resolve(CatRcpPlugin.getDefault().getBundle().getEntry("/")).getFile() 
          + "icons" + File.separator + "16x16" + File.separator;
      viewer.setIconsPath(iconsdir);
    } catch (IOException e1) {
    }
    viewer.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    createImageViewerContextMenu(viewer);

  }
  
  private Composite createRemoteLinkSection(Composite parent) {
    this.toolkit = new FormToolkit(Display.getCurrent());
    Form form = toolkit.createForm(parent);
    form.getBody().setLayout(new GridLayout(1, true));
    form.getBody().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    Composite composite = toolkit.createComposite(form.getBody());
    GridLayout layout = new GridLayout();
    layout.marginWidth = layout.marginHeight = 0;
    layout.numColumns = 2;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    //composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

    FontData fontData = composite.getFont().getFontData()[0];
    Font boldFont = JFaceResources.getFontRegistry().getBold(fontData.getName());

    // Host
    FormLayoutFactory.createPropertyName(toolkit, composite, "Host", boldFont);
    remoteLinkHost = FormLayoutFactory.createPropertyValue(toolkit, composite, null);
    FormLayoutFactory.createSpacerRow(toolkit, composite);
    
    // Path - hyperlink to RemoteSystemsExplorer
    FormLayoutFactory.createPropertyName(toolkit, composite, "Path", boldFont);
    remoteLinkPath= toolkit.createHyperlink(composite, "", SWT.NONE);
    remoteLinkPath.setToolTipText("Browse remote resource in Remote Systems Explorer");
    remoteLinkPath.addHyperlinkListener(new HyperlinkAdapter() {
      public void linkActivated(HyperlinkEvent e) {
        // Call the browse remote resource action
        try {
          RSEUtils.openInRemoteSystemsExplorer(resource);
        } catch (Exception ex) {
          ToolErrorHandler.handleError("Failed to open Remote Systems Explorer", ex, true);
        }               
      }
    });
    
    // Title
    FormLayoutFactory.createPropertyName(toolkit, composite, "Title", boldFont);
    remoteLinkTitle = FormLayoutFactory.createPropertyValue(toolkit, composite, null);
    FormLayoutFactory.createSpacerRow(toolkit, composite); 

    // Description
    FormLayoutFactory.createPropertyName(toolkit, composite, "Description", boldFont);
    remoteLinkDescription = FormLayoutFactory.createPropertyValue(toolkit, composite, null);
    FormLayoutFactory.createSpacerRow(toolkit, composite);      

    return composite;
  }



  /**
   * Layout and initialize key listeners for the body text viewer
   */
  private void initializeBodyTextViewer() {
    final StyledText bodyText = bodyTextViewer.getTextWidget();

    viewer.setLayout(new GridLayout());
    viewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    bodyText.setLayout(new GridLayout());
    bodyText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    bodyTextViewer.setEditable(false);

    bodyText.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        // Implementing the CTRL-A for the keyboard. Equivalent to menu
        // function "Select All"
        if (e.character == 0x01 || e.keyCode == 97) {
          bodyText.selectAll();
        }
        // Implementing the CTRL-F for the keyboard. Equivalent to menu
        // function "Find"
        if (e.character == 0x06 || e.keyCode == 102) {
          ResourceBundle resourceBundle = ResourceBundle.getBundle("gov.pnnl.cat.ui.rcp.views.preview.PreviewView2");
          FindReplaceAction find = new FindReplaceAction(resourceBundle, "find_replace_action", Display.getCurrent().getActiveShell(), bodyTextViewer.getFindReplaceTarget());
          find.run();
        }
      }

      public void keyReleased(KeyEvent e) {
      }
    });

    createPreviewContextMenu(bodyText);
  }

  /**
   * Set the control (view) that should be displayed in the Preview Pane
   * 
   * @param control
   *          The control to be displayed
   */
  private void setTopControl(Control control) {
    stackLayout.topControl = control;
    viewerBody.layout();
  }

  private void updateCursor() {

  }

  private void populateContent() {
    IResource resource = this.resource;

    // If resource is a file or a link to a file
    // (Note that ILinkedResource is currently implemented as an IFile)
    // If resource is a link to a folder, do nothing
    try {
      if (resource != null && resource instanceof ILinkedResource) {
        ILinkedResource link = (ILinkedResource) resource;
        resource = link.getTarget();
      }
    } catch (Throwable e) {
      String errMsg = "Unable to get link target.";
      ToolErrorHandler.handleError(errMsg, e, true);
      return;
    }

    setTopControl(textComp);
    displayTextForPreview("Loading...");

    if (previewJob != null && previewJob.getState() != Job.NONE) {
      logger.debug("canceling previous job");
      previewJob.cancel();
    }

    try {
      // Set the values for the resource
      if(resource == null) { // this occurs if this is a link and the link target is null
        sizeLabel.setText("Broken Link");
        sizeValue.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
        displayTextForPreview("This link points to a file that no longer exists.");
        
      } else if(resource.hasAspect(VeloConstants.ASPECT_REMOTE_LINK)) {
        sizeLabel.setText("Remote Link");
        sizeLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
        sizeValue.setText("");
        populateRemoteLinkData(resource);
        setTopControl(remoteLinkComp);

      } else if(resource instanceof IFile){

        sizeLabel.setText("Size:");
        sizeValue.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
        sizeValue.setText(FileUtils.byteCountToDisplaySize(((IFile)resource).getSize()));

        // run this in the background because it could take a while to load
        previewJob = new UpdatePreviewJob("Updating Preview", (IFile)resource);
        previewJob.setPriority(Job.SHORT);
        previewJob.schedule();

      } else if (resource instanceof IFolder) {
        sizeLabel.setText("Contains:");
        sizeValue.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
        int numKids = ResourcesPlugin.getResourceManager().getChildCount(resource.getPath());
        sizeValue.setText(String.valueOf(numKids) + " children");
        
        if(resource.hasAspect(VeloConstants.ASPECT_RENDITIONED)) {
          // try to display thumbnail - some folders can have thumbnails added
          previewJob = new UpdatePreviewJob("Updating Preview", resource);
          previewJob.setPriority(Job.SHORT);
          previewJob.schedule();
          
        } else {
          displayTextForPreview("");
        }
      }


    } catch (Exception e) {
      ToolErrorHandler.handleError("Failed to update preview.", e, true);
    }

  }
    
  private void populateRemoteLinkData(IResource resource) {
    String host = resource.getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_MACHINE);
    String path = resource.getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_PATH);
    String linkTitle = resource.getPropertyAsString(VeloConstants.PROP_TITLE);
    String linkDescription = resource.getPropertyAsString(VeloConstants.PROP_DESCRIPTION);
    // backwards compatibility check
    String linkUrl = resource.getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_URL);
    
    if(host == null) {
      host = "";
    } else {
      host = host.toLowerCase();
    }
    if(path == null) {
      path = "";
    }
    if(linkTitle == null) {
      linkTitle = "";
    }
    if(linkDescription == null) {
      linkDescription = "";
    }
    
    if(linkUrl != null) {
      // ssh://carinal@hopper.nersc.gov:/scratch/scratchdirs/carinal/Argentina_load/SR2_copy_081514_165456/agni-out.xml
      String hostStr = linkUrl.substring(linkUrl.indexOf('@') + 1);
      int colon = hostStr.indexOf(':');
      host = hostStr.substring(0, colon);
      path = hostStr.substring(colon + 1);
    }
    
    remoteLinkHost.setText(host);
    remoteLinkPath.setText(path);
    remoteLinkTitle.setText(linkTitle);
    remoteLinkDescription.setText(linkDescription);
  }

  /**
   * A method to check to see if the given file is an image by checking the extension of the file
   * 


   * @param resource IResource
   * @return boolean. <code>true</code> if image <code>false</code> if not image */
  private boolean isSelectionAnImage(IResource resource) {
    if(resource instanceof IFile) {
      IFile file = (IFile) resource;
      for (String imageExtension : imageExtensions) {
        if(file.getFileExtension() != null && file.getFileExtension().toString().equalsIgnoreCase(imageExtension)) {
          return true;
        }
      }
    } else if (resource.hasAspect(VeloConstants.ASPECT_RENDITIONED)) {
      // we assume if it has the renditioned aspect, it could have a thumbnail
      // TODO: maybe we should check for the existance of thumbnail here
      return true;
    }
    return false;
  }

  /**
   * A method to check to see if the given file might have a thumbnail by checking the extension of the file and then checking for existance of the thumbnail node
   * 


   * @param resource IResource
   * @return boolean. <code>true</code> if image <code>false</code> if not image */
  private boolean isSelectionThumbnailable(IResource resource) {
    if(resource instanceof IFile) {
      IFile file = (IFile) resource;
      for (String thumbnailExtension : thumbnailExtenstionsContributions) {
        if(file.getFileExtension() != null && file.getFileExtension().toString().equalsIgnoreCase(thumbnailExtension)) {
          //          IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
          //          if(mgr.resourceExists(file.getPath().append(VeloConstants.PROP_createQNameString(VeloConstants.PROP_ASSOC_NAME_IMG_PREVIEW))) ||
          //              mgr.resourceExists(file.getPath().append(VeloConstants.PROP_createQNameString(VeloConstants.PROP_ASSOC_NAME_IMG_DOCLIB))) ){
          return true;
          //          }
        }
      }
    }
    return false;
  }

  /**
   * Creates an image by getting the contents from the given image file.
   * 

   * @param resource IResource
   * @param useThumbnail boolean
   */
  private void displayImageForPreview(IResource resource) {
    try {
      if(imageContent != null) {
        try {
          ImageData data = new ImageData(imageContent);
          Image image = new Image(header.getDisplay(), data);
          viewer.setImage(image);
          setTopControl(imageComp);  
        } catch (Throwable e) {
          logger.error("Failed to load thumbnail image.", e);
          displayTextForPreview("No Thumbnail Found");
        }
      } else {
        displayTextForPreview("No Thumbnail Found");
      }

    } catch (Exception e) {
      logger.error("An error occurred while trying to create the thumbnail for resource: " + resource.getPath(), e);
    }
  }

  private void loadImageForPreview(IResource resource, boolean useThumbnail) {
    try {
      IResource file = resource;
      if(file instanceof ILinkedResource) {
        file = ((ILinkedResource)file).getTarget();
      }
      IResourceManager mgr = ResourcesPlugin.getResourceManager();   
      imageContent = null;
      
      if(useThumbnail) {
        try {
          imageContent = mgr.getThumbnail(resource.getPath(), null);
        } catch (Throwable e) {
          imageContent = null;
        }

      } 
      if(imageContent == null && (file instanceof IFile)) {
        // use the resource itself
        imageContent = ((IFile)file).getContent();
      }


    } catch (Exception e) {
      logger.error("An error occurred while trying to create the thumbnail for resource: " + resource.getPath(), e);
    }
  }

  private void loadTextForPreview(IFile file) {

    // TODO: if we run the content extractor asynchronously on the server, then the
    // raw text node might not have been created yet! So, we need to check if the
    // file.getTextTransform() exists, and if not, wait until it has been created.
    // It should always exist, even if the transform failed.
    try {
      String encoding = "UTF-8";
      InputStream input = file.getContentAsText();
      if (input != null) {
        InputStreamReader is= new InputStreamReader(input, encoding);
        textContent = CatUIUtil.readStreamFully(is, MAX_LENGTH);
        
      } else {

        // raw text is null because an error occurred - need to display this message
        TransformData textNode = file.getTextTransform();
        if (textNode != null) {

          // Assume that the error text is a java exception string
          // (i.e. class name + ":" + message)
          String errorMsg = textNode.getErrorMessage();
          if (errorMsg != null) {
            String[] msgParts = errorMsg.split(":", 2);

            // Do not parse the error message if it's a no transformer found error
            if (msgParts.length > 1 && !msgParts[0].startsWith("No text transformer found for")) {
              textContent = new StringBuffer(msgParts[1]);
            } else {
              textContent = new StringBuffer(errorMsg);
            }

          } else {
            textContent = new StringBuffer(MSG_NO_RAW_TEXT);
          }
        } else {
          textContent = new StringBuffer(MSG_NO_RAW_TEXT);
        }

      }
    } catch (Throwable e) {
      //textContent = new StringBuffer(MSG_NO_RAW_TEXT);
      textContent = new StringBuffer(e.toString());
      logger.error("Could not get raw text", e);
    } 
    
  }


  /**
   */
  private class UpdatePreviewJob extends Job {
    private IResource file;

    /**
     * Constructor for UpdatePreviewJob.
     * @param name String
     * @param file IFile
     */
    public UpdatePreviewJob(String name, IResource file) {
      super(name);
      this.file = file;
    }

    /**
     * Method run.
     * @param monitor IProgressMonitor
     * @return IStatus
     */
    protected IStatus run(final IProgressMonitor monitor) {
      IStatus status = Status.OK_STATUS;
      boolean image = false;


      if (isSelectionAnImage(resource)) {
        loadImageForPreview(resource, true);
        image = true;

      } else if (isSelectionThumbnailable(resource)) {
        loadImageForPreview(resource, true);
        image = true;

      } else {
        loadTextForPreview((IFile)resource);
      }

      final boolean finalImage = image;
      if (isCanceled(monitor)) {
        status =  Status.CANCEL_STATUS;

      } else {
        Display.getDefault().asyncExec(new Runnable() {
          
          @Override
          public void run() {
            if(finalImage) {
              displayImageForPreview(file);
            } else {
              displayTextForPreview(textContent.toString());
            }       
          }
        });
      }
      return status;
    }

  }

  /**
   * Method isCanceled.
   * @param monitor IProgressMonitor
   * @return boolean
   */
  private static boolean isCanceled(IProgressMonitor monitor) {
    return monitor != null && monitor.isCanceled();
  }

  /**
   * Method setText.
   * @param text String
   */
  private void displayTextForPreview(final String text) {

    try {
      bodyTextViewer.setDocument(new Document(text.trim()));
    } catch (Exception e) {
      logger.debug(text, e);
    }

  }

}
