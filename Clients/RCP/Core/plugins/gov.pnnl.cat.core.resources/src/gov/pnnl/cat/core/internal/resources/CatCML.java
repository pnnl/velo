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
package gov.pnnl.cat.core.internal.resources;

import gov.pnnl.cat.core.resources.ICatCML;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.util.alfresco.AlfrescoUtils;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import org.alfresco.webservice.types.CMLAddAspect;
import org.alfresco.webservice.types.CMLCopy;
import org.alfresco.webservice.types.CMLCreate;
import org.alfresco.webservice.types.CMLDelete;
import org.alfresco.webservice.types.CMLMove;
import org.alfresco.webservice.types.CMLRemoveAspect;
import org.alfresco.webservice.types.CMLUpdate;
import org.alfresco.webservice.types.CMLWriteContent;
import org.alfresco.webservice.types.ContentFormat;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.ParentReference;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.Utils;
import org.apache.log4j.Logger;

/**
 */
public class CatCML implements ICatCML {

  protected CMLManager cmlManager = new CMLManager();

//  private List<CMLCreate>       creates = new ArrayList<CMLCreate>();
//  private List<CMLAddAspect>    addAspects = new ArrayList<CMLAddAspect>();
//  private List<CMLRemoveAspect> removeAspects = new ArrayList<CMLRemoveAspect>();
//  private List<CMLUpdate>       updates = new ArrayList<CMLUpdate>();
//  private List<CMLCopy>         copies  = new ArrayList<CMLCopy>();
//  private List<CMLMove>         moves   = new ArrayList<CMLMove>();
//  private List<CMLDelete>       deletes = new ArrayList<CMLDelete>();

  protected static Logger logger = CatLogger.getLogger(CatCML.class);
  
  public CatCML() {
  }

  /**
   * Method addFolder.
   * @param path CmsPath
   * @see gov.pnnl.cat.core.resources.ICatCML#addFolder(CmsPath)
   */
  public void addFolder(CmsPath path) {
    CmsPath parentPath = path.getParent();
    createFolder(path, parentPath);
  }


  public void writeContent(CmsPath path, String property, String content, String mimeType) {
    ContentFormat contentFormat = new ContentFormat(mimeType, "UTF-8");
    CMLWriteContent writeContent = new CMLWriteContent(
        property,
        content.getBytes(),
        contentFormat,
        AlfrescoUtils.getPredicate(path),
        null);
    cmlManager.add(writeContent);
  }



  public void addFolder(CmsPath parentPath, String childAssocQualifiedName, String assocType, String childNameProp) {
    addNode(parentPath, childAssocQualifiedName, assocType, childNameProp, Constants.TYPE_FOLDER);
  }


  public void addNode(CmsPath parentPath, String childAssocQualifiedName, String assocType, String childNameProp, String type) {
    String childNameStr = childAssocQualifiedName;

    ParentReference parentReference = AlfrescoUtils.getParentReference(
        parentPath,
        childNameStr,
        assocType);

    NamedValue[] props = new NamedValue[] {
        Utils.createNamedValue(Constants.PROP_NAME, childNameProp)
    };

    String createID = "" + this.cmlManager.getCMLTotal();

    CMLCreate create = new CMLCreate(
        createID,
        parentReference,
        null,
        null,
        childNameStr,
        type,
        props);
    this.cmlManager.add(create);
  }


  /**
   * Method createFolder.
   * @param path CmsPath
   * @param parentPath CmsPath
   */
  private void createFolder(CmsPath path, CmsPath parentPath) {
    String name = path.last().getName();

    ParentReference parentReference = AlfrescoUtils.getParentReference(parentPath, 
        Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, name));

    NamedValue[] props = new NamedValue[] {
        Utils.createNamedValue(Constants.PROP_NAME, name)
    };
    String createID = "" + this.cmlManager.getCMLTotal();
    CMLCreate create = new CMLCreate(createID,
        parentReference,
        null,
        Constants.ASSOC_CONTAINS,
        name,
        Constants.TYPE_FOLDER,
        props);
    this.cmlManager.add(create);
  }

  public void addAspect(CmsPath path, String aspect) {
    CMLAddAspect addAspect = new CMLAddAspect(aspect, null, AlfrescoUtils.getPredicate(path), null);
    this.cmlManager.add(addAspect);
  }

  public void removeAspect(CmsPath path, String aspect) {
    CMLRemoveAspect removeAspect = new CMLRemoveAspect(aspect, AlfrescoUtils.getPredicate(path), null);
    this.cmlManager.add(removeAspect);
  }

  /**
   * Method addLink.
   * @param linkPath CmsPath
   * @param target IResource
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.ICatCML#addLink(CmsPath, IResource)
   */
  public void addLink(CmsPath linkPath, String targetUuid) throws ResourceException {

    CmsPath parentPath = linkPath.getParent();
    String name = linkPath.last().getName();
    String qualifiedName = Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, name);

    ParentReference parentReference = AlfrescoUtils.getParentReference(parentPath, qualifiedName);

    NamedValue[] props = new NamedValue[] {
        Utils.createNamedValue(VeloConstants.PROP_NAME, name),
        Utils.createNamedValue(VeloConstants.PROP_LINK_DESTINATION, AlfrescoUtils.getReferenceString(AlfrescoUtils.CAT_STORE, targetUuid))
    };
    String createID = "" + this.cmlManager.getCMLTotal();
    CMLCreate create = new CMLCreate(createID,
        parentReference,
        null,
        Constants.ASSOC_CONTAINS,
        qualifiedName,
        VeloConstants.TYPE_LINKED_FILE,
        props);
    this.cmlManager.add(create);
  }

  /**
   * Method copy.
   * @param source CmsPath
   * @param destination CmsPath
   * @see gov.pnnl.cat.core.resources.ICatCML#copy(CmsPath, CmsPath)
   */
  @Override
  public void copy(CmsPath source, CmsPath destination, boolean overwrite) {
    if(overwrite) {
      // make sure dest is deleted first
      deleteResource(destination);
    }
    CmsPath parentPath = destination.getParent();
    String newName = destination.last().getName();

    ParentReference parentReference = AlfrescoUtils.getParentReference(parentPath, 
        Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, newName));
 
    CMLCopy copy = new CMLCopy(parentReference, null, Constants.ASSOC_CONTAINS, newName, AlfrescoUtils.getPredicate(source), null, true);
    this.cmlManager.add(copy);
  }

  /**
   * Method deleteResource.
   * @param path CmsPath
   * @see gov.pnnl.cat.core.resources.ICatCML#deleteResource(CmsPath)
   */
  public void deleteResource(CmsPath path) {
    CMLDelete delete = new CMLDelete(AlfrescoUtils.getPredicate(path));
    this.cmlManager.add(delete);
  }

  /**
   * Method move.
   * @param source CmsPath
   * @param destination CmsPath
   * @see gov.pnnl.cat.core.resources.ICatCML#move(CmsPath, CmsPath)
   */
  public void move(CmsPath source, CmsPath destination) {
    CmsPath parentPath = destination.getParent();
    String newName = destination.last().getName();

    ParentReference parentReference = AlfrescoUtils.getParentReference(parentPath, 
        Constants.createQNameString(Constants.NAMESPACE_CONTENT_MODEL, newName));
      
    CMLMove move = new CMLMove(parentReference, null, Constants.ASSOC_CONTAINS, newName, AlfrescoUtils.getPredicate(source), null);
    //also adding to the cml to set the name property:
    setProperty(source, VeloConstants.PROP_NAME, newName);
    this.cmlManager.add(move);
  }

  /**
   * Method setProperty.
   * @param path CmsPath
   * @param key QualifiedName
   * @param value String
   * @see gov.pnnl.cat.core.resources.ICatCML#setProperty(CmsPath, QualifiedName, String)
   */
  public void setProperty(CmsPath path, String key, String value) {
    // Need to encode special characters or they will crash Axis' XML
    value = preparePropertyValueForXML(value);
    
    NamedValue[] properties = new NamedValue[]{
        Utils.createNamedValue(key, value)
    };
    CMLUpdate cmlUpdate = new CMLUpdate(properties, AlfrescoUtils.getPredicate(path), null);
    this.cmlManager.add(cmlUpdate);
  }
  
  /**
   * Make sure the property value will create valid XML when added to 
   * a SOAP request.  This is the same logic used to clean up extracted
   * metadata values on the server.
   * @param value
  
   * @return the formated value, ready to be included in an XML document */
  private String preparePropertyValueForXML(String value) {
    if(value == null) {
      return value;
    }
    char[] characters = value.toCharArray();
    StringBuilder out = new StringBuilder();
    char character;
    int charInt;
    
    for (int i = 0; i < characters.length; i++) {
      character = characters[i];
      charInt = (int)character;
      if(charInt >= 32 && charInt <= 127) {
        out.append(character);
        
      } else {
        logger.warn("Invalid XML character [" + character + "] being removed from property value.");
        out.append(' ');
      }
      
    }
    return out.toString();
  }

  /**
   * Method updateLinkTarget.
   * @param link CmsPath
   * @param destination IResource
   * @throws ResourceException
   * @see gov.pnnl.cat.core.resources.ICatCML#updateLinkTarget(CmsPath, IResource)
   */
  public void updateLinkTarget(CmsPath link, String targetUuid) throws ResourceException {
    String reference = AlfrescoUtils.getReferenceString(AlfrescoUtils.CAT_STORE, targetUuid);
    setProperty(link, VeloConstants.PROP_LINK_DESTINATION, reference);
  }

  /**
   * Method getCMLSize.
   * @return int
   * @see gov.pnnl.cat.core.resources.ICatCML#getCMLSize()
   */
  public int getCMLSize() {
    return this.cmlManager.getCMLTotal(); 
  }
  
  /**
   * Method getAdapter.
   * @param adapter Class
   * @return Object
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
   */
  public Object getAdapter(Class adapter) {
    // TODO Auto-generated method stub
    return null;
  }

}
