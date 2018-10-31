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
package gov.pnnl.cat.ui.rcp.testdata.users;

import java.util.Vector;

/**
 */
public class CatUserRoot {

  private static Vector dummyusers = new Vector();

  static{ 
    dummyusers.add(new CatUser("Zoe", "Graddy", "zoe@pnl.gov", "d3l028", "(509) 372-6857", true, false));
    dummyusers.add(new CatUser("James", "Doyle", "james.doyle@pnl.gov", "d3k746", "(509) 372-6482", false, true));
    dummyusers.add(new CatUser("Carina", "Lansing", "carina.lansing@pnl.gov", "d3k339", "(509) 375-2482", false, false));
    dummyusers.add(new CatUser("Eric", "Marshall", "eric.marshall@pnl.gov", "d3m517", "(509) 375-6563", true, true));
    dummyusers.add(new CatUser("Alex", "Gibson", "alex@pnl.gov", "d3k388", "(509) 372-6086", false, true));
  }
  
  /**
   * Method getUsers.
   * @return Vector
   */
  public static Vector getUsers(){
    return dummyusers;
  }
}
