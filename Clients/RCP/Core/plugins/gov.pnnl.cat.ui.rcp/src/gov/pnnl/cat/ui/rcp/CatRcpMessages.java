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
package gov.pnnl.cat.ui.rcp;

import org.eclipse.osgi.util.NLS;

/**
 */
public class CatRcpMessages extends NLS {

  private static final String BUNDLE_NAME= "gov.pnnl.cat.ui.rcp.CatRcpMessages";//$NON-NLS-1$

  private CatRcpMessages() {
    // Do not instantiate
  }

  static {
    NLS.initializeMessages(BUNDLE_NAME, CatRcpMessages.class);
  }
  public static String SendToTaxonomy_job_title;
  public static String CreateTaxonomy_parent_cannot_be_taxonomy;
  public static String CreateProject_parent_cannot_be_taxonomy;
  public static String ImportTaxonomy_window_title;
  public static String NewTaxonomy_window_title;
  public static String NewTaxonomy_description;
  public static String NewTaxonomy_folder_text;
  
  public static String NewTaxonomy_already_exists_title;
  public static String NewTaxonomy_already_exists_message;
  public static String NewTaxonomy_cannot_create_message;
  public static String SendToTaxonomy_title;
  public static String SendToTaxonomy_description;
  public static String SendToTaxonomy_destination_label;
  public static String SendToTaxonomy_select_taxonomy_dialog_title;
  public static String SendToTaxonomy_destination_must_be_taxonomy_error;
  public static String CIFS_error_message;

}
