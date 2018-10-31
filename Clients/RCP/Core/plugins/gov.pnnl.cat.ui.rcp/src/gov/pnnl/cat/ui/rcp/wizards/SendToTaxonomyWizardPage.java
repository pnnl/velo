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
package gov.pnnl.cat.ui.rcp.wizards;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatRcpMessages;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListDialog;

/**
 */
public class SendToTaxonomyWizardPage extends WizardPage {
  public static final String NAMESPACE_TAX_MODEL  = "http://www.pnl.gov/dmi/model/taxonomy/1.0";
  public static final String ASPECT_TAXONOMY_ROOT = VeloConstants.ASPECT_TAXONOMY_ROOT;
  public static final String NAMESPACE_CAT_MODEL  = "http://www.pnl.gov/cat/model/content/1.0";
  public static final String ASPECT_FAVORITES_ROOT = VeloConstants.ASPECT_FAVORITES_ROOT;
  
  private Logger logger = CatLogger.getLogger(SendToTaxonomyWizardPage.class);
  private IWorkbenchWindow workbench;
  private IResourceManager mgr;
  private Text text;
  /**
   * Constructor for SendToTaxonomyWizardPage.
   * @param selection ISelection
   * @param workbench IWorkbenchWindow
   */
  protected SendToTaxonomyWizardPage(ISelection selection, IWorkbenchWindow workbench) {
    super("sendtotaxonomy");
    setTitle(CatRcpMessages.SendToTaxonomy_title);
    setDescription(CatRcpMessages.SendToTaxonomy_description);
    this.workbench = workbench;
  }

  /**
   * Method createControl.
   * @param parent Composite
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    container.setLayout(gridLayout);
    
    setControl(container);

    final Label taxonomyDestinationLabel = new Label(container, SWT.NONE);
    taxonomyDestinationLabel.setText(CatRcpMessages.SendToTaxonomy_destination_label);

    text = new Text(container, SWT.BORDER);
    text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    
    text.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    });
    
    final Button browseButton = new Button(container, SWT.NONE);
    browseButton.setText("Browse...");
    browseButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        ListDialog dialog = new ListDialog(workbench.getShell());
        dialog.setTitle(CatRcpMessages.SendToTaxonomy_select_taxonomy_dialog_title);
        dialog.setAddCancelButton(true);
        dialog.setContentProvider(new ArrayContentProvider());
        dialog.setLabelProvider(new CatWorkbenchLabelProvider(dialog.getTableViewer()));
        IResource selectedResource = null;
        try{
          mgr = ResourcesPlugin.getResourceManager();
          IFolder[] taxonomies = mgr.getTaxonomies();
          
          // CatWorkbenchLabelProvider requires that the input be CatItemNodes
          List<IResource> inputs = new ArrayList<IResource>();
          for(IFolder taxonomy : taxonomies) {
              inputs.add(taxonomy);
          }
          
          dialog.setInput(inputs);
          dialog.open();
          
          Object[] results = dialog.getResult();
          if (results != null && results.length > 0) {
            selectedResource = (IResource) results[0];
          }
          if(selectedResource != null){
            text.setText(selectedResource.getPath().toDisplayString());
          }
         
        }
        catch(Exception ex){
          logger.error(ex);
        }
      }
    });
    dialogChanged();
  }
  
  /**
   * Method getDestination.
   * @return String
   */
  public String getDestination() {
    return text.getText();
  }
  
  /**
   * Method getDestinationFolder.
   * @return CmsPath
   */
  public CmsPath getDestinationFolder() {
    return new CmsPath(getDestination());
  }
  
  /**
   * Method isValid.
   * @return boolean
   */
  public boolean isValid(){
    try {
      CmsPath destination = new CmsPath(getDestination());
      boolean exists = mgr.resourceExists(destination);
      if(exists){
       IResource resource = mgr.getResource(destination);
       return  resource.hasAspect(ASPECT_TAXONOMY_ROOT); 
      }
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
      return false;
    }
    return false;
  }
  
  private void dialogChanged(){
    if (getDestination().length() == 0) {
      updateStatus("Destination folder must be specified.");
      return;
    }
    if(!isValid()){
      updateStatus(CatRcpMessages.SendToTaxonomy_destination_must_be_taxonomy_error);
      return;
    }
    
    updateStatus(null);
  }
  
  /**
   * Method updateStatus.
   * @param message String
   */
  protected void updateStatus(String message){
    setErrorMessage(message);
    setPageComplete(message == null);
  }

}
