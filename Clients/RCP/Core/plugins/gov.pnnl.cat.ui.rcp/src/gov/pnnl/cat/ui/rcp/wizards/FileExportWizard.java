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

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.utils.CatUIUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 */
public class FileExportWizard extends Wizard implements IExportWizard,
IRunnableWithProgress {

  private static final int COMMON_EXPORT_SIZE = 25;

  private final static int MAX_LENGTH = 1024 * 1024 * 10;

  public static final String COMMON_DATE_FORMAT = "EEE M/d/yyyy h:mm a";

  //The constants for the overwrites 3 states
  private static final int OVERWRITE_NOT_SET = 0;

  private static final int OVERWRITE_NONE = 1;

  private static final int OVERWRITE_ALL = 2;

  private int overwriteState = OVERWRITE_NOT_SET;

  private List<IResource> resourcesToExport;

  private FileExportPage page;

  private String destinationPath;
  private String newNameForSingleExportedResource;

  private InfiniteProgress monitor;

  private boolean exportComments = false;

  private boolean atleastOneFileExported = false;

  private IResourceManager mgr;

  private Object catTreeRoot = null;
  private boolean showComments;

  private static final Logger logger = CatLogger
      .getLogger(FileExportWizard.class);

  public FileExportWizard() {
    this(null, true);
  }

  /**
   * Constructor for FileExportWizard.
   * @param catTreeRoot Object
   * @param showComments boolean
   */
  public FileExportWizard(Object catTreeRoot, boolean showComments) {
    this.catTreeRoot = catTreeRoot;
    this.showComments = showComments;
  }

  private void initState() {
    overwriteState = OVERWRITE_NOT_SET;
  }

  /**
   * Method performFinish.
   * @return boolean
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  @Override
  public boolean performFinish() {
    destinationPath = page.getDestinationFileValue();
    exportComments = page.getExportComments();
    resourcesToExport = page.getResourcesToExport();
    newNameForSingleExportedResource = page.getNewNameForSingleExportedResource();

    try {
      getContainer().run(true, true, this); 

    } catch (InterruptedException e) {
      showNotificationMessage("Export Cancelled", "The exporting of the file(s) has been cancelled.");
      return false;
    } catch (InvocationTargetException e) {
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      logger.error("An error occurred while trying to export files.", e);
      return false;
    }

    initState();

    //If at least one file has been exported show a message.
    //This handles the case when a user says "NO" to overwriting
    //a file when that is the only file being exported.
    if(isAtleastOneFileExported()){
      showNotificationMessage("Export Successful", "Successfully exported files to "
          + page.getDestinationFileValue());
    }
    return true;
  }

  /**
   * Exports the resource(s) to the target folder destination path

   * @throws InterruptedException */
  private void exportResources() throws InterruptedException{

    if(resourcesToExport.size() == 1 && newNameForSingleExportedResource != null) {
      File file = new File(destinationPath);
      destinationPath = file.getParentFile().getAbsolutePath();
    }

    Map<CmsPath, File> filesToDownload = new HashMap<CmsPath, File>();

    for(IResource resource : resourcesToExport) {
      if(resource.isType(IResource.LINK)){
        resource = ((ILinkedResource) resource).getTarget();
      }
      if(resource.isType(IResource.FILE)){
        exportFile(resource, destinationPath, filesToDownload);

      } else if(resource.isType(IResource.FOLDER)) {
        File targetFolder = null;
        if(resourcesToExport.size() == 1 && newNameForSingleExportedResource != null) {
          // use the new name for the exported folder
          targetFolder = new File(destinationPath, newNameForSingleExportedResource);

        } else {
          // use the same name as the resource
          targetFolder = new File(destinationPath, resource.getName());
        }
        if (!targetFolder.exists()) {
          targetFolder.mkdir();
        }
        exportResourceChildren(((IFolder)resource).getChildren(), targetFolder.getPath(), filesToDownload);
      }
    }
    monitor.subTask("Streaming files...");
    mgr.bulkDownload(filesToDownload);
    monitor.worked(1);
  }

  /**
   * A recursive method to find and export all of the files/folders in the resource
   * @param children The children of the resource
   * @param destinationFolder The target folders destination path

   * @throws InterruptedException */
  private void exportResourceChildren(List<IResource> children, String destinationFolder, Map<CmsPath, File> filesToDownload) throws InterruptedException{
    for (IResource child : children) {
      if(child.isType(IResource.LINK)){
        child = ((ILinkedResource) child).getTarget();
      }
      if(child.isType(IResource.FILE)) {
        exportFile(child, destinationFolder, filesToDownload);
        
      } else if(child.isType(IResource.FOLDER)){
        File targetFolder = new File(destinationFolder,child.getName());
        if (!targetFolder.exists()) {
          targetFolder.mkdir();
        }
        exportResourceChildren(((IFolder)child).getChildren(), targetFolder.getPath(), filesToDownload);
      }
    }
  }

  /**
   * Exports the given resource to the target folder
   * @param resource Resource to be exported
   * @param destinationFolder Target folder destination path

   * @throws InterruptedException */
  private void exportFile(IResource resource, String destinationFolder, Map<CmsPath, File> filesToDownload) throws InterruptedException{
    //Show a subtask message to the user so 
    //they know files are being exported during
    //long exports
    monitor.subTask("/" + resource.getName());

    File targetFile = null;
    if(resourcesToExport.size() == 1 && newNameForSingleExportedResource != null) {
      targetFile = new File(destinationFolder, newNameForSingleExportedResource);
    } else {
      targetFile = new File(destinationFolder, resource.getName());
    }    
    if (targetFile.exists()) {
      if (overwriteState == OVERWRITE_NONE) {
        return;
      }

      if (overwriteState != OVERWRITE_ALL) {
        int overwriteAnswer = queryYesToAllNoToAll(resource.getName());

        if (overwriteAnswer == IDialogConstants.CANCEL_ID) {
          throw new InterruptedException();
        }

        if (overwriteAnswer == IDialogConstants.NO_ID) {
          monitor.worked(1);
          return;
        }

        if (overwriteAnswer == IDialogConstants.NO_TO_ALL_ID) {
          monitor.worked(1);
          overwriteState = OVERWRITE_NONE;
          return;
        }

        if (overwriteAnswer == IDialogConstants.YES_TO_ALL_ID) {
          overwriteState = OVERWRITE_ALL;
        }
      }
    }

    //Export comments if the user has selected to do so
    if(isExportComments()){
      if(resource.hasAspect(VeloConstants.ASPECT_DISCUSSABLE)){
        exportCommentAsFile(resource, destinationFolder);
      }
    }
    filesToDownload.put(resource.getPath(), targetFile);
    setAtleastOneFileExported(true);
    monitor.worked(1);
    ModalContext.checkCanceled(monitor);
  }

  /**
   * Writes the comments for the resource to the given target folder
   * @param resource Resource that has the comments associated with it
   * @param targetFolder The destination path of the target folder
   */
  private void exportCommentAsFile(IResource resource, String targetFolder){
    File file = new File(targetFolder, resource.getName() + ".comments");
    BufferedOutputStream out = null;
    try {
      file.createNewFile();
      out = new BufferedOutputStream(new FileOutputStream(file));
    } catch (Exception e) {
      logger.error("An error occurred while trying to export comments for resource: " + resource.getPath(), e);
      return;
    }

    CmsPath discussionPath = resource.getPath().append(VeloConstants.ASSOC_NAME_DISCUSSION);
    List<IResource> topics = mgr.getChildren(discussionPath);
    for (IResource topicResource : topics) {
      // they should all be folders, but let's not break if someone adds a file here
      if (topicResource instanceof IFolder) {
        IFolder topic = (IFolder) topicResource;

        // get the posts
        List<IResource> children = topic.getChildren();
        for (IResource postResource : children) {
          // once again, they should all be files, but don't break if someone adds a folder here
          if (postResource instanceof IFile) {
            IFile post = (IFile) postResource;

            writeCommentsFile(file, topic.getName(), topic.getPropertyAsString(VeloConstants.PROP_MODIFIER), 
                post.getPropertyAsString(VeloConstants.PROP_MODIFIED), post, out);
          }
        }
      }
    }

    try {
      out.close();
    } catch (IOException e) {
      logger.error("An error occurred while trying to close the export comments file: " + resource.getPath(), e);
    }
  }

  /**
   * Writes the comments of a particular post to the comments file
   * @param file
   * @param postTopic Topic of the post
   * @param postAuthor Author of the post
   * @param postDate Date of the post
   * @param postContent Content of the post
   * @param out
   */
  private void writeCommentsFile(File file, String postTopic, String postAuthor, String postDate, IFile postContent, BufferedOutputStream out){
    try {
      byte[] topic = postTopic.getBytes();
      byte[] author = postAuthor.getBytes();
      SimpleDateFormat convertFrom = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
      SimpleDateFormat convertTo = new SimpleDateFormat(COMMON_DATE_FORMAT);
      InputStream postContentInputStream = postContent.getContent();
      Date formattedPostDate = convertFrom.parse(postDate.trim());
      StringBuffer content = CatUIUtil.readStreamFully(new InputStreamReader(postContentInputStream), MAX_LENGTH);
      out.write(topic,0,topic.length);
      out.write(" by ".getBytes());
      out.write(author, 0, author.length);
      out.write(" on ".getBytes());
      out.write(convertTo.format(formattedPostDate).getBytes(), 0, convertTo.format(formattedPostDate).toString().length());
      out.write("\n".getBytes());
      out.write(content.toString().getBytes());
      out.write("\n".getBytes());
      out.write("\n".getBytes());
    } catch (FileNotFoundException e) {
      logger.error("An error occurred while trying to export comments. The file " + file.getName() + " was not found", e);
    } catch(Exception e){
      logger.error("An error occurred while trying to export comments for resource: " + file.getName(),e);
    }
  }

  /**
   * OK dialog that is displayed to the user when a 
   * corrupt file is encountered during the exporting process
   * @param fileName String
   */
  private void queryOKdialog(String fileName) {
    final MessageDialog dialog = new MessageDialog(
        getContainer().getShell(),
        "Corrupt File",
        null,
        String.format(
            "An error occurred while trying to export file %s. The file will not be exported.",
            fileName), MessageDialog.INFORMATION,
            new String[] { IDialogConstants.OK_LABEL }, 0);

    page.getControl().getDisplay().syncExec(new Runnable() {
      public void run() {
        dialog.open();
      }
    });
  }

  /**
   * Method queryYesToAllNoToAll.
   * @param fileName String
   * @return int
   */
  private int queryYesToAllNoToAll(String fileName) {
    final int[] result = new int[1];

    final MessageDialog dialog = new MessageDialog(getContainer()
        .getShell(), "Question", null, String.format(
            "The file %s already exists.  Do you want to overwrite it?",
            fileName), MessageDialog.QUESTION, new String[] {
      IDialogConstants.YES_LABEL, IDialogConstants.YES_TO_ALL_LABEL,
      IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL,
      IDialogConstants.CANCEL_LABEL }, 0);

    page.getControl().getDisplay().syncExec(new Runnable() {
      public void run() {
        dialog.open();
      }
    });
    result[0] = dialog.getReturnCode();
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
   * Method init.
   * @param workbench IWorkbench
   * @param selection IStructuredSelection
   * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
   */
  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    initState();
    setNeedsProgressMonitor(true);
    mgr = ResourcesPlugin.getResourceManager();

    // init resources to export based on initial selection
    resourcesToExport = RCPUtil.getResources(selection);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages() {
    page = new FileExportPage("Select File or Folder to Export", resourcesToExport);
    page.setCatTreeRoot(catTreeRoot);
    page.setShowExportComments(showComments);
    addPage(page);
  }

  /**
   * Method showNotificationMessage.
   * @param title String
   * @param msg String
   */
  public void showNotificationMessage(String title, String msg) {
    CatUIUtil.showNotificationMessage(page.getControl(), title, msg);
  }

  /**
   * Method run.
   * @param monitor IProgressMonitor
   * @throws InvocationTargetException
   * @throws InterruptedException
   * @see org.eclipse.jface.operation.IRunnableWithProgress#run(IProgressMonitor)
   */
  @Override
  public void run(IProgressMonitor monitor) throws InvocationTargetException,
  InterruptedException {
    this.monitor = new InfiniteProgress(monitor);
    try {
      this.monitor.beginTask("Exporting file(s):", COMMON_EXPORT_SIZE);
      exportResources();
    } catch (Throwable e) {
      ToolErrorHandler.handleError("Failed to download files.", e, true);
    } finally {
      monitor.done();
    }
  }

  /**
   * Method dispose.
   * @see org.eclipse.jface.wizard.IWizard#dispose()
   */
  @Override
  public void dispose() {
    super.dispose();
  }

  /**
   * Method isExportComments.
   * @return boolean
   */
  public boolean isExportComments() {
    return exportComments;
  }

  /**
   * Method isAtleastOneFileExported.
   * @return boolean
   */
  public boolean isAtleastOneFileExported() {
    return atleastOneFileExported;
  }

  /**
   * Method setAtleastOneFileExported.
   * @param atleastOneFileExported boolean
   */
  public void setAtleastOneFileExported(boolean atleastOneFileExported) {
    this.atleastOneFileExported = atleastOneFileExported;
  }

}
