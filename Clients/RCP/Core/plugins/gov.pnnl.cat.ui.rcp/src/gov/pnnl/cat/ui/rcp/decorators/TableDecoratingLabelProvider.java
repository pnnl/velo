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
package gov.pnnl.cat.ui.rcp.decorators;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Class that supports Decoration of TableViewer and TreeViewer with TreeColumns
 * 
 * This class was copied from the web on 2007-03-09
 * http://wiki.eclipse.org/index.php/FAQ_How_to_decorate_a_TableViewer_or_TreeViewer_with_Columns%3F
 *
 * @author Annamalai Chockalingam
 * @version $Revision: 1.0 $
 */
public class TableDecoratingLabelProvider extends DecoratingLabelProvider implements ITableLabelProvider {

  ITableLabelProvider provider;
  ILabelDecorator decorator;

  /**
   * @param provider
   * @param decorator
   */
  public TableDecoratingLabelProvider(ILabelProvider provider,
      ILabelDecorator decorator) {
    super(provider, decorator);
    this.provider = (ITableLabelProvider) provider;
    this.decorator = decorator;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  public Image getColumnImage(Object element, int columnIndex) {
    Image image = provider.getColumnImage(element, columnIndex);
    if (decorator != null) {
      Image decorated = decorator.decorateImage(image, element);
      if (decorated != null) {
        return decorated;
      }
    }
    return image;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    String text = provider.getColumnText(element, columnIndex);
    if (decorator != null) {
      String decorated = decorator.decorateText(text, element);
      if (decorated != null) {
        return decorated;
      }
    }
    return text;
  }

}
