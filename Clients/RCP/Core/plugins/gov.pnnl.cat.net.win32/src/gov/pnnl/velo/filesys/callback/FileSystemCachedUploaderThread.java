package gov.pnnl.velo.filesys.callback;

import gov.pnnl.cat.logging.CatLogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class FileSystemCachedUploaderThread implements Runnable {

  private static final int NEW_FILE_POOLING_INTERVAL = 5 * 1000;//5 seconds

  private static final int UPLOAD_BATCH_SIZE = 50;

  // using linkedHashMaps because we need to preserve order, this way the parent folder will be
  // created on the server BEFORE a child file/folder is created
  private LinkedHashMap<String, File> newFilesCachedByPath = new LinkedHashMap<String, File>();

  private LinkedHashMap<String, File> updatedExistingFilesCachedByPath = new LinkedHashMap<String, File>();

  // this is a map of child uuid's given a parent's path
  private Map<String, List<String>> childrenUuidsCachedByPath = new HashMap<String, List<String>>();

  // this is a map of all child resources currently being cached (pending upload/folder create)
  private LinkedHashMap<String, String> uuidsToPaths = new LinkedHashMap<String, String>();

  // the path can be to a file in the cache or a file from the server - both cases need the FileChannel cached to speed up reads/writes
  private Map<String, FileChannel> pathToFileChannel = new HashMap<String, FileChannel>();

  // this is to keep track of which file channels are currently in use (being written to/read from)
  // so the channelcloser thread knows when its safe to close the opened channels
  private Map<String, Boolean> pathToUsingFileChannel = new HashMap<String, Boolean>();

//  private Thread closerThread;
//
//  private OpenedFileChannelCloserThread closer;

  private String monitor = new String("monitor");

  private boolean uploading = false;

  private LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<String>(5000);

  private boolean canceled = false;

  private Logger logger = CatLogger.getLogger(this.getClass());

  public static SimpleDateFormat messageDateFormat = new SimpleDateFormat("M/d/y KK:mm:ssa");

  // TODO keep track if any files are opened to alert when user attempts to unmount

  public FileSystemCachedUploaderThread() {
//    this.closer = new OpenedFileChannelCloserThread();
//    this.closerThread = new Thread(closer);
//    closerThread.start();
  }

  public boolean isUploading() {
    return uploading;
  }

  public boolean isUploadsPending() {
    return uuidsToPaths.keySet().size() > 0;
  }

  public File getFileByPath(String path) {
    File resource = null;
    synchronized (monitor) {
      resource = newFilesCachedByPath.get(path);
    }
    return resource;
  }
  
  public File getExistingFileByPath(String path) {
    File resource = null;
    synchronized (monitor) {
      resource = updatedExistingFilesCachedByPath.get(path);
    }
    return resource;
  }

  public void addExistingFileByPath(String path, File file) {
    synchronized (monitor) {
      updatedExistingFilesCachedByPath.put(path, file);
      logger.debug("added " + path + " to updatedExistingFilesCachedByPath as file " + file.getAbsolutePath());
    }
  }

  public String getUuidByPath(String path) {
    synchronized (monitor) {
      return getUuidByPathUnSynched(path);
    }
  }

  private String getUuidByPathUnSynched(String path) {
    for (String uuid : uuidsToPaths.keySet()) {
      if (uuidsToPaths.get(uuid).equalsIgnoreCase(path)) {
        return uuid;
      }
    }
    return null;
  }

  public boolean exists(String path) {
    return uuidsToPaths.containsValue(path);
  }

  public File getFile(UUID uuid) {
    String path = null;
    File resource = null;

    synchronized (monitor) {
      path = uuidsToPaths.get(uuid.toString());
      if (path != null) {
        resource = newFilesCachedByPath.get(path);
      }
    }
    return resource;
  }

  public String getPath(UUID uuid) {
    String path = null;
    synchronized (monitor) {
      path = uuidsToPaths.get(uuid.toString());
    }
    return path;
  }

//  public void addFile(File file, UUID uuid, String path) {
//    synchronized (monitor) {
//      newFilesCachedByPath.put(path, file);
//      addChildUnsynched(uuid, path);
//    }
//  }

  public FileChannel cacheFileChannel(String path, File file) throws IOException {
    synchronized (monitor) {
//      FileChannel fc = FileChannel.open(Paths.get(file.getAbsolutePath()), StandardOpenOption.READ, StandardOpenOption.WRITE);
      RandomAccessFile aFile = new RandomAccessFile(file.getAbsolutePath(), "rw");
      FileChannel fc = aFile.getChannel();
      pathToFileChannel.put(path, fc);
      return fc;
    }
  }
//  public FileChannel cacheFileChannel(String path, File file, boolean cachedFile) throws IOException {
//    synchronized (monitor) {
//      FileChannel fc = FileChannel.open(Paths.get(file.getAbsolutePath()), StandardOpenOption.READ, StandardOpenOption.WRITE);
//      pathToFileChannel.put(path, fc);
//      if (!cachedFile) {
//        updatedExistingFilesCachedByPath.put(path, file);
//      }
//      return fc;
//    }
//  }

  public FileChannel getFileChannel(String path) {
    synchronized (monitor) {
      FileChannel fc = pathToFileChannel.get(path);
      if (fc != null) {
        pathToUsingFileChannel.put(path, Boolean.TRUE);
      }
      return fc;
    }
  }

  public void closeFileChannel(String path) {
    synchronized (monitor) {
      pathToUsingFileChannel.remove(path);
      FileChannel fc = pathToFileChannel.remove(path);
      try {
        fc.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private boolean togglePathToFileChannel(String path) {
    synchronized (monitor) {
      if (pathToUsingFileChannel.get(path)) {
        pathToUsingFileChannel.put(path, Boolean.FALSE);
        // channel was in use, setting flag to false
        return true;
      } else {
        // channel was not in use, ready to close
        return false;
      }
    }
  }

//  public void addFolder(UUID uuid, String path) {
//    synchronized (monitor) {
//      childrenUuidsCachedByPath.put(path, new ArrayList<String>());
//      addChildUnsynched(uuid, path);
//    }
//  }

//  private void addChildUnsynched(UUID uuid, String pathOfChild) {
//    uuidsToPaths.put(uuid.toString(), pathOfChild);
//    String parentPath = pathOfChild.substring(0, pathOfChild.lastIndexOf("/"));
//
//    // see if parent is in cache, if so add this as a child:
//    List<String> children = null;
//    if (childrenUuidsCachedByPath.containsKey(parentPath)) {
//      children = childrenUuidsCachedByPath.get(parentPath);
//    } else {
//      children = new ArrayList<String>();
//    }
//
//    if (!children.contains(uuid.toString())) {
//      children.add(uuid.toString());
//      childrenUuidsCachedByPath.put(parentPath, children);
//    }
//  }

//  public List<String> getChildrenUuids(String parentPath) {
//    List<String> children = null;
//    synchronized (monitor) {
//      children = childrenUuidsCachedByPath.get(parentPath);
//    }
//    return children;
//  }
//
//  public String getName(String uuid) {
//    String name = null;
//    synchronized (monitor) {
//      String path = uuidsToPaths.get(uuid);
//      name = path.substring(path.lastIndexOf("/"));
//    }
//    return name;
//  }
//
//  public File getFileByUuid(UUID uuid) {
//    File file = null;
//    synchronized (monitor) {
//      String path = uuidsToPaths.get(uuid.toString());
//      file = newFilesCachedByPath.get(path);
//    }
//    return file;
//  }
//
//  public void move(String source, String destination) {
//    synchronized (monitor) {
//      // first off, close any fileChannels that may be open on file getting moved:
//      pathToUsingFileChannel.remove(source);
//      FileChannel fc = pathToFileChannel.remove(source);
//      if (fc != null) {
//        try {
//          fc.close();
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//      }
//
//      List<String> foldersToRemove = new ArrayList<String>();
//      Map<String, List<String>> modifiedChildrenUuidsCachedByPath = new HashMap<String, List<String>>();
//      List<String> filesToRemove = new ArrayList<String>();
//      Map<String, File> modifiedFilesCachedByPath = new HashMap<String, File>();
//
//      // always update the uuid from source to destination
//      String uuid = getUuidByPathUnSynched(source);
//      uuidsToPaths.put(uuid, destination);
//
//      // update the path of the file (this applies to just a file rename and a file move)
//      for (String filePath : newFilesCachedByPath.keySet()) {
//        if (filePath.startsWith(source)) {
//          String newFilePath = destination + filePath.substring(source.length());
//          File file = newFilesCachedByPath.get(filePath);
//          modifiedFilesCachedByPath.put(newFilePath, file);
//          filesToRemove.add(filePath);
//        }
//      }
//
//      // remove old paths to files
//      for (String filePath : filesToRemove) {
//        newFilesCachedByPath.remove(filePath);
//      }
//
//      // add back in new paths to files
//      for (String newPath : modifiedFilesCachedByPath.keySet()) {
//        newFilesCachedByPath.put(newPath, modifiedFilesCachedByPath.get(newPath));
//      }
//
//      String newParentPath = destination.substring(0, destination.lastIndexOf("/"));
//      String oldParentPath = source.substring(0, source.lastIndexOf("/"));
//
//      boolean newParentFound = false;
//      boolean renameOnly = newParentPath.equalsIgnoreCase(oldParentPath);
//      boolean fileRenameOnly = renameOnly && newFilesCachedByPath.containsKey(destination);
//      // handle the case where the parent was renamed
//      if (!fileRenameOnly) {// don't need to change the children list since it was a file rename, nothing a moved
//        for (String folderPath : childrenUuidsCachedByPath.keySet()) {
//          if (folderPath.equalsIgnoreCase(oldParentPath)) {
//            List<String> unchangedChildren = new ArrayList<String>();
//            List<String> children = childrenUuidsCachedByPath.get(folderPath);
//            // if this is a folder or file move, only include children of the parent that aren't getting moved
//            for (String childUuid : children) {
//              if (!childUuid.equalsIgnoreCase(uuid)) {
//                if (!unchangedChildren.contains(childUuid)) {
//                  unchangedChildren.add(childUuid);
//                }
//              }
//            }
//            // if this folder is the old parent, update its list of children with the ones remaining after the moved one was removed
//            modifiedChildrenUuidsCachedByPath.put(folderPath, unchangedChildren);
//          }
//          if (folderPath.startsWith(source)) {
//            String newFolderPath = destination + folderPath.substring(source.length());
//            List<String> children = childrenUuidsCachedByPath.get(folderPath);
//            for (String childUuid : children) {
//              String oldPath = uuidsToPaths.get(childUuid);
//              String name = oldPath.substring(oldPath.lastIndexOf("/"));
//              uuidsToPaths.put(childUuid, newFolderPath + name);
//            }
//            // if this folder is the folder that was renamed or moved, add it to the list of folders to remove from the map and
//            // add to the map its children but with the new path
//            if (folderPath.equalsIgnoreCase(source)) {
//              foldersToRemove.add(folderPath);
//              modifiedChildrenUuidsCachedByPath.put(destination, children);
//            } else {
//              modifiedChildrenUuidsCachedByPath.put(newFolderPath, children);
//            }
//            // if this folder is the new parent of the moved file/folder, add the moved file/folder to its list of children
//          } else if (folderPath.equalsIgnoreCase(newParentPath)) {
//            List<String> children = childrenUuidsCachedByPath.get(folderPath);
//            if (!children.contains(uuid)) {
//              children.add(uuid);
//            }
//            modifiedChildrenUuidsCachedByPath.put(folderPath, children);
//            newParentFound = true;
//          }
//        }
//        // this is the case where a file or folder is moved from a cached only folder to a folder on the server
//        if (!newParentFound) {
//          System.out.println("****new uuid, new parent " + newParentPath + " not found");
//          addChildUnsynched(UUID.randomUUID(), destination);
//        }
//
//        // remove old paths of parents
//        for (String path : foldersToRemove) {
//          childrenUuidsCachedByPath.remove(path);
//        }
//
//        // add back in new paths of parents
//        for (String path : modifiedChildrenUuidsCachedByPath.keySet()) {
//          childrenUuidsCachedByPath.put(path, modifiedChildrenUuidsCachedByPath.get(path));
//        }
//      }
//    }
//  }

//  public void delete(String pathStr) {
//    // NOTE: OS calls the delete callback recursively for us, so no need to have that logic here. Just delete the one
//    // resource that is passed in, no children.
//    synchronized (monitor) {
//      pathToUsingFileChannel.remove(pathStr);
//      FileChannel fc = pathToFileChannel.remove(pathStr);
//      if (fc != null) {
//        try {
//          fc.close();
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//      }
//      File file = newFilesCachedByPath.remove(pathStr);
//      String uuid = null;
//      if (file != null) {
//        uuid = file.getName();
//        file.delete();
//      }
//
//      if (uuid == null) {
//        for (String uuidStr : uuidsToPaths.keySet()) {
//          if (uuidsToPaths.get(uuidStr).equalsIgnoreCase(pathStr)) {
//            uuid = uuidStr;
//            break;
//          }
//        }
//      }
//
//      if (uuid != null) {
//        uuidsToPaths.remove(uuid);
//
//        String parentPath = pathStr.substring(0, pathStr.lastIndexOf("/"));
//        List<String> siblings = childrenUuidsCachedByPath.get(parentPath);
//        if (siblings != null) {
//          siblings.remove(uuid);
//        }
//        // by the time it calls delete on this resource, all of the children should have already been removed
//        List<String> children = childrenUuidsCachedByPath.get(pathStr);
//        if (children != null && children.size() > 0) {
//          // this should never happen
//          System.out.println(pathStr + " has " + children.size() + " children. failing delete to prevent orphaned nodes");
//          throw new RuntimeException(pathStr + " has " + children.size() + " children. failing delete to prevent orphaned nodes");
//        } else {
//          childrenUuidsCachedByPath.remove(pathStr);
//        }
//      } else {
//        throw new RuntimeException("Error deleting resource, uuid cannot be determined");
//      }
//    }
//  }

  public void logMessage(String message) {
    try {
      messages.put(messageDateFormat.format(new Date()) + " " + message);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public List<String> getMessages() {
    List<String> queuedMessages = new ArrayList<String>();
    while (messages.peek() != null) {
      try {
        queuedMessages.add((String) messages.take());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return queuedMessages;
  }


  public boolean isRunning() {
    return running;
  }

  public void setCanceled(boolean newVal) {
    this.canceled = newVal;
    if (this.canceled) {
      // close all open filechannels, kill all threads
//      try {
//        closer.terminate();
//        closerThread.join();
//        logMessage("File channel thread stopped.");
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
      Collection<FileChannel> channels = pathToFileChannel.values();
      for (FileChannel fileChannel : channels) {
        try {
          fileChannel.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  private boolean running = true;
  
  public void terminate() {
    running = false;
  }
  
  // this thread will close opened file channels if they haven't been used in the past 3 seconds
  @Override
  public void run() {
    while (running) {
      try {
        Thread.sleep(1000);// check every 1 seconds if a channel is still being read/written to, if so keep it open, if not close it
      } catch (InterruptedException e) {
      }
      
      List<String> channelsToClose = new ArrayList<String>();
      
      for (String path : pathToUsingFileChannel.keySet()) {
        if (!togglePathToFileChannel(path)) {
          // System.out.println("****adding path to list to close: " + path);
          channelsToClose.add(path);
        }
      }
      
      for (String path : channelsToClose) {
        logger.debug("closing channel: " + path);
        closeFileChannel(path);
      }
      
    }
  }
  
  
  
//  class OpenedFileChannelCloserThread implements Runnable {
//
//
//  }

  // simple progress monitor implementation so that we can log a message with each successful file upload from the bulk upload servlet
//  class UploadProgressMonitor extends NullProgressMonitor {
//
//    @Override
//    public void worked(int work) {
//      super.worked(work);
//    }
//
//    @Override
//    public void subTask(String name) {
//      super.subTask(name);
//      // logging each filename that was uploaded was too much
//      // logMessage(name);
//      logger.debug(name);
//    }
//
//  }

}
