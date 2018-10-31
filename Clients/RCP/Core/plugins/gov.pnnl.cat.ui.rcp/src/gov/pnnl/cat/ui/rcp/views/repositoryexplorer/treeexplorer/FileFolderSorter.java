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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.util.StringComparator;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 */
public class FileFolderSorter extends ViewerSorter {
  private static Logger logger = CatLogger.getLogger(FileFolderSorter.class);
  private static final StringComparator STRING_COMPARATOR = new StringComparator();
  

  @Override
  public void sort(Viewer viewer, Object[] elements) {
    // If the elements are not an IResource, then return them as-is - do not sort
    boolean isAllResources = true;
    
    for (int i = 0; i < elements.length; i++) {
      if(! (elements[i] instanceof IResource) ) {
        isAllResources = false;
        break;
      }
    }
    
    if(isAllResources) {
      super.sort(viewer, elements);
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
   */
  @Override
  public int category(Object element) {
//    System.out.print("category for " + element);
    IResource resource = RCPUtil.getResource(element);

    if (resource == null) {
      return 0;   
   
    } else {

      if (resource instanceof IFolder) {
        //      System.out.println("1");
        try {
          if (resource.isType(IResource.TAXONOMY_ROOT)) {
            return 1;
          }
          if (resource.isType(IResource.TAXONOMY_FOLDER)) {
            return 2;
          }
        } catch (ResourceException e) {
          logger.error(e);
        }

        return 9;
      } else if (resource instanceof IFile) {
        //      System.out.println("2");
        return 10;
      } else {
        //      System.out.println("3");
        return 20;
      }
    }
  }

  /**
   * Method compare.
   * @param viewer Viewer
   * @param e1 Object
   * @param e2 Object
   * @return int
   */
  public int compare(Viewer viewer, Object e1, Object e2) {
    int cat1 = category(e1);
    int cat2 = category(e2);

    if (cat1 != cat2) {
      return cat1 - cat2;
    }

    IResource resource1 = RCPUtil.getResource(e1);
    IResource resource2 = RCPUtil.getResource(e2);

    if (resource1 != null && resource2 != null) {
//      return resource1.getName().compareTo(resource2.getName());
      return STRING_COMPARATOR.compare(resource1.getName(), resource2.getName());
    }

    return super.compare(viewer, e1, e2);
  }
}
