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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;

/**
 * This class wraps a MetadataExtracter so that any metadata retrieved can be modified
 * before being returned to the component requesting the metadata.  This allows for
 * functions like data cleanup, removing of bad characters, and other stuff like this.
 * 
 * @author d3g574
 *
 * @version $Revision: 1.0 $
 */
public class WrappedMetadataExtracter implements MetadataExtracter {

	private MetadataExtracter metadataExtracter;
	private List<MetadataModifier> modifiers;
	
	// methods required for MetadataExtracter interface.  If they retrieve metadata, make the call
	// to the extracter, then call applyModifications.
	// if the call does not retrieve metadata, allow the call to proceed normally
	
	/**
	 * Constructor for WrappedMetadataExtracter.
	 * @param extracter MetadataExtracter
	 * @param modifiers List<MetadataModifier>
	 */
	public WrappedMetadataExtracter(MetadataExtracter extracter, List<MetadataModifier> modifiers) {
		this.metadataExtracter = extracter;
		this.modifiers = modifiers;
	}
	
	/**
	 * Method extract.
	 * @param reader ContentReader
	 * @param destination Map<QName,Serializable>
	 * @return Map<QName,Serializable>
	 * @see org.alfresco.repo.content.metadata.MetadataExtracter#extract(ContentReader, Map<QName,Serializable>)
	 */
	public Map<QName, Serializable> extract(ContentReader reader,
			Map<QName, Serializable> destination) {
		Map<QName, Serializable> changedMetadata = metadataExtracter.extract(reader, destination);
		return applyModifications(changedMetadata, destination);
	}

	/**
	 * Method extract.
	 * @param reader ContentReader
	 * @param overwritePolicy OverwritePolicy
	 * @param destination Map<QName,Serializable>
	 * @return Map<QName,Serializable>
	 * @see org.alfresco.repo.content.metadata.MetadataExtracter#extract(ContentReader, OverwritePolicy, Map<QName,Serializable>)
	 */
	public Map<QName, Serializable> extract(ContentReader reader,
			OverwritePolicy overwritePolicy, Map<QName, Serializable> destination) {
		Map<QName, Serializable> changedMetadata = metadataExtracter.extract(reader, overwritePolicy, destination);
		return applyModifications(changedMetadata, destination);
	}

	/**
	 * Method extract.
	 * @param reader ContentReader
	 * @param overwritePolicy OverwritePolicy
	 * @param destination Map<QName,Serializable>
	 * @param mapping Map<String,Set<QName>>
	 * @return Map<QName,Serializable>
	 * @see org.alfresco.repo.content.metadata.MetadataExtracter#extract(ContentReader, OverwritePolicy, Map<QName,Serializable>, Map<String,Set<QName>>)
	 */
	public Map<QName, Serializable> extract(ContentReader reader,
			OverwritePolicy overwritePolicy, Map<QName, Serializable> destination,
			Map<String, Set<QName>> mapping) {
		Map<QName, Serializable> changedMetadata = metadataExtracter.extract(reader, overwritePolicy, destination, mapping);
		return applyModifications(changedMetadata, destination);
	}

	/**
	 * Method getExtractionTime.
	 * @return long
	 * @see org.alfresco.repo.content.metadata.MetadataExtracter#getExtractionTime()
	 */
	public long getExtractionTime() {
		return metadataExtracter.getExtractionTime();
	}

	/**
	 * Method getReliability.
	 * @param mimetype String
	 * @return double
	 * @see org.alfresco.repo.content.metadata.MetadataExtracter#getReliability(String)
	 */
	public double getReliability(String mimetype) {
		return metadataExtracter.getReliability(mimetype);
	}

	/**
	 * Method isSupported.
	 * @param mimetype String
	 * @return boolean
	 * @see org.alfresco.repo.content.metadata.MetadataExtracter#isSupported(String)
	 */
	public boolean isSupported(String mimetype) {
		return metadataExtracter.isSupported(mimetype);
	}
	
	// given the metadata, apply each of the registered modifiers
	/**
	 * Method applyModifications.
	 * @param modifiedMetadata Map<QName,Serializable>
	 * @param destinationMetadata Map<QName,Serializable>
	 * @return Map<QName,Serializable>
	 */
	private Map<QName, Serializable> applyModifications(Map<QName, Serializable> modifiedMetadata, Map<QName, Serializable> destinationMetadata) {
		// modified metadata is a map of all properties that were just changed by the extracter we are wrapping
		// so pass this Map to the registered metadata modifiers to apply any changes
		for (MetadataModifier modifier : modifiers) {
			modifiedMetadata = modifier.modifyMetadata(modifiedMetadata);
		}
		// make sure the destination map also has all of the modifications we just made
		for (QName qname : modifiedMetadata.keySet()) {
			destinationMetadata.put(qname, modifiedMetadata.get(qname));
		}
		return modifiedMetadata;
	}

}
