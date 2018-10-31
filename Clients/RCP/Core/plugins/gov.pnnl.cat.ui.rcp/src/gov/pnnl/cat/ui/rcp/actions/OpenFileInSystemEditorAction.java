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
package gov.pnnl.cat.ui.rcp.actions;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.EditorDescriptor;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.editors.ResourceTextEditor;
import gov.pnnl.cat.ui.rcp.editors.VeloEditorUtil;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

/**
 */
public class OpenFileInSystemEditorAction extends ViewerAction {
  
  private boolean updateName = false;
  private Program prog = null;
  private IFile theFile;
  private Logger logger = CatLogger.getLogger(this.getClass());
  private IEditorRegistry registry;

  public OpenFileInSystemEditorAction() {
    this(false);
  }

  /**
   * Constructor for OpenFileInSystemEditorAction.
   * @param updateName boolean
   */
  public OpenFileInSystemEditorAction(boolean updateName) {
    super("Open");
    this.updateName = updateName;
    setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_DOC, SharedImages.CAT_IMG_SIZE_16));
    setToolTipText("Open File in System Editor");
    this.registry = PlatformUI.getWorkbench().getEditorRegistry();
  }

  /**
   * Method run.
   * @see org.eclipse.jface.action.IAction#run()
   */
  public void run() {
    // find out what was selected to put in new view part
    StructuredSelection selectedFile = (StructuredSelection) getViewer().getSelection();
    IResource resource = RCPUtil.getResource(selectedFile.getFirstElement());

    try {
      if (resource instanceof ILinkedResource) {
        resource = ((ILinkedResource)resource).getTarget();
      }
    } catch (Throwable e) {
      ToolErrorHandler.handleError("An error occurred trying to retrieve the link target.", e, true);
      return;
    }
    
    if (resource instanceof IFile) {
      theFile = (IFile) resource;
      if(resource.hasAspect(VeloConstants.ASPECT_REMOTE_LINK)) {
        openRemoteFile(resource);
      } else {
        openFile(theFile);
      }
    }       
  }

  public void openRemoteFile(IResource file) {
    // Call the browse remote resource action
    try {
      RSEUtils.openInRemoteSystemsExplorer(file);
    } catch (Exception ex) {
      ToolErrorHandler.handleError("Failed to open Remote Systems Explorer", ex, true);
    } 
  }
  
  /**
   * Method openFile.
   * @param file IFile
   */
  public void openFile(IFile file) {

    try {
      // If this is a text file, open in text editor IF another velo editor isn't registered to open it by filename
      String mimetype = file.getMimetype();
      String resourceFileName = file.getName();
      IEditorDescriptor[] editors = registry.getEditors(resourceFileName);
      if (mimetype != null && mimetype.toLowerCase().contains("text")) {
        IEditorDescriptor textEditor = registry.findEditor(ResourceTextEditor.RESOURCE_TEXT_EDITOR_ID);
        VeloEditorUtil.openFilesInEditor(true, null, textEditor, file);
      }else if (editors != null && editors.length > 0) {
        // if more than one found, just use the first:
        IEditorDescriptor editorDescriptor = editors[0];
        VeloEditorUtil.openFilesInEditor(true, null, editorDescriptor, file);
      } else {
        Vector<IResource> files = new Vector<IResource>();
        files.add(file);
        OpenWithSystemEditor.openFilesInSystemEditor(files, null);
      }
    } catch (Exception ex) {
      logger.error(ex);
    }

  }
  
  
  

  /**
   * Method getPolicy.
   * @return int
   */
  public int getPolicy() {
    return ENABLED_ON_SINGLE_FILE;
  }

  /**
   * Method getFileExtension.
   * @return String
   */
  private String getFileExtension() {
    String extension = null;

    // find out what was selected to put in new view part
    StructuredSelection selectedFile = (StructuredSelection) getViewer().getSelection();
    IResource resource = RCPUtil.getResource(selectedFile.getFirstElement());
    try {
      if (resource instanceof ILinkedResource) {
        resource = ((ILinkedResource) resource).getTarget();
      }
    } catch (Throwable e) {
      ToolErrorHandler.handleError("An error occurred trying to retrieve the link target.", e, true);
      return null;
    }
  
    if (resource instanceof IFile) {
      theFile = (IFile) resource;
      int iExtPos = theFile.getName().lastIndexOf('.');

      if (iExtPos != -1) {
        extension = theFile.getName().substring(iExtPos);
      }
    }
    return extension;
  }

  /**
   * Method updateProgram.
   * @return boolean
   */
  public boolean updateProgram() {
    try {
      //for now we are hard coding the types of applications that understand webdav.
      //once we have a mapped drive pointing to web dav, we won't have to do this
      //IE: for .doc & .txt open in word 
      String strExt = getFileExtension();
      // open .doc & .txt files in word:
      if (strExt != null && (".doc".equalsIgnoreCase(strExt) || ".txt".equalsIgnoreCase(strExt))) {
        prog = findPreferredfProgram(".doc");
        if (updateName) {
          this.setText("Word");
          determineImageDescriptor(prog);
        }
      }
      // open .xls in excel:
      else if (strExt != null && ".xls".equalsIgnoreCase(strExt)) {
        prog = findPreferredfProgram(".xls");
        if (updateName) {
          this.setText("Excel");
          determineImageDescriptor(prog);
        }
      }
      // open .pdf in pdf viewers....ie adobe acrobat
      else if (strExt != null && ".pdf".equalsIgnoreCase(strExt)) {
        prog = findPreferredfProgram(".pdf");
        if (updateName) {
          this.setText("Adobe Acrobat");
          determineImageDescriptor(prog);
        }
      }
      // open .ppt in excel:
      else if (strExt != null && (".ppt".equalsIgnoreCase(strExt) || ".pps".equalsIgnoreCase(strExt))) {
        prog = findPreferredfProgram(".ppt");
        if (updateName) {
          this.setText("PowerPoint");
          determineImageDescriptor(prog);
        }
      }
      // open everything else in the browser:  
      else {
        prog = findPreferredfProgram(".html");
        if (updateName) {
          this.setText("Browser");
          determineImageDescriptor(prog);
        }
      }
      return true;
    } catch (Exception ex) {
      logger.error(ex);
    }
    return false;
  }

  /**
   * Method findPreferredfProgram.
   * @param extension String
   * @return Program
   */
  private Program findPreferredfProgram(String extension) {
    IEditorRegistry fRegistry = PlatformUI.getWorkbench().getEditorRegistry();
    IEditorDescriptor editorDesc= fRegistry.getDefaultEditor(extension);
    Program progFound = null;
    if(editorDesc instanceof EditorDescriptor){
      logger.debug("YAY!!!!  " + editorDesc.getLabel());
      progFound = ((EditorDescriptor)editorDesc).getProgram();
    }else{
//      logger.debug("NO!!!!!!!!");
    }
        
    if(progFound != null){
      return progFound;
    }else 
      return Program.findProgram(extension);
  }

  /**
   * Method determineImageDescriptor.
   * @param prog Program
   */
  private void determineImageDescriptor(Program prog) {
    if (prog != null) {
      ImageData idata = prog.getImageData();
      if (idata != null) {
        this.setImageDescriptor(ImageDescriptor.createFromImageData(idata));
      } else {
        this.setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_WINDOW, SharedImages.CAT_IMG_SIZE_16));
      }
    }
  }
}
