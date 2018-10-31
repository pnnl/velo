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
package gov.pnnl.cat.server.webservice.imprt;

import gov.pnnl.cat.imprt.ImportService;

import java.rmi.RemoteException;

import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class ImportWebService extends AbstractWebService implements ImportServiceSoapPort {

	private static Log logger = LogFactory.getLog(ImportWebService.class); 

	private ImportService importService;
	
	private NodeService nodeService;
	private SearchService searchService;
	private NamespaceService namespaceService;

	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
	  this.nodeService = nodeService;
	}

	/**
	 * Method setSearchService.
	 * @param searchService SearchService
	 */
	public void setSearchService(SearchService searchService) {
	  this.searchService = searchService;
	}

	/**
	 * Method setNamespaceService.
	 * @param namespaceService NamespaceService
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
	  this.namespaceService = namespaceService;
	}

	/**
	 * Method setImportService.
	 * @param importService ImportService
	 */
	public void setImportService(ImportService importService) {
	  this.importService = importService;
	}

	/**
	 * Method urlImportAction.
	 * @param xml String
	 * @param target Reference
	 * @throws RemoteException
	 * @throws ImportFault
	 * @see gov.pnnl.cat.server.webservice.imprt.ImportServiceSoapPort#urlImportAction(String, Reference)
	 */
	public void urlImportAction(String xml, Reference target) throws RemoteException, ImportFault {
		try {
	      NodeRef folderNodeRefs = Utils.convertToNodeRef(target, this.nodeService, this.searchService, this.namespaceService);

		  importService.importUrlListAsXml(xml, folderNodeRefs);

		} catch (Exception e) {
			logger.error("Import Failed", e);
			throw new ImportFault(0, e.getMessage());
		}
    }

	/**
	 * Method testMethod.
	 * @param xml String
	 * @param target Reference
	 * @throws RemoteException
	 * @throws ImportFault
	 * @see gov.pnnl.cat.server.webservice.imprt.ImportServiceSoapPort#testMethod(String, Reference)
	 */
	public void testMethod(String xml, Reference target) throws RemoteException, ImportFault {

	  
	}


 

}
