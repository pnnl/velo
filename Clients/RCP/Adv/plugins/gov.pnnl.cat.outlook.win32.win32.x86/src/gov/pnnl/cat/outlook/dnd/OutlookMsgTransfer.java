package gov.pnnl.cat.outlook.dnd;
import gov.pnl.ezjcom.outlook.Application;
import gov.pnl.ezjcom.outlook.MAPIFolder;
import gov.pnl.ezjcom.outlook.Selection;
import gov.pnl.ezjcom.outlook._Application;
import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.outlook.jobs.UploadOutlookEmailJob;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.internal.ole.win32.COM;
import org.eclipse.swt.internal.ole.win32.FORMATETC;
import org.eclipse.swt.internal.win32.TCHAR;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * The class <code>OutlookMsgTransfer</code> provides a platform specific mechanism 
 * for converting an Outlook items (messages or folders) to files that can be uploaded
 * to the CAT server. 
 *
 * Author: Kevin Lai
 * @see Transfer
 */
@SuppressWarnings("restriction")
public class OutlookMsgTransfer extends ByteArrayTransfer {

  private static OutlookMsgTransfer _instance = new OutlookMsgTransfer();
  private static final String CF_UNICODETEXT = "CF_UNICODETEXT"; //$NON-NLS-1$
  private static final String CF_TEXT = "CF_TEXT"; //$NON-NLS-1$
  private static boolean isFolder = false; 

  // TODO: every outlook file upload should be in a unique subdir of the temp directory
  public static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
  private static Logger logger = CatLogger.getLogger(OutlookMsgTransfer.class);

  /**
   * Returns the singleton instance of the OutlookMsgTransfer class.
   *
   * @return the singleton instance of the OutlookMsgTransfer class
   */
  public static OutlookMsgTransfer getInstance () {
    return _instance;
  }

  /**
   * This implementation of <code>javaToNative</code> converts plain text
   * represented by a java <code>String</code> to a platform specific representation.
   * 
   * @param object a java <code>String</code> containing text
   * @param transferData an empty <code>TransferData</code> object; this object
   *  will be filled in on return with the platform specific format of the data
   *  
   * (non-Javadoc)
   * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
   */
//  @Override
//  public void javaToNative (Object object, TransferData transferData){
    // We aren't using this method at this time, so I commented it out

    //    if (!checkText(object) || !isSupportedType(transferData)) {
    //      DND.error(DND.ERROR_INVALID_DATA);
    //    }
    //    transferData.result = COM.E_FAIL;
    //    String string = (String)object;
    //    switch (transferData.type) {
    //      case COM.CF_UNICODETEXT: {
    //        int charCount = string.length ();
    //        char[] chars = new char[charCount+1];
    //        string.getChars (0, charCount, chars, 0);
    //        int byteCount = chars.length * 2;
    //        int newPtr = OS.GlobalAlloc(COM.GMEM_FIXED | COM.GMEM_ZEROINIT, byteCount);
    //        OS.MoveMemory(newPtr, chars, byteCount);
    //        transferData.stgmedium = new STGMEDIUM();
    //        transferData.stgmedium.tymed = COM.TYMED_HGLOBAL;
    //        transferData.stgmedium.unionField = newPtr;
    //        transferData.stgmedium.pUnkForRelease = 0;
    //        transferData.result = COM.S_OK;
    //        break;
    //      }
    //      case COM.CF_TEXT: {
    //        int count = string.length();
    //        char[] chars = new char[count + 1];
    //        string.getChars(0, count, chars, 0);
    //        int codePage = OS.GetACP();
    //        int cchMultiByte = OS.WideCharToMultiByte(codePage, 0, chars, -1, null, 0, null, null);
    //        if (cchMultiByte == 0) {
    //          transferData.stgmedium = new STGMEDIUM();
    //          transferData.result = COM.DV_E_STGMEDIUM;
    //          return;
    //        }
    //        int lpMultiByteStr = OS.GlobalAlloc(COM.GMEM_FIXED | COM.GMEM_ZEROINIT, cchMultiByte);
    //        OS.WideCharToMultiByte(codePage, 0, chars, -1, lpMultiByteStr, cchMultiByte, null, null);
    //        transferData.stgmedium = new STGMEDIUM();
    //        transferData.stgmedium.tymed = COM.TYMED_HGLOBAL;
    //        transferData.stgmedium.unionField = lpMultiByteStr;
    //        transferData.stgmedium.pUnkForRelease = 0;
    //        transferData.result = COM.S_OK;
    //        break;
    //      }
    //    }
//    return;
//  }

  /**
   * Converts an Outlook Transfer (representing one or more email to be copied) to 
   * an array of Strings, where each String represents a path to a temporary file containing
   * the email file to be uploaded to CAT.
   * 
   * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(org.eclipse.swt.dnd.TransferData)
   */
  @Override
  public Object nativeToJava(TransferData transferData) {
    UploadOutlookEmailJob job = null;
    
    try {
      //Look up the Outlook Application COM object currently in use
      Application outlookComObject = new Application(true, false);
      _Application outlook = outlookComObject.get_Application();

      // Get the currently selected files from the Outlook Application
      Selection selection = outlook.ActiveExplorer().getSelection();
      
      // Get the currently selected folder
      MAPIFolder selectedFolder = null;
      if(isFolder) {
        selectedFolder = outlook.ActiveExplorer().getCurrentFolder();
      }

      job = new UploadOutlookEmailJob(selection, selectedFolder);
      
    } catch (Exception e) {
      StatusUtil.handleStatus("Error transferring email.", e, StatusManager.SHOW);
    }

    return job;
  }

  /**
   * First found: 49392-4 folder, 49392-12 message
   * then on 09/20/2007 found: 49394-4 folder, 49394-12 message
   * then on 09/21/2007 found: 49397-4 folder, 49397-12 message
   * then on 09/26/2007 found: 49395-4 folder, 49395-12 message
   * then on 10/12/2007 on Dave's found: 49370-4 folder, 49370-12 message
   * On 10/12/2007, delete 49392-4 because it is the same as in FileTransfer for Dave.
   * then on 10/12/2007 on Curt's found: 47406-4 or 12
   * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
   */
  protected int[] getTypeIds(){
    return new int[] {49370, 49394,49395, 49397};
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.ByteArrayTransfer#isSupportedType(org.eclipse.swt.dnd.TransferData)
   */
  public boolean isSupportedType(TransferData transferData){
    if (transferData == null) return false;
    boolean isSupported = false;

    FORMATETC format = transferData.formatetc;
    int id = format.cfFormat;
    logger.debug("OutlookMsgTransfer TransferData: " + format.cfFormat + ":" + format.tymed);

    int maxSize = 128;
    TCHAR buffer = new TCHAR(0, maxSize);
    int size = COM.GetClipboardFormatName(id, buffer, maxSize);
    if (size != 0) {
      String name = buffer.toString(0, size);
      logger.debug("OutlookMsgTransfer TransferData name: " + name);
      name = name.trim();
      if(name.equals("FileContents"))
      {
        if((format.tymed & 8) == 8)
        {
          isSupported = true;
          isFolder = false;
        }
        else if((format.tymed & 4) == 4)
        {
          isSupported = true;
          isFolder = true;
        }
      }
    }
    return isSupported;
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
   */
  protected String[] getTypeNames(){
    return new String[] {CF_UNICODETEXT, CF_TEXT};
  }

  /**
   * @param object
   * @return
   */
  protected boolean checkText(Object object) {
    return (object != null  && object instanceof String && ((String)object).length() > 0);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.Transfer#validate(java.lang.Object)
   */
  protected boolean validate(Object object) {
    return checkText(object);
  }

}

