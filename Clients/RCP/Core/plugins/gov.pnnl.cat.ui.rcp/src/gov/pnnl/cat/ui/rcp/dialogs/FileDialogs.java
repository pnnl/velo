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
package gov.pnnl.cat.ui.rcp.dialogs;


import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter;
import gov.pnnl.velo.model.CmsPath;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 */
public class FileDialogs extends AbstractDialog {

  private static Logger logger = CatLogger.getLogger(FileDialogs.class);
 
  /**
   * Prompt the user for permission to overwrite a folder.  Dialog will be of type Yes/No.
  
  
  
  
   * @param shell Shell
   * @param folderPath CmsPath
   * @param isLinked boolean
   * @return int
   */
  public static int canOverWriteFolder(final Shell shell, CmsPath folderPath, boolean isLinked) {
    synchronized (lock) {
      return fileOrFolderAlreadyExists(shell, folderPath, true, isLinked);
    }
  }



  /**
   * Prompt the user  for permission to overwrite a file.  Dialog will be of type Yes/No.
  
   * @param filePath
  
  
   * @param shell Shell
   * @param isLinked boolean
   * @return int
   */
  public static int canOverWriteFile(Shell shell, CmsPath filePath, boolean isLinked) {
    synchronized (lock) {
      return fileOrFolderAlreadyExists(shell, filePath, false, isLinked);
    }
  }



  /**
   * Displays a Yes/No dialog asking the user if they want to over write the resource (File or Folder).
  
   * @param folderPath 
  
   * @param isaFolder
  
   * @param shell Shell
   * @param isLinked boolean
   * @return int
   */
  private static int fileOrFolderAlreadyExists(Shell shell, final CmsPath folderPath, final boolean isaFolder, final boolean isLinked) {
    
    //if (bOverwrite == false) {
      
      //Display.getDefault().syncExec(new Runnable() {

        //public void run() {
          String title = "";
          if(isLinked){
            title = "Linked ";
          }
          // Compose the dialog's title.
          
          if (isaFolder) {
            title += "Folder Already Exists";
          } else {
            title += "File Already Exists";
          }

          // Compose the dialog's message.
          String message = "The";
          if(isLinked){
            message += " linked";
          }
          message += " file already exists on the server:\n\"" +folderPath.toDisplayString()+ "\"\n\nDo you want to overwrite it?";
          if (isaFolder) {
            message = "A";
            if(isLinked){
              message += " linked";
            }
            message +=" folder with the same name already exists on the server:\n\n\"" + folderPath.toDisplayString() +"\"\n";
            if(!isLinked){             
               message += "\nAny contents of this folder that exist in the destination will be replaced.";
            }
            message += "  Do you want to MERGE this folder?";
          }
          
          // Display the dialog.
          //Shell shell = getShell(control);
          return queryYesToAllNoToAll(title, message, shell);
        //}
      //});
     
    //}

  }

  
  /**
   * Method queryYesToAllNoToAll.
   * @param title String
   * @param message String
   * @param shell Shell
   * @return int
   */
  private static int queryYesToAllNoToAll(final String title, final String message, Shell shell) {
 
    final int[] result = new int[1]; 
    logger.debug(Display.getDefault().toString());
    final Shell finalShell = checkShell(shell);
    
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        MessageDialog dialog = new MessageDialog(
            finalShell,
            title,
            null,
            message,
            MessageDialog.QUESTION, new String[] {
                IDialogConstants.YES_LABEL,
                IDialogConstants.YES_TO_ALL_LABEL,
                IDialogConstants.NO_LABEL,
                IDialogConstants.NO_TO_ALL_LABEL,
                IDialogConstants.CANCEL_LABEL }, 0);
        dialog.open();
        result[0] = dialog.getReturnCode();
      }
    });
    int value = result[0];
    if (value == 0)
      return IDialogConstants.YES_ID;
    if (value == 1)
      return IDialogConstants.YES_TO_ALL_ID;
    if (value == 2)
      return IDialogConstants.NO_ID;
    if (value == 3)
      return IDialogConstants.NO_TO_ALL_ID;
    
    return IDialogConstants.CANCEL_ID;
  }  
  

  /**
   * Method openLocalFileReadErrorDialog.
   * @param shell Shell
   * @param pathOfLocalFile String
   */
  public static void openLocalFileReadErrorDialog(final Shell shell, final String pathOfLocalFile) {
    synchronized (lock) {
      Display.getDefault().syncExec(new Runnable() {

        public void run() {
          MessageDialog.openError(shell, "File Does Not Exist or Cannot be Read", "File Does Not Exist or Cannot be Read: " + pathOfLocalFile);
        }
      });
    }
  }
  



  /**
   * Method openResourceMovedOrDeletedErrorDialog.
   * @param shell Shell
   * @param pathOfLocalFile String
   * @param moveOrCopy String
   */
  public static void openResourceMovedOrDeletedErrorDialog(final Shell shell, 
      final String pathOfLocalFile, final String moveOrCopy) {
    synchronized (lock) {
    
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          MessageDialog.openError(shell, "Resource was moved or deleted", "Cannot " + moveOrCopy + ":\n" + pathOfLocalFile + "\nThis resource was moved or deleted.");
        }
      });
    }
  }
  
  
  /**
   * Method confirmDeleteDialog.
   * @param shell Shell
   * @param selectedResources ITransferObjectAdapter[]
   * @return boolean
   */
  public static boolean confirmDeleteDialog(Shell shell, final ITransferObjectAdapter[] selectedResources) {

    synchronized (lock) {
      
      StringBuffer title = new StringBuffer("Confirm ");
      StringBuffer message = new StringBuffer ("Are you sure you want to delete ");
      if (selectedResources.length > 1) {
        message.append( "these ").append(selectedResources.length).append(" items and any links pointing to them?");
        title.append("Multiple File Delete");
      } else { // Deletion of a single resource. Gets here if the numberOfFiles is 0 or 1.
        IResource resource = (IResource) selectedResources[0].getObject();
        String name = resource.getName();
        if (resource instanceof ILinkedResource) {
          title.append("File Delete");
          message.append("the link '").append(name).append("'?");
        } else if (resource instanceof IFolder) {
          title.append("Folder Delete");
          message.append("the folder '").append(name).append("', all of its contents, and any links pointing to its contents?");
        } else {
          title.append("File Delete");
          message.append("the file '").append(name).append("' and any links pointing to it?");
        }
      }
      
      return queryYesNo(shell, title.toString(), message.toString());
    }
  }
  
  public static boolean confirmMergeOverwrite(Shell shell, String title) {
    String message = "Any existing folders will be merged.  Any existing files will be overwritten.";
    return queryYesNo(shell, title, message);
    
  }
  
  /**
   * Method confirmDeleteDialog.
   * @param shell Shell
   * @param selectedResources IResource[]
   * @return boolean
   */
  public static boolean confirmDeleteDialog(Shell shell, final IResource[] selectedResources) {

    synchronized (lock) {
      
      StringBuffer title = new StringBuffer("Confirm ");
      StringBuffer message = new StringBuffer ("Are you sure you want to delete ");
      if (selectedResources.length > 1) {
        message.append( "these ").append(selectedResources.length).append(" items and any links pointing to them?");
        title.append("Multiple File Delete");
      } else { // Deletion of a single resource. Gets here if the numberOfFiles is 0 or 1.
        IResource resource = (IResource) selectedResources[0];
        String name = resource.getName();
        if (resource instanceof ILinkedResource) {
          title.append("File Delete");
          message.append("the link '").append(name).append("'?");
        } else if (resource instanceof IFolder) {
          title.append("Folder Delete");
          message.append("the folder '").append(name).append("', all of its contents, and any links pointing to its contents?");
        } else {
          title.append("File Delete");
          message.append("the file '").append(name).append("' and any links pointing to it?");
        }
      }
      
      return queryYesNo(shell, title.toString(), message.toString());
    }
  }
  
  /**
   * Method checkShell.
   * @param shell Shell
   * @return Shell
   */
  private static Shell checkShell(Shell shell) {
    final Shell[] shells = {shell};
    if (shell == null) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {         
          shells[0] = Display.getDefault().getActiveShell();          
        }
      });
    }
    return shells[0];
  }
  
  /**
   * Method queryYesNo.
   * @param shell Shell
   * @param title String
   * @param message String
   * @return boolean
   */
  private static boolean queryYesNo(Shell shell, String title, String message) {
    shell = checkShell(shell);
    
    final MessageDialog dialog = new MessageDialog(
        shell,
        title.toString(),
        null,
        message.toString(),
        MessageDialog.QUESTION, new String[] {
            IDialogConstants.YES_LABEL,
            IDialogConstants.NO_LABEL,
            IDialogConstants.CANCEL_LABEL }, 0);
    
    shell.getDisplay().syncExec(new Runnable() {
      public void run() {
        dialog.open();
      }
    });
    
    int result = dialog.getReturnCode();
    if (result == 0) {
      return true;
    } 
    return false;          
  }
  
  /**
   * Method confirmDeleteResourceLinksDialog.
   * @param shell Shell
   * @param resource IResource
   * @param links Collection
   * @return int
   */
  public static int confirmDeleteResourceLinksDialog(Shell shell, final IResource resource, 
      Collection links) {

    // TODO: Should be using a dialog with Yes To All and No To All options
    synchronized (lock) {
      
      // Generate the message, including a list of the links
      StringBuffer buf = new StringBuffer("The file: ");
      buf.append(resource.getName());
      buf.append(" has the following links:\n\n");
      Iterator it = links.iterator();
      IResource link;
      while (it.hasNext()) {
        link = (IResource)it.next();
        buf.append("    ");
        buf.append(link.getPath().toString());
        buf.append("\n");       
      }
      buf.append("\nAre you sure you want to delete this file and all its links?");
      return queryYesToAllNoToAll("Confirm Remove Links", buf.toString(), shell);
    }
  }
  
  /**
   * Method confirmALotOfFilesWarning.
   * @param shell Shell
   * @param resource IResource
   * @return int
   */
  public static int confirmALotOfFilesWarning(Shell shell, final IResource resource) {
    
    synchronized (lock) {
      final int[] result = new int[1]; 
      logger.debug(Display.getDefault().toString());
      final Shell finalShell = checkShell(shell);
      
      Display.getDefault().syncExec(new Runnable() {
        public void run() {//TODO: wording on this dialog could be better, i'm open to suggestions.
          MessageDialog dialog = new MessageDialog(
              finalShell,
              "Folder with a lot files",
              null,
              "This action will result in " +resource.getName() + " containing more than 1000 documents. As this folder grows larger than 1000 documents performance will degrade.  Do you want to continue?",
              MessageDialog.QUESTION, new String[] {
                  IDialogConstants.YES_LABEL,
                  IDialogConstants.NO_LABEL }, 0);
          dialog.open();
          result[0] = dialog.getReturnCode();
        }
      });
      int value = result[0];
      if (value == 0)
        return IDialogConstants.YES_ID;
      if (value == 2)
        return IDialogConstants.NO_ID;
      
      return IDialogConstants.CANCEL_ID;
      
    }
  }
  
}
