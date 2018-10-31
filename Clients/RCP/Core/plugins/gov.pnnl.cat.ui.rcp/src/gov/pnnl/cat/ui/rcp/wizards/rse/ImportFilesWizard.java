package gov.pnnl.cat.ui.rcp.wizards.rse;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.cat.rse.filechooser.SelectFilesGroup;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.cat.ui.utils.CatUIUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.ACL;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.CmsPathComparator;
import gov.pnnl.velo.model.Properties;
import gov.pnnl.velo.model.RemoteLink;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;


public class ImportFilesWizard extends Wizard implements IImportWizard, IRunnableWithProgress {
  public static final String ID = ImportFilesWizard.class.getCanonicalName();
  
  public static String METADATA_FILENAME_REGEX = ".*[mM][eE][tT][aA][dD][aA][tT][aA].*";
  private static String TAGS_PROP_NAME = "TAGS";

  protected FilesPage importPage;

  private static Logger logger = CatLogger.getLogger(ImportFilesWizard.class);

  private IWorkbenchWindow workbenchWindow;

  private IStructuredSelection selection;

//  private MetadataPage metadataPage;

  private AccessPage permissionsPage;

  private ACL acl; // These will be set on the initial page, then modified on the last.

  private IRemoteFile globalMetadataFile = null;
  private IRemoteFile imageMetadataFile = null;
  //private CmsPath thumbnailImage;  // maybe this could apply at some point

  protected CmsPath destPath;

  protected Properties globalMetadata = null;
  private Map<IHost, List<IRemoteFile>> datasetFiles;
  private Map<IHost, String> importActions;
  private List<CmsPath> resourcesToSetPermissions = new ArrayList<CmsPath>();

  private Object catTreeRoot = null;
  private String title = null;

  public ImportFilesWizard() {
    this("Import Files", null);
  }
  
  public IStructuredSelection getSelection(){
    return this.selection;
  }
  
  public ImportFilesWizard(String title, Object catTreeRoot) {
    super();
    setWindowTitle(title);
    setNeedsProgressMonitor(true);
    setTitleBarColor(new RGB(2, 43, 43));
    this.catTreeRoot = catTreeRoot;
    this.title = title;
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.workbenchWindow = workbench.getActiveWorkbenchWindow();
    this.selection = selection;
  }


  /***
   * Add the 'Detail' and 'Import Model' pages
   * 
   * @see Wizard@addpages
   */
  public void addPages() {
    this.getShell().setSize(650, 620);
    Point parentLocation = getShell().getParent().getLocation();
    this.getShell().setLocation(parentLocation.x + 200, parentLocation.y + 200);
    setDefaultPageImageDescriptor(CatRcpPlugin.getDefault().getImageDescriptor("folder_documents.png", 64));
    importPage = new FilesPage("Import Files", "", workbenchWindow, selection) {

      @Override
      protected String validateSelectedFiles() {
        return null;
      }
      
    };
    importPage.setDescription("Select a folder from the local file system to import. Data will be imported and properties will be extracted and added as metadata.");
    importPage.setTitle(title);
    importPage.setCatTreeRoot(catTreeRoot);

//    metadataPage = new MetadataPage("Metadata", workbenchWindow);
//    metadataPage.setDescription("Metadata to be associated with the imported files.");
//    metadataPage.setTitle("Metadata");

    permissionsPage = new AccessPage("Add Allowed Users or Teams");
    permissionsPage.setDescription("Select the users or teams who should have access to this dataset.");
    permissionsPage.setTitle("Add Allowed Users or Teams");

    addPage(importPage);
//    addPage(metadataPage);
    addPage(permissionsPage);
  }

  public void setACL(ACL newAcl) {
    this.acl = newAcl;
  }

  public ACL getACL() {
    if(acl == null) {
      acl = new ACL();
      acl.setInheritPermissions(false);
      acl.setOwner(ResourcesPlugin.getSecurityManager().getActiveUser().getUsername());
      acl.setNodePath("/dataset");// fake path since this node hasn't been added yet
    }
    return acl;
  }

//  @Override
//  public IWizardPage getNextPage(IWizardPage page) {
//    IWizardPage nextPage = super.getNextPage(page);
//    if (nextPage != null && nextPage instanceof MetadataPage) {
//      MetadataPage exppage = (MetadataPage) nextPage;
//      // Fill the experiment metadata file box
//      if (exppage.getMetaFile() == null) {
//        IRemoteFile file = ((FilesPage) page).getMetaDataFile();
//        // we can't open globus files locally at this time
//        if (file != null && !file.getHost().getSystemType().getId().contains("Globus")) {
//          exppage.setMetaFile(file);
//        }
//      }
//    }
//
//    return nextPage;
//  }

  @Override
  public boolean performFinish() {

    // Only allow the user to click the finish button when all the pages are ready to go
    if(!importPage.isPageComplete() || !permissionsPage.isPageComplete())
      return false;

    // Now pull all of the variables out of UI pages to member variables so we can
    // run in the background:

    // 1) destination folder
    destPath = importPage.getDestinationFolder();
    
//    // 2) TODO: global metadata (what should be the correct format to request for general import?)
//    this.globalMetadataFile = metadataPage.getMetaFile();


    // 4) Files
    this.datasetFiles = importPage.getSelectedFiles();    
    this.importActions = importPage.getImportActions();

    try {
      getContainer().run(true, true, this);

    } catch (InterruptedException e) {
      CatUIUtil.showNotificationMessage(importPage.getControl(), "Import Cancelled", "The importing of the file(s) has been cancelled.");
      return false;

    } catch (InvocationTargetException e) {
      ToolErrorHandler.handleError("An error occurred while trying to import files.", e, true);
      return false;
    }

    return true;
  }



  @Override
  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    monitor.beginTask("Importing Files", 1200);
    try {

      // 1) prepare metadata
      if(globalMetadataFile != null && globalMetadataFile.exists()){
        monitor.subTask("Preparing Metadata");
        globalMetadata = getGlobalProperties(globalMetadataFile);
      }
      monitor.worked(20);
      
      // 1.5) pull out from metadata the tags and categories since we don't want them added as properties
      List<String> tags;
      if(globalMetadata != null) {
        tags = globalMetadata.getProperties().remove(TAGS_PROP_NAME);
      } else {
        tags = new ArrayList<String>();
      }
      
      // 2) create folders
      monitor.subTask("Creating Folders");
      Map<IHost, Map<IRemoteFile, CmsPath>> folderPaths = createFolders(destPath, globalMetadata);
      monitor.worked(200);
      if(monitor.isCanceled()) {
        return;
      }

      // 3) create remote links
      monitor.subTask("Linking Remote Files");
      createRemoteLinks(destPath, globalMetadata);
      monitor.worked(200);
      if(monitor.isCanceled()) {
        return;
      }

      // 4) upload files with metadata attached
      //    add the tags only to files uploaded
      SubMonitor subMonitor = SubMonitor.convert(monitor, 700);
      uploadFiles(destPath, folderPaths, globalMetadata, tags, null, subMonitor);
      if(monitor.isCanceled()) {
        return;
      }
      // 5) set permissions on the uploaded files
      monitor.setTaskName("");
      monitor.subTask("Setting Permissions");
      setPermissions(resourcesToSetPermissions);
      monitor.worked(100);

      // 6) add the dataset to the selected category(s)
      //TODO
    } catch (Throwable e) {
      ToolErrorHandler.handleError("An error occurred uploading dataset.", e, true);
    }
    monitor.done();
  }

  protected Properties getGlobalProperties(IRemoteFile remoteFile) throws Exception {
    File tempFolder = RSEUtils.createTemporaryFolder();
    File metadataFile = RSEUtils.createTemporaryFile(tempFolder, remoteFile.getName());
    RSEUtils.getRemoteFileContents(remoteFile, metadataFile);
    Properties properties = null;
    if (metadataFile.exists()) {
      properties = convertExcelToProperties(metadataFile);
    }
    FileUtils.deleteDirectory(tempFolder);

    return properties;
  }
  
  protected Properties convertExcelToProperties(File metadataFile) {
    Properties properties = new Properties();
    
    try {
      // Get the workbook object for XLSX file
      Workbook wBook = WorkbookFactory.create(new FileInputStream(metadataFile));
      // Get first sheet from the workbook
      Sheet sheet = wBook.getSheetAt(0);
      Row row;
      Cell cell;
      // Iterate through each rows from first sheet
      Iterator<Row> rowIterator = sheet.iterator();

      while (rowIterator.hasNext()) {
        row = rowIterator.next();
        String propName = null;
        List<String> propValues = new ArrayList<String>();
        
        boolean first = true;
        boolean second = true;
        // For each row, iterate through each columns
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {

          cell = cellIterator.next();
          
          if(first){  //fist cell is hidden column of fully qualified prop name
            first = false;
            propName = cell.getStringCellValue();
            continue;
          }else if(second){ //second column is just the human readable label, always just skip it
            second = false;
            continue;
          }
          //all remaining columns are the value(s) for a property
          
          
          //IDK why but when a cell is formatted for a date it's type is "CELL_TYPE_NUMBERIC" but calling cell.getDateCellValue()
          //returns a true date.  So just try that to start with to see if we have a date, if it fails move on.
          try{
            Date date = cell.getDateCellValue();
            if(date != null){
              propValues.add(date.toString());
              continue;
            }
          }catch(Exception e){}

          switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
              propValues.add(String.valueOf(cell.getBooleanCellValue()));
              break;
            case Cell.CELL_TYPE_NUMERIC://cell.getDateCellValue()
              propValues.add(String.valueOf(cell.getNumericCellValue()));
              break;
            case Cell.CELL_TYPE_STRING:
              String cellValue = String.valueOf(cell.getStringCellValue());
              //try to parse the value as a date, if no exception is thrown need to use the date.toString format of
              //a date to set the property as it has to be in the ISO8601 format as server expects in order to parse 
              try {
                // date should be in the format MM/DD/YYYY
                Date date = new SimpleDateFormat("MM/dd/yyyy").parse(cellValue);
                propValues.add(date.toString());

              } catch (ParseException e) {
                propValues.add(cellValue);
              }
              break;
            case Cell.CELL_TYPE_BLANK:
              break;
            default:
              String cellValueDefault = String.valueOf(cell.toString());
              //try to parse the value as a date, if no exception is thrown need to use the date.toString format of
              //a date to set the property as it has to be in the ISO8601 format as server expects in order to parse 
              try {
                // date should be in the format MM/DD/YYYY
                Date date = new SimpleDateFormat("MM/dd/yyyy").parse(cellValueDefault);
                propValues.add(date.toString());

              } catch (ParseException e) {
                propValues.add(cellValueDefault);
              }

          }
        }
        
        //after iterating over all columns of this row, add the property and all of its values to the map returned 
        if(propName != null && !propName.trim().isEmpty() && propValues.size() > 1){
          properties.setProperty(propName, propValues);
        }else if(propName != null && !propName.trim().isEmpty()  && propValues.size() == 1 && !propValues.get(0).isEmpty()){
          properties.setProperty(propName, propValues.get(0));
        }
        
      }


      return properties;
    } catch (Exception ioe) {
      ioe.printStackTrace();
      return null;
    }
  }
  
  
  
  protected File convertExcelToCsv(File metadataFile) {

    try {
      File outputFile = File.createTempFile("expMetadata", "csv");
      StringBuffer data = new StringBuffer();
      // Get the workbook object for XLSX file
//      XSSFWorkbook wBook = new XSSFWorkbook(new FileInputStream(metadataFile));
      Workbook wBook = WorkbookFactory.create(new FileInputStream(metadataFile));
      // Get first sheet from the workbook
      Sheet sheet = wBook.getSheetAt(0);
      Row row;
      Cell cell;
      // Iterate through each rows from first sheet
      Iterator<Row> rowIterator = sheet.iterator();

      while (rowIterator.hasNext()) {
        row = rowIterator.next();

        // For each row, iterate through each columns
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {

          cell = cellIterator.next();
          String delim = "";
          if (cellIterator.hasNext()) {
            delim = ",";
          }

          switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
              data.append(cell.getBooleanCellValue() + delim);

              break;
            case Cell.CELL_TYPE_NUMERIC:
              data.append(cell.getNumericCellValue() + delim);
              break;
            case Cell.CELL_TYPE_STRING:
              data.append(cell.getStringCellValue() + delim);
              break;
            case Cell.CELL_TYPE_BLANK:
              data.append("" + delim);
              break;
            default:
              data.append(cell + delim);

          }
        }
        data.append("\r\n");
      }

      // make sure it is UTF-8 so it won't crash the json parser
      FileUtils.writeStringToFile(outputFile, data.toString(), "UTF-8");

      return outputFile;
    } catch (Exception ioe) {
      ioe.printStackTrace();
      return null;
    }
  }
  
  private void setPermissions(List<CmsPath> files) {
    ISecurityManager smgr = ResourcesPlugin.getSecurityManager();
    List<ACL> acls = new ArrayList<ACL>();
    for(CmsPath file : files) {
      ACL a = new ACL();
      a.setAces(acl.getAces());
      a.setInheritPermissions(acl.isInheritPermissions());
      a.setOwner(acl.getOwner());
      a.setNodePath(file.toAssociationNamePath()); 
      acls.add(a);
    }
    smgr.setPermissions(acls.toArray(new ACL[acls.size()]));    
  }

  private void createRemoteLinks(CmsPath datasetRoot, Properties globalMetadata) {
    
    Map<String, List<String>> globalProps = null;
    if(globalMetadata != null) {
      globalProps = globalMetadata.toList();
    }
    
    for(IHost host : datasetFiles.keySet()) {
      String action = importActions.get(host);

      if(action != null && action.equals(SelectFilesGroup.ACTION_LINK)) {
        Map<CmsPath, IRemoteFile> linkMap = extractTopLevelRemoteLinks(datasetRoot, host);
        List<RemoteLink> remoteLinks = new ArrayList<RemoteLink>();

        for(CmsPath path : linkMap.keySet()) {
          IRemoteFile remoteFile = linkMap.get(path);
          String type = VeloConstants.TYPE_FILE;
          if(remoteFile.isDirectory()) {
            type = VeloConstants.TYPE_FOLDER;
          }
          //ISystemViewElementAdapter adapter = RSEUtils.getRseViewAdapter(remoteFile);
          RemoteLink link = new RemoteLink(type, path.getParent().toAssociationNamePath(), host.getHostName(), remoteFile.getAbsolutePath(), "", "");
          if(globalProps != null) {
            link.getProperties().putAll(globalProps);
          }
          remoteLinks.add(link);
          
          // we will set permissions on root links
          resourcesToSetPermissions.add(path);
        }
        ResourcesPlugin.getResourceManager().createRemoteLinks(remoteLinks);
      }
    }
  }

  /**
   * We only create folders filesystems that were selected with the Upload option.
   */
  private Map<IHost, Map<IRemoteFile, CmsPath>> createFolders(CmsPath datasetRoot, Properties globalMetadata) {
    Map<IHost, Map<IRemoteFile, CmsPath>> folderPaths = new HashMap<IHost, Map<IRemoteFile, CmsPath>>();
    Set<CmsPath> foldersToCreate = getFoldersToCreate();
    
    // First collect the hierarchy of folders to create for each host
    for(IHost host : datasetFiles.keySet()) {
      String action = importActions.get(host);

      if(action != null && action.equals(SelectFilesGroup.ACTION_UPLOAD)) {
        Map<IRemoteFile, CmsPath> foldersPerHost = extractFolderHierarchy(datasetRoot, datasetFiles.get(host));
        folderPaths.put(host, foldersPerHost);       
        foldersToCreate.addAll(foldersPerHost.values());
      }
    }
    
    // then create them
    List<Resource> folders = new ArrayList<Resource>();
    Map<String, List<String>> globalProps = null;
    if(globalMetadata != null) {
      globalProps = globalMetadata.toList();
    }
    
    for(CmsPath folder: foldersToCreate) { 
      Resource resource = new Resource(VeloConstants.TYPE_FOLDER, folder);
      if(globalProps != null) {
        resource.setProperties(globalProps);
      }
      folders.add(resource);
    }

    ResourcesPlugin.getResourceManager().createFolders(folders);

    return folderPaths;
  }
  
  //iic's dataset import wizard overrides this in order to include the dataset folder as a folder to create
  protected Set<CmsPath> getFoldersToCreate() {
    return new TreeSet<CmsPath>(new CmsPathComparator());
  }

  /**
   * TODO: we need to determine based on user selection which files will be uploaded and which will be linked.
   * For those remote files being uploaded, we need to use RSE API
   */
  private void uploadFiles(CmsPath datasetRoot,Map<IHost, Map<IRemoteFile, CmsPath>> folderPaths, Properties globalMetadata, List<String> tags, Map<String, Properties> fileSpecificMetadata, 
      IProgressMonitor monitor) throws Exception {
    Map<File, CmsPath> filesToUpload = new HashMap<File, CmsPath>();
    File tempFolder = RSEUtils.createTemporaryFolder();
    File file;
    CmsPath path;
    
    // make sure metadata files are uploaded too, in case we want to edit and re-apply properties later
    // we must upload metadata files to velo - we put them directly under the dataset root
    if(globalMetadataFile != null) {
      file = RSEUtils.createTemporaryFile(tempFolder, globalMetadataFile.getName());
      RSEUtils.getRemoteFileContents(globalMetadataFile, file);
      path = datasetRoot.append(globalMetadataFile.getName());
      filesToUpload.put(file, path);
    }
    if(imageMetadataFile != null) {
      file = RSEUtils.createTemporaryFile(tempFolder, imageMetadataFile.getName());
      RSEUtils.getRemoteFileContents(imageMetadataFile, file);
      path = datasetRoot.append(imageMetadataFile.getName());
      filesToUpload.put(file, path);        
    }

    for(IHost host : datasetFiles.keySet()) {
      String action = importActions.get(host);
      boolean local = host.getName().equalsIgnoreCase("local");
      Map<IRemoteFile, CmsPath> folderPathsPerHost = folderPaths.get(host);
      List<IRemoteFile> remoteFiles = datasetFiles.get(host);
      
      // make sure metadata files don't get uploaded twice
      if(remoteFiles.contains(globalMetadataFile)) {
        remoteFiles.remove(globalMetadataFile);
      }
      if(imageMetadataFile != null && remoteFiles.contains(imageMetadataFile)) {
        remoteFiles.remove(imageMetadataFile);   
      }

      if(action != null && action.equals(SelectFilesGroup.ACTION_UPLOAD)) {

        for(IRemoteFile remoteFile : remoteFiles) {
          
          if(remoteFile.isDirectory()) {
            continue; // skip folders
          }

          // compute path
          IRemoteFile parentFile = remoteFile.getParentRemoteFile();
          if(folderPathsPerHost.get(parentFile) != null) {
            path = folderPathsPerHost.get(parentFile).append(remoteFile.getName());
          } else {
            // The file goes directly under the dataset root
            path = datasetRoot.append(remoteFile.getName());
            
            // We need to set permissions on root files
            resourcesToSetPermissions.add(path);
          }

          // compute file
          if(local) {
            file = new File(remoteFile.getAbsolutePath());
          } else {
            // we have to download the file from the remote location before we can upload it
            file = RSEUtils.createTemporaryFile(tempFolder, remoteFile.getName());
            RSEUtils.getRemoteFileContents(remoteFile, file);
          }

          filesToUpload.put(file, path);
        }

      }

    }
    
    // want to INCLUDE tags in the 'globalMetadata' for bulk upload as there is logic on server look for
    // the 'tags' by the prop name "TAGS" (instead of a qname) and set them as tags and not properties
    if(!filesToUpload.isEmpty()) {
      if(tags != null && !tags.isEmpty()){
        globalMetadata.setProperty(TAGS_PROP_NAME, tags);
      }
      ResourcesPlugin.getResourceManager().bulkUpload(filesToUpload, globalMetadata, fileSpecificMetadata, 
          monitor, false); // there could be a lot of files, so don't copy them to the file cache
    }
    
    FileUtils.deleteDirectory(tempFolder);

  }

  /**
   * Returns a list of the folders that we need to create in order to 
   * keep the hierarchy of the files selected by the user.  This is needed
   * since there is no guarantee the folders above a given file will be 
   * selected, for example:
   * 
   *\Users\port091\Desktop\SampleData\file1 
   *\Users\port091\Desktop\SampleData\SampleData2\file2
   *
   * I could select file1, file2 and the folder SampleData
   * We need to additionally upload the SampleData2 folder
   * 
   * @return
   */
  public Map<IRemoteFile, CmsPath> extractFolderHierarchy(CmsPath destPath, List<IRemoteFile> remoteFiles) {
    // First compute the root folders
    Set<IRemoteFile> folders = new LinkedHashSet<IRemoteFile>(); // use linked hash set to retain insertion order

    // for now we are only looking at local folders because remote folders will be handled
    // by create remote links or remote transfer
    for(IRemoteFile file : remoteFiles) {
      if(file.isDirectory()) {
        folders.add(file);
      }
    }
    List<IRemoteFile> rootFolders = new ArrayList<IRemoteFile>();
    for(IRemoteFile folder : folders) {
      IRemoteFile parent = folder.getParentRemoteFile();
      if(!folders.contains(parent)) {
        rootFolders.add(folder);
      }
    }

    // Then create CmsPaths for the root folders and all their child folder subtree
    HashMap<IRemoteFile,CmsPath> folderPaths = new HashMap<IRemoteFile,CmsPath>();
    for(IRemoteFile rootFolder : rootFolders) {
      recursiveAddChildFolders(rootFolder, destPath, folderPaths);
      
      // we will set permissions on root folders
      CmsPath path = destPath.append(rootFolder.getName());
      resourcesToSetPermissions.add(path);
    }

    return folderPaths;
  }

  public Map<CmsPath, IRemoteFile> extractTopLevelRemoteLinks(CmsPath datasetRoot, IHost host) {
    
    Map<String, IRemoteFile> rootFiles = new HashMap<String, IRemoteFile>();
    
    // only link the top-level remote selections, so we don't end up 
    // creating a ton of links - we will let user navigate remote data

    for(IRemoteFile file : datasetFiles.get(host)) {
      String filePath = file.getAbsolutePath().toLowerCase();
      boolean add = true;
      
      for(String path : rootFiles.keySet()) {
        if(filePath.startsWith(path + "/")) { // we have already included this path
          add = false;
        }
      }
      if(add) {
        rootFiles.put(filePath, file);
      }
    }

    
    HashMap<CmsPath, IRemoteFile> linkMap = new HashMap<CmsPath, IRemoteFile>();
    CmsPath rootPath = new CmsPath(datasetRoot);  
    
    for(IRemoteFile rootFile : rootFiles.values()) {
      CmsPath path = rootPath.append(rootFile.getName());
      linkMap.put(path, rootFile);
    }

    return linkMap;
  }

  private void recursiveAddChildFolders(IRemoteFile folder, CmsPath parentPath, HashMap<IRemoteFile, CmsPath> folderPaths) {
    CmsPath path = parentPath.append(folder.getName());
    folderPaths.put(folder, path);
    ISystemViewElementAdapter adapter = RSEUtils.getRseViewAdapter(folder);
    
    for(Object child : adapter.getChildren((IAdaptable)folder, new NullProgressMonitor())) {
      if(child instanceof IRemoteFile) {
        IRemoteFile childFolder = (IRemoteFile)child;
        if(childFolder.isDirectory()) {
          recursiveAddChildFolders(childFolder, path, folderPaths);
        }
      }
    }
  }

  //pull path of metadataTemplate out into method so subclasses can override
  public CmsPath getMetadataTemplatePath() {
    return new CmsPath("/Configuration Files/metadataTemplate.xlsx");
  }


}
