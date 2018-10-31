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
package gov.pnnl.cat.core.resources.tests.util;

import gov.pnnl.cat.core.resources.util.StringComparator;
import junit.framework.TestCase;

/**
 */
public class StringComparatorTest extends TestCase {
  private StringComparator strCompare = new StringComparator();

  public void testCompareTo() {
    String[] larger = {
        "abc", "abc123", "abc123", "124", "1000", "123abc99", "aaa", "122",
        "1000", "123abc500", "123abc123", "123abc1000", "123a", "92233720368547758079223372036854775807",
        "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
    };
    String[] smaller = {
        "aaa", "abc122", "abc12a", "123", "99",   "123abc20", "123", "5",
        "123",  "123abc123", "123abc100", "123abc500",  "123", "9223372036854775807",
        "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
    };

    for (int i = 0; i < larger.length; i++) {
      assertTrue(strCompare.compare(larger[i], smaller[i]) > 0);
      assertTrue(strCompare.compare(smaller[i], larger[i]) < 0);
    }
  }

  public void testCompareEquals() {
    String[] larger = {
        "abc", "abc123", "abc123", "124", "1000", "123abc99", "aaa"
    };
    String[] smaller = {
        "abc", "abc123", "abc123", "124", "1000", "123abc99", "aaa"
    };
    
    for (int i = 0; i < larger.length; i++) {
      assertTrue(strCompare.compare(larger[i], smaller[i]) == 0);
    }
  }
}
