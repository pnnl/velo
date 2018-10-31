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
package gov.pnnl.cat.core.resources.util;

import java.math.BigInteger;
import java.util.Comparator;

/**
 */
public class StringComparator implements Comparator<String> {

  /**
   * Method compare.
   * @param str1 String
   * @param str2 String
   * @return int
   */
  public int compare(String str1, String str2) {
    str1 = str1.toLowerCase();
    str2 = str2.toLowerCase();

    if (str1.equals(str2)) {
      return 0;
    }

    char[] str1Arr = str1.toCharArray();
    char[] str2Arr = str2.toCharArray();

    for (int i = 0; i < Math.min(str1Arr.length, str2Arr.length); i++) {
      char c1 = str1Arr[i];
      char c2 = str2Arr[i];

      if (isInt(c1) && isInt(c2)) {
        int val = compareLeadingInts(str1.substring(i), str2.substring(i));

        if (val != 0) {
          return val;
        }
      }

      if (c1 != c2) {
        return c1 - c2;
      }
    }

    return str1Arr.length - str2Arr.length;
  }

  /**
   * Method compareLeadingInts.
   * @param str1 String
   * @param str2 String
   * @return int
   */
  private int compareLeadingInts(String str1, String str2) {
    BigInteger int1 = getLeadingInts(str1);
    BigInteger int2 = getLeadingInts(str2);

    BigInteger dif = int1.subtract(int2);

    return dif.signum();
  }

  // TODO: if using BigIntegers has too much overhead,
  //       consider other ways to do this, perhaps counting the number
  //       of digits first to determine how big of an object we need. 
  /**
   * Method getLeadingInts.
   * @param str String
   * @return BigInteger
   */
  private BigInteger getLeadingInts(String str) {
    StringBuilder sb = new StringBuilder();
    char[] chars = str.toCharArray();

    for (char c : chars) {
      if (isInt(c)) {
        sb.append(c);
      } else {
        return new BigInteger(sb.toString());
      }
    }

    return new BigInteger(sb.toString());
  }

  /**
   * Method isInt.
   * @param c char
   * @return boolean
   */
  private boolean isInt(char c) {
    try {
      Integer.parseInt(new String(new char[] {c}));
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
