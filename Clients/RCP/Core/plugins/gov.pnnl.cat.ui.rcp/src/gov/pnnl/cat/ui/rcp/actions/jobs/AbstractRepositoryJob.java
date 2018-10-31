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

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter;
import gov.pnnl.cat.ui.rcp.dialogs.FileDialogs;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;

/**
 */
public abstract class AbstractRepositoryJob extends Job {
  protected long begin = 0;

  private static IResourceManager actionMgr;
  public static final String CANCEL_OPERATION = "cancel";

  // Private since we're only using for logic of making a name

  protected String[] sourcePaths;
  protected Shell shell;
  private ITransferObjectAdapter[] transObj;
  private boolean isCanceled = false;

  //  // A map to keep track of invalid folders so that when checking to see
  //  // if a file already exists on the server already, we don't have
  //  // to check it if the parent folder doesn't exist.
  //  protected HashSet<CmsPath> invalidFolders = new HashSet<CmsPath>();


  private Logger logger = CatLogger.getLogger(getClass());


  /**
   * Constructor for AbstractRepositoryJob.
   * @param sourcePaths String[]
   * @param shell Shell
   */
  public AbstractRepositoryJob(String[] sourcePaths, Shell shell) {
    super("");

    // Variables needed within the run thread.

    this.setName(getJobDescription());
    this.sourcePaths = sourcePaths;
    this.shell = shell;
  }


  /**
   * Method getJobType.
   * @return String
   */
  abstract public String getJobType();


  /**
   * Method getDestination.
   * @param currentFile ITransferObjectAdapter
   * @return CmsPath
   * @throws ResourceException
   */
  abstract public CmsPath getDestination(ITransferObjectAdapter currentFile) throws ResourceException;

  /**
   * Method getJobDescription.
   * @return String
   */
  abstract public String getJobDescription();

  /**
   * Method resolveDestinationPath.
   * @param currentFile ITransferObjectAdapter
   * @param destination CmsPath
   * @return CmsPath
   * @throws ResourceException
   */
  abstract public CmsPath resolveDestinationPath(ITransferObjectAdapter currentFile, CmsPath destination) throws ResourceException;

  /**
   * Method checkFolderOperation.
   * @param destination CmsPath
   * @param newDestinationFolderPath CmsPath
   * @param dialogAnswers HashMap<String,Integer>
   * @return boolean
   * @throws ResourceException
   */
  abstract public boolean checkFolderOperation(CmsPath destination, CmsPath newDestinationFolderPath, HashMap<String, Integer> dialogAnswers) throws ResourceException; 

  /**
   * Method getTotalWork.
   * @return int
   */
  abstract public int getTotalWork();

  /**
   * Method canStartOperation.
   * @param currentFile ITransferObjectAdapter
   * @param destination CmsPath
   * @return boolean
   */
  public boolean canStartOperation(ITransferObjectAdapter currentFile, CmsPath destination) {
    return true;
  }

  /**
   * Method currentOperationComplete.
   * @param currentFile ITransferObjectAdapter
   * @param destination CmsPath
   * @param dialogAnswers HashMap<String,Integer>
   */
  public void currentOperationComplete(ITransferObjectAdapter currentFile, CmsPath destination, HashMap<String, Integer> dialogAnswers) {
  }

  /**
   * Method jobTraversalComplete.
   * @param monitor IProgressMonitor
   */
  public void jobTraversalComplete(IProgressMonitor monitor) {
  }


  /**
   * Method getSourceFiles.
   * @return ITransferObjectAdapter[]
   */
  final protected ITransferObjectAdapter[] getSourceFiles() {
    if (transObj == null) {
      transObj = getSourceFilesForJob();
    }
    return transObj;
  }

  /**
   * Method getSourceFilesForJob.
   * @return ITransferObjectAdapter[]
   */
  protected ITransferObjectAdapter[] getSourceFilesForJob() {
    return getIResourceArray(sourcePaths, getJobType());
  }


  /**
   * Method countSourceFileBytes.
   * @return long
   */
  protected long countSourceFileBytes() {
    return countFileBytes(getSourceFiles());
  }

  /**
   * Method canTreatAsFile.
   * @param currentFile ITransferObjectAdapter
   * @return boolean
   */
  protected boolean canTreatAsFile(ITransferObjectAdapter currentFile) {
    return (currentFile.isFile() || currentFile.isLinked());
  }

  /**
   * Method canRun.
   * @param selectedResources ITransferObjectAdapter[]
   * @return boolean
   */
  protected boolean canRun(ITransferObjectAdapter[] selectedResources) {
    return true;
  }

  /**
   * Method operate.
   * @param currentFile ITransferObjectAdapter
   * @param destination CmsPath
   * @param dialogAnswers HashMap<String,Integer>
   * @param monitor IProgressMonitor
   * @param checkExistence boolean
   * @throws ResourceException
   */
  public void operate(ITransferObjectAdapter currentFile, CmsPath destination, HashMap<String, Integer> dialogAnswers, IProgressMonitor monitor, boolean checkExistence) throws ResourceException {

    if (canTreatAsFile(currentFile)) {
      // Transfer a file

      if (!transferFile(currentFile, destination, monitor, dialogAnswers, checkExistence)) {
        return;
      }
    } else {
      // Transfer a folder
      // Check that it is not being moved into a subfolder of itself if it is a move
      transferFolder(currentFile, destination, monitor, dialogAnswers, checkExistence);
    }
    currentOperationComplete(currentFile, destination, dialogAnswers);
  }

  /**
   * Method run.
   * @param monitor IProgressMonitor
   * @return IStatus
   */
  protected IStatus run(IProgressMonitor monitor) {
    long startCounting = System.currentTimeMillis();

    monitor.setTaskName("Preparing to " + getJobType() + " Files...");
    monitor.subTask("Counting Files...");
    //    monitor.beginTask(getJobDescription() + "s", getTotalWork());
    HashMap<String, Integer> dialogAnswers = new HashMap<String, Integer>();

    ITransferObjectAdapter[] sourceTransferFiles = getSourceFiles();

    if (canRun(sourceTransferFiles)) {
      begin = System.currentTimeMillis();
      monitor.setTaskName("Executing " + getJobType());
      for (int i = 0; i < sourceTransferFiles.length; i++) {

        ITransferObjectAdapter currentFile = sourceTransferFiles[i];
        try {
          if (canStartOperation(currentFile, getDestination(currentFile))) {
            operate(currentFile, getDestination(currentFile), dialogAnswers, monitor, true);
          }
        } catch (ResourceException e) {
          ToolErrorHandler.handleError("Resource could not be processed: " + currentFile.getPath(), e, true);
        }
        if (monitor.isCanceled() || isOperationCanceled()) {
          return Status.CANCEL_STATUS;
        }
      }
    }
    long endCounting = System.currentTimeMillis();

    logger.debug("Time to count files: " + (endCounting - startCounting) + " ms");

    jobTraversalComplete(monitor);

    if(logger.isDebugEnabled()) {
      long end = System.currentTimeMillis();
      logger.debug(getJobType() + " COMPLETE: TOOK " + (end - begin) + " ms");
    }
    return Status.OK_STATUS;
  }

  /**
   * Used to retrieve the resource tree manager.
   * 
  
   * @return IResourceManager */
  protected IResourceManager getManager() {
    if (actionMgr == null) {
      actionMgr = ResourcesPlugin.getResourceManager();
    }
    return actionMgr;
  }

  /**
   * Converts an array of CAT resource paths into an array of transfer objects.
   * 
   * @param sourcePaths
   *          The path to the resource within the CAT
   * @param jobType
   *          Used in the error dialog box to indicate what type of operation failed (i.e. "move" or "copy").
  
   * @return ITransferObjectAdapter[] */
  private ITransferObjectAdapter[] getIResourceArray(String[] sourcePaths, String jobType) {
    Vector<ITransferObjectAdapter> fileVector = new Vector<ITransferObjectAdapter>(); // Vector of ITransferObjectAdapter(s).
    IResourceManager mgr = getManager();

    // ITransferObjectAdapter[] fileArray = new ITransferObjectAdapter[sourcePaths.length];
    for (int i = 0; i < sourcePaths.length; i++) {
      CmsPath curPath = new CmsPath(sourcePaths[i]);

      try {
        IResource curResource = mgr.getResource(curPath);
        fileVector.addElement((ITransferObjectAdapter) ((IAdaptable)curResource).getAdapter(ITransferObjectAdapter.class));
        // fileArray[i] = (ITransferObjectAdapter) curResource.getAdapter( ITransferObjectAdapter.class );
      } catch (ResourceException e) {
        // we will get javax.jcr.PathNotFoundException exception when someone copys to the clipboard
        // then an item that was copied gets deleted or moved, then the user tries to paste
        // the item that was deleted or moved:
        //EZLogger.logError(e, "ResourceExecption while tring to convert the CmsPath \"" + curPath.toOSString() + "\" into an IResource.");
        logger.error("ResourceExecption while tring to convert the CmsPath \"" + curPath.toDisplayString() + "\" into an IResource.", e);
        FileDialogs.openResourceMovedOrDeletedErrorDialog(shell, curPath.toDisplayString(), jobType);
      }
    }

    return (ITransferObjectAdapter[]) fileVector.toArray(new ITransferObjectAdapter[fileVector.size()]);
  }

  /**
   * Utility function for counting the number of files in an array of ITransferObjectAdapter objects.<br>
   * 
   * @param transObj
   *          An array of strings representing the individual files.
  
   * @return long The total number of bytes in all files contained in the array. */
  private long countFileBytes(ITransferObjectAdapter[] transObj) {
    long lCount = 0;

    for (int i = 0; i < transObj.length; i++) {
      lCount += countFileFolderBytes(transObj[i]);
    }
    return lCount;
  }

  /**
   * A recursive utility method that counts the number of bytes in files from this file down.<br>
   * 
   * @param transObj
   *          The root file to begin counting from.
  
   * @return long The total number of files from this file down. */
  private long countFileFolderBytes(ITransferObjectAdapter transObj) {
    long lCount = 0;
    if (transObj.isFile() || transObj.isLinked()) {
      return transObj.getSize();
    } else if (transObj.isFolder()) {
      ITransferObjectAdapter[] fileArray = transObj.getChildren();
      for (int i = 0; i < fileArray.length; i++) {
        lCount += countFileFolderBytes(fileArray[i]);
      }
    } else {
      logger.warn("Failed to count a file.  Reason maybe a new CAT object type was added? Object label = " + transObj.getLabel());
    }
    return lCount;
  }

  /**
   * Method transferFolder.
   * @param folder ITransferObjectAdapter
   * @param destinationFolder CmsPath
   * @param monitor IProgressMonitor
   * @param dialogAnswers HashMap<String,Integer>
   * @param checkExistence boolean
   * @return boolean
   * @throws ResourceException
   */
  private boolean transferFolder(final ITransferObjectAdapter folder, CmsPath destinationFolder, IProgressMonitor monitor, HashMap<String, Integer> dialogAnswers, boolean checkExistence) throws ResourceException {

    ITransferObjectAdapter[] fileArray = folder.getChildren();
    CmsPath newDestinationFolderPath = resolveDestinationPath(folder, destinationFolder);

    //make sure that the folder name does not contain any invalid character
    //Not sure if folder is always a File - so put this in a try block
    //this invalid character test is only for file folders, so Exception catch does not do anything
    try {
      File folderAsFile = (File) folder.getObject();
      if(folderAsFile.getName().matches(IResource.invalidCharactersRegex))
      {
        String errorMsg = "Upload failed.  A folder name " + IResource.invalidCharactersMsg;
        ToolErrorHandler.handleError(errorMsg, null, true);
        return false;
      }
    }
    catch (Exception e)
    {
      ;
    }

    try {

      boolean newFolder = true;
      boolean proceed = true;
      System.out.println("getting children for folder " + destinationFolder.toDisplayString());
      getManager().getChildren(destinationFolder);
      if ( (checkExistence == true) && (getManager().resourceCached(newDestinationFolderPath) == true)) {
        newFolder = false;
        logger.debug("checking folder existence");
        proceed = checkFolderOperation(destinationFolder, newDestinationFolderPath, dialogAnswers);
      }

      if (proceed) {
        boolean checkChildExistence = !newFolder;

        // Only schedule folder upload if it is new
        // TODO: come up with a more generic way of determining whether existence checks are needed.
        // Each transfer type needs to be able to implement its own rules for existence checking.
        // We don't want to waste time on existence checks that aren't needed.
        if(newFolder) {
          boolean completeTransfer = doFolderTransfer(folder, destinationFolder, newDestinationFolderPath, monitor);
          if (completeTransfer) {
            return true;
          }
        }
        if(!newFolder) {
          // we are going to iterate over children to do a merge, so we have to make sure the destination folder's children
          // are in the cache so we can check for conflicts quickly without have to do a resourceExists on every child
          getManager().getChildren(newDestinationFolderPath);
        }
        for (int i = 0; i < fileArray.length; i++) {
          if (monitor.isCanceled() || isOperationCanceled()) {
            return false;
          }

          // clone the map so that subfolders and files can see the answers that we have received, without
          // altering behavior for the parent.
          @SuppressWarnings("unchecked")
          HashMap<String, Integer> dialogAnswersCopy = (HashMap<String, Integer>) dialogAnswers.clone();

          operate(fileArray[i], newDestinationFolderPath, dialogAnswersCopy, monitor, checkChildExistence);
        }
        //after a successful copy of all its children, the folder needs to be removed!
        doRemoveFolder(folder);
      }
    } catch (FileNotFoundException e) {
      String errMsg = "Folder could not be found: " + folder.getPath();
      ToolErrorHandler.handleError(errMsg, e, true);
    } catch (ResourceException e) {
      String errMsg = "Folder can not be processed: " + folder.getPath();
      ToolErrorHandler.handleError(errMsg, e, true);
    }

    return false;
  }

  /**
   * Method transferFile.
   * @param transferFile ITransferObjectAdapter
   * @param destinationFolder CmsPath
   * @param monitor IProgressMonitor
   * @param dialogAnswers HashMap<String,Integer>
   * @param checkExistence boolean
   * @return boolean
   */
  private boolean transferFile(final ITransferObjectAdapter transferFile, CmsPath destinationFolder, IProgressMonitor monitor, HashMap<String, Integer> dialogAnswers, boolean checkExistence) {
    try {

      if (transferFile.exists() && transferFile.canRead()) {

        //make sure that the file name does not contain any invalid character
        //Not sure if transferFile is always a File - so put this in a try block
        //this invalid character test is only for files, so Exception catch does not do anything
        try {
          File file = (File) transferFile.getObject();
          if(file.getName().matches(IResource.invalidCharactersRegex))
          {
            String errMsg = "Upload failed.  A file name " + IResource.invalidCharactersMsg;
            ToolErrorHandler.handleError(errMsg, null, true);
            return false;
          }
        }
        catch (Exception e)
        {
          ;
        }

        CmsPath destination = resolveDestinationPath(transferFile, destinationFolder);
        monitor.subTask("Checking File: " + transferFile.getLabel());

        if(checkExistence) {
          // we are assuming that from transfer folder the destination folder
          // has been loaded in the cache, so we can check for conflicts using
          // the cache instead of hitting the server each time
          boolean exists = getManager().resourceCached(destination);

          if(exists) {
            boolean proceed = checkFileOperation(destination, dialogAnswers);
            if (proceed && !monitor.isCanceled()) {
              doPreFileTransfer(destination);
              doFileTransfer(transferFile, destination, monitor);
            }

          } else if(!monitor.isCanceled()) {
            doFileTransfer(transferFile, destination, monitor);
          }

        } else if(!monitor.isCanceled()) {
          doFileTransfer(transferFile, destination, monitor);          
        }

        //        monitor.worked((int)transferFile.getSize());
      } else {
        FileDialogs.openLocalFileReadErrorDialog(shell, transferFile.getPath());
      }

    } catch (FileNotFoundException e) {
      String errMsg = "File could not be found: " + transferFile.getPath();
      ToolErrorHandler.handleError(errMsg, e, true);
      return false;
    } catch (ResourceException e) {
      String errMsg = "File can not be processed: " + transferFile.getPath();
      ToolErrorHandler.handleError(errMsg, e, true);
      return false;
    }

    return true;
  }

  /**
   * Method doPreFileTransfer.
   * @param resolvedDestination CmsPath
   * @throws ResourceException
   */
  public abstract void doPreFileTransfer(CmsPath resolvedDestination)  throws ResourceException;
  /**
   * Method doFileTransfer.
   * @param transferFile ITransferObjectAdapter
   * @param resolvedDestination CmsPath
   * @param monitor IProgressMonitor
   * @throws ResourceException
   * @throws FileNotFoundException
   */
  public abstract void doFileTransfer(final ITransferObjectAdapter transferFile, CmsPath resolvedDestination, IProgressMonitor monitor) throws ResourceException, FileNotFoundException;
  /**
   * Method doFolderTransfer.
   * @param transferFolder ITransferObjectAdapter
   * @param originalDestination CmsPath
   * @param newDestination CmsPath
   * @param monitor IProgressMonitor
   * @return boolean
   * @throws ResourceException
   * @throws FileNotFoundException
   */
  public abstract boolean doFolderTransfer(final ITransferObjectAdapter transferFolder, CmsPath originalDestination, CmsPath newDestination, IProgressMonitor monitor) throws ResourceException, FileNotFoundException;
  /**
   * Method doRemoveFolder.
   * @param folder ITransferObjectAdapter
   * @return boolean
   */
  public boolean doRemoveFolder(final ITransferObjectAdapter folder) {return false;}

  /**
   * Method checkFileOperation.
   * @param filePath CmsPath
   * @param dialogAnswers HashMap<String,Integer>
   * @return boolean
   * @throws ResourceException
   */
  protected abstract boolean checkFileOperation(CmsPath filePath, HashMap<String, Integer> dialogAnswers) throws ResourceException;

  /**
   * Method isOperationCanceled.
   * @return boolean
   */
  protected boolean isOperationCanceled() {
    return this.isCanceled;
  }

  public void cancelOperation() {
    this.isCanceled = true;
  }

  /**
   * Method putDialogValue.
   * @param dialogAnswers HashMap<String,Integer>
   * @param key String
   * @param value int
   * @return boolean
   */
  protected boolean putDialogValue(HashMap<String, Integer> dialogAnswers, String key, int value) {
    if (value == IDialogConstants.CANCEL_ID) {
      cancelOperation();
      return false;
    }

    dialogAnswers.put(key, new Integer(value));
    if (value == IDialogConstants.YES_ID || value == IDialogConstants.YES_TO_ALL_ID) {
      return true;
    }
    return false;
  }

  /**
   * Method getDialogValue.
   * @param dialogAnswers HashMap<String,Integer>
   * @param key String
   * @return int
   */
  protected int getDialogValue(HashMap<String, Integer> dialogAnswers, String key) {
    Integer intDialogValue = dialogAnswers.get(key);
    if (intDialogValue == null) {
      return -1;
    }
    return intDialogValue.intValue();
  }
}
