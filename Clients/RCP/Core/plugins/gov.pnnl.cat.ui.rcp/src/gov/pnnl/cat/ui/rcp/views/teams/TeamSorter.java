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
package gov.pnnl.cat.ui.rcp.views.teams;

import gov.pnnl.cat.core.resources.security.ITeam;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 */
public class TeamSorter extends ViewerSorter {

  /**
   * Method compare.
   * @param viewer Viewer
   * @param e1 Object
   * @param e2 Object
   * @return int
   */
  public int compare(Viewer viewer, Object e1, Object e2) {
    ITeam team1 = (ITeam) e1;
    ITeam team2 = (ITeam) e2;
    String team1Name = team1.getName().toLowerCase();
    String team2Name = team2.getName().toLowerCase();
    int difference = team1Name.compareTo(team2Name);
    return difference;
  }
}
