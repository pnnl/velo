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
public class CatViewIDs {

  //People views
  //public static final String PEOPLE_NAVIGATOR =  "gov.pnnl.cue.people.navigator";
  //public static final String PEOPLE_SEARCH = "gov.pnnl.cue.people.search";
    
  public static final String SEARCH_RESULTS = "gov.pnnl.cat.search.ui.SearchResultsView";
  public static final String SEARCH = "gov.pnnl.cat.search.ui.CatSearchView";
  //public static final String PREVIEW = "gov.pnnl.cat.ui.views.preview";
  //public static final String EXPLORER_DETAILS = "gov.pnnl.cat.ui.rcp.views.explorerdetails";
  public static final String TAXONOMY_MANAGER_DATA_SOURCES = "gov.pnnl.cat.ui.rcp.views.advancedrepositoryexplorer";
  public static final String TAXONOMY_MANAGER_TAXONOMIES = "gov.pnnl.cat.ui.rcp.views.advancedrepositoryexplorer.taxonomies";
  
//  public static final String NEW_WINDOW_FOR_DETAIL_EXPLORER = "gov.pnnl.cat.ui.rcp.views.detailedrepositoryexplorer";
  //data explorer perspective specific views:
  public static final String DATA_INSPECTOR = "gov.pnnl.cat.ui.rcp.views.AdminDataBrowser";
  public static final String DATA_INSPECTOR_TAXONOMIES = "gov.pnnl.cat.ui.rcp.views.detailedrepositoryexplorer.taxonomies";
  public static final String DATA_INSPECTOR_PROJECTS = "gov.pnnl.cat.ui.rcp.views.detailedrepositoryexplorer.projects";
  
  //these 2 views are shared between both the data explorer and taxonomy manager perspectives
  public static final String PERSONAL_LIBRARY_VIEW = "gov.pnnl.cat.ui.rcp.views.detailedrepositoryexplorer.personal";
  public static final String FAVORITES_VIEW = "gov.pnnl.cat.ui.rcp.views.advancedrepositoryexplorer.favorites";
  
  // General Views
  public static final String PROGRESS_MONITOR_VIEW = "org.eclipse.ui.views.ProgressView";
  
  //users perspective views
  public static final String USERS = "gov.pnnl.cat.ui.rcp.views.users";
  public static final String USER_DETAILS = "gov.pnnl.cat.ui.rcp.views.users.details";
  
  //teams perspective views
  public static final String TEAM = "gov.pnnl.cat.ui.rcp.views.team";
  public static final String TEAM_DETAILS = "gov.pnnl.cat.ui.rcp.views.team.details";
  
  //Preview Pane
  public static final String PREVIEW = "gov.pnnl.cat.ui.rcp.preview";
  
}
