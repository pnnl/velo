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
package gov.pnnl.cat.ui.rcp.expressions;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.AbstractExplorerView;

import org.apache.log4j.Logger;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;

/**
 * Is the active workbench part a proper resource table with a TableExplorer component?
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class IsResourceTablePropertyTester extends PropertyTester {
  private static final Logger logger = CatLogger.getLogger(IsResourceTablePropertyTester.class);

  /* (non-Javadoc)
   * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
   */
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    IWorkbenchPart part = (IWorkbenchPart) receiver;
    IWorkbenchSite site = part.getSite();
    
    boolean isTableInput = false;
    
    if (site.getSelectionProvider() != null) {
      ISelection selection = site.getSelectionProvider().getSelection();
      
      // Need to check to see if this is being called on a table.
      if (selection instanceof IStructuredSelection && (part instanceof AbstractExplorerView)) {
        IStructuredSelection struct = (IStructuredSelection) selection;
        AbstractExplorerView tableView = (AbstractExplorerView)part;
        if(tableView.getTableViewer() != null && tableView.isTableActive()) {
          isTableInput = true;
        }
      }

    }
    return isTableInput;
  }
  

}
