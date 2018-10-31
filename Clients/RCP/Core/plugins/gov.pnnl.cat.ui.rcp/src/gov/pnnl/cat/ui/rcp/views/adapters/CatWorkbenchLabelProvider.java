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
package gov.pnnl.cat.ui.rcp.views.adapters;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.decorators.LinkDecorator;
import gov.pnnl.cat.ui.rcp.images.ImageDataImageDescriptor;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.util.VeloConstants;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.overwritten.WorkbenchLabelProvider;

/**
 * Note that this class serves as a label provider for CAT resources whether they appear in a tree or table.
 * Therefore, this class implements basic label provider methods as well as table-only label provider
 * methods. 
 *  TODO: refactor this class to be only a label provider and then extend it with a table label provider
 *  subclass
 *
 * @version $Revision: 1.0 $
 */
public class CatWorkbenchLabelProvider extends WorkbenchLabelProvider implements ITableLabelProvider, IStyledLabelProvider {


  protected static final String EXTENSION_POINT = "gov.pnnl.cat.ui.rcp.styledWorkbenchLabelProvider";
  protected static final String ATTRIBUTE = "class";
  protected static List<StyledWorkbenchLabelProvider> styledWorkbenchLabelProviders = new ArrayList<StyledWorkbenchLabelProvider>();
  
  static {
    loadStyledLabelProviders();
  }
  
  private static void loadStyledLabelProviders() {
    
    try {
      // look up all the extensions for the StyledWorkbenchLabelProvider extension point
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

      for (IConfigurationElement configurationElement : elements) {
        Object obj = configurationElement.createExecutableExtension(ATTRIBUTE);
        if(obj instanceof StyledWorkbenchLabelProvider) {
          styledWorkbenchLabelProviders.add((StyledWorkbenchLabelProvider)obj);
        }
      }

    } catch (Throwable e) {
      throw new RuntimeException ("Unable to load custom styled label extension points.", e);
    }
  }
  
  private StructuredViewer viewer;
  private LinkDecorator linkDecorator;
  private static Logger logger = CatLogger.getLogger(CatWorkbenchLabelProvider.class);

  
  /**
   * Constructor for CatWorkbenchLabelProvider.
   * @param viewer StructuredViewer
   */
  public CatWorkbenchLabelProvider(StructuredViewer viewer) {
    this.viewer = viewer;
    this.linkDecorator = new LinkDecorator();
  }

  
  
  /**
   * Method getStyledText.
   * @param element Object
   * @return StyledString
   * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider$IStyledLabelProvider#getStyledText(Object)
   */
  @Override
  public StyledString getStyledText(Object element) {
    StyledString styledString = super.getStyledText(element);;
    if(styledWorkbenchLabelProviders.size() > 0){
      for (StyledWorkbenchLabelProvider styledLabelProvider : styledWorkbenchLabelProviders) {
        styledString = styledLabelProvider.getStyledText(element, styledString);
      }
    }
    if(styledString != null){
      return styledString;
    }else{
      return super.getStyledText(element);
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.overwritten.WorkbenchLabelProvider#getDecorationStyle(java.lang.Object)
   */
  @Override
  protected Styler getDecorationStyle(Object element) {
    Styler styler = super.getDecorationStyle(element);
    
    if(styledWorkbenchLabelProviders.size() > 0){
      for (StyledWorkbenchLabelProvider styledLabelProvider : styledWorkbenchLabelProviders) {
        styler = styledLabelProvider.getDecorationStyle(element, styler);
      }
    }
    if(styler != null){
      return styler;
    }else{
      return super.getDecorationStyle(element);
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.WorkbenchLabelProvider#decorateText(java.lang.String, java.lang.Object)
   */
  @Override
  protected String decorateText(String input, Object element) {
    ICatWorkbenchAdapter catAdapter = RCPUtil.getCatAdapter(element);
    if (catAdapter == null) {
      return super.decorateText(input, element);
    }

    String label = catAdapter.getLabel(element);
    if (label == null) {
      label = "Loading...";
    }
    return label;
  }

  /**
   * This method is called by the normal eclipse label provider (used for tree viewers), but not by the
   * table label provider used by table viewers.
   * 
   * @see org.eclipse.ui.model.WorkbenchLabelProvider#decorateImage(org.eclipse.jface.resource.ImageDescriptor, java.lang.Object)
   */
  @Override
  protected ImageDescriptor decorateImage(ImageDescriptor input,
      Object element) {
    ICatWorkbenchAdapter catAdapter = RCPUtil.getCatAdapter(element);
    // This could be null if the children are still loading
    if (catAdapter == null) {
      return input;
    }
    
    Image decoratedImage = linkDecorator.decorateImage(catAdapter.getColumnImage(element, 0),element);
    return new ImageDataImageDescriptor(decoratedImage);
    
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    ICatWorkbenchAdapter catAdapter = RCPUtil.getCatAdapter(element);
    if (catAdapter == null) {
      return null;
    }

    return linkDecorator.decorateImage(catAdapter.getColumnImage(element, columnIndex),element);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  @Override
  public String getColumnText(Object element, int columnIndex) {
    ICatWorkbenchAdapter catAdapter = RCPUtil.getCatAdapter(element);
    if (catAdapter == null) {
      return null;
    }

    String label = catAdapter.getColumnText(element, columnIndex);
    if (label == null) {
      label = "Loading...";
    }

    return label;
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.WorkbenchLabelProvider#getForeground(java.lang.Object)
   */
  @Override
  public Color getForeground(Object element) {
    Color color = super.getForeground(element);

    IResource resource = RCPUtil.getResource(element);
    try {
      if (resource instanceof IFile) {
    	String propVal = resource.getPropertyAsString(VeloConstants.PROP_NEEDS_FULL_TEXT_INDEXED);
        if(propVal != null){
        	color = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
        }
      }
    } catch (ResourceException e) {
      logger.error("Failed to get aspect for index status", e);
    }

    return color;
  }
  
}
