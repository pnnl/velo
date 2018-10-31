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
package gov.pnnl.cat.alerts.views;

import gov.pnnl.cat.alerts.AlertService;
import gov.pnnl.cat.alerts.AlertsPlugin;
import gov.pnnl.cat.alerts.model.IAlert;
import gov.pnnl.cat.core.resources.ServerException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.progress.IElementCollector;

/**
 */
public class AlertsInput extends DeferredWorkbenchAdapter {

  /**
   * Method fetchDeferredChildren.
   * @param object Object
   * @param collector IElementCollector
   * @param monitor IProgressMonitor
   * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(Object, IElementCollector, IProgressMonitor)
   */
  @Override
  public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
    AlertService alertsService = AlertsPlugin.getDefault().getAlertsService();

    try {
      IAlert[] alerts = alertsService.getAlerts();

      if (alerts.length > 0) {
        collector.add(alerts, monitor);
      }

    } catch (ServerException e) {
      throw new RuntimeException(e);
    }

    collector.done();
  }

  /**
   * Method isContainer.
   * @return boolean
   * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
   */
  @Override
  public boolean isContainer() {
    return true;
  }

  /**
   * Method getLabel.
   * @param o Object
   * @return String
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(Object)
   */
  @Override
  public String getLabel(Object o) {
    return "Alerts";
  }

}
