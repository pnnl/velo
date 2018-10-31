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
package gov.pnnl.cat.search.eclipse.search.ui;

import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Constants for menu groups used in context menus for Search views and editors.
 * <p>
 * This interface declares constants only; it is not intended to be implemented.
 * </p>
 * 
 * @since 2.0
 * @version $Revision: 1.0 $
 */
public interface IContextMenuConstants {

	/**
	 * Pop-up menu: name of group for goto actions (value <code>"group.open"</code>).
	 * <p>
	 * Examples for open actions are:
	 * <ul>
	 *  <li>Go Into</li>
	 *  <li>Go To</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_GOTO=		"group.goto"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for open actions (value <code>"group.open"</code>).
	 * <p>
	 * Examples for open actions are:
	 * <ul>
	 *  <li>Open To</li>
	 *  <li>Open With</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_OPEN=		"group.open"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: name of group for show actions (value <code>"group.show"</code>).
	 * <p>
	 * Examples for show actions are:
	 * <ul>
	 *  <li>Show in Navigator</li>
	 *  <li>Show in Type Hierarchy</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_SHOW= "group.show"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: name of group for new actions (value <code>"group.new"</code>).
	 * <p>
	 * Examples for new actions are:
	 * <ul>
	 *  <li>Create new class</li>
	 *  <li>Create new interface</li>
	 * </ul>
	 * </p>
	 */
	public static final String GROUP_NEW= "group.new"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for build actions (value <code>"group.build"</code>).
	 */
	public static final String GROUP_BUILD= "group.build"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: name of group for reorganize actions (value <code>"group.reorganize"</code>).
	 */	
	public static final String GROUP_REORGANIZE= IWorkbenchActionConstants.GROUP_REORGANIZE;	
	
	/**
	 * Pop-up menu: name of group for code generation or refactoring actions (
	 * value <code>"group.generate"</code>).
	 */	
	public static final String GROUP_GENERATE= "group.generate"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for search actions (value <code>"group.search"</code>).
	 */	
	public static final String GROUP_SEARCH= "group.search"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: name of group for additional actions (value <code>"group.additions"</code>).
	 */	
	public static final String GROUP_ADDITIONS= "additions"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for viewer setup actions (value <code>"group.viewerSetup"</code>).
	 */	
	public static final String GROUP_VIEWER_SETUP= "group.viewerSetup"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for properties actions (value <code>"group.properties"</code>).
	 */	
	public static final String GROUP_PROPERTIES= "group.properties"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for remove match actions (value <code>"group.removeMatches"</code>).
	 * @since 2.1
	 */	
	public static final String GROUP_REMOVE_MATCHES= "group.removeMatches"; //$NON-NLS-1$
}
