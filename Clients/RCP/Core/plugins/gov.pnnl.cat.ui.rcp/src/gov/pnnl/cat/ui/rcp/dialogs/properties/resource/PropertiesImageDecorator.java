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
package gov.pnnl.cat.ui.rcp.dialogs.properties.resource;

import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;


/**
 */
public class PropertiesImageDecorator implements ILabelDecorator {

  
  private Logger logger = CatLogger.getLogger(this.getClass());
  
  /**
   * Method decorateImage.
   * @param baseImage Image
   * @param element Object
   * @return Image
   * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(Image, Object)
   */
  public Image decorateImage(Image baseImage, Object element) {
    Image thisImage = baseImage;
    IResource node = (IResource)element;
    if(node == null || thisImage == null){
      return null;
    }
  
    boolean matched = false;
    
    if(node instanceof ILinkedResource){
      matched = true;
      thisImage = SWTResourceManager.decorateImage(thisImage, SharedImages.getInstance().getImage(SharedImages.CAT_IMG_LINK_DECORATOR, SharedImages.CAT_IMG_SIZE_12), SWTResourceManager.BOTTOM_LEFT);
      
    }
    
    if(!matched){
      return null;
    }else{
      return thisImage;
    }
  }

  
  
  
  
  
  
  //not doing label (text) decoration for now
  /**
   * Method decorateText.
   * @param text String
   * @param element Object
   * @return String
   * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(String, Object)
   */
  public String decorateText(String text, Object element) {
    // TODO Auto-generated method stub
    return text;
  }







  /**
   * Method decorate.
   * @param element Object
   * @param decoration IDecoration
   */
  public void decorate(Object element, IDecoration decoration) {
    logger.debug("Within the decorate");
    // TODO Auto-generated method stub
    
  }







  /**
   * Method addListener.
   * @param listener ILabelProviderListener
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
   */
  public void addListener(ILabelProviderListener listener) {
    // TODO Auto-generated method stub
    
  }







  /**
   * Method dispose.
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
   */
  public void dispose() {
    // TODO Auto-generated method stub
    
  }







  /**
   * Method isLabelProperty.
   * @param element Object
   * @param property String
   * @return boolean
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object, String)
   */
  public boolean isLabelProperty(Object element, String property) {
    // TODO Auto-generated method stub
    return false;
  }







  /**
   * Method removeListener.
   * @param listener ILabelProviderListener
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
   */
  public void removeListener(ILabelProviderListener listener) {
    // TODO Auto-generated method stub
    
  }

}
