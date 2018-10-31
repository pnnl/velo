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
package gov.pnnl.cat.intercept.extracter;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * A simple interface defining how metadata can be modified between the
 * metadata extracter and the alfresco module requesting the metadata
 * @author d3g574
 *
 * @version $Revision: 1.0 $
 */
public interface MetadataModifier {

	
	/**
	 * Method modifyMetadata.
	 * @param metadata Map<QName,Serializable>
	 * @return Map<QName,Serializable>
	 */
	public Map<QName, Serializable> modifyMetadata(Map<QName, Serializable> metadata);
}
