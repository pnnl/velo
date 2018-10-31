package gov.pnnl.velo.filesys.callback;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eldos.cbfs.CallbackFileSystem;
import eldos.cbfs.ECBFSError;
import eldos.cbfs.ICbFsDirectoryEnumerationInfo;
import eldos.cbfs.ICbFsEnumerateEvents;
import eldos.cbfs.ICbFsFileEvents;
import eldos.cbfs.ICbFsFileInfo;
import eldos.cbfs.ICbFsHandleInfo;
import eldos.cbfs.ICbFsNamedStreamsEnumerationInfo;
import eldos.cbfs.ICbFsStorageEvents;
import eldos.cbfs.ICbFsVolumeEvents;
import eldos.cbfs.boolRef;
import eldos.cbfs.byteArrayRef;
import eldos.cbfs.dateRef;
import eldos.cbfs.intRef;
import eldos.cbfs.longRef;
import eldos.cbfs.stringRef;
import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

public class Handler implements ICbFsStorageEvents, ICbFsVolumeEvents, ICbFsEnumerateEvents, ICbFsFileEvents {

  private FileSystemCachedUploaderThread cache;
	private HashMap<String, PipedInputStream> pathToInputStreams = new HashMap<String, PipedInputStream>();
	private HashMap<String, PipedOutputStream> pathToOutputStreams = new HashMap<String, PipedOutputStream>();
	private HashMap<String, Boolean> pathToOpenOutputStreams = new HashMap<String, Boolean>();
    private String monitor = new String("monitor");  //just something to lock for synchronized blocks when writing to maps
    

  private int sectorSize = 512;//copied from sample...TODO look up what this means and see if we should use a different value
  private Logger logger = CatLogger.getLogger(this.getClass());
  
  private String desktopIniFileContents = "[.ShellClassInfo]\n[ViewState]\nMode=\nVid=\nFolderType=Documents";
  private UUID desktopIniUuid = UUID.randomUUID();
  private OpenedOutputStreamCloserThread openedOutputStreamCloserThread;
  private Thread closerThread;

  public Handler(FileSystemCachedUploaderThread cache) {
    this.cache = cache;
    this.openedOutputStreamCloserThread = new OpenedOutputStreamCloserThread();
    this.closerThread = new Thread(openedOutputStreamCloserThread);
    closerThread.start();
  }

  @Override
  public void onGetFileNameByFileId(CallbackFileSystem callbackFileSystem, long l, String s) throws Exception {
  }

  @Override
  public void onMount(CallbackFileSystem callbackFileSystem) throws Exception {
    if (callbackFileSystem != null)
      sectorSize = callbackFileSystem.getSectorSize();
  }

  @Override
  public void onUnmount(CallbackFileSystem callbackFileSystem) throws Exception {
  }

  @Override
  public void onGetVolumeSize(CallbackFileSystem sender, longRef totalAllocationUnits, longRef availableAllocationUnits) throws Exception {
    // TotalAllocationUnits - the event handler must place the total number of the allocation units (clusters) on device to this parameter
    // AvailableAllocationUnits - the event handler must place the number of available (free) allocation units (clusters) on device to this parameter

    long TotalSpace = Long.MAX_VALUE;//1000000000 * sectorSize;

    totalAllocationUnits.setValue(TotalSpace / sectorSize);
    availableAllocationUnits.setValue(TotalSpace/ sectorSize);
    // TODO - what to return here? will anything break if I don't set anything?
    // yes, need to set a really large number or users will be blocked by the OS when trying to upload data because it'll think there isn't enough room
  }

  @Override
  public void onGetVolumeLabel(CallbackFileSystem callbackFileSystem, stringRef VolumeLabel) throws Exception {
    VolumeLabel.setValue("Velo Virtual Filesystem");
  }

  @Override
  public void onStorageEjected(CallbackFileSystem callbackFileSystem) throws Exception {
  }

  @Override
  public void onCloseFile(CallbackFileSystem callbackFileSystem, ICbFsFileInfo fileInfo, ICbFsHandleInfo handleInfo) throws Exception {
  }

  @Override
  public void onSetVolumeLabel(CallbackFileSystem callbackFileSystem, String VolumeLabel) throws Exception {
  }

  @Override
  public void onGetVolumeID(CallbackFileSystem callbackFileSystem, intRef VolumeID) throws Exception {
    VolumeID.setValue(0x12345678);
  }

  @Override
  public void onCreateFile(CallbackFileSystem sender, String fileName, long desiredAccess, long fileAttributes, long shareMode, ICbFsFileInfo fileInfo, ICbFsHandleInfo handleInfo) throws Exception {
    logger.debug("onCreateFile " + fileInfo.getFileName() );
    String pathStr = fileName.replaceAll("\\\\", "/");
	  CmsPath path = new CmsPath(pathStr);
	  
	  //first check if the file/folder already exists, if so just return
	  IResource resource = CmsServiceLocator.getResourceManager().getResource(path);
	  if(resource != null){
		  return;
	  }else if (fileAttributes == FileAttributes.DIRECTORY) {
        logger.debug("onCreateFile DIRECTORY " + fileInfo.getFileName());
        CmsServiceLocator.getResourceManager().createFolder(path);
      } else {
    	  CmsServiceLocator.getResourceManager().createFile(path, null);
      }	  
  }
  
//  public void onCreateFileOld(CallbackFileSystem sender, String fileName, long desiredAccess, long fileAttributes, long shareMode, ICbFsFileInfo fileInfo, ICbFsHandleInfo handleInfo) throws Exception {
//    String pathStr = fileName.replaceAll("\\\\", "/");
//
//      String existingUuid = cache.getUuidByPath(pathStr);
//      if(existingUuid != null){
//        return;
//      }
//      UUID uuid = UUID.randomUUID();
//      if (fileAttributes == FileAttributes.DIRECTORY) {
//        logger.debug("onCreateFile DIRECTORY " + fileInfo.getFileName());
//        CmsServiceLocator.getResourceManager().createFolder(new CmsPath(pathStr));
//        //not caching folder creates since they are only created one at a time right now anyways (no bulk create service yet).
////        cache.addFolder(uuid, pathStr);
//      } else {
//        logger.debug("onCreateFile FILE " + fileInfo.getFileName());
//        File file = File.createTempFile("velofs", ".tmp");
//        file.deleteOnExit();
//        File newFile = new File(file.getParentFile().getAbsolutePath() + File.separator + uuid.toString());
//        file.renameTo(newFile);
//        cache.addFile(newFile, uuid, pathStr);
//        try {
//          //when create is called, the next thing thats called is write - get a filechannel cached 
//          cache.cacheFileChannel(pathStr, newFile); //, true
//        } catch (FileNotFoundException e) {
//          e.printStackTrace();
//        }
//      }
//  }

  @Override
  public void onOpenFile(CallbackFileSystem sender, String fileName, long desiredAccess, long fileAttributes, long shareMode, ICbFsFileInfo fileInfo, ICbFsHandleInfo handleInfo) throws Exception {
    if (fileInfo.getUserContext() != null)
      return;

    if (fileName.equals("\\")) {
      fileInfo.setUserContext(CmsServiceLocator.getResourceManager().getRoot());
    } else {
      String pathStr = fileName.replaceAll("\\\\", "/");
      String name = pathStr.substring(pathStr.lastIndexOf("/") + 1);
      CmsPath path = new CmsPath(pathStr);
      // check exists?

      IResource resource = CmsServiceLocator.getResourceManager().getResource(path);
      if (resource != null) {
        fileInfo.setUserContext(resource);
//      } else if (cache.exists(pathStr)) {
//        String uuid = cache.getUuidByPath(pathStr);
//        fileInfo.setUserContext(uuid);
      } else if(name.equalsIgnoreCase("desktop.ini")){
        fileInfo.setUserContext(desktopIniUuid.toString());
      } else {
        logger.error("onOpenFile for called for path not in cache nor on server, path: " + fileName);
        throw new ECBFSError(2);
      }
    }

  }
  
  private long getFileAttributes(String name, boolean isFolder){
    //desktop.ini is a special file that tells windows what icon to use for the parent folder and what sorts of files
    //are in the folder (when FolderType=Documents, windows doesn't ask to read files' contents to make thumbnails).
    
    //doesn't seem to matter now what the file attributes are for desktop.ini, but leaving this logic in to keep things consistent with 
    //the attributes of a desktop.ini file the OS creates (hidden/system/read-only)
    if (!isFolder && name.equalsIgnoreCase("desktop.ini")) {
      return FileAttributes.READ_ONLY | FileAttributes.HIDDEN | FileAttributes.SYSTEM;
    }else if(isFolder){
      //setting all folder's attributes to inclue READ_ONLY as that has to be set in order for desktop.ini files to be used by the OS
      //but it doesn't seem to have any negative impact, so setting it on all folders, not just ones that contain a desktop.ini file
      return FileAttributes.DIRECTORY | FileAttributes.READ_ONLY;
    }
    //default to return a normal file's attributes, just OFFLINE in our case
    return FileAttributes.OFFLINE;
  }

  @Override
  public void onGetFileInfo(CallbackFileSystem sender, String fileName, boolRef fileExists, dateRef creationTime, dateRef lastAccessTime, dateRef lastWriteTime, longRef endOfFile, longRef allocationSize, longRef fileId, longRef fileAttributes, stringRef shortFileName, stringRef realFileName) throws Exception {
    try{
    String pathStr = fileName.replaceAll("\\\\", "/");

    String name = pathStr.substring(pathStr.lastIndexOf("/") + 1);

    CmsPath path = new CmsPath(pathStr);
      IResource resource = CmsServiceLocator.getResourceManager().getResource(path);
      if (resource != null) {
        fileExists.setValue(true);
        creationTime.setValue(resource.getPropertyAsDate(VeloConstants.PROP_CREATED).getTime());
        lastAccessTime.setValue(resource.getPropertyAsDate(VeloConstants.PROP_MODIFIED).getTime());
        lastWriteTime.setValue(resource.getPropertyAsDate(VeloConstants.PROP_MODIFIED).getTime());
        if (resource instanceof IFolder) {
          endOfFile.setValue(0);
          allocationSize.setValue(0);
        } else {
          endOfFile.setValue(Long.parseLong(resource.getPropertyAsString(VeloConstants.PROP_SIZE)));
          allocationSize.setValue(Long.parseLong(resource.getPropertyAsString(VeloConstants.PROP_SIZE)));
        }
        fileAttributes.setValue(getFileAttributes(name, resource instanceof IFolder));
        logger.debug("onGetFileInfo resource folder "+ (resource instanceof IFolder) +" "+ pathStr);
//      } else if (cache.exists(pathStr)) {
//        fileExists.setValue(true);
//        creationTime.setValue(new Date());
//        lastAccessTime.setValue(new Date());
//        lastWriteTime.setValue(new Date());
//        File file = cache.getFileByPath(pathStr);
//        if (file == null) {// null file means its a folder
//          endOfFile.setValue(0);
//          allocationSize.setValue(0);
//        } else {
//          endOfFile.setValue(file.length());
//          allocationSize.setValue(file.length());
//        }
//        fileAttributes.setValue(getFileAttributes(name, file == null));
//        logger.debug("onGetFileInfo cache folder "+ (file == null) +" "+ pathStr);

      } else if(name.equalsIgnoreCase("desktop.ini")){
        //if the os is asking for a desktop.ini file and one wasn't returned from the server, return 
        //the desktop.ini file distributed with this filesystem code that will tell the OS to NOT generate thumbnails for images 
        fileExists.setValue(true);
        creationTime.setValue(new Date());
        lastAccessTime.setValue(new Date());
        lastWriteTime.setValue(new Date());
        endOfFile.setValue(desktopIniFileContents.getBytes().length);
        allocationSize.setValue(desktopIniFileContents.getBytes().length);
        fileAttributes.setValue(getFileAttributes(name, false));
      }else {
        fileExists.setValue(false);
      }
    }catch(Exception e){
      e.printStackTrace();
      throw e;
    }
  }

  @Override
  public void onIsDirectoryEmpty(CallbackFileSystem callbackFileSystem, ICbFsFileInfo fileInfo, String fileName, boolRef isEmpty) throws Exception {
    isEmpty.setValue(true);
    String pathStr = fileName.replaceAll("\\\\", "/");
    CmsPath path = new CmsPath(pathStr);
    IResource resource = CmsServiceLocator.getResourceManager().getResource(path);
    if (resource != null && resource instanceof IFolder) {
      if (((IFolder) resource).getChildren().size() > 0) {
        isEmpty.setValue(false);
      }
//    } else {
//      File file = cache.getFileByPath(pathStr);
//      // if the path points to a file, leave isEmpty as true, otherwise see if we have the folder cached
//      if (file == null) {
//        String uuid = cache.getUuidByPath(pathStr);
//        if (uuid != null && cache.getChildrenUuids(pathStr) != null) {
//          isEmpty.setValue(cache.getChildrenUuids(pathStr).isEmpty());
//        }
//      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see eldos.cbfs.ICbFsEnumerateEvents#onEnumerateDirectory(eldos.cbfs.CallbackFileSystem, eldos.cbfs.ICbFsFileInfo, eldos.cbfs.ICbFsHandleInfo, eldos.cbfs.ICbFsDirectoryEnumerationInfo, java.lang.String, int, boolean, eldos.cbfs.boolRef, eldos.cbfs.stringRef, eldos.cbfs.stringRef, eldos.cbfs.dateRef, eldos.cbfs.dateRef, eldos.cbfs.dateRef, eldos.cbfs.longRef, eldos.cbfs.longRef, eldos.cbfs.longRef, eldos.cbfs.longRef)
   */
  @Override
  public void onEnumerateDirectory(CallbackFileSystem sender, ICbFsFileInfo directoryInfo, ICbFsHandleInfo 
      handleInfo, ICbFsDirectoryEnumerationInfo enumerationInfo, String mask, int index, boolean restart, 
      boolRef fileFound, stringRef fileName, stringRef shortFileName, dateRef creationTime, dateRef lastAccessTime,
      dateRef lastWriteTime, longRef endOfFile, longRef allocationSize, longRef fileId, longRef fileAttributes) throws Exception {
try{
    boolean resetEnumeration = false;
    EnumerationInfo cachedEnumParameters;
    IResource childResource = null;
    IResource enumeratedResource = null;

    String name = null;
    long size = 0;
    Date created = null;
    Date modified = null;
    boolean folder = true;
    int nonCacheChildrenCount = 0;

    String enumeratedPath = null;
    String cachedPath = null;
    File cachedFile = null;

    boolean exactMatch = !mask.equals("*");

    if ((restart || enumerationInfo.getUserContext() == null) && !exactMatch){
      resetEnumeration = true;
    }

    if (restart && (enumerationInfo.getUserContext() != null)) {
      enumerationInfo.setUserContext(null);
    }

    if (enumerationInfo.getUserContext() == null) {
      cachedEnumParameters = new EnumerationInfo();

      enumerationInfo.setUserContext(cachedEnumParameters);
      enumeratedPath = directoryInfo.getFileName().replaceAll("\\\\", "/");
      enumeratedResource = CmsServiceLocator.getResourceManager().getResource(new CmsPath(enumeratedPath));
      
      if (enumeratedResource != null) {
        cachedEnumParameters.setResource(enumeratedResource);
      } else {
//        String uuid = cache.getUuidByPath(enumeratedPath);
//        if (uuid != null) {
//          cachedEnumParameters.setId(UUID.fromString(uuid));
//        } else {
          logger.error("enumerated path wasn't found in cache nor was it found on the server, path: " + enumeratedPath);
          fileFound.setValue(false);
          return;
//        }
      }
    } else {
      cachedEnumParameters = (EnumerationInfo) enumerationInfo.getUserContext();
      enumeratedResource = cachedEnumParameters.getResource();
//      if (enumeratedResource == null) {
//        // this is the case when the UI is enumerating a folder that only exists in the cache, 
//        //we will have set the uuid of the cached folder thats getting enumerated during the previous eumerat callback
//        enumeratedPath = cache.getPath(cachedEnumParameters.getId());
//      } else {
        enumeratedPath = enumeratedResource.getPath().toDisplayString();
//      }
    }

    if (resetEnumeration)
      cachedEnumParameters.setIndex(0);

    if (exactMatch && enumeratedResource != null) {
      CmsPath childPath = enumeratedResource.getPath().append(mask);
      //logger.debug("onEnumerateDirectory " + directoryInfo.getFileName() + " mask " + mask);
      childResource = CmsServiceLocator.getResourceManager().getResource(childPath);
//      if (childResource == null) {// try cache
//        cachedPath = childPath.toDisplayString();
//        String cachedUuid = cache.getUuidByPath(cachedPath);
//        // will be null if its not in the cache (doesn't exist)
//        if (cachedUuid != null) {
//          cachedFile = cache.getFileByUuid(UUID.fromString(cachedUuid));
//        } else {
//          cachedPath = null;// reset to null to indicate later that nothing was found
//        }
//      }
    }

    if (!exactMatch && (enumeratedResource != null || enumeratedPath != null)) {
      //logger.debug("onEnumerateDirectory " + directoryInfo.getFileName() + " index " + cachedEnumParameters.getIndex());
      if (enumeratedResource != null && enumeratedResource instanceof IFolder) {
        List<IResource> children = ((IFolder) enumeratedResource).getChildren();
        if (cachedEnumParameters.getIndex() < children.size()) {
          childResource = children.get(cachedEnumParameters.getIndex());
        } else {
          // else, the index is greater than whats on the server, get the server's children count to
          // use to get the cached children at the specified index (index - serverChildrenCount = cachedChildrenIndex)
          nonCacheChildrenCount = children.size();
        }
      } else if (enumeratedResource != null) {
        logger.error("Something other than an IFolder was attempted to be enumerated, path " + enumeratedResource.getPath().toDisplayString());
      }

      // wasn't found on the server, check the temporary cache
//      if (childResource == null && enumeratedPath != null) {
//        List<String> children = cache.getChildrenUuids(enumeratedPath);
//        if (children != null && cachedEnumParameters.getIndex() < (children.size() + nonCacheChildrenCount)) {
//          String cachedUuid = children.get(cachedEnumParameters.getIndex() - nonCacheChildrenCount);
//          cachedPath = cache.getPath(UUID.fromString(cachedUuid));
//          cachedFile = cache.getFileByUuid(UUID.fromString(cachedUuid));
//        }
//      }
    }

    // if we found a match in the cache, use it to fill out whats returned
    if (cachedPath != null) {
      name = cachedPath.substring(cachedPath.lastIndexOf("/") + 1);
      created = new Date();
      modified = new Date();
      if (cachedFile != null) {
        folder = false;
        size = (long) cachedFile.length();
      }
      logger.debug("onEnumerate cache folder "+ folder +" "+ cachedPath);
    } else if (childResource != null) {// otherwise we found a match on the server
      //trying to catch case where a blank file shows up in explorer after uploads
      if(childResource.getName() == null || childResource.getPropertyAsDate(VeloConstants.PROP_CREATED) == null ||
          childResource.getPropertyAsDate(VeloConstants.PROP_MODIFIED) == null ||
          (!(childResource instanceof IFolder) && childResource.getPropertyAsString(VeloConstants.PROP_SIZE) == null)){
        logger.error("wtf");
      }
      name = childResource.getName();
      created = childResource.getPropertyAsDate(VeloConstants.PROP_CREATED).getTime();
      modified = childResource.getPropertyAsDate(VeloConstants.PROP_MODIFIED).getTime();
      if (!(childResource instanceof IFolder)) {
        folder = false;
        size = Long.parseLong(childResource.getPropertyAsString(VeloConstants.PROP_SIZE));
      }

      logger.debug("onEnumerate resource folder "+ folder +" "+ childResource.getPath().toDisplayString());
    } else if(mask != null && mask.equalsIgnoreCase("desktop.ini")){
      //if the os is asking for a desktop.ini file and one wasn't returned from the server, return 
      //the desktop.ini file distributed with this filesystem code that will tell the OS to NOT generate thumbnails for images 
      name = "desktop.ini";
      created = new Date();
      modified = new Date();
      folder = false;
      size = (long) desktopIniFileContents.getBytes().length;
    }

    fileFound.setValue(name != null);

    if (fileFound.getValue()) {
      fileName.setValue(name);
      creationTime.setValue(created);
      if (folder) {
        endOfFile.setValue(0);
        allocationSize.setValue(0);
        lastWriteTime.setValue(new Date());
        lastAccessTime.setValue(new Date());
      } else {
        lastAccessTime.setValue(modified);
        lastWriteTime.setValue(modified);
        endOfFile.setValue(size);
        allocationSize.setValue(size);
      }
      fileAttributes.setValue(getFileAttributes(name, folder));
    }
    cachedEnumParameters.setIndex(cachedEnumParameters.getIndex() + 1);
}catch(Exception e){
  e.printStackTrace();
  throw e;
}
  }


  @Override
  public void onSetFileAttributes(CallbackFileSystem sender, ICbFsFileInfo fileInfo, ICbFsHandleInfo handleInfo, Date CreationTime, Date LastAccessTime, Date LastWriteTime, long Attributes) throws Exception {
    // don't do anything as the server will set all attributes like create time and last mod time
  }

  @Override
  public void onCanFileBeDeleted(CallbackFileSystem callbackFileSystem, ICbFsFileInfo FileInfo, ICbFsHandleInfo handleInfo, boolRef CanBeDeleted) throws Exception {
    CanBeDeleted.setValue(true);
  }

  @Override
  public void onDeleteFile(CallbackFileSystem callbackFileSystem, ICbFsFileInfo FileInfo) throws Exception {
    logger.debug("onDeleteFile " + FileInfo.getFileName());
    String pathStr = FileInfo.getFileName().replaceAll("\\\\", "/");
//    if (cache.getUuidByPath(pathStr) != null) {
//      cache.delete(pathStr);
//    } else {
      CmsServiceLocator.getResourceManager().deleteResource(new CmsPath(pathStr));
//    }
  }

  @Override
  public void onRenameOrMove(CallbackFileSystem callbackFileSystem, ICbFsFileInfo fileInfo, String newFileName) throws Exception {
    String strSource = fileInfo.getFileName().replaceAll("\\\\", "/");
    String strDestination = newFileName.replaceAll("\\\\", "/");
    logger.debug("onRenameOrMove  source: " + strSource + "  dest: " + strDestination);
    
    CmsPath source = new CmsPath(strSource);
    CmsPath destination = new CmsPath(strDestination);
    IResource sourceResource = CmsServiceLocator.getResourceManager().getResource(source);
    IResource destResource = CmsServiceLocator.getResourceManager().getResource(destination.getParent());
    
    if (sourceResource != null && destResource != null) {
      //call a different method if this is just a rename, otherwise we lose the transformed content
//      if(source.removeLastSegments(1).equals(destination.removeLastSegments(1))){
//        logger.debug("renaming " + source + " to " + destination.getName());
//        CmsServiceLocator.getResourceManager().setProperty(source, VeloConstants.PROP_NAME, destination.getName());
//      } else {
//        logger.debug("moving");
        CmsServiceLocator.getResourceManager().move(source, destination);
//      }
    } else {// don't allow anything else - if I did it would also complicate deletes
      // not implementing the complex logic to move something accross cache and server, instead just throw an error
      // this should rarely happen, the cache shouldn't be too far behind the actual server
      // 16 ERROR_CURRENT_DIRECTORY = "he directory cannot be removed."
      throw new ECBFSError(16);
    }
  }

  @Override
  public void onReadFile(CallbackFileSystem callbackFileSystem, ICbFsFileInfo fileInfo, long position, byteArrayRef buffer, int bytesToRead, intRef bytesRead) throws Exception {
    //this can generate a lot of logging when a large file is getting read
    logger.debug("onReadFile " + fileInfo.getFileName() + " position " + position + " bytesToRead " + bytesToRead );
        try{
    String pathStr = fileInfo.getFileName().replaceAll("\\\\", "/");

    int bytesSuccessfullyRead = 0;
//    if(!cache.exists(pathStr) && !CmsServiceLocator.getResourceManager().resourceExists(new CmsPath(pathStr)) && pathStr.substring(pathStr.lastIndexOf("/") + 1).equalsIgnoreCase("desktop.ini")){
   	if(!CmsServiceLocator.getResourceManager().resourceExists(new CmsPath(pathStr)) && pathStr.substring(pathStr.lastIndexOf("/") + 1).equalsIgnoreCase("desktop.ini")){
      byte[] buf = new byte[bytesToRead];
      System.arraycopy(this.desktopIniFileContents.getBytes(), (int)position, buf, 0, bytesToRead);
      buffer.setValue(buf);
      bytesSuccessfullyRead = bytesToRead;
      bytesRead.setValue(bytesToRead);
    }else{

    Date start = new Date();
    
    FileChannel fc = getCmsFileChannel(pathStr);
    
    //if a file channel couldn't be found, see if the requested file is desktop.ini - if so then return our hard coded contents
    if(fc != null){
      logger.debug("onReadFile " + fileInfo.getFileName() + " fc.size() " + fc.size());
      ByteBuffer byteBuffer = ByteBuffer.allocate(bytesToRead);
      bytesSuccessfullyRead = fc.read(byteBuffer, position);
      //hack, sort of...  without this fix, word and explorer would freeze and quit responding when trying to save an opened word file.  Word is 
      //creating a temp file, then immediately trying to read from it even though nothing has been written to the file.
      //file channel's read method states that it'll return "-1 if the given position is greater than or equal to the file's current size"
      //but setting -1 for the value of bytes read was causing the above problem, so for this case return instead the number of bytes requested to be read
      if(bytesSuccessfullyRead == -1){
        bytesSuccessfullyRead = bytesToRead;
      }
      buffer.setValue(byteBuffer.array());
      bytesRead.setValue(bytesSuccessfullyRead);
    } else {
        //otherwise there's a big problem, throw an error
        // see http://msdn.microsoft.com/en-us/library/windows/desktop/ms681382(v=vs.85).aspx for the error code number to use
        // as the ECBFSError constructor param
        // 29 = ERROR_WRITE_FAULT
        throw new ECBFSError(29);
    }
    }

        }catch(Throwable error){
          error.printStackTrace();
        }
  }

  private long totalTime = 0;
  
  
  private FileChannel getCmsFileChannel(String pathStr) throws Exception{
    //when a doc is opened, MS word will read the file, create a temp file and then immediately write to it.
    //when that same doc is changed and then saved, the following happens in this order one immedately after the other:
    // 1. word creates another temp file and then immediately writes to it
    // 2. word reads from it but at this point since writes are in another thread the second temp file has no contents to read yet (from the CMS)
    // 3. word renames the original doc to the a third NEW temp file created
    // 4. yea, so word is being stupid and creating all sorts of temp files and moving contents around so I'm giving up and putting in the wait loop below...
    
    //we can now assume all files that are being opened will exist already on the server - BUT can NOT assume their contents are done being written to
    int count = 0;
    //try waiting for the upload thread to close the stream before allowing the file to be read from
    while(pathToOpenOutputStreams.containsKey(pathStr)){
      Thread.sleep(100);
      count++;
    }
    
    if(count != 0){
      System.out.println("getCmsFileChannel had to wait for file's contents to be uploaded " + count + " times" );
    }
    
    FileChannel channel = cache.getFileChannel(pathStr);
    if (channel == null) {
      CmsPath path = new CmsPath(pathStr);
      File file = CmsServiceLocator.getResourceManager().getContentPropertyAsFile(path, VeloConstants.PROP_CONTENT);
      if (file != null) {
        //cache the opened file channel since read is called many times to get a few bytes at a time
        cache.addExistingFileByPath(pathStr, file);
        channel = cache.cacheFileChannel(pathStr, file);
      }else{
        return null;
      }
    }
    return channel;
  }

  
  
//  private FileChannel getFileChannel(String pathStr) throws Exception{
//    FileChannel channel = cache.getFileChannel(pathStr);
//    if (channel == null) {
//      try {
//        // its either a file just created and so is already in the cache
//        if (cache.getFileByPath(pathStr) != null) {
//          channel = cache.cacheFileChannel(pathStr, cache.getFileByPath(pathStr));
//        } else if(cache.getExistingFileByPath(pathStr) != null){
//          channel = cache.cacheFileChannel(pathStr, cache.getExistingFileByPath(pathStr));
//        } else {
//          // or its a file from the server
//          CmsPath path = new CmsPath(pathStr);
//          File file = CmsServiceLocator.getResourceManager().getContentPropertyAsFile(path, VeloConstants.PROP_CONTENT);
//          if (file != null) {
////            File tempFile = File.createTempFile(file.getName(), "");
////            FileUtils.copyFile(file, tempFile);
//            cache.addExistingFileByPath(pathStr, file);
//            channel = cache.cacheFileChannel(pathStr, file);
//          }else{
//            // see http://msdn.microsoft.com/en-us/library/windows/desktop/ms681382(v=vs.85).aspx for the error code number to use
//            // as the ECBFSError constructor param
//            // 29 = ERROR_WRITE_FAULT
////            throw new ECBFSError(29);
//            return null;
//          }
//        }
//      } catch (FileNotFoundException e) {
//        e.printStackTrace();
//        throw new ECBFSError(29);
//      }
//    }
//    return channel;
//  }
//  
  
  
  @Override
  public void onWriteFile(CallbackFileSystem callbackFileSystem, ICbFsFileInfo fileInfo, final long position, final byteArrayRef buffer, final int bytesToWrite, intRef bytesWritten) throws Exception {
      logger.debug("1. onWriteFile " + fileInfo.getFileName() +" position: " + position + " buffer size: " + buffer.getValue().length + " number of bytes to write: " + bytesToWrite);
    //this logging can generate a lot of output when a large file is being written out
//    logger.debug("onWriteFile " + fileInfo.getFileName());
//    if (position != 0 || bytesToWrite != buffer.getValue().length) {
//    }

    	//TODO - add a map of path to last write position & length to ensure the file is being written to in order.  If not throw an exception so at least that way
    	// the user will get an error message and can 'save-as' outside of velo and not lose the file or have corrupt contents.
    	
    final String pathStr = fileInfo.getFileName().replaceAll("\\\\", "/");

    // regardless of if this file is only in the cache or also on the server, the output stream will likely be cached since
    // onWriteFile is called in increments of 65536 bytes at a time and we cache it on the first call

    Date start = new Date();

//    FileChannel ch = getFileChannel(pathStr);
//    int bytesSuccessfullyWritten = ch.write(ByteBuffer.wrap(buffer.getValue(), 0, bytesToWrite), position);
//    bytesWritten.setValue(bytesSuccessfullyWritten);
    
    //create an in-line thread here and start it, the ResourceService will terminate the thread once I close the inputstream from my thread that closes steams that haven't been written to in x seconds
    final PipedOutputStream stream = getOrCreateOutputStream(pathStr, position);
    try{
      logger.debug("5. "+ pathStr + " writing to stream position: " + position + " buffer size: " + buffer.getValue().length + " number of bytes to write: " + bytesToWrite);
      stream.write(buffer.getValue());
    }catch(Throwable error){
      error.printStackTrace();
    }
    bytesWritten.setValue(bytesToWrite);
    
//    Date end = new Date();
//    long time = end.getTime() - start.getTime();
//    totalTime += time;
//    logger.debug(" time to write bytes: " + time + " bytesSuccessfullyWritten: " +bytesSuccessfullyWritten);
  }

  
  @Override
  public void onCleanupFile(CallbackFileSystem callbackFileSystem, ICbFsFileInfo fileInfo, ICbFsHandleInfo handleInfo) throws Exception {
    // This event is fired when the OS needs to close the previously created or opened handle to the file.
    // This event is different from OnCloseFile in that OnCleanupFile happens immediately when the last handle is
    // closed by the application, while OnCloseFile can be called much later when the OS itself decides that the file can be
    // closed. Use FileInfo and HandleInfo to identify the file that needs to be closed.
//    logger.debug(" onCleanupFile" + fileInfo.getFileName());

  }

  @Override
  public void onFlushFile(CallbackFileSystem callbackFileSystem, ICbFsFileInfo fileInfo) throws Exception {
    //logger.debug("onFlushFile " + fileInfo.getFileName());
    //This event is fired when the OS needs to flush the data of the open file or volume
  }

  @Override
  public void onSetAllocationSize(CallbackFileSystem sender, ICbFsFileInfo fileInfo, long attrs) throws Exception {
  }

  @Override
  public void onSetEndOfFile(CallbackFileSystem arg0, ICbFsFileInfo fileInfo, long endOfFile) throws Exception {
    logger.debug("onSetEndOfFile " + fileInfo.getFileName() + " endOfFile: " + endOfFile);
    // this is how we know the target size of a file about to be written to
//    cache.setFileLength(fileInfo.getFileName().replaceAll("\\\\", "/"), endOfFile);

//    String pathStr = fileInfo.getFileName().replaceAll("\\\\", "/");
//    FileChannel fc = getFileChannel(pathStr);
//    try {
//      //this will grow/shrink the file's bytes, when 'growing' the content (bytes) defaults to zeros
//      if(fc.size() < endOfFile){
//        //1. create a byte[] that is the size of the amount of padding we need to add:
//        ByteBuffer byteBuffer = ByteBuffer.allocate((int) (endOfFile - fc.size()));
//        int bytesWritten =fc.write(byteBuffer, fc.size());
//        logger.debug("onSetEndOfFile " + fileInfo.getFileName() + " endOfFile: " + endOfFile + " bytesWritten " + bytesWritten + " filesize " + fc.size());
//      }else if(fc.size() > endOfFile){
//        fc = fc.truncate(endOfFile);
//
//        logger.debug("onSetEndOfFile " + fileInfo.getFileName() + " endOfFile: " + endOfFile + " truncated to " + fc.size());
//      }//otherwise, the size didn't really change, don't do anything
//    } catch (IOException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//      logger.error(e);
//    }
  }

  @Override
  public void onCloseDirectoryEnumeration(CallbackFileSystem sender, ICbFsFileInfo fileInfo, ICbFsDirectoryEnumerationInfo dirInfo) throws Exception {
  }

  @Override
  public void onCloseNamedStreamsEnumeration(CallbackFileSystem arg0, ICbFsFileInfo fileInfo, ICbFsNamedStreamsEnumerationInfo namedStream) throws Exception {
    logger.debug("onCloseNamedStreamsEnumeration " + fileInfo.getFileName() + " ICbFsNamedStreamsEnumerationInfo: " + namedStream.getUserContext());
  }

  
  
  class OpenedOutputStreamCloserThread implements Runnable {

    private boolean running = true;

    public void terminate() {
      running = false;
    }

    @Override
    public void run() {
      while (running) {
        try {
          Thread.sleep(500);// check every 1/2 second if a stream is still being read/written to, if so keep it open, if not close it
        } catch (InterruptedException e) {
        }

        if(pathToOpenOutputStreams.isEmpty()){
          //logger.debug("pathToOpenOutputStreams.isEmpty()");
          continue;
        }
        
        List<String> channelsToClose = new ArrayList<String>();

        for (String path : pathToOpenOutputStreams.keySet()) {
          if (!togglePathToOpenOutputStream(path)) {
            logger.debug("****adding path to list to close: " + path);
            channelsToClose.add(path);
          }
        }

        for (String path : channelsToClose) {
          logger.debug("closing stream: " + path);
          closeOutputStream(path);
        }

      }
    }

  }
 
  private boolean togglePathToOpenOutputStream(String path) {
	    synchronized (monitor) {
	      if (pathToOpenOutputStreams.get(path)) {
	        pathToOpenOutputStreams.put(path, Boolean.FALSE);
	        // channel was in use, setting flag to false
	        return true;
	      } else {
	        // channel was not in use, ready to close
	        return false;
	      }
	    }
	  }
  
  public void closeOutputStream(String path) {
    synchronized (monitor) {
      pathToOpenOutputStreams.remove(path);
      PipedOutputStream fc = pathToOutputStreams.remove(path);
      try {
        fc.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
  private PipedOutputStream getOrCreateOutputStream(String pathStr, long offset) throws Exception {
    logger.debug("2. "+ pathStr + " getOrCreateOutputStream");
    PipedOutputStream inputStream = getOutputStream(pathStr);
    if (inputStream == null) {
         // can assume now the file will always already be on the server
         inputStream = createOutputStream(pathStr, offset);
    }
    return inputStream;
  }
  
  public PipedOutputStream createOutputStream(final String path, final long offset) throws IOException {
    synchronized (monitor) {
      logger.debug("3. "+ path + " createOutputStream");
      PipedOutputStream outputStream = new PipedOutputStream();
      final PipedInputStream inputStream = new PipedInputStream(outputStream);
      pathToOutputStreams.put(path, outputStream);
      pathToInputStreams.put(path, inputStream);
      pathToOpenOutputStreams.put(path, Boolean.TRUE);
      Thread uploadThread = new Thread(new Runnable() {
        public void run() {
          logger.debug("4. "+ path + " updateContent");
          //only pass in the offset if when we're writing for the first time that the position is not zeor:
          String offsetStr = null;
          if(offset != 0){
            offsetStr = String.valueOf(offset);
            logger.debug("writing at offset: " + offset);
          }
          CmsServiceLocator.getResourceManager().updateContent(new CmsPath(path), inputStream, null, null, offsetStr);
        }
      });
      uploadThread.start();
      return outputStream;
    }
  }
  
  public PipedOutputStream getOutputStream(String path) {
    synchronized (monitor) {
      PipedOutputStream fc = pathToOutputStreams.get(path);
      if (fc != null) {
        logger.debug("2.5. pathToOpenOutputStreams.put " + path);
        pathToOpenOutputStreams.put(path, Boolean.TRUE);
      }
      return fc;
    }
  }
}
