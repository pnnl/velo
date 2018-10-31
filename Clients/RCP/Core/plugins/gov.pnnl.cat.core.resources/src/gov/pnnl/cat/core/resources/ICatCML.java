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
package gov.pnnl.cat.core.resources;

import gov.pnnl.velo.model.CmsPath;

import org.eclipse.core.runtime.IAdaptable;

/**
 * CML represents a compound statement used for
 * executing several write operations in a single
 * remote call.  CML stands for Content Manipulation
 * Language (coined by Alfresco).  We can pick a 
 * better name.  I just used this initially because
 * it was short 
 *
 * @version $Revision: 1.0 $
 */
public interface ICatCML extends IAdaptable {

  /**
   * This class only contains resource methods that can be executed
   * within an Alfresco CML block.
   * @param folderPath CmsPath
   */

  /**
   * TODO: Change this to setProperties
   * Method setProperty.
   * @param resourcePath CmsPath
   * @param key QualifiedName
   * @param value String
   * @throws ResourceException
   */
  public void setProperty(CmsPath resourcePath, String key, String value) throws ResourceException;
  /**
   * Method addFolder.
   * @param path CmsPath
   */
  public void addFolder(CmsPath path);
  /**
   * Method deleteResource.
   * @param path CmsPath
   */
  public void deleteResource(CmsPath path);
  /**
   * Method move.
   * @param source CmsPath
   * @param destination CmsPath
   */
  public void move(CmsPath source, CmsPath destination);
  /**
   * Method copy.
   * @param source CmsPath
   * @param destination CmsPath
   */
  public void copy(CmsPath source, CmsPath destination, boolean overwrite);  

  /**
   * Method addLink.
   * @param path CmsPath
   * @param target IResource
   * @throws ResourceException
   */
  public void addLink(CmsPath linkPath, String targetUuid) throws ResourceException;
  /**
   * Method updateLinkTarget.
   * @param link CmsPath
   * @param destination IResource
   * @throws ResourceException
   */
  public void updateLinkTarget(CmsPath link, String targetUuid) throws ResourceException ;
  /**
   * Method getCMLSize.
   * @return int
   */
  public int getCMLSize();

  /**
   * Method addAspect.
   * @param path CmsPath
   * @param aspect String
   */
  public void addAspect(CmsPath path, String aspect);
  /**
   * Method removeAspect.
   * @param path CmsPath
   * @param aspect String
   */
  public void removeAspect(CmsPath path, String aspect);
  /**
   * Method addFolder.
   * @param parentPath CmsPath
   * @param childName QualifiedName
   * @param assocType QualifiedName
   * @param childNameProp String
   */
  public void addFolder(CmsPath parentPath, String childName, String assocType, String childNameProp);
  /**
   * Method addNode.
   * @param parentPath CmsPath
   * @param childName QualifiedName
   * @param assocType QualifiedName
   * @param childNameProp String
   * @param type QualifiedName
   */
  public void addNode(CmsPath parentPath, String fullyQualifiedChildAssocName, String assocType, String childNameProp, String type);
  
  /**
   * Method writeContent.
   * @param path CmsPath
   * @param property QualifiedName
   * @param content String
   * @param mimeType String
   */
  public void writeContent(CmsPath path, String property, String content, String mimeType);
}
