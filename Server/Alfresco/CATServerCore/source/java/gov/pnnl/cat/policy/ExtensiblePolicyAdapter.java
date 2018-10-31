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
/**
 * Notice: This computer software was prepared by Battelle Memorial Institute,
 * hereinafter the Contractor for the Department of Homeland Security under the
 * terms and conditions of the U.S. Department of Energy's Operating Contract
 * DE-AC06-76RLO with Battelle Memorial Institute, Pacific Northwest Division.
 * All rights in the computer software are reserved by DOE on behalf of the
 * United States Government and the Contractor as provided in the Contract. You
 * are authorized to use this computer software for Governmental purposes but it
 * is not to be released or distributed to the public. NEITHER THE GOVERNMENT
 * NOR THE CONTRACTOR MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
 * LIABILITY FOR THE USE OF THIS SOFTWARE. This notice including this sentence
 * must appear on any copies of this computer software.
 */
package gov.pnnl.cat.policy;

import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.PrioritizedThreadPoolExecutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.node.integrity.IntegrityRecord;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * All custom policy should probably extend this class.
 * Contains common service initialization so we can easily use 
 * them in the various custom policy classes.
 *
 * @version $Revision: 1.0 $
 */
public abstract class ExtensiblePolicyAdapter implements ExtensiblePolicyInterface
{ 
  /** The Dictionary Service */
  protected DictionaryService dictionaryService;
	
  /** The policy component */
  protected PolicyComponent policyComponent;
  
  /** The node service */
  protected NodeService nodeService;
  
  /** The copy service */
  protected CopyService copyService;
  
  /** The category service */
  protected CategoryService categoryService;
  
  /** For getting the current user */
  protected AuthenticationComponent authenticationComponent;
  
  /** For getting user properties like home folder */
  protected PersonService personService;
  
  /** The search service */
  protected SearchService searchService;
  
  /** PermissionService bean reference */
  protected PermissionService permissionService;

  /** OwnableService bean reference */
  protected OwnableService ownableService;
  
  /** AuthorityService bean reference */
  protected AuthorityService authorityService;
  
  /** Asynchronous Thread Pools */
  protected PrioritizedThreadPoolExecutor lowPriorityThreadPool;
  protected PrioritizedThreadPoolExecutor mediumPriorityThreadPool;
  protected PrioritizedThreadPoolExecutor highPriorityThreadPool;
 
  /** Node Utils */
  protected NodeUtils nodeUtils;
  
  /** Namespace bean reference */
  protected NamespacePrefixResolver namespacePrefixResolver;
  protected NamespaceService namespaceService;
  
  protected RuleService ruleService;
  
  protected ContentService contentService;
  
  protected ActionService actionService;

  protected TransactionService transactionService;
  
  protected VersionService versionService;
  
  /**
   * Init method called by Spring container
   *
   */
  public abstract void init();
  
  /**
   * Method setActionService.
   * @param actionService ActionService
   */
  public void setActionService(ActionService actionService) {
    this.actionService = actionService;
  }

  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }
 
  /**
   * Method setHighPriorityThreadPool.
   * @param highPriorityThreadPool PrioritizedThreadPoolExecutor
   */
  public void setHighPriorityThreadPool(PrioritizedThreadPoolExecutor highPriorityThreadPool) {
    this.highPriorityThreadPool = highPriorityThreadPool;
  }

  /**
   * Method setLowPriorityThreadPool.
   * @param lowPriorityThreadPool PrioritizedThreadPoolExecutor
   */
  public void setLowPriorityThreadPool(PrioritizedThreadPoolExecutor lowPriorityThreadPool) {
    this.lowPriorityThreadPool = lowPriorityThreadPool;
  }

  /**
   * Method setMediumPriorityThreadPool.
   * @param mediumPriorityThreadPool PrioritizedThreadPoolExecutor
   */
  public void setMediumPriorityThreadPool(PrioritizedThreadPoolExecutor mediumPriorityThreadPool) {
    this.mediumPriorityThreadPool = mediumPriorityThreadPool;
  }

  /**
   * Method setNamespaceService.
   * @param namespaceService NamespaceService
   */
  public void setNamespaceService(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  /**
   * Sets the policy component
   * 
   * @param policyComponent   the policy component
   */
  public void setPolicyComponent(PolicyComponent policyComponent)
  {
      this.policyComponent = policyComponent;
  }
  
  /**
   * Method setDictionaryService.
   * @param dictionaryService DictionaryService
   */
  public void setDictionaryService(DictionaryService dictionaryService) {
	  this.dictionaryService = dictionaryService;
  }
  
  /**
   * Method setRuleService.
   * @param ruleService RuleService
   */
  public void setRuleService(RuleService ruleService) {
    this.ruleService = ruleService;
  }
  
  /** 
   * Sets the node service 
   * 
   * @param nodeService   the node service
   */
  public void setNodeService(NodeService nodeService)
  {
      this.nodeService = nodeService;
  }
  
  /**
   * Method setCopyService.
   * @param copyService CopyService
   */
  public void setCopyService(CopyService copyService)
  {
      this.copyService = copyService;
  }
  
  /**
   * Method setNamespacePrefixResolver.
   * @param namespacePrefixResolver NamespacePrefixResolver
   */
  public void setNamespacePrefixResolver(
		NamespacePrefixResolver namespacePrefixResolver) {
	this.namespacePrefixResolver = namespacePrefixResolver;
}

/**
   * @param categoryService  The CategoryService to set.
   */
  public void setCategoryService(CategoryService categoryService)
  {
     this.categoryService = categoryService;
  }

  /**
   * 
   * @param authenticationComponent
   */
  public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
    this.authenticationComponent = authenticationComponent;
  }
  
  /**
   * 
   * @param personService
   */
  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  /**
   * Method setSearchService.
   * @param searchService SearchService
   */
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }
   
  /**
   * @param permissionService      The PermissionService to set.
   */
  public void setPermissionService(PermissionService permissionService)
  {
     this.permissionService = permissionService;
  }

  /**
   * @param ownableService         The ownableService to set.
   */
  public void setOwnableService(OwnableService ownableService)
  {
     this.ownableService = ownableService;
  }

  /**
   * Method setNodeUtils.
   * @param nodeUtils NodeUtils
   */
  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  /**
   * Method setAuthorityService.
   * @param authorityService AuthorityService
   */
  public void setAuthorityService(AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }
  
  /**
   * helper method to quickly create an integrity exception
   * @param errorMessage
  
   * @throws IntegrityException */
  public void throwIntegrityException(String errorMessage) throws IntegrityException {
    IntegrityRecord rec = new IntegrityRecord(errorMessage);
    List<IntegrityRecord> integrityRecords = new ArrayList<IntegrityRecord>(0);
    integrityRecords.add(rec);
    throw new IntegrityException(integrityRecords);
  }

  /**
   * Method onAddAspect.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy#onAddAspect(NodeRef, QName)
   */
  public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onCreateNode.
   * @param childAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy#onCreateNode(ChildAssociationRef)
   */
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method beforeRemoveAspect.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeRemoveAspectPolicy#beforeRemoveAspect(NodeRef, QName)
   */
  public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method beforeDeleteNode.
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  public void beforeDeleteNode(NodeRef nodeRef) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onMoveNode.
   * @param oldChildAssocRef ChildAssociationRef
   * @param newChildAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnMoveNodePolicy#onMoveNode(ChildAssociationRef, ChildAssociationRef)
   */
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onUpdateProperties.
   * @param nodeRef NodeRef
   * @param before Map<QName,Serializable>
   * @param after Map<QName,Serializable>
   * @see org.alfresco.repo.node.NodeServicePolicies$OnUpdatePropertiesPolicy#onUpdateProperties(NodeRef, Map<QName,Serializable>, Map<QName,Serializable>)
   */
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onCopyComplete.
   * @param arg0 QName
   * @param arg1 NodeRef
   * @param arg2 NodeRef
   * @param arg3 boolean
   * @param arg4 Map<NodeRef,NodeRef>
   * @see org.alfresco.repo.copy.CopyServicePolicies$OnCopyCompletePolicy#onCopyComplete(QName, NodeRef, NodeRef, boolean, Map<NodeRef,NodeRef>)
   */
  public void onCopyComplete(QName arg0, NodeRef arg1, NodeRef arg2, boolean arg3, Map<NodeRef, NodeRef> arg4) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onContentUpdate.
   * @param nodeRef NodeRef
   * @param newContent boolean
   * @see org.alfresco.repo.content.ContentServicePolicies$OnContentUpdatePolicy#onContentUpdate(NodeRef, boolean)
   */
  public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
    // TODO Auto-generated method stub
    
  }
  
  /**
   * Method onCreateAssociation.
   * @param nodeAssocRef AssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateAssociationPolicy#onCreateAssociation(AssociationRef)
   */
  public void onCreateAssociation(AssociationRef nodeAssocRef) {
	    // TODO Auto-generated method stub
  }

  /**
   * Method onDeleteAssociation.
   * @param nodeAssocRef AssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnDeleteAssociationPolicy#onDeleteAssociation(AssociationRef)
   */
  public void onDeleteAssociation(AssociationRef nodeAssocRef) {
	    // TODO Auto-generated method stub	  
  }
  
  /**
   * Method onCreateChildAssociation.
   * @param childAssocRef ChildAssociationRef
   * @param isNewNode boolean
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateChildAssociationPolicy#onCreateChildAssociation(ChildAssociationRef, boolean)
   */
  @Override
  public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
  
  }

  /**
   * Method setVersionService.
   * @param versionService VersionService
   */
  public void setVersionService(VersionService versionService) {
    this.versionService = versionService;
  }
  
}
