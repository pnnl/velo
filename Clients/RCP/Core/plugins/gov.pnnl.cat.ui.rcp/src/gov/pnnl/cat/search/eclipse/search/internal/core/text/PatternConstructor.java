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
package gov.pnnl.cat.search.eclipse.search.internal.core.text;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @version $Revision: 1.0 $
 */
public class PatternConstructor {
	

	private PatternConstructor() {
		// don't instanciate
	}

	/**
	 * Creates a pattern element from the pattern string which is either a reg-ex expression or in our old
	 * 'StringMatcher' format.
	 * @param pattern The search pattern
	 * @param isCaseSensitive Set to <code>true</code> to create a case insensitve pattern
	 * @param isRegexSearch <code>true</code> if the passed string already is a reg-ex pattern
	
	
	 * @return The created pattern * @throws PatternSyntaxException */
	public static Pattern createPattern(String pattern, boolean isCaseSensitive, boolean isRegexSearch) throws PatternSyntaxException {
		if (!isRegexSearch)
			pattern= asRegEx(pattern);
		
		if (!isCaseSensitive)
			return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
		
		return Pattern.compile(pattern, Pattern.MULTILINE);
	}
	
	/*
	 * Converts '*' and '?' to regEx variables.
	 */
	/**
	 * Method asRegEx.
	 * @param pattern String
	 * @return String
	 */
	private static String asRegEx(String pattern) {
		
		StringBuffer out= new StringBuffer(pattern.length());
		
		boolean escaped= false;
		boolean quoting= false;
		
		int i= 0;
		while (i < pattern.length()) {
			char ch= pattern.charAt(i++);
			
			if (ch == '*' && !escaped) {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append(".*"); //$NON-NLS-1$
				escaped= false;
				continue;
			} else if (ch == '?' && !escaped) {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("."); //$NON-NLS-1$
				escaped= false;
				continue;
			} else if (ch == '\\' && !escaped) {
				escaped= true;
				continue;								
				
			} else if (ch == '\\' && escaped) {
				escaped= false;
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("\\\\"); //$NON-NLS-1$
				continue;								
			}
			
			if (!quoting) {
				out.append("\\Q"); //$NON-NLS-1$
				quoting= true;
			}
			if (escaped && ch != '*' && ch != '?' && ch != '\\')
				out.append('\\');
			out.append(ch);
			escaped= ch == '\\';
			
		}
		if (quoting)
			out.append("\\E"); //$NON-NLS-1$
		
		return out.toString();
	}

}
