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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;

/**
 */
public class TableCellModifier implements ICellModifier {
  private final TableViewer viewer;
  private Logger logger = CatLogger.getLogger(TableCellModifier.class);
  
  public boolean allowMod = false;
  public Object element;
  public String property;
  
  /**
   * Constructor for TableCellModifier.
   * @param viewer TableViewer
   */
  public TableCellModifier(TableViewer viewer) {
      this.viewer = viewer;
  }
  
  /* 
   * element = row
   * property = column name
   * (non-Javadoc)
   * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
   */
  public boolean canModify(Object element, String property) {
    this.element = element;
    this.property = property;
    
    switch (getColumnIndex(property)) {
      case TableExplorer.NAME_COLUMN:
        return this.allowMod;
      default:
        return false;
    }
        
  }

  /**
   * Method getValue.
   * @param element Object
   * @param property String
   * @return Object
   * @see org.eclipse.jface.viewers.ICellModifier#getValue(Object, String)
   */
  public Object getValue(Object element, String property) {
    int columnIndex = getColumnIndex(property);
    
    IResource resource = RCPUtil.getResource(element);
    
    switch (columnIndex) {
      case TableExplorer.NAME_COLUMN:
        return resource.getName();
      
      default:
        return "THIS SHOULD NOT HAPPEN";  // The canModify() determines this.
    }
  }

  /**
   * Method getColumnIndex.
   * @param property String
   * @return int
   */
  private int getColumnIndex(String property) {
    //  Find the index of the column
    String[] properties = (String[]) viewer.getColumnProperties();
    int columnIndex = 0;
    for (int i = 0; i < properties.length; i++) {
      if (properties[i] == property) {
        columnIndex = i;
      }
    }
    return columnIndex;
  }

  
  /* element  - the row (object).
   * property - the column
   * value    - the new value for that property.
   * (non-Javadoc)
   * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
   */
  public void modify(Object element, String property, Object value) {
    IResource resource = (IResource) ((TableItem) element).getData();
    
    try {
      String name = resource.getName();
      String newName = (String) value;

      // compare the value with the current name
      if (value.equals(name)){
        return;//nothing was changed so don't do anything
      }

      resource.move(resource.getParent().getPath().append(newName));      
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }
    
    // Update the viewer.
    viewer.update(resource, new String[] { property });
    this.allowMod = false;
  }        
}
