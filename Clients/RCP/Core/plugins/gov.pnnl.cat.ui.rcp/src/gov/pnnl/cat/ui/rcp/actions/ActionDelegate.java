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
package gov.pnnl.cat.ui.rcp.actions;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;



/**
 * 
 * @see Action
 * @version $Revision: 1.0 $
 */
public class ActionDelegate extends Action {

  // Local Variables
  private ActionDelegate actionDelegate;
  private Map            viewerActions = new HashMap();
  private Control        currentActionListener;



  public ActionDelegate() {
    actionDelegate = this;
    actionDelegate.setEnabled(false);
  }



  /**
   * @param view
   * @param action
   */
  public void addViewerAction(final Control view, final IAction action) {
    viewerActions.put(view, action);

    action.addPropertyChangeListener(new IPropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent event) {
        if (view.equals(currentActionListener)) {
          // System.out.println("CurrentView: " + currentActionListener + " == " + view);
          // System.out.println("propertyChange event: " + event.getProperty());
          // System.out.println("action.isEnabled()" + action.isEnabled());
          // System.out.println("action.getText()" + action.getText());

          actionDelegate.setEnabled(action.isEnabled());
        }
      }
    });

    view.addFocusListener(new FocusListener() {

      public void focusGained(FocusEvent e) {
        setCurrentActionListener(view);
        IAction action = (IAction) viewerActions.get(currentActionListener);

        actionDelegate.setEnabled(action.isEnabled());
      }



      public void focusLost(FocusEvent e) {
        // TODO Auto-generated method stub

      }
    });
  }



  /**
   * @param view
   */
  private void setCurrentActionListener(Control view) {
    this.currentActionListener = view;
  }


  /* (non-Javadoc)
   * @see org.eclipse.jface.action.IAction#run()
   */
  public void run() {
    IAction action = (IAction) viewerActions.get(currentActionListener);
    action.run();
  }

}
