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
package gov.pnnl.cat.ui.rcp.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import gov.pnnl.velo.core.util.ToolErrorHandler;

/**
 */
public class ResourceTextEditor extends TextEditor {
  public static final String RESOURCE_TEXT_EDITOR_ID = "gov.pnnl.cat.ui.rcp.editors.ResourceTextEditor"; //$NON-NLS-1$

 
  
  /**
   * Method isSaveAsAllowed.
   * @return boolean
   * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
   */
  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  /**
   * Method doSaveAs.
   * @see org.eclipse.ui.ISaveablePart#doSaveAs()
   */
  @Override
  public void doSaveAs() {
    String errMsg = "Save As not yet supported.";
    ToolErrorHandler.handleError(errMsg, null, true);
  }

  /**
   * Method doSave.
   * @param progressMonitor IProgressMonitor
   * @see org.eclipse.ui.ISaveablePart#doSave(IProgressMonitor)
   */
  @Override
  public void doSave(IProgressMonitor progressMonitor) {
    super.doSave(progressMonitor);
  }
  
  @Override
  protected void performSave(boolean overwrite, IProgressMonitor progressMonitor) {
    IDocumentProvider provider= getDocumentProvider();
    if (provider == null)
      return;

    try {

      provider.aboutToChange(getEditorInput());
      IEditorInput input= getEditorInput();
      provider.saveDocument(progressMonitor, input, getDocumentProvider().getDocument(input), overwrite);
      
      //todo run this as a background task? do we need to wait until the super class's save method completes first?
      ResourceFileStoreEditorInput myInput = (ResourceFileStoreEditorInput)this.getEditorInput();
      VeloEditorUtil.saveToServer(myInput);
      editorSaved();

    } catch (CoreException x) {
      IStatus status= x.getStatus();
      if (status == null || status.getSeverity() != IStatus.CANCEL)
        handleExceptionOnSave(x, progressMonitor);
    } catch (Throwable e)  {
      ToolErrorHandler.handleError("Save failed.", e, true);
    } finally {
      provider.changed(getEditorInput());
    } 
  }

}
