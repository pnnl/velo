package gov.pnnl.cat.core.resources.tests.myemsl;

import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.velo.model.CmsPath;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadFileTest extends AbstractVeloTest {
  public static String PROP_FILES_TO_UPLOAD = "files.to.upload";
  public static String PROP_NUM_ITERATIONS = "num.iterations";
  public static String PROP_DEST_FOLDER = "destination.folder";
  public static String PROP_CLEAN_DEST_FOLDER = "clean.destination.folder";

  private String createFolderOutputFileName = "CreateFolderTestOutput.csv";
  protected File createFolderOutputFile = new File(createFolderOutputFileName); // located in current run dir

  public UploadFileTest(String[] commandLineArgs) throws Exception {
    super(commandLineArgs);
    createFolderOutputFile.delete();
    
    // add headers to outputFiles
    appendToFile(getOutputFile(), "Upload#,MB Uploaded,Time (ms),MB/s");
    appendToFile(createFolderOutputFile, "Upload#,Time to Create Folder(ms)");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      UploadFileTest test = new UploadFileTest(args);
      test.run();

    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Done");
    System.exit(0);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.tests.myemsl.AbstractVeloTest#run()
   */
  @Override
  protected void run() throws Exception {
    // Create destination folder
    IResource destFolder = createDestFolder();

    long numIterations = Long.valueOf((String)properties.get(PROP_NUM_ITERATIONS));
    List<File> filesToUpload = getFilesToUpload();
    boolean moreIterations = true;
    long count = 1;
    while(moreIterations) {
      System.out.println("Performing upload #" + count);
      uploadFiles(destFolder, count, filesToUpload);

      if(numIterations != -1) {
        count++;
        if(count > numIterations) {
          moreIterations = false;
        }
      }
    }

    // TODO: add metadata extraction for fasta file sequence ID multi-valued property
    // TODO: do we need upload service for data sets
    // Want a JUnit test suite sitting on top of the low-level peformance test classes

  }
  
  private IResource createDestFolder() {
    boolean cleanDestFolder = Boolean.valueOf(properties.getProperty(PROP_CLEAN_DEST_FOLDER));
    String destPath = properties.getProperty(PROP_DEST_FOLDER);
    CmsPath cmsPath = new CmsPath(destPath);
    IResource destFolder = null;
    
    // make sure dest folder is cleaned before starting
    if(cleanDestFolder && resourceManager.resourceExists(cmsPath)) {
      resourceManager.deleteResource(cmsPath);
    }
    
    for (int i = 1; i < cmsPath.size(); i++) {
      CmsPath subPath = cmsPath.subPath(0, i+1);  
      destFolder = resourceManager.createFolder(subPath);
    }
    return destFolder;
  }

  private List<File> getFilesToUpload() {
    // TODO: support folders (i.e., datasets)
    String[] fileList = properties.getProperty(PROP_FILES_TO_UPLOAD).split(",");
    List<File> filesToUpload = new ArrayList<File>();

    for(String file : fileList) {
      File fileToUpload = new File(file);
      filesToUpload.add(fileToUpload);
    }
    return filesToUpload;
  }

  private void uploadFiles(IResource parentFolder, long index, List<File>filesToUpload) {

    IResourceManager mgr = CmsServiceLocator.getResourceManager();

    // Create destination folder
    long start = System.currentTimeMillis();
    long bytesUploaded = 0;
    IResource destFolder = mgr.createFolder(parentFolder, String.valueOf(index));  
    long end = System.currentTimeMillis();    
    appendToFile(createFolderOutputFile, String.valueOf(index) + "," + String.valueOf(end-start));

    // Upload file to destination folder
    Map<File, CmsPath> filesToDestination = new HashMap<File, CmsPath>();
    for(File fileToUpload : filesToUpload) {
      CmsPath destFilePath = destFolder.getPath().append(fileToUpload.getName());
      filesToDestination.put(fileToUpload, destFilePath);
      bytesUploaded += fileToUpload.length();
    }
    
    long mbUploaded = bytesUploaded/(1024*1024);

    start = System.currentTimeMillis();
    mgr.bulkUpload(filesToDestination, null);
    end = System.currentTimeMillis();
    long time = (end-start);
    double mbPerSec = mbUploaded / (time * 1.0/1000);
    appendToFile(getOutputFile(), String.valueOf(index) + "," + String.valueOf(mbUploaded) + "," + String.valueOf(time) + "," + String.valueOf(mbPerSec));

  }

}
