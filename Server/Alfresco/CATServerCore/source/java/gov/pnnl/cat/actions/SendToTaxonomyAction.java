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
package gov.pnnl.cat.actions;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Pass in a list of node references (in string format)
 * to possibly send to a taxonomy.  If this list is not set,
 * then use the actioned upon node ref.  This is so we can call this
 * method for a large selection from CAT or also on a single node via a rule
 * trigger.
 *
 * For each folder in the taxonomy, do a keyword search on the folder name
 * against the text content of each document.  If the name is found in the text,
 * then add that document to the corresponding taxonomy folder.
 * 
 * TODO: this implementation is not very efficient, so may need to refactor later
 * @version $Revision: 1.0 $
 */
public class SendToTaxonomyAction extends ActionExecuterAbstractBase {

  private static final String PARAM_REFERENCE_LIST = "reference-list";
  private static final String DISPLAY_LABEL_REFERENCE_LIST = "Semicolon-separated list of node refs to send";
  
  private static final String PARAM_TAXONOMY_REF = "taxonomy-ref";
  private static final String DISPLAY_LABEL_TAXONOMY_REF = "Destination taxonomy folder ref";
  
 
  private static final Log logger = LogFactory.getLog(SendToTaxonomyAction.class);
  private NodeService nodeService;
  private SearchService searchService;
  private TransactionService transactionService;

  /**
   * Set the node service
   * 
   * @param nodeService  set the node service
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
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }


  /**
   * Define the parameters that can be passed into this action
   * reference-list example: workspace://SpacesStore/0311e735-9601-11db-a9ef-e33f3c65b6b7;workspace://SpacesStore/0311e735-9601-11db-a9ef-e33f3c65b6b7
   * taxonomy-ref example: workspace://SpacesStore/0311e735-9601-11db-a9ef-e33f3c65b6b7
   * @param paramList List<ParameterDefinition>
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    logger.debug("setting parameter list");
    
    ParameterDefinitionImpl nodeRefList = new ParameterDefinitionImpl(
        PARAM_REFERENCE_LIST, // parameter name
        DataTypeDefinition.TEXT, // parameter data type
        false, // not a mandatory parameter
        DISPLAY_LABEL_REFERENCE_LIST); // parameter display label

    ParameterDefinitionImpl taxonomyRef = new ParameterDefinitionImpl(
        PARAM_TAXONOMY_REF, // parameter name
        DataTypeDefinition.TEXT, // parameter data type
        true, // mandatory parameter
        DISPLAY_LABEL_TAXONOMY_REF); // parameter display label
   
    paramList.add(nodeRefList);
    paramList.add(taxonomyRef);
  }

  /**
   * Method executeImpl.
   * @param ruleAction Action
   * @param nodeActedUpon NodeRef
   */
  @Override
  protected void executeImpl(Action ruleAction, NodeRef nodeActedUpon) {
    logger.debug("Executing action");
    
    // Get the taxonomy to upload to
   NodeRef taxonomyRoot = getTaxonomyRoot(ruleAction, nodeActedUpon);

    // Find the list of nodes to run on
    String nodeRefsStr = (String)ruleAction.getParameterValue(PARAM_REFERENCE_LIST);
    List<NodeRef> nodeRefs = new ArrayList<NodeRef>();   
    
    if(nodeRefsStr != null) {
      
      StringTokenizer tokens = new StringTokenizer(nodeRefsStr, ";", false);
      while (tokens.hasMoreTokens()) {
        String reference = tokens.nextToken().trim();
        if(!reference.equals("")) {
          nodeRefs.add(new NodeRef(reference));
        }
      }
    }
    
    if (nodeRefs.size() == 0) {
      nodeRefs.add(nodeActedUpon);
    }
    logger.debug("Trying to import "+nodeRefs.size()+" selected file(s)/folder(s)");
      
    // Get all of the folders in the taxonomy
    List<NodeRef>folders = getTaxonomyFolders(taxonomyRoot);
    logger.debug("number of folders in taxonomy = " + folders.size());

    // Get all of the selected files to import
    List<NodeRef>allFiles = getExpandedFileSet(nodeRefs);
    logger.debug("total number of files to import = " + allFiles.size());
    
    // For each taxonomy folder, see which files should get added
    List<LinkInfo> linksToAdd = new ArrayList<LinkInfo>();
    for(NodeRef folder : folders) {
        checkMembership(folder, allFiles, linksToAdd);
    }
    
    // Now add each link in its own writeable transaction
    // NOTE: The writable tx MUST go AFTER the read-only tx, because they
    // seem to mess up the indexes for the other tx, so that searches fail
    for(LinkInfo info : linksToAdd) {
      addFolderLink(info.getTaxonomyFolder(), info.getOriginalFile());
    }
    
  }
  
  /**
   * Method getTaxonomyRoot.
   * @param ruleAction Action
   * @param nodeActedUpon NodeRef
   * @return NodeRef
   */
  protected NodeRef getTaxonomyRoot(final Action ruleAction, final NodeRef nodeActedUpon) {

    RetryingTransactionCallback<NodeRef> cb = new RetryingTransactionCallback<NodeRef>()
    {
      public NodeRef execute() throws Throwable {
        // Get the taxonomy to upload to
        String taxonomyRef = (String)ruleAction.getParameterValue(PARAM_TAXONOMY_REF);
        NodeRef taxonomyFolder = new NodeRef(taxonomyRef);
        if(!nodeService.hasAspect(taxonomyFolder, CatConstants.ASPECT_TAXONOMY_ROOT)) {
          throw new AlfrescoRuntimeException("Taxonomy parameter must be a taxonomy root!");
        }    
        return taxonomyFolder;
      }
    };
    return transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);
    
  } 

  /**
   * Method getTaxonomyFolders.
   * @param taxonomyRoot NodeRef
   * @return List<NodeRef>
   */
  @SuppressWarnings("unchecked")
  protected List<NodeRef> getTaxonomyFolders(final NodeRef taxonomyRoot) {
    
    RetryingTransactionCallback<List<NodeRef>> cb = new RetryingTransactionCallback<List<NodeRef>>()
    {
      public List<NodeRef> execute() throws Throwable 
      {
        List<NodeRef> folders = new ArrayList<NodeRef>();
        recursiveTaxonomyLookup(taxonomyRoot, folders);
        return folders;
      }
    };
    return transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);
     
  }
  
  /**
   * Method recursiveTaxonomyLookup.
   * @param taxonomyFolder NodeRef
   * @param folders List<NodeRef>
   */
  protected void recursiveTaxonomyLookup(NodeRef taxonomyFolder, List<NodeRef>folders) {
   
    if(nodeService.hasAspect(taxonomyFolder, CatConstants.ASPECT_TAXONOMY_FOLDER)) {
      folders.add(taxonomyFolder);
    }

    // Recurse through rest of the taxonomy
    List<ChildAssociationRef> children = nodeService.getChildAssocs(taxonomyFolder);
    for (ChildAssociationRef child : children) {
      NodeRef childRef = child.getChildRef();
      if(nodeService.hasAspect(childRef, CatConstants.ASPECT_TAXONOMY_FOLDER)) {
        recursiveTaxonomyLookup(childRef, folders);
      }
    }
  }
  
  /**
   * Method getExpandedFileSet.
   * @param nodeRefs List<NodeRef>
   * @return List<NodeRef>
   */
  @SuppressWarnings("unchecked")
  protected List<NodeRef> getExpandedFileSet(final List<NodeRef> nodeRefs) {
    
    RetryingTransactionCallback<List<NodeRef>> cb = new RetryingTransactionCallback<List<NodeRef>>()
    {
      public List<NodeRef> execute() throws Throwable 
      {
        List<NodeRef> allFiles = new ArrayList<NodeRef>();

        for(NodeRef nodeRef : nodeRefs) {
          recursiveFileLookup(nodeRef, allFiles);
        }
        return allFiles;       
      }
    };
    return transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);
    
  }  
  
  /**
   * Method recursiveFileLookup.
   * @param nodeRef NodeRef
   * @param allFiles List<NodeRef>
   */
  protected void recursiveFileLookup(NodeRef nodeRef, List<NodeRef> allFiles) {
  
    QName type = nodeService.getType(nodeRef);
    if (type.equals(ContentModel.TYPE_FOLDER)) {
      // recursively call on all it's children
      List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
      for (ChildAssociationRef child : children) {
        recursiveFileLookup(child.getChildRef(), allFiles);
      }

    } else if (type.equals(ContentModel.TYPE_CONTENT) || type.equals(ContentModel.TYPE_LINK)) {

      try {
        if(type.equals(ContentModel.TYPE_LINK)) {
          nodeRef = (NodeRef) nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);
        }
        allFiles.add(nodeRef);
   
      } catch (Exception e) {
        logger.error("Error looking up file: " + nodeService.getPath(nodeRef), e);
      }
    }    
  }
 
  /**
   * Method addFolderLink.
   * @param taxonomyFolder NodeRef
   * @param fileRef NodeRef
   */
  protected void addFolderLink(final NodeRef taxonomyFolder, final NodeRef fileRef) {
    long begin = System.currentTimeMillis();
    
    RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>()
    {
      public Object execute() throws Throwable {
        if(logger.isDebugEnabled()) {
          String name = (String)nodeService.getProperty(fileRef, ContentModel.PROP_NAME);     
          String folderName = (String)nodeService.getProperty(taxonomyFolder, ContentModel.PROP_NAME);
          logger.debug("Found hit for " + folderName + ".  Adding file: " + name + " to tax folder.");
        }
        
        // First see if a link of same name already exists:
        String name = (String)nodeService.getProperty(fileRef, ContentModel.PROP_NAME);
        
        // Note that link file names end with .url!
        // TODO: we need to make this a constant somewhere
        if(!name.endsWith(".url")) {
          name = name.concat(".url");
        }
        NodeRef linkNode = nodeService.getChildByName(taxonomyFolder, ContentModel.ASSOC_CONTAINS, name);
        
        if(linkNode == null) {
          logger.debug("trying to create link");
          NodeUtils.createLinkedFile(fileRef, taxonomyFolder, nodeService);
 
        } else {
          logger.debug("Link with same name (" + name + ") already exists in taxonomy folder.");
        }
        return null;
      }
    };
    transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);
    
    long end = System.currentTimeMillis();
    logger.debug("time to add link in tx =  " + (end - begin));
  }
  
  /**
   * Method query.
   * @param taxonomyFolder NodeRef
   * @param allFiles List<NodeRef>
   * @return List<NodeRef>
   */
  @SuppressWarnings("unchecked")
  protected List<NodeRef> query(final NodeRef taxonomyFolder, final List<NodeRef> allFiles) {

    RetryingTransactionCallback<List<NodeRef>> cb = new RetryingTransactionCallback<List<NodeRef>>()
    {
      public List<NodeRef> execute() throws Throwable 
      {
        // build the query
        StringBuffer query = new StringBuffer();  
        query.append("(");
        int count = 0;

        for(NodeRef fileRef : allFiles) {

          if(count > 0) {
            query.append(" OR ");
          }
          query.append("ID:\"");
          query.append(fileRef.toString());
          query.append("\"");
          count++;
        }
        query.append(") AND TEXT:(\"");
        String folderName = (String)nodeService.getProperty(taxonomyFolder, ContentModel.PROP_NAME);
        query.append(folderName);
        query.append("\")");

        if(logger.isDebugEnabled())
          logger.debug("query = " + query.toString());

        long start = System.currentTimeMillis();
        ResultSet results = null;
        try {
          results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());
          long end = System.currentTimeMillis();
          logger.debug("time to execute query = " + (end - start));

          return results.getNodeRefs();      
        } finally {
          if(results != null) {
            results.close();
          }
        }
      }
    };
    return transactionService.getRetryingTransactionHelper().doInTransaction(cb, true, true);
  
  }
  
  /**
   * Method checkMembership.
   * @param taxonomyFolder NodeRef
   * @param allFiles List<NodeRef>
   * @param linksToAdd List<LinkInfo>
   */
  protected void checkMembership(NodeRef taxonomyFolder, List<NodeRef> allFiles, List<LinkInfo>linksToAdd) {

    List<NodeRef> queryResults = query(taxonomyFolder, allFiles);
    
    for(NodeRef nodeRef : queryResults) {
      linksToAdd.add(new LinkInfo(taxonomyFolder, nodeRef));      
    }          
      
  }
  
  /**
   * holds info so we know which folder to create each link in
   * @version $Revision: 1.0 $
   */
  private class LinkInfo {
    
    private NodeRef taxonomyFolder;
    private NodeRef originalFile;
    
    /**
     * Constructor for LinkInfo.
     * @param taxonomyFolder NodeRef
     * @param originalFile NodeRef
     */
    public LinkInfo(NodeRef taxonomyFolder, NodeRef originalFile) {
      this.taxonomyFolder = taxonomyFolder;
      this.originalFile = originalFile;
    }
    
    /**
     * Method getOriginalFile.
     * @return NodeRef
     */
    public NodeRef getOriginalFile() {
      return originalFile;
    }
    /**
     * Method getTaxonomyFolder.
     * @return NodeRef
     */
    public NodeRef getTaxonomyFolder() {
      return taxonomyFolder;
    }
    
  }

}
