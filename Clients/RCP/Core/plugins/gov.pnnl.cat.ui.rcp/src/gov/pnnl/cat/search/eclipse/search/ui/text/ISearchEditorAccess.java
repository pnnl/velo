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
package gov.pnnl.cat.search.eclipse.search.ui.text;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * <p>This interface allows editors to provide customized access to editor internals for the 
 * search implementation to highlight matches. The search system will use the document to 
 * do line/character offset conversion if needed and it will add annotations to the annotation 
 * model.</p>
 * <p> The search system will ask an editor for an adapter of this class whenever it needs 
 * access to the document or the annotation model of the editor. Since an editor might use 
 * multiple documents and/or annotation models, the match is passed in when asking the editor. 
 * The editor is then expected to return the proper annotation model or document for the given 
 * match.</p>
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * @since 3.0
 * @version $Revision: 1.0 $
 */
public interface ISearchEditorAccess {
	/**
	 * Finds the document displaying the match.
	 * @param match the match
	
	 * @return the document displaying the given match. */
	IDocument getDocument(Match match);
	/**
	 * Finds the annotation model for the given match
	 * @param match the match
	
	 * @return the annotation model displaying the given match. */
	IAnnotationModel getAnnotationModel(Match match);
}
