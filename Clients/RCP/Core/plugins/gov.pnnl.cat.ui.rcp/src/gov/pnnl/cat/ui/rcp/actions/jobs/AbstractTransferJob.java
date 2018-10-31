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
package gov.pnnl.cat.ui.rcp.actions.jobs;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.actions.ActionUtil;
import gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter;
import gov.pnnl.cat.ui.rcp.dialogs.FileDialogs;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;


/**
 */
public abstract class AbstractTransferJob extends AbstractRepositoryJob {

  public static final String LINKOPERATION = "Link to";
  public static final String COPYOPERATION = "Copy of";
  private Logger logger = CatLogger.getLogger(AbstractTransferJob.class);
  public static final String OVERWRITE_FILES = "overwrite files";
  public static final String OVERWRITE_FOLDERS = "overwrite folders";
  private CmsPath destination;
  private String errorTitle = "";
  private String errorMsg = "";
  
  /**
   * Method getAction.
   * @return int
   */
  abstract public int getAction();
  
  /**
   * Constructor for AbstractTransferJob.
   * @param sourcePaths String[]
   * @param destination CmsPath
   * @param shell Shell
   */
  public AbstractTransferJob(String[] sourcePaths, CmsPath destination, Shell shell) {
    super(sourcePaths, shell);
    this.destination = destination;
  }
  
  /**
   * Method getDestination.
   * @param currentFile ITransferObjectAdapter
   * @return CmsPath
   * @throws ResourceException
   */
  public CmsPath getDestination(ITransferObjectAdapter currentFile)  throws ResourceException{
    return this.destination;
  }  
  
  /**
   * Method getDestination.
   * @return CmsPath
   */
  public CmsPath getDestination(){
    return this.destination;
  }
  
  /**
   * Method isSourceEqualToDestination.
   * @param currentFile ITransferObjectAdapter
   * @param destination CmsPath
   * @return boolean
   */
  protected boolean isSourceEqualToDestination(ITransferObjectAdapter currentFile, CmsPath destination) {
    CmsPath transObjPath = new CmsPath(currentFile.getPath());
  
    return (transObjPath.removeLastSegments(1).equals(destination));
  }

  /**
   * Method resolveDestinationPath.
   * @param currentFile ITransferObjectAdapter
   * @param destinationFolder CmsPath
   * @return CmsPath
   * @throws ResourceException
   */
  public CmsPath resolveDestinationPath(ITransferObjectAdapter currentFile, CmsPath destinationFolder) throws ResourceException {
    if(currentFile.getType() == ITransferObjectAdapter.TYPE_RESOURCE_FILE){
      return getUniqueName(destinationFolder, (IResource)currentFile.getObject(), this.getAction());
    }
    return destinationFolder.append(currentFile.getLabel());
  }
  
  /**
   * Method checkFolderOperation.
   * @param destinationFolder CmsPath
   * @param newDestinationFolderPath CmsPath
   * @param dialogAnswers HashMap<String,Integer>
   * @return boolean
   * @throws ResourceException
   */
  public boolean checkFolderOperation(CmsPath destinationFolder, CmsPath newDestinationFolderPath, HashMap<String, Integer> dialogAnswers) throws ResourceException {
    int dialogAnswer;

    int overwriteFilesValue = getDialogValue(dialogAnswers, OVERWRITE_FOLDERS);

    if (overwriteFilesValue == IDialogConstants.YES_TO_ALL_ID) {
      return true;
    } else if (overwriteFilesValue == IDialogConstants.NO_TO_ALL_ID) {
      return false;
    }

    IResource resDestinationFolder = null;
    try {
      resDestinationFolder = getManager().getResource(destinationFolder);
    } catch (ResourceException e) {
      logger.error(e);
      return true;
    }
    
    // resource already exists
    dialogAnswer = FileDialogs.canOverWriteFolder(shell, newDestinationFolderPath, resDestinationFolder.isType(IResource.LINK)); 

    return putDialogValue(dialogAnswers, OVERWRITE_FOLDERS, dialogAnswer);
  }

  /**
   * Method checkFileOperation.
   * @param filePath CmsPath
   * @param dialogAnswers HashMap<String,Integer>
   * @return boolean
   */
  protected boolean checkFileOperation(CmsPath filePath, HashMap<String, Integer> dialogAnswers) {
    if (isOperationCanceled()) {
      return false;
    }

    IResource resource = null;
    int overwriteFilesValue = getDialogValue(dialogAnswers, OVERWRITE_FILES);
    int overwriteFoldersValue = getDialogValue(dialogAnswers, OVERWRITE_FOLDERS);

    // we need to check the map's OVERWRITE_FOLDER value since this applies to its child files
    // we can return true right away without checking if there is even a conflict
    // if they answered yes to all already
    if (overwriteFilesValue == IDialogConstants.YES_TO_ALL_ID ||
        overwriteFoldersValue == IDialogConstants.YES_ID ||
        overwriteFoldersValue == IDialogConstants.YES_TO_ALL_ID) {
      return true;
    }
// no longer doing this else if here since it would return false even 
// on non-conflicting files which made all other files not upload.
//    } else if (overwriteFilesValue == IDialogConstants.NO_TO_ALL_ID) {
//      return false;
//    }

    //no conflict, just return
    try {     
      if (overwriteFilesValue == IDialogConstants.NO_TO_ALL_ID) {
        return false;
      }
      
      resource = getManager().getResource(filePath);
    } catch (ResourceException e) {
      ToolErrorHandler.handleError(errorTitle + " " + errorMsg, e, true);
      return false;
    }

    boolean isLinked = resource instanceof ILinkedResource;   
    int dialogAnswer = FileDialogs.canOverWriteFile(shell, filePath, isLinked);

    return putDialogValue(dialogAnswers, OVERWRITE_FILES, dialogAnswer);
  }

  
  /**
   * Create a unique named CmsPath for a IResource.  The new CmsPath is based on the 
   * destination CmsPath and the names of files currently in the destination 
   * folder.
   * 
   * If a file with that name already exist in the location create a new name with an index.
   * Examples:
   *   readme.txt
   *   <label param> (2) readme.txt
   *   <label param> (3) readme.txt
   *   
   * @param destinationFolder
   * @param sourceItem
   * @param operation
  
  
   * @return CmsPath
   * @throws ResourceException  */
  protected CmsPath getUniqueName(CmsPath destinationFolder, IResource sourceItem, int operation) throws ResourceException {
    // Find the next available index for a file of this name.
    String operationName = getOperationText(operation);
    String newFileName = getNewFileName(sourceItem, destinationFolder, operationName);
    
    // OK now construct the name and path.
//    CmsPath proposedPathOfNewLink;
//    CmsPath pathToLinksParent = destinationFolder;
//    proposedPathOfNewLink = pathToLinksParent.append(newFileName);
//
//    return proposedPathOfNewLink;
    return destinationFolder.append(newFileName);
  }
  
  /**
   * Method getNewFileName.
   * @param sourceItem IResource
   * @param destinationFolder CmsPath
   * @param operationName String
   * @return String
   * @throws ResourceException
   */
  public static String getNewFileName(IResource sourceItem, CmsPath destinationFolder, String operationName) throws ResourceException {
    return getNewFileName(sourceItem, destinationFolder, operationName, false);
  }

  /**
   * Method getNewFileName.
   * @param sourceItem IResource
   * @param destinationFolder CmsPath
   * @param operationName String
   * @param forceUnique boolean
   * @return String
   * @throws ResourceException
   */
  public static String getNewFileName(IResource sourceItem, CmsPath destinationFolder, String operationName, boolean forceUnique) throws ResourceException {
    IResourceManager mgr = ResourcesPlugin.getResourceManager();

    //first: check if this copy is going into the same folder as the original since if NOT
    //we will not change the name of the new copy but instead ask the user if they want to 
    //replace the existing file:
    if(!forceUnique && !destinationFolder.equals(sourceItem.getPath().removeLastSegments(1))){
      //means they are NOT in the same folder, so just return the original filename:
      return sourceItem.getName();
    } else if (forceUnique) {
      // our mandate is to make the new name unique, so it may or may not
      // include the operation name.
      // look at the children and see if we can get by without the operation
      // name (e.g. "Link to <filename>")
      IFolder destFolder = (IFolder) mgr.getResource(destinationFolder);
      List<IResource> children = destFolder.getChildren();
      boolean isUnique = true;

      for (IResource child : children) {
        if (child.getName().equals(sourceItem.getName())) {
          isUnique = false;
        }
      }

      if (isUnique) {
        return sourceItem.getName();
      }
    }

    //second: check if "Copy of *filename*" or "Link to *filename*" already exists in this folder:
    String fileName = sourceItem.getName();
    String newName = operationName +" "+ fileName;
    CmsPath checkPath =  destinationFolder.append(newName);
    if(!mgr.resourceExists(checkPath)){
      return newName;
    }

    //if that default name already exists, start checking for the next available int to use in the name, IE: 
    //"Copy of (2) zoe.txt"
    int i = 1;
    newName = operationName +" ("+String.valueOf(i) +") "+ fileName;
    checkPath = destinationFolder.append(newName);
    while(mgr.resourceExists(checkPath)){
      i++;
      newName = operationName +" ("+String.valueOf(i) +") "+ fileName;
      checkPath = destinationFolder.append(newName);
    }
    //got out of the above loop, so whatever we set for newName in the loop is the good-en
    return newName;
  }

  /**
   * Fills the array "usedIndexes" based on if the index in use by another file within the target directory.  An value other then 0 in the array denotes a used index.
   * 
   * Example:
   *   "readme.txt" and
   *   "Link to readme.txt"
   *   
   *   Then the next file created should have the name "Link to (2) readme.txt"
   *   
   * @param resourceNeedingShortcut
   * @param parentChildren
   * @param operation
  
   * @return int[]
   */ 
  private int[] nextAvailableIndex(IResource resourceNeedingShortcut, Vector parentChildren, int operation) {
    String operationName = getOperationText(operation);
    //System.out.println("zoe");
    int[] usedIndexes = new int[parentChildren.size() + 1];
    
    // Walk the children of the destination folder and determine which indexes 
    //     are currently being used.  i.e. those with the same basic name.
    for (Iterator iter = parentChildren.iterator(); iter.hasNext();) {
      
      // Get the resources name
      IResource element = (IResource) iter.next();
      String name = element.getName();
      
      String labelWithParen = operationName + " ("; 
      
      // Does this file have an index AND the names match. i.e. "Link to (2) Readme.txt"
      if ((name.startsWith(labelWithParen)) && (name.endsWith(resourceNeedingShortcut.getName()))) {
        int beginningPosition = name.indexOf("(");
        int endingPosition = name.indexOf(")");
        
        int index = 0;
        
        try {
          String ourIndex = name.substring(beginningPosition + 1, endingPosition);
          index = Integer.parseInt(ourIndex);
        } catch (NumberFormatException nfe) {
          // Failed to read an index number. continue anyway.
          continue;
        }

        usedIndexes[index] = index;
      } else if ((name.startsWith(operationName)) && (name.endsWith(resourceNeedingShortcut.getName()))) {
        // The names match but the file does not have an index number. i.e. "Link to Readme.txt"
        usedIndexes[1] = 1;
      } else if (name.equalsIgnoreCase(resourceNeedingShortcut.getName())) {
        // The name matches the original without any labels. i.e. "Readme.txt"
        usedIndexes[0] = 1;
      }
    }
    
    return usedIndexes;
  }

  
  
  /**
   * Used to determine the starting text that a duplicate file name should have for this operation.
   * @param operation Type of operation being performed.  Based on the ActionUtil static type ActionUtil.COPY, ActionUtil.MOVE, and etc.
  
  
   * @return Text string constant for a duplicate file name and this operation.  * @see ActionUtil.COPYOPERATION, ActionUtil.LINKOPERATION */
  private String getOperationText(int operation) {
    String operationName = "";
    if (operation == ActionUtil.COPY || operation == ActionUtil.MOVE) {
      operationName = COPYOPERATION;
      if(operation == ActionUtil.COPY){
        errorTitle = "Error During Copy";
        errorMsg = "An error has occured during the copy process.";
      }
      if(operation == ActionUtil.MOVE){
        errorTitle = "Error During Move";
        errorMsg = "An error has occurred during the move process.";
      }
    } else if (operation == ActionUtil.LINK) {
      operationName = LINKOPERATION;
    }
    return operationName;
  }
}
