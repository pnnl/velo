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
package gov.pnnl.cat.harvester.engine;

import gov.pnnl.cat.harvester.HarvestRequest;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class HarvestEngineUtil {
	
	private static final Log logger = LogFactory.getLog(HarvestEngineUtil.class);


	/**
	 * Method substituteFieldValues.
	 * @param inputTemplate String
	 * @param request HarvestRequest
	 * @param prefixResolver NamespacePrefixResolver
	 * @param formatter PropertyValueFormatter
	 * @return String
	 */
	public static String substituteFieldValues(String inputTemplate, HarvestRequest request, NamespacePrefixResolver prefixResolver, PropertyValueFormatter formatter) {
		/**
		 * Locate all @ symbols in the inputTemplate
		 * For each @ find the next @ symbol
		 * The text in the middle will match a short name of a HarvestRequest parameter key
		 * extract the value, substitute it in the text
		 * repeat
		 * return
		 */

		Map<QName, Serializable> parameters = request.getParameters();
		String outputString = inputTemplate;
		
		int atSignLeftPosition = outputString.indexOf('@');
		while (atSignLeftPosition >= 0) {
			int atSignRightPosition = outputString.indexOf('@', atSignLeftPosition+1);
			String keyShortString = outputString.substring(atSignLeftPosition + 1, atSignRightPosition);
			String defaultValue = "";
			
			int slash = keyShortString.indexOf("/");
			if (slash >= 0) {
				defaultValue = keyShortString.substring(slash+1, keyShortString.length());
				keyShortString = keyShortString.substring(0, slash);
			}
			
			
			QName keyQName = QName.createQName(keyShortString, prefixResolver);
			
			Serializable value = parameters.get(keyQName);
			if (value != null) {
				value = formatter.formatPropertyValue(request, keyQName);
			} else {
				value = defaultValue;
			}
			outputString = outputString.substring(0, atSignLeftPosition) + value + outputString.substring(atSignRightPosition + 1);

			atSignLeftPosition = outputString.indexOf('@');
		}
		
		return outputString;
		
	}
	
	/**
	 * Take a multi-valued field, concatenate all values into a single string
	 * opposite of split function
	 * use separator as the separator between values
	
	 * @param separator
	
	 * @param value Serializable
	 * @return String
	 */
	public static String collapseMultiValue(Serializable value, String separator) {
		
		if (value == null) {
			// not defined.  return empty string;
			return "";
		}
		if (value instanceof String) {
			// just a plain string.  return as is
			return (String)value;
		}
		if ((value instanceof List) == false) {
			// wrong type.  convert to a string and return
			return value.toString();
		}
		
		// right type, go ahead and process
		List<String> values = (List<String>) value;
		
		StringBuffer result = new StringBuffer();
		for (int i = 0; i<values.size(); i++) {
			if (i>0) {
				result.append(separator);
			}
			result.append(values.get(i));
		}
		return result.toString();
	}
	
	/**
	 * Same as collapseMultiValueWithUrlEncoding(value, " ", false);
	 * @param value
	
	 * @return String
	 */
	public static String collapseMultiValueWithUrlEncoding(Serializable value) {
		return 	collapseMultiValueWithUrlEncoding(value, " ", false);
	}
	
	/**
	 * Collapse a multiple string value into one string, then UrlEncode the results
	 * @param value - expected to be List<String>
	 * @param separator - put in between the values.  Remember, this separator will be UrlEncoded too
	 * @param includeQuotes - put quote marks around each value before concatenating
	
	 * @return String
	 */
	public static String collapseMultiValueWithUrlEncoding(Serializable value, String separator, boolean includeQuotes) {
		
		if (value == null) {
			// not defined.  return empty string;
			return "";
		}
		if (value instanceof String) {
			// just a plain string.  return as is
			return urlEncode((String)value);
		}
		if ((value instanceof List) == false) {
			// wrong type.  convert to a string and return
			return urlEncode(value.toString());
		}
		
		// right type, go ahead and process
		List<String> values = (List<String>) value;
		
		StringBuffer result = new StringBuffer();

		// skip any null or empty values
		// concatenate all of the restS
		boolean firstEntryFound = false;
		for (int i = 0; i<values.size(); i++) {
			String avalue = values.get(i);
			if (value != null && avalue.trim().equals("")==false) {
				if (firstEntryFound) {
					result.append(separator);
				}
				firstEntryFound = true;
				if (includeQuotes) {
					// enclose each value in quote marks
					result.append('\"').append(avalue).append('\"');
				} else {
					result.append(values.get(i));
				}
			}
		}
		return urlEncode(result.toString());
	}
	
	/**
	 * Take a string of multi words, concatenate all words into a single word using some kind of delimeter
	 * use separator as the separator between words
	
	 * @param separator
	
	 * @param value Serializable
	 * @return String
	 */
	public static String collapseMultiWordString(Serializable value, String separator) {
		
		if (value == null) {
			// not defined.  return empty string;
			return "";
		}
		if ((value instanceof String) == false) {
			return value.toString();
		}
		
		// right type, go ahead and process
		String stringValue = (String)value;
		String[] values = stringValue.split("\\s+");
		
		StringBuffer result = new StringBuffer();
		for (int i = 0; i<values.length; i++) {
			if (i>0) {
				result.append(separator);
			}
			result.append(values[i]);
		}
		return result.toString();
	}

	/**
	 * Method urlEncode.
	 * @param s String
	 * @return String
	 */
	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(s);
		}
	}
	
}
