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
package gov.pnnl.cat.search.taxonomy.actions;

import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.cat.search.taxonomy.preferences.PreferenceConstants;
import gov.pnnl.cat.search.taxonomy.query.FindCommonFilesHelper;
import gov.pnnl.cat.search.taxonomy.query.ITaxonomyIntersectionQuery;
import gov.pnnl.cat.search.taxonomy.query.TaxonomyIntersectionQuery;
import gov.pnnl.cat.ui.rcp.CatPerspectiveIDs;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.cat.ui.utils.PerspectiveOpener;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 */
public class FindCommonFilesAction implements IViewActionDelegate {
  private IViewPart viewPart;
  private ISelection currentSelection;

	/**
	 * Constructor for Action1.
	 */
	public FindCommonFilesAction() {
		super();
	}

  /**
   * Method init.
   * @param view IViewPart
   * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
   */
  public void init(IViewPart view) {
    this.viewPart = view;
  }

  /**
   * Method run.
   * @param action IAction
   * @see org.eclipse.ui.IActionDelegate#run(IAction)
   */
  public void run(IAction action) {
    Shell shell = viewPart.getSite().getShell();
    IPreferenceStore store;
    PerspectiveOpener perspectiveOpener;
    String key;
    int returnCode;
    
    // this should never actually execute as long as the requirements in our 
    // plugin.xml are working properly.
    if (this.currentSelection == null || this.currentSelection.isEmpty()) {
      // throw up a dialog
      MessageDialog.openError(shell, "Nothing Selected", 
          "You must have something selected to perform this operation.");
    } else {
      // TaxonomyActivator.getDefault().getDialogSettings()
      store = CatRcpPlugin.getDefault().getPreferenceStore();
      key = PreferenceConstants.P_SWITCH_PERSPECTIVES;

      perspectiveOpener = new PerspectiveOpener(CatPerspectiveIDs.SEARCH, null, this.viewPart.getSite().getWorkbenchWindow());
      returnCode = perspectiveOpener.openPerspectiveWithPrompt();

      if (returnCode != IDialogConstants.CANCEL_ID) {
        FindCommonFilesHelper helper = new FindCommonFilesHelper(this.currentSelection);
        ITaxonomyIntersectionQuery query = new TaxonomyIntersectionQuery(helper);

        NewSearchUI.activateSearchResultView();
        NewSearchUI.runQueryInBackground(query);
      }
    }
  }

  /**
   * Method selectionChanged.
   * @param action IAction
   * @param selection ISelection
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
//    System.out.println("Selection changed!" + action.getText() + ", " + action.getDescription());
    this.currentSelection = selection;
  }


}
