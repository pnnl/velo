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

/**
 * This class has all static members to identify all the IDs for our Views.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class CatActionIDs {

  //action sets
	public static final String EDIT_MENU_ACTION_SET = "gov.pnnl.cat.ui.rcp.actions.editMenuActionSet";
  public static final String COPY_ACTION_SET = "gov.pnnl.cat.ui.rcp.actions.copyActionSet";
  public static final String CUT_ACTION_SET = "gov.pnnl.cat.ui.rcp.actions.cutActionSet";
  public static final String PASTE_ACTION_SET = "gov.pnnl.cat.ui.rcp.actions.pasteActionSet";
  public static final String DELETE_ACTION_SET = "gov.pnnl.cat.ui.rcp.actions.deleteActionSet";
	public static final String SEARCH_ACTION_SET = "gov.pnnl.cat.search.searchActionSet";
  public static final String NEW_FOLDER_ACTION_SET = "gov.pnnl.cat.ui.rcp.actions.newfolderActionSet";
  public static final String SELECT_ALL_ACTION_SET = "gov.pnnl.cat.ui.rcp.actions.selectallActionSet";
  public static final String RENAME_ACTION_SET = "gov.pnnl.cat.ui.rcp.actions.renameActionSet";
  public static final String ADMIN_ACTION_SET = "gov.pnnl.cat.admin.adminActionSet";
  public static final String ACCOUNT_ACTION_SET = "gov.pnnl.cat.ui.rcp.actions.accountMenuActionSet";
  
  // actions
  public static final String COPY_ACTION = "gov.pnnl.cat.ui.rcp.actions.copyAction";
  public static final String CUT_ACTION = "gov.pnnl.cat.ui.rcp.actions.cutAction";
  public static final String PASTE_ACTION = "gov.pnnl.cat.ui.rcp.actions.pasteAction";
  public static final String DELETE_ACTION = "gov.pnnl.cat.ui.rcp.actions.deleteAction";
  public static final String NEW_FOLDER_ACTION = "gov.pnnl.cat.ui.rcp.actions.newfolderAction";
  public static final String SELECT_ALL_ACTION = "gov.pnnl.cat.ui.rcp.actions.selectallAction";
  public static final String RENAME_ACTION = "gov.pnnl.cat.ui.rcp.actions.renameAction";
  
  // command ids
  public static final String DELETE_COMMAND = "org.eclipse.ui.edit.delete";
  public static final String COPY_COMMAND = "org.eclipse.ui.edit.copy";
  public static final String CUT_COMMAND = "org.eclipse.ui.edit.cut";
  public static final String PASTE_COMMAND = "org.eclipse.ui.edit.paste";
  public static final String PASTE_SHORTCUT_COMMAND = "gov.pnnl.cat.edit.paste.shortcut";
  public static final String NEW_FOLDER_COMMAND = "gov.pnnl.cat.ui.rcp.newfoldercommand";  
  public static final String SELECT_ALL_COMMAND = "org.eclipse.ui.edit.selectAll";
  public static final String RENAME_COMMAND = "org.eclipse.ui.edit.rename";
  
}
