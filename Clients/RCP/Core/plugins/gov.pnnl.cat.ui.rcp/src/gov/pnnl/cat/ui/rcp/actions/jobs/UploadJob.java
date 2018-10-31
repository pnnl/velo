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
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.dialogs.FileDialogs;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class UploadJob extends Job {
  
  private String[] sourcePaths;
  private Shell shell;
  private IFolder destination;
  IResourceManager mgr = ResourcesPlugin.getResourceManager();

  public UploadJob(Shell shell, String[] sourcePaths, IFolder destination) {
    super("Uploading to " + destination.getPath().toDisplayString());
    this.sourcePaths = sourcePaths;
    this.shell = shell;
    this.destination = destination;
    
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      
      runInternal(monitor);
        
    } catch (Throwable e) {
      ToolErrorHandler.handleError("Failed to upload files.", e, true);
    }
    
    if (monitor.isCanceled()) {
      return Status.CANCEL_STATUS;
    }
    
    return Status.OK_STATUS;
  }
  
  private void runInternal(IProgressMonitor monitor) {
    // First iterate through the source paths to find all folders to create and all files to upload
    Map<File, CmsPath> filesToUpload = new HashMap<File, CmsPath>();
    List<Resource> foldersToCreate = new ArrayList<Resource>();
    boolean merge = false;
    File file;
    CmsPath destinationPath = destination.getPath();
    mgr.getChildren(destinationPath); // make sure children are in the cache so we don't have to check existence on every child

    // don't check down the tree if the parent folder doesn't exist
    // this prevents us from recursively calling resource exists if parent folder doesnt exist
    boolean destinationPathExists = true;
    
    for(String sourcePath : sourcePaths) {
      file = new File(sourcePath);
      if(file.isDirectory()) {
        merge = merge || addFolder(file, foldersToCreate, filesToUpload, destinationPath, destinationPathExists);
      } else {
        merge = merge || addFile(file, filesToUpload, destinationPath, destinationPathExists);
      }
      
    }
    
    boolean proceed = true;
    // Ask user if merge/overwrite is ok
    if(merge) {
      proceed = FileDialogs.confirmMergeOverwrite(shell, "Confirm Upload");
    }

    if(proceed) {
      monitor.beginTask("Creating " + foldersToCreate.size() + " folders...", 1);


      // Now create all folders in one call
      mgr.createFolders(foldersToCreate);
      monitor.worked(1);

      // Now upload all files in one call
      mgr.bulkUpload(filesToUpload, monitor);
    }
    
  }

  private boolean addFolder(File folder, List<Resource> foldersToCreate, Map<File, CmsPath> filesToUpload, CmsPath destinationPath, boolean destinationPathExists) {
    CmsPath folderPath = destinationPath.append(folder.getName());
    Resource resource = new Resource(VeloConstants.TYPE_FOLDER, folderPath);
    foldersToCreate.add(resource);
    boolean merge = false;
    if(destinationPathExists) {
      merge = mgr.resourceExists(folderPath);
    }
    if(merge) {
      mgr.getChildren(folderPath); // make sure children are in the cache so we don't have to check existence on every child
    }
    for(File file : folder.listFiles()) {
      if(file.isDirectory()) {
        merge = merge || addFolder(file, foldersToCreate, filesToUpload, folderPath, merge);
      } else {
        merge = merge || addFile(file, filesToUpload, folderPath, merge);
      }
    }
    return merge;
  }
  
  private boolean addFile(File file, Map<File, CmsPath> filesToUpload, CmsPath destinationPath, boolean destinationPathExists) {
    CmsPath filePath = destinationPath.append(file.getName());
    boolean merge = false;
    if(destinationPathExists) {
      // we know we have previously loaded the children, so we can check the cache instead of calling resource exists for every file
      merge = mgr.resourceCached(filePath);
    }
    filesToUpload.put(file, filePath);
    return merge;
  }

}
