/**
 * 
 */
package gov.pnnl.cat.outlook.jobs;

import ezjcom.JComObject;
import ezjcom.JComVariant;
import gov.pnl.ezjcom.outlook.Attachment;
import gov.pnl.ezjcom.outlook.Attachments;
import gov.pnl.ezjcom.outlook.MAPIFolder;
import gov.pnl.ezjcom.outlook.Selection;
import gov.pnl.ezjcom.outlook._Folders;
import gov.pnl.ezjcom.outlook._Items;
import gov.pnl.ezjcom.outlook._MailItem;
import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.outlook.dnd.OutlookMsgTransfer;
import gov.pnnl.cat.ui.rcp.exceptionhandling.ExceptionHandler;
import gov.pnnl.velo.model.CmsPath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Uploads a selection of emails from your Outlook application to CAT server
 */
public class UploadOutlookEmailJob extends Job {
  private static Logger logger = CatLogger.getLogger(UploadOutlookEmailJob.class);

  private Selection selection;

  MAPIFolder selectedFolder;

  private boolean errorOccurred = false;

  private IFolder destinationFolder;

  public UploadOutlookEmailJob(Selection selectedEmail, MAPIFolder selectedFolder) {
    super("Outlook Upload Job");
    this.selection = selectedEmail;
    this.selectedFolder = selectedFolder;
  }

  /**
   * @param destinationFolder
   *          the destinationFolder to set
   */
  public void setDestinationFolder(IFolder destinationFolder) {
    this.destinationFolder = destinationFolder;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {

      // can it be folder itself if count==0. No, this does not work
      // when a folder is selected, but none of its item highlighted.
      if (selectedFolder != null) {
        monitor.beginTask("Uploading email folder...  Do not close Outlook until this job is complete.", IProgressMonitor.UNKNOWN);
        recursiveSaveFolder(OutlookMsgTransfer.TEMP_DIRECTORY, destinationFolder.getPath(), selectedFolder, monitor);
        return Status.OK_STATUS;

      } else {
        return saveEmails(monitor);
      }

    } catch (Throwable e) {
      StatusUtil.handleStatus("Error uploading email.", e, StatusManager.SHOW);

    } finally {
      if (errorOccurred) {
        StatusUtil.handleStatus("Some email files failed to upload successfully.  Check your CatLog.txt file for more details.", StatusManager.SHOW);
      }
    }
    return Status.OK_STATUS;
  }

  public IStatus recursiveSaveFolder(String tempFolderPath, CmsPath destinationFolderPath, MAPIFolder outlookFolder, IProgressMonitor monitor) throws Exception {

    if (monitor.isCanceled()) {
      return Status.CANCEL_STATUS;
    }

    File emailFolder = null;
    try {
      // First create a temporary directory
      String folderName = outlookFolder.getName();
      String tempPath = tempFolderPath + "\\" + folderName;
      emailFolder = new File(tempPath);
      emailFolder.mkdir();

      IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
      CmsPath alfrescoPath = destinationFolderPath.append(folderName);
      mgr.createFolder(alfrescoPath);

      // Save the email in that folder
      _Items items = outlookFolder.getItems();
      int numEmail = items.getCount();
      List<String> fileNames = new ArrayList<String>();

      for (int i = 1; i <= numEmail; i++) {
        _MailItem email = (_MailItem) items.Item(new JComVariant(i));
        saveEmail(email, fileNames, tempPath, alfrescoPath);
        if (monitor.isCanceled()) {
          return Status.CANCEL_STATUS;
        }
      }

      // Now iterate through all possible sub-folders
      _Folders folders = outlookFolder.getFolders();
      int numFolders = folders.getCount();

      for (int i = 1; i <= numFolders; i++) {
        MAPIFolder curFolder = folders.Item(new JComVariant(i));
        IStatus status = recursiveSaveFolder(tempPath, alfrescoPath, curFolder, monitor); // save a sub-folder
        if (status.equals(Status.CANCEL_STATUS)) {
          return status;
        }
      }

    } finally {
      if (emailFolder != null) {
        // Now erase the temp folder
        emailFolder.delete();
      }
    }
    return Status.OK_STATUS;
  }

  private IStatus saveEmails(IProgressMonitor monitor) throws Exception {
    List<String> fileNames = new ArrayList<String>();
    int numberOfMessages = selection.getCount();
    monitor.beginTask("Uploading email... Do not close Outlook until this job is complete.", numberOfMessages);

    for (int mailIndex = 1; mailIndex <= numberOfMessages; mailIndex++) {
      JComObject item = selection.Item(new JComVariant(mailIndex));
      if (item == null)
        continue;

      if (!(item instanceof _MailItem)) {
        CatLogger.getLogger(getClass()).warn("Cannot handle object of type: " + item.getClass().getName());
        Display.getDefault().asyncExec(new Runnable() {
          public void run() {
            MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Unknown Type", "Cannot import that type of object.");
          }
        });
        continue;
      }
      _MailItem email = (_MailItem) item;
      saveEmail(email, fileNames, OutlookMsgTransfer.TEMP_DIRECTORY, destinationFolder.getPath());

      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }
      monitor.worked(1);
    }
    return Status.OK_STATUS;

  }

  private void saveEmail(_MailItem email, List<String> fileNames, String tempFolderPath, CmsPath destinationFolderPath) throws Exception {

    List<String> fileNamesCurrentBatch = new ArrayList<String>();

    // Get message subject
    String subjectStr = email.getSubject();

    // Remove any invalid characters from subject string
    subjectStr = fixSubjectString(subjectStr);

    // Create unique message path
    String msgName = tempFolderPath + subjectStr;
    String msgFile = msgName + ".msg";

    // Checking for duplicate email names to be uploaded in same batch
    boolean duplicate = true;
    int ticker = 0;
    if (fileNames.size() > 0) {
      while (duplicate) {
        // if duplicate exists add "(integer)" to string until unique name is present
        // i.e. Subject(1).msg, Subject(2).msg etc.
        if (fileNames.contains(msgFile.toLowerCase()) || fileNames.contains(msgName.toLowerCase())) {
          ticker++;
          msgName = tempFolderPath + subjectStr + "(" + ticker + ")";
          msgFile = msgName + ".msg";
        } else {
          duplicate = false;
        }
      }
    }

    // Save files to be uploaded to a temp directory (we can't upload them directly from Outlook)
    // Save email and attachments separately so they will be indexed
    saveEmailAndAttachmentsToTempFolder(email, fileNamesCurrentBatch, msgName, new File(msgFile));

    // Add the saved files to the filenames list so we can check for duplicates
    for (String fileNameCurrentBatch : fileNamesCurrentBatch) {
    fileNames.add(fileNameCurrentBatch.toLowerCase());
    }

    // Now upload the temp files
    // FIXME: check for overwrites - see if we can reuse the checking done in
    // the regular upload file job. For now we are overwriting by default.
    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
    for (String filePath : fileNamesCurrentBatch) {
      File file = new File(filePath);
      try {
        String fileName = file.getName();
        CmsPath path = destinationFolderPath.append(fileName);
        if (file.isDirectory()) {
          mgr.createFolder(path);
          // list files and save folder's children
          for (File child : file.listFiles()) {
            try {
              String childName = child.getName();
              CmsPath childPath = path.append(childName);
              mgr.createFile(childPath, child);
            } catch (Throwable e) {
              logger.debug("Failed to save attachment: " + child.getAbsolutePath(), e);
              errorOccurred = true;
            } finally {
              // clean up temp files
              child.delete();
            }
          }
        } else {
          mgr.createFile(path, file);
        }
      } catch (Exception e) {
        logger.debug("Failed to save file: " + file.getAbsolutePath(), e);
        ExceptionHandler.FatalException(e, "Error Saving Email", "Failed to save file: " + file.getAbsolutePath(), true);        

      } finally {
        file.delete();
      }
    }
  }

  /**
   * CAT file name cannot contain any invalid characters
   * 
   * @param subject
   * @return string
   */
  private String fixSubjectString(String subject) {
    if (subject.matches(IResource.invalidCharactersRegex)) {
      // the following defined by IResource.invalidCharactersRegex
      subject = subject.replace('"', ' ');
      subject = subject.replace('*', ' ');
      subject = subject.replace('\\', ' ');
      subject = subject.replace('/', ' '); // how about change it to "-"?
      subject = subject.replace(':', ' ');
      subject = subject.replace('|', ' ');
      subject = subject.replace('%', ' ');
      subject = subject.replace('&', ' ');
      subject = subject.replace('+', ' ');
      subject = subject.replace(';', ' ');
      // further testing found that ? is also problematic for Dispatch.call()
      subject = subject.replace('?', ' ');
      // Further testing found that > is problematic when saving the temp files to Windows
      subject = subject.replace('>', '-');
      // Further testing found that < is problematic when saving the temp files to Windows
      subject = subject.replace('<', '-');

      // Further testing found that trailing white space in a folder name causes saving an attachment
      // to the temp folder to fail
      subject = subject.trim();
    }

    // CAT limits the total file name length to 100. Considering ".msg", we limit
    // the message subject to 96
    if (subject.length() > 96) {
      subject = subject.substring(0, 96);
    }
    return subject;
  }

  private void saveEmailAndAttachmentsToTempFolder(_MailItem mailItem, List<String> fileNames, String msgName, File msgFile) {
    try {

      // Save the attachments, if present
      Attachments attachments = mailItem.getAttachments();
      int numAttachments = attachments.getCount();
      if (numAttachments > 0) {

        File attachmentsFolder = new File(msgName);
        attachmentsFolder.mkdir();

        // Save the attachments
        for (int atIndex = 1; atIndex <= numAttachments; atIndex++) {
          Attachment attachment = (Attachment) attachments.Item(new JComVariant(atIndex));
          try {
            String fileName = attachment.getFileName();
            String attachmentPath = attachmentsFolder.getAbsolutePath() + "\\" + fileName.toString();
            attachment.SaveAsFile(attachmentPath);

          } catch (Exception e) {
            logger.warn("Could not save attachment.", e);
          }
        }
        // If we had embedded attachments that could not be saved, delete the attachments folder
        if (attachmentsFolder.listFiles().length == 0) {
          attachmentsFolder.delete();
        } else {
          fileNames.add(attachmentsFolder.getAbsolutePath());
        }
        
        // Now save the email to the same folder as attachments
        mailItem.SaveAs(attachmentsFolder.getAbsolutePath() + File.separatorChar + msgFile.getName());
        
      } else {
        // Save the email to temp folder
        mailItem.SaveAs(msgFile.getAbsolutePath());
        fileNames.add(msgFile.getAbsolutePath());        
      }
      
    } catch (Throwable e) {
      logger.error(e);
      StatusUtil.handleStatus("Error saving message as text file: " + msgFile, e, StatusManager.SHOW);
    }

  }
}
