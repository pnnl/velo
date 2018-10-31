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
package gov.pnnl.cat.imprt;

import gov.pnnl.cat.actions.UrlImportAction;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.XmlUtility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class ImportServiceImpl implements ImportService {

  private NodeService nodeService;
  private SearchService searchService;
  private NamespaceService namespaceService;
  private NodeService publicNodeService;
  private TransactionService transactionService;
  private String[] localUrlBaseList;

  /**
   * Set the node service
   * 
   * @param nodeService  set the node service
   */
  public void setNodeService(NodeService nodeService) 
  {
    this.nodeService = nodeService;
  }

  /**
   * Method setPublicNodeService.
   * @param nodeService NodeService
   */
  public void setPublicNodeService(NodeService nodeService) {
    this.publicNodeService = nodeService;
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
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Method setLocalUrlBaseList.
   * @param list String[]
   */
  public void setLocalUrlBaseList(String[] list) {
    logger.debug("setLocalUrlBaseList: " + list);
    this.localUrlBaseList = list;
    for (int i = 0; i < localUrlBaseList.length; i++) {
      logger.debug("localUrlBaseList[" + i +"] = " + localUrlBaseList[i]);
    }
  }

  private static final Log logger = LogFactory.getLog(UrlImportAction.class);


  /**
   * Method importUrlListAsXml.
   * @param xmlFileList String
   * @param targetFolder NodeRef
   * @see gov.pnnl.cat.imprt.ImportService#importUrlListAsXml(String, NodeRef)
   */
  public void importUrlListAsXml(String xmlFileList, NodeRef targetFolder) {

    List urlList = XmlUtility.deserialize(xmlFileList);

    // Look up the files in one tx
    List<NodeRef> filesToLink = getFilesToLink(urlList);

    // Create the links, one per tx
    for(NodeRef targetNode : filesToLink) {
      addFolderLink(targetFolder, targetNode);	
    }
    logger.debug("--- end");	  
  }

  /**
   * Method addFolderLink.
   * @param folderRef NodeRef
   * @param fileRef NodeRef
   */
  protected void addFolderLink(final NodeRef folderRef, final NodeRef fileRef) {
    long begin = System.currentTimeMillis();

    //		RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
    //		{
    //		public Object execute() throws Throwable 
    //		{
    try {
      if(logger.isDebugEnabled()) {
        String name = (String)nodeService.getProperty(fileRef, ContentModel.PROP_NAME);     
        String folderName = (String)nodeService.getProperty(folderRef, ContentModel.PROP_NAME);
        logger.debug("Adding link for: " + name + " to folder: " + folderName);
      }

      // First see if a link of same name already exists:
      String name = (String)nodeService.getProperty(fileRef, ContentModel.PROP_NAME);
      NodeRef linkNode = nodeService.getChildByName(folderRef, ContentModel.ASSOC_CONTAINS, name);
      if(linkNode == null) {
        logger.debug("trying to create link");
        NodeUtils.createLinkedFile(fileRef, folderRef, nodeService);

      } else {
        logger.debug("Link with same name (" + name + ") already exists in folder.");
      }
    } catch (Throwable t) {
      logger.error("Exception",t);
    }

    //		return null;
    //		}
    //		};
    //		transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);

    long end = System.currentTimeMillis();
    logger.debug("time to add link in tx =  " + (end - begin));
  }

  /**
   * Method getFilesToLink.
   * @param urlList List
   * @return List<NodeRef>
   */
  @SuppressWarnings("unchecked")
  private List<NodeRef> getFilesToLink(final List urlList) {

    //		RetryingTransactionCallback<List<NodeRef>> cb = new RetryingTransactionCallback<List<NodeRef>>()
    //		{
    //		public List<NodeRef> execute() throws Throwable 
    //		{
    List<NodeRef> filesToLink = new ArrayList<NodeRef>();

    // convert URLs to NodeRefs
    for (Iterator i = urlList.iterator(); i.hasNext(); ) {
      String urlstring = (String)i.next();
      logger.debug("Processing " + urlstring);

      if (!isExternalUrl(urlstring.toLowerCase())) {

        // this is a url internal to Alfresco
        NodeRef targetNode = getNodeRefFromUrl(urlstring);
        if(targetNode != null) {
          filesToLink.add(targetNode);
        }
      }
    }
    return filesToLink;
    //		}
    //		};
    //		return transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);

  }

  /**
   * Method isExternalUrl.
   * @param url String
   * @return boolean
   */
  private boolean isExternalUrl(String url) {
    // return true or false if the provided URL is internal or external to this server
    // the server is external if it does not start with one of the provided url prefixes
    return (getMatchingBaseUrl(url) == null);
  }

  /**
   * Method getMatchingBaseUrl.
   * @param url String
   * @return String
   */
  private String getMatchingBaseUrl(String url) {
    // return a matching base url, or null if this doesn't match any
    // if null is returned, it can be assumed this url is an external url
    // if a string is returned, it can be assumed this url is an internal url
    //for (Iterator i = localUrlBaseList.iterator(); i.hasNext(); ) {
    for (int i = 0; i < localUrlBaseList.length; i++) {
      //			String urlBase = (String)i.next();
      String urlBase = localUrlBaseList[i].toLowerCase();
      if (url.startsWith(urlBase)) {
        return urlBase;
      }
    }
    return null;
  }

  /**
   * Method getNodeRefFromUrl.
   * @param url String
   * @return NodeRef
   */
  private NodeRef getNodeRefFromUrl(String url) {
    // many thanks to the BaseDownloadContentServlet :)
    String ARG_PROPERTY = "property";
    String ARG_ATTACH   = "attach";
    String ARG_PATH     = "path";

    url = url.toLowerCase();
    String matchingBaseUrl = getMatchingBaseUrl(url).toLowerCase();
    if (matchingBaseUrl != null) {
      url = url.substring(url.indexOf(matchingBaseUrl) + matchingBaseUrl.length());
    }
    StringTokenizer t = new StringTokenizer(url, "/");
    int tokenCount = t.countTokens();

    String servlet = t.nextToken();    // skip servlet name

    if (servlet.equals("catxml")) {
      // parse the special catxml servlet syntax
      // catxml/single/workspace/SpacesStore/cc1f82e0-397b-4377-98fe-c5119c3a23ff

      // ignore the single/recurse token
      t.nextToken();

      // get or calculate the noderef and filename to download as
      NodeRef nodeRef = null;
      String filename;

      if (tokenCount < 5)
      {
        throw new IllegalArgumentException("Download URL did not contain all required args: " + url); 
      }

      // assume 'workspace' or other NodeRef based protocol for remaining URL elements
      StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
      String id = t.nextToken();
      if (id.indexOf('?') >= 0) {
        // strip off the querystring
        id = id.substring(0, id.indexOf('?'));
      }
      // build noderef from the appropriate URL elements
      nodeRef = new NodeRef(storeRef, id);  // check for failure
      if (!nodeService.exists(nodeRef)) {
        return null;
      }
      return nodeRef;
      
      
    } else {
      // attachment mode (either 'attach' or 'direct')
      String attachToken = t.nextToken();
      boolean attachment = attachToken.equals(ARG_ATTACH);

      // get or calculate the noderef and filename to download as
      NodeRef nodeRef = null;
      String filename;

      if (tokenCount < 6)
      {
        throw new IllegalArgumentException("Download URL did not contain all required args: " + url); 
      }

      // assume 'workspace' or other NodeRef based protocol for remaining URL elements
      StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
      String id = t.nextToken();
      // build noderef from the appropriate URL elements
      nodeRef = new NodeRef(storeRef, id);  // check for failure
      if (!nodeService.exists(nodeRef)) {
        return null;
      }

      //if (nodeService.getType(nodeRef).equals(CatConstants.TYPE_TRANSFORM)) {
      //	// this is a text transform node.  get the real content node
      //	nodeRef = TransformUtils.getContentNodeFromTransformNode(nodeRef, nodeService);
      //}

      // filename is last remaining token.  not needed for this method
      filename = t.nextToken();

      return nodeRef;
    }
  }



}
