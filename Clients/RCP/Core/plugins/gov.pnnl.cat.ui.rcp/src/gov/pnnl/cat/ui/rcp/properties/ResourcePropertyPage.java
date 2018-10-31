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
package gov.pnnl.cat.ui.rcp.properties;

import gov.pnnl.cat.core.resources.ICatCML;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

import java.text.NumberFormat;

import org.apache.log4j.Logger;
import org.apache.turbine.util.FileUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 */
public class ResourcePropertyPage extends PropertyPage {

  protected Text authorText;

  protected Text nameText;
  protected Text titleText;
  protected Text descText;
  private Logger logger = CatLogger.getLogger(ResourcePropertyPage.class);
  protected Text sizeTextBox;
  protected Text containsTextBox;
  
  
  /**
   * Constructor for SamplePropertyPage.
   */
  public ResourcePropertyPage() {
    super();
    noDefaultAndApplyButton();
    //resource = RCPUtil.getResource(getElement());
  }

  /**
   * Method addSeparator.
   * @param parent Composite
   */
  protected void addSeparator(Composite parent) {
    Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
    label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1)); 
  }

  /**
   * @see PreferencePage#createContents(Composite)
   */
  protected Control createContents(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 15;
    layout.verticalSpacing = 10;
    layout.marginWidth = 10;
    layout.numColumns = 2;
    composite.setLayout(layout);
    GridData data = new GridData(GridData.FILL);
    data.grabExcessHorizontalSpace = true;
    composite.setLayoutData(data);

    createDefaultComposite(composite);
    //addSeparator(composite);
    return composite;
  }
  
  /**
   * Method getResource.
   * @return IResource
   */
  protected IResource getResource() {
    return RCPUtil.getResource(getElement());
  }
  
  protected void setFolderStats(IResource resource, Composite composite) {
    containsTextBox = ResourcePropertiesUtil.createNotEditableProperty("Contains", "", composite);
    
    // TODO: call a web service method to get the folder stats recursively
//    if (sizeTextBox != null && !sizeTextBox.isDisposed()) {
//      sizeTextBox.setText(createSizeDisplay(folderStats[0], true));
//    }
//    if (containsTextBox != null && !containsTextBox.isDisposed()) {
//      containsTextBox.setText(createContentsDisplay(folderStats[1], folderStats[2], true));
//    } 
  }
  
  /**
   * Method createDefaultComposite.
   * @param composite Composite
   * @return Composite
   */
  protected Composite createDefaultComposite(Composite composite) {
    IResource resource = getResource();
    
    //Use file type image instead of "Name" as label for file name
    Label imageLabel = new Label(composite, SWT.NONE);
    imageLabel.setImage(ResourcePropertiesUtil.getImage(resource));
    nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    nameText.setLayoutData(new GridData(SWT.FILL,  SWT.BOTTOM, true, false));
    nameText.setText(resource.getName());

    titleText = ResourcePropertiesUtil.createEditableProperty("&Title", VeloConstants.PROP_TITLE, resource, composite);
    descText = ResourcePropertiesUtil.createEditableProperty("&Description", VeloConstants.PROP_DESCRIPTION, resource, composite, 4);

    addSeparator(composite);

    ResourcePropertiesUtil.createNotEditableProperty("T&ype", VeloConstants.PROP_MIMETYPE, resource, composite);
    ResourcePropertiesUtil.createNotEditableProperty("&Path", resource.getPath().toDisplayString(), composite);

    sizeTextBox = ResourcePropertiesUtil.createNotEditableProperty("&Size", VeloConstants.PROP_SIZE, resource, composite);
    try {
      if (resource.isType(IResource.FILE)) {
        sizeTextBox.setText(ResourcePropertiesUtil.getFileSize((IFile)resource));
      } else if (resource.isType(IResource.FOLDER)) {
        setFolderStats(resource, composite);        
      }
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }

    addSeparator(composite);

    ResourcePropertiesUtil.createNotEditableProperty("&Created", VeloConstants.PROP_CREATED, resource, composite);
    ResourcePropertiesUtil.createNotEditableProperty("C&reator", VeloConstants.PROP_CREATOR, resource, composite);
    authorText = ResourcePropertiesUtil.createEditableProperty("&Author", VeloConstants.PROP_AUTHOR, resource, composite);

    ResourcePropertiesUtil.createNotEditableProperty("&Modified", VeloConstants.PROP_MODIFIED, resource, composite);
    ResourcePropertiesUtil.createNotEditableProperty("M&odifier", VeloConstants.PROP_MODIFIER, resource, composite);
    Text ownerText = ResourcePropertiesUtil.createNotEditableProperty("Ow&ner", VeloConstants.PROP_OWNER, resource, composite);
    ownerText.setText(ownerText.getText().replaceAll("GROUP_/", "(Team) "));//to make owner text that is a team more readable
    return composite;
  }
  
  /**
   * Check the following 4 properties:
   *    Name
   *    Title
   *    Description
   *    Author
   * Compare them with old values and update if necessary
   * @return boolean
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk() {
    compareAndSetProperty(VeloConstants.PROP_AUTHOR, authorText);
    compareAndSetProperty(VeloConstants.PROP_DESCRIPTION, descText);
    compareAndSetProperty(VeloConstants.PROP_TITLE, titleText);
    compareAndSetProperty(VeloConstants.PROP_NAME, nameText);

    return true;
  }
  
  /**
   * Method compareAndSetProperty.
   * @param prop QualifiedName
   * @param text Text
   */
  private void compareAndSetProperty(String prop, Text text)
  {
    try
    {
      IResource resource = getResource();
      String oldValue = resource.getPropertyAsString(prop);
      String newValue = text.getText();
      boolean dirty = false;

      if(oldValue == null || oldValue.length() == 0)
      {
        if(newValue.length() > 0)
          dirty = true;
      }
      else
      {
        if(!oldValue.equals(newValue))
          dirty = true;
      }

      if(dirty)
      {
        if(prop.equals(VeloConstants.PROP_NAME))
        {
          try {
            resource.move(resource.getParent().getPath().append(text.getText()));
          }
          catch (ResourceException e) {
            String errMsg = "An error occurred trying to rename.";
            ToolErrorHandler.handleError(errMsg, e, true);
          }
        }
        // author property and aspect have the same name!
        else if (prop.equals(VeloConstants.PROP_AUTHOR) && !resource.hasAspect(prop))
        {
          // We MUST add the author aspect in order to set this property
          // or else this property can never be copied
          IResourceManager mgr = ResourcesPlugin.getResourceManager();
          ICatCML cml = mgr.getCML();
          cml.addAspect(resource.getPath(), prop);
          cml.setProperty(resource.getPath(), prop, newValue);
          mgr.executeCml(cml);
        }
        else
        {
          resource.setProperty(prop, newValue);
        }
      }
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }
  }

  /**
   * Creates the folder contents String to display to the user (e.g. "27 Files, 4 Folders").
   * 
   * @param totalFiles
   * @param totalFolders
   * @param incremental
  
   * @return String
   */
  private static String createContentsDisplay(long totalFiles, long totalFolders, boolean incremental) {
    StringBuffer buf = new StringBuffer();
    buf.append(NumberFormat.getInstance().format(totalFiles));
    buf.append(" File");
    if (totalFiles != 1) {
      buf.append("s");
    }
    buf.append(", ");
    buf.append(NumberFormat.getInstance().format(totalFolders));
    buf.append(" Folder");
    if (totalFolders != 1) {
      buf.append("s");
    }
    if (incremental) {
      buf.append("...");
    }
    return buf.toString();
  }

  /**
   * Method createSizeDisplay.
   * @param size long
   * @param incremental boolean
   * @return String
   */
  private static String createSizeDisplay(long size, boolean incremental) {
    String text = FileUtils.byteCountToDisplaySize(size) + " (" + NumberFormat.getInstance().format(size) + " bytes)";
    if (incremental) {
      text = text + "...";
    }
    return text;
  }

}
