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
package gov.pnnl.cat.search.eclipse.search2.internal.ui;

import org.eclipse.osgi.util.NLS;

/**
 */
public final class SearchMessages extends NLS {
  
	private static final String BUNDLE_NAME= "gov.pnnl.cat.search.eclipse.search2.internal.ui.SearchMessages";//$NON-NLS-1$

	private SearchMessages() {
		// Do not instantiate
	}

	public static String AbstractTextSearchViewPage_update_job_name;
	public static String RemoveSelectedMatchesAction_label;
	public static String RemoveSelectedMatchesAction_tooltip;
	public static String SearchAgainAction_label;
	public static String SearchAgainAction_tooltip;
	public static String SearchAgainAction_Error_title;
	public static String SearchAgainAction_Error_message;
	public static String SearchDropDownAction_label;
	public static String SearchDropDownAction_tooltip;
	public static String SearchesDialog_remove_label;
	public static String SearchDropDownAction_running_message;
	public static String Search_Error_openResultView_message;
	public static String SearchView_refresh_progress_job_label;
	public static String SearchView_update_title_job_name;
	public static String Search_Error_openResultView_title;
	public static String ShowSearchesAction_label;
	public static String ShowSearchesAction_tooltip;
	public static String ShowSearchesAction_dialog_title;
	public static String ShowSearchesAction_dialog_message;
	public static String SearchView_empty_message;
	public static String SearchView_title_search;
	public static String SearchView_showIn_menu;
	public static String SearchesDialog_title;
	public static String SearchesDialog_message;
	public static String RemoveAllSearchesAction_label;
	public static String RemoveAllSearchesAction_tooltip;
	public static String RemoveAllMatchesAction_label;
	public static String RemoveAllMatchesAction_tooltip;
	public static String ShowNextResultAction_label;
	public static String ShowNextResultAction_tooltip;
	public static String SortDropDownActon_label;
	public static String SortDropDownActon_tooltip;
	public static String SortDropDownActon_ascending_label;
	public static String SortDropDownActon_descending_label;
	public static String SortDropDownActon_nosort_label;
	public static String ShowPreviousResultAction_label;
	public static String ShowPreviousResultAction_tooltip;
	public static String RemoveMatchAction_label;
	public static String RemoveMatchAction_tooltip;
	public static String DefaultSearchViewPage_show_match;
	public static String DefaultSearchViewPage_error_no_editor;
	public static String AbstractTextSearchViewPage_flat_layout_label;
	public static String AbstractTextSearchViewPage_flat_layout_tooltip;
	public static String AbstractTextSearchViewPage_hierarchical_layout_label;
	public static String AbstractTextSearchViewPage_hierarchical_layout_tooltip;
	public static String CancelSearchAction_label;
	public static String CancelSearchAction_tooltip;
	public static String AbstractTextSearchViewPage_searching_label;
	public static String CollapseAllAction_0;
	public static String CollapseAllAction_1;
	public static String ExpandAllAction_label;
	public static String ExpandAllAction_tooltip;
	public static String SearchView_error_noResultPage;
	public static String InternalSearchUI_error_unexpected;
	public static String NewSearchUI_error_title;
	public static String NewSearchUI_error_label;
	public static String AnnotationHighlighter_error_badLocation;
	public static String AnnotationHighlighter_error_noDocument;
	public static String EditorAccessHighlighter_error_badLocation;

	static {
		NLS.initializeMessages(BUNDLE_NAME, SearchMessages.class);
	}
}
