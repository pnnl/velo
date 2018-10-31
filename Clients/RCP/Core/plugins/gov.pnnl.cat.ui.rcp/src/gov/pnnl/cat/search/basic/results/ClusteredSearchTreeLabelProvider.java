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
package gov.pnnl.cat.search.basic.results;

import gov.pnnl.cat.core.resources.search.ICluster;
import gov.pnnl.cat.ui.images.SharedImages;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 */
public class ClusteredSearchTreeLabelProvider extends LabelProvider {
  
  /**
   * Method getImage.
   * @param element Object
   * @return Image
   * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
   */
  @Override
  public Image getImage(Object element) {
    ICluster cluster = (ICluster)element;
    
    //no image for Uncategorized result so it won't look like a tree
    if(cluster.getPath().toString().indexOf("Uncategorized") >= 0)
      return null;
    return SharedImages.getInstance().getImage(SharedImages.CAT_IMG_FOLDER_CLOSED, SharedImages.CAT_IMG_SIZE_16);
  }
  
  /**
   * Method getText.
   * @param element Object
   * @return String
   * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
   */
  @Override
  public String getText(Object element) {
    String retStr = "";
    if(element == null)
      return retStr;
    
    ICluster cluster = (ICluster)element;
    int count = cluster.getTotalCount();
    
    if(cluster.getPath().size() == 0)
    {
      retStr = "All Categories"; 
    }
    else 
    {
      retStr = cluster.getName();
    }

    //for Uncategorized, do not print (count)
    if(cluster.getPath().toString().indexOf("Uncategorized") < 0)
    {
      retStr += " (" + count + ")";
    }
    return retStr;
    
  }
  
}
