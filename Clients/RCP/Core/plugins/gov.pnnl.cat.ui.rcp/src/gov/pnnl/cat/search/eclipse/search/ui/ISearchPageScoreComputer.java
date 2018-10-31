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

/**
 * Computes a score that is used by the search dialog
 * to find the best fitting page for a selection when opened.
 * The score has no upper limit but must be at least
 * <code>LOWEST</code>. Higher values means the page is better
 * suited for the given selection input.
 * <p>
 * For example, a Java-specific search page score computer could test
 * if the page is a Java search page and returns high scores
 * for Java elements as selection input.
 * 
 * Intended to be implemented.
 * </p>
 * @version $Revision: 1.0 $
 */
public interface ISearchPageScoreComputer {

	/**
	 * Invalid score value indicating a score is unknown or undecided.
	 */
	public static final int UNKNOWN= -1;

	/**
	 * Lowest possible valid score.
	 */
	public static final int LOWEST= 0;

	/**
	 * Computes and returns a score indicating how good the page with the given
	 * id can handle the given input element.
	 * The search page id appears as the <code>id</code> attribute of the
	 * <code>&lt;page&gt;</code> element contributed to the
	 * search pages extension point (<code>"org.eclipse.search.searchPages"</code>).
	 *
	 * @param	pageId	the string id of the page for which the score is computed
	 * @param	input		the object based on which the page should open
	
	 * @return	a score higher or equal to <code>LOWEST</code>, or
	 *		<code>UNKNOWN</code> if this computer cannot decide */
	public int computeScore(String pageId, Object input);
}
