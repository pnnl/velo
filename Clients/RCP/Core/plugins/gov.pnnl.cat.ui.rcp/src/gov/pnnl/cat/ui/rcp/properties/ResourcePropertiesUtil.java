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
package gov.pnnl.cat.ui.rcp.properties;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.util.DateFormatUtility;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.dialogs.properties.resource.PropertiesImageDecorator;
import gov.pnnl.velo.util.VeloConstants;

import java.text.NumberFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.turbine.util.FileUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 */
public class ResourcePropertiesUtil {
private static Logger logger = CatLogger.getLogger(ResourcePropertiesUtil.class);
  
  /**
   * Method getProperty.
   * @param resource IResource
   * @param key QualifiedName
   * @return String
   */
  public static String getProperty(IResource resource, String key) {
    if (key == null) {
      return "";
    }
    
    String displayedProperty = null;
    try {
      displayedProperty = resource.getPropertyAsString(key);
    } catch (ResourceException e) {
      logger.error(e);
    }
    
    if ( isDate(key)) {
      Date date = DateFormatUtility.parseJcrDate(displayedProperty);
      if (date != null) {//getDateFromJCRDate will return null if it can't figure out the date 
        displayedProperty = DateFormatUtility.formatDefaultDateTime(date);
      }
    }
    
    try {
      if(key.equals(VeloConstants.PROP_MIMETYPE))
      {
        displayedProperty = resource.getMimetype();
      }
    } catch (ResourceException e) {
      logger.error(e);
    }

    if (displayedProperty != null) {  
      return displayedProperty;
    } else {
      return "";
    }
  }

  /**
   * Method isDate.
   * @param key QualifiedName
   * @return boolean
   */
  private static boolean isDate(String key) {
    if (key == VeloConstants.PROP_CREATED || key == VeloConstants.PROP_MODIFIED) {
      return true;
    }
    return false;
  }

  /**
   *  
   * @param labelText
   * @param value
   * @param composite
  
   * @param rows int
   * @return Text
   */
  public static Text createEditableProperty(String labelText, String value, Composite composite, int rows)
  {
    final Label label = new Label(composite, SWT.NONE);
    label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    label.setText(labelText + ":");
    if(rows == 1){
      Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
      
      GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
      text.setLayoutData(gd);
      text.setText(value);
      return text;
    }
    else{
      Composite textComp = new Composite(composite, SWT.NONE);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.heightHint = 60;
      gd.widthHint = 223; //make it the default size of a property page regardless the text size
      textComp.setLayoutData(gd);
      GridLayout gridLayout = new GridLayout();
      gridLayout.marginWidth = 0;
      gridLayout.marginHeight = 0;
      gridLayout.horizontalSpacing = 0;
      textComp.setLayout(gridLayout);
      Text text= new Text(textComp, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
      gd= new GridData(GridData.FILL_BOTH);
      text.setLayoutData(gd);
      text.setText(value);
      return text;
    }
  }

//  Text text = new Text (shell, SWT.BORDER);
//  int columns = 10;
//  GC gc = new GC (text);
//  FontMetrics fm = gc.getFontMetrics ();
//  int width = columns * fm.getAverageCharWidth ();
//  int height = fm.getHeight ();
//  gc.dispose ();
//  text.setSize (text.computeSize (width, height));
  
  /**
   * Method createEditableProperty.
   * @param labelText String
   * @param key QualifiedName
   * @param resource IResource
   * @param composite Composite
   * @param rows int
   * @return Text
   */
  public static Text createEditableProperty(String labelText, String key, IResource resource, Composite composite, int rows){
    return createEditableProperty(labelText, getProperty(resource, key), composite, rows); 
  }
  
  /**
   * Method createEditableProperty.
   * @param labelText String
   * @param key QualifiedName
   * @param resource IResource
   * @param composite Composite
   * @return Text
   */
  public static Text createEditableProperty(String labelText, String key, IResource resource, Composite composite)
  {
    return createEditableProperty(labelText, key, resource, composite, 1);
  }

  /**
   * Method createNotEditableProperty.
   * @param labelText String
   * @param value String
   * @param composite Composite
   * @return Text
   */
  public static Text createNotEditableProperty(String labelText, String value, Composite composite)
  {
    final Label label = new Label(composite, SWT.NONE);
    label.setLayoutData(new GridData());
    label.setText(labelText + ":");

    Text text = new Text(composite, SWT.SINGLE);
    
    GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
    text.setLayoutData(gd);
    text.setText(value);
    
    

    text.setEditable(false);
    return text;
  }

  /**
   * Method createNotEditableProperty.
   * @param labelText String
   * @param key QualifiedName
   * @param resource IResource
   * @param composite Composite
   * @return Text
   */
  public static Text createNotEditableProperty(String labelText, String key, IResource resource, Composite composite)
  {
    Text text = createNotEditableProperty(labelText, getProperty(resource, key), composite);
    return text;
  }


  /**
   * Method getFileSize.
   * @param file IFile
   * @return String
   */
  public static String getFileSize(final IFile file) {
    long size = 0;
    try {
      size = file.getSize();
    } catch (ResourceException e) {
      logger.error(e);
    }

    StringBuffer sizeText = new StringBuffer();
    sizeText.append(FileUtils.byteCountToDisplaySize(size));
    if (size >= 1024) {
      sizeText.append(" (");
      sizeText.append(NumberFormat.getInstance().format(size));
      sizeText.append(" bytes)");
    }

    return sizeText.toString();
  }

  //copied from GeneralProperties.java
  /**
   * Method getImage.
   * @param resource IResource
   * @return Image
   */
  static public Image getImage(IResource resource) {
    Image image = null;
    //TODO: refactor to request size as well...
//    image = SharedImages.getInstance().getImageForResource(resource, SharedImages.CAT_IMG_SIZE_32);
    
    if (resource instanceof IFolder) {
      image = SharedImages.getInstance().getImageForResource(resource, SharedImages.CAT_IMG_SIZE_32);
    } else if (resource instanceof IFile) {
      image = SharedImages.getInstance().getImageForResource(resource, SharedImages.CAT_IMG_SIZE_32);
      if (image != null && image.getBounds().height < 32) {
        ImageData idata = image.getImageData().scaledTo(32,32);
        if (idata != null) {
          image = ImageDescriptor.createFromImageData(idata).createImage();
        }
      }
    }
    if (image == null) {
      image = SharedImages.getInstance().getImage(SharedImages.CAT_IMG_DOC, SharedImages.CAT_IMG_SIZE_32);
    }
    
    PropertiesImageDecorator imageDecorator = new PropertiesImageDecorator();
    Image decoratedImage = imageDecorator.decorateImage(image, resource);
    if(decoratedImage == null){
      return image;
    }else {
      return decoratedImage;
    }
  }

  /**
   * Method stringsEqual.
   * @param s1 String
   * @param s2 String
   * @return boolean
   */
  public static boolean stringsEqual(String s1, String s2) {
    if (s1 == null) {
      return s2 == null || s2.trim().length() == 0;
    }
    return s1.equals(s2);
  }

}
