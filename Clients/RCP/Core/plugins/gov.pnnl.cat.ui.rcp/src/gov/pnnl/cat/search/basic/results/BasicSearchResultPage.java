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

import gov.pnnl.cat.ui.rcp.decorators.TableDecoratingLabelProvider;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;

/** 
 * Provides custom behavior for displaying results of a repository search.
 * 
 * @author Eric Marshall
 *
 * @version $Revision: 1.0 $
 */
public class BasicSearchResultPage extends AbstractBasicSearchResultsPage {

  
  public BasicSearchResultPage() {
    // call our super constructor noting that we only support a flat layout.
    super(FLAG_LAYOUT_FLAT);
  }

  
  /**
   * Method configureTreeViewer.
   * @param viewer TreeViewer
   */
  protected void configureTreeViewer(TreeViewer viewer) {
    throw new RuntimeException("Tree Viewer is not supported in search results.");
  }

  /**
   * Method configureTableViewer.
   * @param viewer TableViewer
   */
  protected void configureTableViewer(TableViewer viewer) {
    ILabelProvider labelProvider = new CatWorkbenchLabelProvider(viewer);
    ILabelDecorator decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
    viewer.setLabelProvider(new TableDecoratingLabelProvider(labelProvider, decorator));

    viewer.setContentProvider(new SearchResultsTableContentProvider(this));
    this.mContentProvider = (ISearchResultsContentProvider) viewer.getContentProvider();  

    createContextMenu(viewer);
  }
}
