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
package gov.pnnl.cat.policy.criminalintel;

import gov.pnnl.cat.criminalintel.CriminalIntelOptionsBean;
import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.TagTimerConstants;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.namespace.QName;



/**
 */
public class CriminalIntelBehavior extends ExtensiblePolicyAdapter {
  private CriminalIntelOptionsBean criminalIntelOptions;
  
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
    if (aspectTypeQName.equals(TagTimerConstants.ASPECT_CRIMINAL_INTEL)) {
      if(nodeService.hasAspect(nodeRef, TagTimerConstants.ASPECT_CRIMINAL_INTEL_REMOVED)){
        nodeService.removeAspect(nodeRef, TagTimerConstants.ASPECT_CRIMINAL_INTEL_REMOVED);
      }
      //set expiry date based off type of data
      setExpiryDate(nodeRef);
    }
    if(aspectTypeQName.equals(TagTimerConstants.ASPECT_CRIMINAL_INTEL_REMOVED)){
      //make sure ASPECT_US_PRESONS_DATA is removed:
      if(nodeService.hasAspect(nodeRef, TagTimerConstants.ASPECT_CRIMINAL_INTEL)){
        nodeService.removeAspect(nodeRef, TagTimerConstants.ASPECT_CRIMINAL_INTEL);
      }
      String username = this.authenticationComponent.getCurrentUserName();
      nodeService.setProperty(nodeRef, TagTimerConstants.PROP_INTEL_REMOVED_DATE, new Date());
      nodeService.setProperty(nodeRef, TagTimerConstants.PROP_INTEL_REMOVED_USER, username);
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
    if (nodeService.hasAspect(nodeRef, TagTimerConstants.ASPECT_CRIMINAL_INTEL)){
      String typeBefore = (String) before.get(TagTimerConstants.PROP_INTEL_MATERIAL_TYPE);
      String typeAfter = (String) after.get(TagTimerConstants.PROP_INTEL_MATERIAL_TYPE);
      // If the type changed update the expiry date.  Don't even need to check justification since you 
      // can't do both at the same time.
      if ((typeBefore == null && typeAfter != null) ||
          (typeBefore != null && typeAfter != null && typeBefore.equalsIgnoreCase(typeAfter) == false)) {
        setExpiryDate(nodeRef);
      } else {
        Date justificationDateBefore = (Date) before.get(TagTimerConstants.PROP_INTEL_JUSTIFICATION_DATE);
        Date justificationDateAfter = (Date) after.get(TagTimerConstants.PROP_INTEL_JUSTIFICATION_DATE);
        //if justification date changed or initially set, reset the expiry date
        if ((justificationDateBefore == null && justificationDateAfter != null) || 
           (justificationDateBefore != null && justificationDateAfter != null 
            && justificationDateAfter.compareTo(justificationDateBefore) != 0)){ 
          setExpiryDate(nodeRef);
          String username = this.authenticationComponent.getCurrentUserName();
          nodeService.setProperty(nodeRef, TagTimerConstants.PROP_INTEL_JUSTIFICATION_USER, username);
        }
      }  
    }
    
  }
  
  /**
   * Method setExpiryDate.
   * @param nodeRef NodeRef
   */
  private void setExpiryDate(NodeRef nodeRef) {   
    String type = (String) nodeService.getProperty(nodeRef, TagTimerConstants.PROP_INTEL_MATERIAL_TYPE);
    Map<String, String> intelTypes = criminalIntelOptions.getIntelTypes();
        
    Date expiryDate = new Date();
    Date lastJustificationDate = (Date) nodeService.getProperty(nodeRef, TagTimerConstants.PROP_INTEL_JUSTIFICATION_DATE);
    
    if (lastJustificationDate != null){
      expiryDate = Duration.add(lastJustificationDate, new Duration(intelTypes.get(type)));
    } else {
      Date created = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED);
      expiryDate = Duration.add(created, new Duration(intelTypes.get(type)));
    }
    
    nodeService.setProperty(nodeRef, TagTimerConstants.PROP_INTEL_EXPIRE_DATE, expiryDate);
    
  }

  /**
   * Method setCriminalIntelOptions.
   * @param criminalIntelOptions CriminalIntelOptionsBean
   */
  public void setCriminalIntelOptions(CriminalIntelOptionsBean criminalIntelOptions) {
    this.criminalIntelOptions = criminalIntelOptions;
  }

    
}
