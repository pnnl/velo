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
package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class WebScriptUtils {
  private static Log logger = LogFactory.getLog(WebScriptUtils.class);

  private static ThumbnailService thumbnailService;

  /**
   * Method setThumbnailService.
   * 
   * @param thumbnailService
   *          ThumbnailService
   */
  public void setThumbnailService(ThumbnailService thumbnailService) {
    WebScriptUtils.thumbnailService = thumbnailService;
  }

  public static NodeRef getNodeRef(Resource resource, NodeService nodeService) {
    return getNodeRef(resource.getPath(), resource.getUuid(), nodeService);
  }

  /**
   * Find the given nodeRef via path or uuid. Depending upon the circumstance,
   * one or the other could be set.
   * 
   * @param path
   * @param uuid
   * @return - null if a path is used and the node does not exist
   */
  public static NodeRef getNodeRef(String path, String uuid, NodeService nodeService) {
    NodeRef nodeRef = null;

    if (uuid != null && !uuid.isEmpty()) {
      nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuid);

    } else if (path != null) {
      // Make sure we are using an association name path and we don't have any
      // prefixes or
      // qualified names in the string
      CmsPath cmsPath = new CmsPath(path);
      nodeRef = NodeUtils.getNodeByName(cmsPath.toAssociationNamePath(), nodeService);
    }

    return nodeRef;
  }

  /**
   * Create an alfresco node based on the given resource object
   * 
   * @param resource
   * @param nodeService
   * @return
   */
  public static NodeRef createNode(Resource resource, NodeService nodeService, DictionaryService dictionaryService) {

    if (resource.getPath() == null) {
      throw new RuntimeException("Cannot create node without path to resource.");
    }
    CmsPath path = new CmsPath(resource.getPath());
    String name = path.getName();
    String parentPath = path.getParent().toAssociationNamePath();
    NodeRef parent = NodeUtils.getNodeByName(parentPath, nodeService);

    if (parent == null) {
      throw new RuntimeException(String.format("Invalid resource path: %s. Unable to find parent: %s", resource.getPath(), parentPath));
    }
    if (resource.getType() == null) {
      throw new RuntimeException(String.format(
          "Resource %s does not have a type set. Please set a valid type(folder, content, link etc.) for the resource to be created", resource.getPath()));
    }

    QName nodeType = QName.createQName(resource.getType());
    Map<QName, Serializable> nodeProps = new HashMap<QName, Serializable>();
    Map<String, List<String>> properties = resource.getProperties();

    for (String key : properties.keySet()) {
      List<String> value = properties.get(key);
      QName propQname = QName.createQName(key);
      if (value != null) {
        Serializable convertedValue = WebScriptUtils.getPropertyValueFromStringList(dictionaryService, propQname, value);
        nodeProps.put(propQname, convertedValue);
      }
    }

    return NodeUtils.createNode(parent, name, nodeType, ContentModel.ASSOC_CONTAINS, nodeProps, nodeService);

  }

  public static void setProperties(NodeRef nodeRef, Resource resource, NodeService nodeService, DictionaryService dictionaryService) {
    setProperties(nodeRef, resource.getProperties(), nodeService, dictionaryService);
  }

  public static void setProperties(NodeRef nodeRef, Map<String, List<String>> properties, NodeService nodeService, DictionaryService dictionaryService) {
    Map<QName, Serializable> alfrescoProps = nodeService.getProperties(nodeRef);

    for (String key : properties.keySet()) {
      List<String> value = properties.get(key);
      QName propQname = QName.createQName(key);
      if (value == null) {
        alfrescoProps.remove(propQname);

      } else {
        Serializable convertedValue = WebScriptUtils.getPropertyValueFromStringList(dictionaryService, propQname, value);
        alfrescoProps.put(propQname, convertedValue);
      }
    }

    // make sure the name property is set correctly
    // This is a backwards-compatible fix for nodes where the name property got
    // erased.
    // This can removed at a later date after nodes have been corrected, as it
    // will have a performance impact
    String nameProp = (String) alfrescoProps.get(ContentModel.PROP_NAME);
    ChildAssociationRef childRef = nodeService.getPrimaryParent(nodeRef);
    String namePath = childRef.getQName().getLocalName();

    if (!namePath.equals(nameProp)) {
      // change it to the path
      alfrescoProps.put(ContentModel.PROP_NAME, namePath);
    }

    nodeService.setProperties(nodeRef, alfrescoProps);
  }

  public static void setAspects(NodeRef nodeRef, Resource resource, NodeService nodeService) {
    // update any aspects present
    QName aspectQName = null;
    for (String aspect : resource.getAspects()) {
      aspectQName = QName.createQName(aspect);
      if (!nodeService.hasAspect(nodeRef, aspectQName)) {
        nodeService.addAspect(nodeRef, aspectQName, null);
      }
    }

  }

  /**
   * Method getNode.
   * 
   * @param nodeRef
   *          NodeRef
   * @param nodeService
   *          NodeService
   * @param dictionaryService
   *          DictionaryService
   * @param contentService
   *          ContentService
   * @return Resource
   * @throws Exception
   */
  public static Resource getResource(NodeRef nodeRef, NodeService nodeService, NamespaceService namespaceService, DictionaryService dictionaryService,
      ContentService contentService) {
    return getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService, false);
  }

  /**
   * Method getNode.
   * 
   * @param nodeRef
   *          NodeRef
   * @param nodeService
   *          NodeService
   * @param dictionaryService
   *          DictionaryService
   * @param contentService
   *          ContentService
   * @param includeThumbnails
   *          boolean
   * @return Resource
   * @throws Exception
   */
  public static Resource getResource(NodeRef nodeRef, NodeService nodeService, NamespaceService namespaceService, DictionaryService dictionaryService,
      ContentService contentService, boolean includeThumbnails) {

    // first check if node exists
    if (!nodeService.exists(nodeRef)) {
      return null;
    }

    // then check if resource has cat ignore aspect - if so, skip it
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_IGNORE)) {
      return null;
    }

    // Get the uuid
    Resource resource = new Resource(nodeRef.getId());

    // Get the type
    QName type = nodeService.getType(nodeRef);
    resource.setType(type.toString());

    // Get the aspects applied to the node
    Set<QName> aspects = nodeService.getAspects(nodeRef);
    for (QName aspect : aspects) {
      String aspectName = aspect.toString();
      // skip some common aspects we don't care about
      if (!aspectName.equals("{http://www.alfresco.org/model/content/1.0}auditable")
          && !aspectName.equals("{http://www.alfresco.org/model/application/1.0}uifacets")
          && !aspectName.equals("{http://www.alfresco.org/model/system/1.0}referenceable")
          && !aspectName.equals("{http://www.alfresco.org/model/content/1.0}titled")
          && !aspectName.equals("{http://www.pnl.gov/dmi/model/notification/1.0}notifiable")
          && !aspectName.equals("{http://www.alfresco.org/model/system/1.0}localized")) {
        resource.getAspects().add(aspectName);
      }
    }

    // get all the properties of the node
    Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
    for (QName propName : props.keySet()) {
      addProperty(resource, propName, props.get(propName), nodeService, dictionaryService);
    }

    // Add the node's path
    // We are using the prefix string because it's much smaller than using full
    // namespaces,
    // which will minimize the json string
    resource.setPath(nodeService.getPath(nodeRef).toPrefixString(namespaceService).toString());

    // Add a property for the node's size
    String size = null;

    if (type.equals(ContentModel.TYPE_CONTENT)) {

      ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
      if (reader != null) {
        size = String.valueOf(reader.getSize());
      } else {
        size = "0";
      }

    } else {
      size = "0";
    }
    List<String> values = new ArrayList<String>();
    values.add(size);
    resource.getProperties().put(CatConstants.PROP_SIZE.toString(), values);

    // add a property for the thumbnail if it exists
    if (includeThumbnails) {
      ThumbnailDefinition thumbnailDefinition = thumbnailService.getThumbnailRegistry().getThumbnailDefinition("previewPaneImage");
      NodeRef thumbnail = thumbnailService.getThumbnailByName(nodeRef, ContentModel.PROP_CONTENT, thumbnailDefinition.getName());
      if (thumbnail != null) {
        // convert thumbnail to base64 string
        try {
          FileContentReader reader = (FileContentReader) contentService.getReader(thumbnail, ContentModel.PROP_CONTENT);
          String encoded = Base64.encodeFromFile(reader.getFile().getAbsolutePath());
          values = new ArrayList<String>();
          values.add(encoded);
          resource.getProperties().put(CatConstants.PROP_THUMBNAIL.toString(), values);

        } catch (Throwable e) {
          logger.debug("Failed to base64 encode thumbnail.", e);
        }
      }
    }

    // Add the number of children for this node
    resource.setNumChildren(getChildAssociations(nodeRef, nodeService).size());

    // also adding number of subfolders for UI's to use to determine if they
    // should draw the twisty icon or not
    resource.setNumFolderChildren(nodeService.getChildAssocs(nodeRef, CatConstants.FOLDER_TYPES).size());

    return resource;
  }

  /**
   * For the given nodeRef, get the applicable children.
   * 
   * @param nodeRef
   * @return
   */
  public static List<ChildAssociationRef> getChildAssociations(NodeRef nodeRef, NodeService nodeService) {
    // Adding support for categories so we can treat them just like folders
    QName type = nodeService.getType(nodeRef);

    if (type.equals(ContentModel.TYPE_CATEGORYROOT)) {
      return nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CATEGORIES, RegexQNamePattern.MATCH_ALL);

    } else if (type.equals(ContentModel.TYPE_CATEGORY)) {
      return nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_SUBCATEGORIES, RegexQNamePattern.MATCH_ALL);

    } else {
      return nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
    }
  }

  /**
   * Method addProperty.
   * 
   * @param resource
   *          Resource
   * @param propertyName
   *          QName
   * @param propertyValue
   *          Serializable
   * @param nodeService
   *          NodeService
   * @param dictionaryService
   *          DictionaryService
   */
  private static void addProperty(Resource resource, QName propertyName, Serializable propertyValue, NodeService nodeService,
      DictionaryService dictionaryService) {
    String propName = propertyName.toString();

    // Ignore some properties we don't care about
    if (propName.equals("{http://www.alfresco.org/model/system/1.0}node-dbid") || propName.equals("{http://www.alfresco.org/model/application/1.0}icon")
        || propName.equals("{http://www.alfresco.org/model/system/1.0}store-identifier")
        || propName.equals("{http://www.alfresco.org/model/system/1.0}store-protocol")
    // ||
    // propName.equals("{http://www.alfresco.org/model/system/1.0}node-uuid")
    ) {
      return;
    }

    List<String> values = new ArrayList<String>();
    resource.getProperties().put(propName, values);
    if (logger.isDebugEnabled() == true) {
      logger.debug("Creating named value for property '" + propertyName + "' with value '" + propertyValue + "'");
    }

    if (propertyValue != null) {
      PropertyDefinition propDef = dictionaryService.getProperty(propertyName);
      if (propDef != null) {
        if (propDef.isMultiValued() == true || propertyValue.getClass().equals(java.util.ArrayList.class)) {
          if (propertyValue instanceof Collection) {
            if (logger.isDebugEnabled() == true) {
              logger.debug("Converting multivalue for property '" + propertyName + "'");
            }

            Collection<Serializable> collection = (Collection<Serializable>) propertyValue;
            for (Serializable value : collection) {
              values.add(DefaultTypeConverter.INSTANCE.convert(String.class, value));
            }
          }
        } else {
          if (logger.isDebugEnabled() == true) {
            logger.debug("Converting single value for property '" + propertyName + "'");
          }

          values.add(DefaultTypeConverter.INSTANCE.convert(String.class, propertyValue));
        }
      } else {
        if (logger.isDebugEnabled() == true) {
          logger.debug("No property definition found for property '" + propertyName + "'");
        }

        values.add(propertyValue.toString());
      }
    }

  }

  /**
   * Utility method to convert from a string representation of a property value
   * into the correct object representation.
   * 
   * @param dictionaryService
   *          the dictionary service
   * @param propertyName
   *          the qname of the property in question
   * @param value
   *          the property vlaue as a list of strings
   * @return the object value of the property
   */
  public static Serializable getPropertyValueFromStringList(DictionaryService dictionaryService, QName propertyName, List<String> values) {

    Serializable result = null;
    PropertyDefinition propDef = dictionaryService.getProperty(propertyName);
    if (values != null) {

      if (propDef == null) {
        logger.warn("No property definition found for property '" + propertyName + "'");

        if (values.size() > 1) {
          Collection<Serializable> collection = new ArrayList<Serializable>(values.size());
          for (String value : values) {
            collection.add(value);
          }
          result = (Serializable) collection;

        } else if (values.size() == 1) {
          result = values.get(0);
        }

      } else {

        DataTypeDefinition propertyType = propDef.getDataType();
        if (propertyType != null) {
          if (propDef.isMultiValued()) {

            Collection<Serializable> collection = new ArrayList<Serializable>(values.size());
            for (String value : values) {
              collection.add((Serializable) DefaultTypeConverter.INSTANCE.convert(propertyType, value));
            }
            result = (Serializable) collection;

          } else if (values.size() > 0) {
            result = (Serializable) DefaultTypeConverter.INSTANCE.convert(propertyType, values.get(0));
          }

        } else {
          logger.warn("No property definition was found for property '" + propertyName.toString() + "'");
        }

      }

    }

    return result;
  }

}
