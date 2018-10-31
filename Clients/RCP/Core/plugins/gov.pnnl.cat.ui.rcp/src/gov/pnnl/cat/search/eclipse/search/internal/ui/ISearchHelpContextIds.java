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
package gov.pnnl.cat.search.eclipse.search.internal.ui;

import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;

/**
 */
public interface ISearchHelpContextIds {
	
	public static final String PREFIX= NewSearchUI.PLUGIN_ID + "."; //$NON-NLS-1$

	public static final String SEARCH_DIALOG= PREFIX + "search_dialog_context";	 //$NON-NLS-1$

	public static final String TEXT_SEARCH_PAGE= PREFIX + "text_search_page_context"; //$NON-NLS-1$
	public static final String TYPE_FILTERING_DIALOG= PREFIX + "type_filtering_dialog_context"; //$NON-NLS-1$

	public static final String SEARCH_VIEW= PREFIX + "search_view_context"; //$NON-NLS-1$
	public static final String New_SEARCH_VIEW= PREFIX + "new_search_view_context"; //$NON-NLS-1$
	
	public static final String REPLACE_DIALOG= PREFIX + "replace_dialog_context"; //$NON-NLS-1$

	public static final String SEARCH_PREFERENCE_PAGE= PREFIX + "search_preference_page_context"; //$NON-NLS-1$

	public static final String SELECT_ALL_ACTION = PREFIX + "select_all_action_context"; //$NON-NLS-1$
	
	public static final String SEARCH_ACTION = PREFIX + "search_action_context"; //$NON-NLS-1$
	
	public static final String FILE_SEARCH_ACTION= PREFIX + "file_search_action_context"; //$NON-NLS-1$
}
