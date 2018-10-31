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
package gov.pnnl.cat.discussion;

import gov.pnnl.velo.util.VeloConstants;

/**
 */
public class DiscussionConstants {
  private static final String DISCUSSION_QUALIFIER = "http://www.alfresco.org/model/forum/1.0";

  /*
   * Associations
   */
  public static final String ASSOC_DISCUSSION = VeloConstants.createQNameString(DISCUSSION_QUALIFIER,"discussion");

  /*
   * Aspects
   */
  public static final String ASPECT_DISCUSSABLE = VeloConstants.createQNameString(DISCUSSION_QUALIFIER,"discussable");

  /*
   * Names
   */
  public static final String NAME_DISCUSSION = VeloConstants.createQNameString(DiscussionConstants.DISCUSSION_QUALIFIER,"discussion");

  /*
   * Types
   */
  public static final String TYPE_DISCUSSION = VeloConstants.createQNameString(DISCUSSION_QUALIFIER, "forum"); 
  public static final String TYPE_TOPIC = VeloConstants.createQNameString(DISCUSSION_QUALIFIER, "topic"); 
  public static final String TYPE_POST = VeloConstants.createQNameString(DISCUSSION_QUALIFIER, "post"); 

  /*
   * Date Formats
   */
  public static final String COMMON_DATE_FORMAT = "EEE M/d/yyyy h:mm a";
}
