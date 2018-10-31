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
package gov.pnnl.cat.ui.rcp.actions;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author d3k746
 *
 * @version $Revision: 1.0 $
 */
public abstract class OpenWithAction extends Action{

  private Logger logger = CatLogger.getLogger(this.getClass());

  public OpenWithAction() {
    this(null);
  }

  /**
   * Constructor for OpenWithAction.
   * @param text String
   */
  public OpenWithAction(String text) {
    super(text);
  }

  /**
   * @param selection the selection from the tree or table
  
   * @return true if the selected items are allowed this action */
  public abstract boolean getEnabledStatus(IStructuredSelection selection);
  
  
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.action.IAction#run()
   */
  public void run(){
    final Job openWithJob = new Job("Opening Application") {
      protected IStatus run(IProgressMonitor monitor) {
        return runJob(monitor);
      }
    };
    openWithJob.setUser(true);
    openWithJob.schedule();
  }



  /**
   * @param monitor the progress monitor to be displayed to the user while launching this application
  
   * @return the status of the job */
  public abstract IStatus runJob(IProgressMonitor monitor);

  
  /**
   * Utility method to put just the files from the selection
   * into a Vector.  Every child that is a file of a selected folder
   * will be added to the returned vector.
   * 
   * @param selection the selection from the tree or table
  
   * @return a vector of just files from the selection */
  @SuppressWarnings("rawtypes")
  public Vector<IResource> getAllFiles(IStructuredSelection selection){
    Iterator iterator = selection.iterator();
    Vector<IResource> allFiles = new Vector<IResource>();
    Vector<IResource> allFolders = new Vector<IResource>();
    Object curObject;

    while (iterator.hasNext()) {
      curObject = iterator.next();
      convertToResources(RCPUtil.getResource(curObject), allFiles, allFolders);
    }
    return allFiles;
  }

  
  
  /**
   * Provides and file count.  This method will stop count after the upperLimit has been reached.
   * @param selection
   * @param upperLimit
  
   * @return int
   */
  public int fileCount(StructuredSelection selection, int upperLimit) {
    int number = 0;
    
    Iterator<?> iterator = selection.iterator();
    while (iterator.hasNext()) {
      number = numberofFiles(RCPUtil.getResource(iterator.next()), number, upperLimit);
    }
    
    return number;
  }

  
  /**
   * Method numberofFiles.
   * @param resource IResource
   * @param number int
   * @param limit int
   * @return int
   */
  private int numberofFiles(IResource resource, int number, int limit) {
    try {
      if(resource instanceof ILinkedResource){
        resource = ((ILinkedResource)resource).getTarget();
      }

      if(resource instanceof IFile){
        ++number;
      } else if (resource instanceof IFolder){
        List<IResource>children = ((IFolder)resource).getChildren();
        for (IResource element : children) {
          number = numberofFiles(element, number, limit);
          if (number >= limit) {
            return number;
          } 
        }
      }
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }      

    return number;
  }
  
  /**
   * @param resource the IFile or IFolder that is selected or a child of a selected folder
   * @param allFiles all files in the selection
   * @param allFolders Vector<IResource>
   */
  private void convertToResources(IResource resource, Vector<IResource> allFiles, Vector<IResource> allFolders) {
    try {
      if(resource instanceof ILinkedResource){
        resource = ((ILinkedResource)resource).getTarget();
      }

      if(resource instanceof IFile){
        allFiles.addElement(resource);
      }else if (resource instanceof IFolder){
        if (allFolders.contains(resource)) {
          return;
        } else {
          allFolders.addElement(resource);
        }
        List<IResource> children = ((IFolder)resource).getChildren();
        for (IResource element : children) {
          convertToResources(element, allFiles, allFolders);
        }
      }
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }
  }

}
