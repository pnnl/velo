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

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 */
public class TargetPropertiesPage extends ResourcePropertyPage {

  protected static Logger logger = CatLogger.getLogger(TargetPropertiesPage.class);

  /**
   * Method getResource.
   * @return IResource
   */
  protected IResource getResource() {
    try {
      return ((ILinkedResource)RCPUtil.getResource(getElement())).getTarget();
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }
    return null;
  }

  /**
   * Method createDefaultComposite.
   * @param composite Composite
   * @return Composite
   */
  protected Composite createDefaultComposite(Composite composite) {
    final IResource resource = getResource();

    nameText = ResourcePropertiesUtil.createEditableProperty("Target &Name", VeloConstants.PROP_NAME, resource, composite);
    titleText = ResourcePropertiesUtil.createEditableProperty("Target &Title", VeloConstants.PROP_TITLE, resource, composite);
    descText = ResourcePropertiesUtil.createEditableProperty("Target &Description", VeloConstants.PROP_DESCRIPTION, resource, composite);

    addSeparator(composite);

    ResourcePropertiesUtil.createNotEditableProperty("Target T&ype", VeloConstants.PROP_MIMETYPE, resource, composite);
    ResourcePropertiesUtil.createNotEditableProperty("Target &Path", resource.getPath().toDisplayString(), composite);

    sizeTextBox = ResourcePropertiesUtil.createNotEditableProperty("Target &Size", VeloConstants.PROP_SIZE, resource, composite);
    try {
      if (resource.isType(IResource.FILE)) {
        sizeTextBox.setText(ResourcePropertiesUtil.getFileSize((IFile)resource));
      } else if (resource.isType(IResource.FOLDER)) { //should never happen
        setFolderStats(resource, composite);
      }
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }

    addSeparator(composite);

    ResourcePropertiesUtil.createNotEditableProperty("Target &Created", VeloConstants.PROP_CREATED, resource, composite);
    ResourcePropertiesUtil.createNotEditableProperty("Target C&reator", VeloConstants.PROP_CREATOR, resource, composite);
    authorText = ResourcePropertiesUtil.createEditableProperty("Target &Author", VeloConstants.PROP_AUTHOR, resource, composite);

    ResourcePropertiesUtil.createNotEditableProperty("Target &Modified", VeloConstants.PROP_MODIFIED, resource, composite);
    ResourcePropertiesUtil.createNotEditableProperty("Target M&odifier", VeloConstants.PROP_MODIFIER, resource, composite);

    addSeparator(composite);

    final Button findTargetButton = new Button(composite, SWT.PUSH);
    findTargetButton.setText("Find Target...");
    findTargetButton.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        handleFindTarget(resource);
        getShell().close();
      }
    });

    return composite;
  }

  /**
   * Method handleFindTarget.
   * @param resource IResource
   */
  private void handleFindTarget(IResource resource) {
    //System.out.println("handleFindTarget");
    // Retrieve the user perference for this dialog
    // IPreferenceStore store = JcrActivator.getDefault().getPreferenceStore();
    //    ScopedPreferenceStore preferences = new ScopedPreferenceStore(new InstanceScope(), CatPreferenceIDs.CAT_PREFERENCE_ID);
    //    String key = PreferenceConstants.SWITCH_PERSPECTIVES;

    try {
      RCPUtil.selectResourceInTree(resource);
    } catch (Throwable e) {
      ToolErrorHandler.handleError("Failed to navigate to target file.", e, true);
    }
  }

}
