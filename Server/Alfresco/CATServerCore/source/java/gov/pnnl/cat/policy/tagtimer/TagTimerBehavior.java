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
package gov.pnnl.cat.policy.tagtimer;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.TagTimerConstants;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;



/**
 */
public class TagTimerBehavior extends ExtensiblePolicyAdapter {
  
  private int intelReview;

  private int referenceReview;

  @Override
  public void init() {
  }

  /**
   * Method onAddAspect.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy#onAddAspect(NodeRef, QName)
   */
  @Override
  public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
    if (aspectTypeQName.equals(TagTimerConstants.ASPECT_US_PRESONS_DATA)) {
      if(nodeService.hasAspect(nodeRef, TagTimerConstants.ASPECT_US_PRESONS_DATA_REMOVED)){
        nodeService.removeAspect(nodeRef, TagTimerConstants.ASPECT_US_PRESONS_DATA_REMOVED);
      }
      //set expiry date based off type of data
      setExpiryDate(nodeRef);
    }
    if(aspectTypeQName.equals(TagTimerConstants.ASPECT_US_PRESONS_DATA_REMOVED)){
      //make sure ASPECT_US_PRESONS_DATA is removed:
      if(nodeService.hasAspect(nodeRef, TagTimerConstants.ASPECT_US_PRESONS_DATA)){
        nodeService.removeAspect(nodeRef, TagTimerConstants.ASPECT_US_PRESONS_DATA);
      }
      String username = this.authenticationComponent.getCurrentUserName();
      nodeService.setProperty(nodeRef, TagTimerConstants.PROP_REMOVED_DATE, new Date());
      nodeService.setProperty(nodeRef, TagTimerConstants.PROP_REMOVED_USER, username);
    }
  }

  /**
   * Method onUpdateProperties.
   * @param nodeRef NodeRef
   * @param before Map<QName,Serializable>
   * @param after Map<QName,Serializable>
   * @see org.alfresco.repo.node.NodeServicePolicies$OnUpdatePropertiesPolicy#onUpdateProperties(NodeRef, Map<QName,Serializable>, Map<QName,Serializable>)
   */
  @Override
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    if (nodeService.hasAspect(nodeRef, TagTimerConstants.ASPECT_US_PRESONS_DATA)){
      String typeBefore = (String) before.get(TagTimerConstants.PROP_MATERIAL_TYPE);
      String typeAfter = (String) after.get(TagTimerConstants.PROP_MATERIAL_TYPE);
      // If the type changed update the expiry date.  Don't even need to check justification since you 
      // can't do both at the same time.
      if ((typeBefore == null && typeAfter != null) ||
          (typeBefore != null && typeAfter != null && typeBefore.equalsIgnoreCase(typeAfter) == false)) {
        setExpiryDate(nodeRef);
      } else {
        Date justificationDateBefore = (Date) before.get(TagTimerConstants.PROP_JUSTIFICATION_DATE);
        Date justificationDateAfter = (Date) after.get(TagTimerConstants.PROP_JUSTIFICATION_DATE);
        //if justification date changed or initially set, reset the expiry date
        if ((justificationDateBefore == null && justificationDateAfter != null) || 
           (justificationDateBefore != null && justificationDateAfter != null 
            && justificationDateAfter.compareTo(justificationDateBefore) != 0)){ 
          setExpiryDate(nodeRef);
          String username = this.authenticationComponent.getCurrentUserName();
          nodeService.setProperty(nodeRef, TagTimerConstants.PROP_JUSTIFICATION_USER, username);
        }
      }  
    }
    
  }
  
  /**
   * Method setExpiryDate.
   * @param nodeRef NodeRef
   */
  private void setExpiryDate(NodeRef nodeRef) {   
    String type = (String) nodeService.getProperty(nodeRef, TagTimerConstants.PROP_MATERIAL_TYPE);
    GregorianCalendar calendar = new GregorianCalendar();

    Date created = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED);
    Date lastJustificationDate = (Date) nodeService.getProperty(nodeRef, TagTimerConstants.PROP_JUSTIFICATION_DATE);
    
    if (lastJustificationDate != null){
      calendar.setTime(lastJustificationDate);
    } else {
      calendar.setTime(created);
    }
    
    Date expiryDate = new Date();
    if (type.equalsIgnoreCase(TagTimerConstants.TYPE_REFERNCE)){
      calendar.add(Calendar.DAY_OF_YEAR, referenceReview);
      expiryDate = calendar.getTime(); 
    } else if (type.equalsIgnoreCase(TagTimerConstants.TYPE_INTEL)){
      calendar.add(Calendar.DAY_OF_YEAR, intelReview);
      expiryDate = calendar.getTime(); 
    }
    nodeService.setProperty(nodeRef, TagTimerConstants.PROP_EXPIRE_DATE, expiryDate);
    
  }

  
  
  /**
   * Method beforeRemoveAspect.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeRemoveAspectPolicy#beforeRemoveAspect(NodeRef, QName)
   */
  @Override
public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
	// TODO Auto-generated method stub
	  
	  Action action = actionService.createAction("tagtimer-metadata-extraction-action");
      actionService.executeAction(action, nodeRef, false, false);
	  
}

/**
 * Method setIntelReview.
 * @param intelReview int
 */
public void setIntelReview(int intelReview) {
    this.intelReview = intelReview;
  }

  /**
   * Method setReferenceReview.
   * @param referenceReview int
   */
  public void setReferenceReview(int referenceReview) {
    this.referenceReview = referenceReview;
  }

}
