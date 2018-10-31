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
package gov.pnnl.cat.core.resources.util.alfresco;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;

import org.alfresco.webservice.types.ParentReference;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.util.Constants;
import org.apache.log4j.Logger;

/**
 * This class houses some utility functions that the resources plugin uses.
 * Other plugins may also find these utility functions useful if they have to
 * communicate with the Alfresco server.
 *
 * @author Eric Marshall
 * @version $Revision: 1.0 $
 */
public class AlfrescoUtils {
  @SuppressWarnings("unused")
  private static Logger logger = CatLogger.getLogger(AlfrescoUtils.class);

  /**
   * The name of the Alfresco store that CAT uses.
   */
  public final static String CAT_STORE_STRING = "SpacesStore";

  /**
   * The name of the root node in our store.
   */
  public final static String CAT_ROOT = "/app:company_home";

  /**
   * The main Alfresco store that CAT uses.
   */
  public final static Store CAT_STORE = new Store(Constants.WORKSPACE_STORE, CAT_STORE_STRING);

  public final static CmsPath ROOT_PATH = new CmsPath(CAT_ROOT);

  /**
   * Method getPredicate.
   * @param uuid String
   * @return Predicate
   */
  public static Predicate getPredicate(String uuid) {
    return new Predicate(new Reference[] {getReference(uuid)}, CAT_STORE, null);
  }

  /**
   * Method getPredicate.
   * @param path CmsPath
   * @return Predicate
   */
  public static Predicate getPredicate(CmsPath path) {
    return new Predicate(new Reference[] {getReference(path)}, CAT_STORE, null);
  }


  /**
   * Method getReference.
   * @param uuid String
   * @return Reference
   */
  public static Reference getReference(String uuid) {
    return new Reference(CAT_STORE, uuid, null);
  }

  /**
   * Method getReference.
   * @param path CmsPath
   * @return Reference
   */
  public static Reference getReference(CmsPath path) {
    String uuid = getCachedUuid(path);
    Reference reference;

    // create a Reference using the UUID if it was cached
    if (uuid != null) {
      reference = getReference(uuid);
    } else {
      // if the UUID was not cached, we will have to use the path
      String prefixString = path.toPrefixString(true);
      reference =  new Reference(CAT_STORE, null, prefixString);
    }

    return reference;
  }

  /**
   * We assume everything created from CAT will be of the same association type, CONTAINS.
   * @param parentUuid
   * @param fullyQualifiedChildName
  
   * @return ParentReference
   */
  public static ParentReference getParentReference(String parentUuid, String fullyQualifiedChildName) {
    
    return new ParentReference(
        AlfrescoUtils.CAT_STORE,
        parentUuid,
        null,
        Constants.ASSOC_CONTAINS,
        fullyQualifiedChildName);

  }

  /**
   * Method getParentReference.
   * @param parentPath CmsPath
   * @param fullyQualifiedChildName String
   * @return ParentReference
   */
  public static ParentReference getParentReference(CmsPath parentPath, String fullyQualifiedChildName) {
	  return getParentReference(parentPath, fullyQualifiedChildName, Constants.ASSOC_CONTAINS);
  }

  /**
   * Method getParentReference.
   * @param parentPath CmsPath
   * @param fullyQualifiedChildName String
   * @param assocType QualifiedName
   * @return ParentReference
   */
  public static ParentReference getParentReference(CmsPath parentPath, String fullyQualifiedChildName, String assocType) {
    String uuid = getCachedUuid(parentPath);
    ParentReference reference;

    // create a ParentReference using the UUID if it was cached
    if (uuid != null) {
      reference = getParentReference(uuid, fullyQualifiedChildName);
    } else {
      // if the UUID was not cached, we will have to use the path
      String fullyQualifiedPath = parentPath.toPrefixString(true);
      reference =
        new ParentReference(
          AlfrescoUtils.CAT_STORE,
          null,
          fullyQualifiedPath,
          assocType,
          fullyQualifiedChildName);
    }
    return reference;
  }  

  /**
   * Returns the given node's UUID property if it has been cached.
   * If the UUID property has not been cached, this method will return null.
   * @param path
  
   * @return String
   */
  private static String getCachedUuid(CmsPath path) {
    return ResourcesPlugin.getResourceManager().getUUID(path);
  }

  /**
   * Method getReferenceString.
   * @param store Store
   * @param resource IResource
   * @return String
   * @throws ResourceException
   */
  public static String getReferenceString(Store store, String uuid) throws ResourceException {
    return store.getScheme() + "://" + store.getAddress() + "/" + uuid;
  }
  
  /**
   * Method getReferenceString.
   * @param resource IResource
   * @return String
   */
  public static String getReferenceString(String uuid) {
    return CAT_STORE.getScheme() + "://" + CAT_STORE.getAddress() + "/" + uuid;
  }

  /**
   * Parses a specially formatted <code>String</code> into a <code>Reference</code> object.
   * <p>The format of the <code>String</code> is <code>scheme://address/uuid</code>.
   * <p>For example:<br>
   * <code>workspace://SpacesStore/bde67ffb-6f53-11db-bc5c-e54bc818e908</code>
   * @param referenceString the specially formatted <code>String</code>
   * @return Reference
   * @see #getReferenceString(Store, IResource) */
  public static Reference getReferenceFromReferenceString(String referenceString){
    int colonIndex = referenceString.indexOf(":");
    String scheme = referenceString.substring(0, colonIndex);
    String address = referenceString.substring(colonIndex + 3, referenceString.lastIndexOf("/"));
    Store store = new Store(scheme, address);
    return new Reference(store, parseUuidFromReferenceString(referenceString), null);
  }

  /**
   * Parses the UUID from a reference string.
   * A reference string uses the following format:<p/><tt>workspace://SpacesStore/bde67ffb-6f53-11db-bc5c-e54bc818e908</tt>
   * @param reference
  
   * @return String
   */
  public static String parseUuidFromReferenceString(String reference) {
    // TODO: do we really need to parse the UUID? It seems like we shouldn't have to do this.
    // the server should do this for us.
    // workspace://SpacesStore/bde67ffb-6f53-11db-bc5c-e54bc818e908
    return reference.substring(reference.lastIndexOf('/') + 1);
  }
}
