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

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

/**
 * TODO: change IResource to CachedResource so we know that
 * this resource comes from a client cache.
 */
public interface IResource extends Comparable<IResource> {
  /**
   * Indicates this resource is a file (physical or link).
   */
  public static final int FILE = 1;
  /**
   * Indicates this resource is a folder (physical or link).
   */
  public static final int FOLDER = 2;
  /**
   * Indicates this resource is a link.
   */
  public static final int LINK = 4;
  /**
   * Inidicates this resource is physical (i.e. not a link).
   */
  public static final int PHYSICAL = 8;
  /**
   * Inidicates this resource is the root folder of taxonomy.
   */
  public static final int TAXONOMY_ROOT = 16;
  /**
   * Inidicates this resource is a folder in a taxonomy.
   */
  public static final int TAXONOMY_FOLDER = 32;
  /**
   * Indicates this resource is a file in a taxonomy.
   */
  public static final int TAXONOMY_FILE = 64;
  /**
   * Inidicates this resource is a user's home folder.
   */
  public static final int USER_HOME_FOLDER = 128;
  /**
   * Inidicates this resource is a team's home folder.
   */
  public static final int TEAM_HOME_FOLDER = 256;
  /**
   * Inidicates this resource is a user's favorites folder.
   */
  public static final int FAVORITES_ROOT = 512;
  /**
   * Inidicates this resource is a user's peronal library folder.
   */
  public static final int PERSONAL_LIBRARY_ROOT = 1024;
  /**
   * Inidicates this resource is a project.
   */
  public static final int PROJECT = 2048;
  /**
   * Inidicates this resource is the configuration root folder.
   */
  public static final int CONFIG_ROOT = 4096;
  
  // Updated regular expression to match the latest from Alfresco 2.1C
  public static final String invalidCharactersRegex = "(.*[\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)";
  public static final String invalidCharactersMsg = "cannot contain any of the following characters: \" * \\ > < ? / : |" +  
    "\nCannot end with \".\"" + "\nCannot end with space.";
  public static final String INVALID_CHARS = "\" * \\ > < ? / : |";
  
  /**
   * Gets the parent resource.  All resources will have a parent except for the 
   * root of the tree.  If it is a root, parent will be null.
  
  
   * @return IResource
   * @throws ResourceException * @throws ResourceNotFoundException
   */
  public IResource getParent() throws ResourceException, ResourceNotFoundException;


  /***
   * Return the path that represents this resource.  
   * The path will always be the full
   * path to this resource on the remote server 
   * (i.e., /Company Home/Reference Library).  It will include no
   * host or port information, but will include namespaces if 
   * the fully qualified path is needed.  
   * @return CmsPath
   */
  public CmsPath getPath();

  /**
   * Get the CIFS path for the file
   * @return IPath
   * @throws ResourceException */
  //public IPath getCifsPath() throws ResourceException;
  
  /**
   * Returns the name of this resource.
   * @return String
   */
  public String getName();

  /**
   * Returns the property specified as a String.
   * 
   * @param key the qualified name of the property
  
  
   * @return String
   * @throws ResourceException */
  public String getPropertyAsString(String key);

  /**
   * Method getPropertyAsResource.
   * @param property QualifiedName
   * @return IResource
   * @throws ResourceException
   * @throws AccessDeniedException
   */
  public IResource getPropertyAsResource(String property);

  /**
   * Returns a <code>Vector</code> of <code>String</code> objects corresponding to the properties specified.
   * 
   * @param keys a <code>Vector</code> of <code>QualifiedName</code> objects
  
  
   * @return Vector
   * @throws ResourceException */
  public List<String> getPropertiesAsString(List<String> keys);


  /**
   * Returns the property specified as a Calendar object.
   * 
   * @param key the qualified name of the property
   * @return Calendar
   * @throws ParseException
   */
  public Calendar getPropertyAsDate(String key) throws ParseException ;

  /**
   * Sets the specified property to the specified value. If the property does not yet exist, it is created.
   * Passing a <code>null</code> as the second parameter removes the property. 
   * @param key
   * @param value
  
   * @throws ResourceException */
  public void setProperty(String key, String value);

  /**
   * Moves this resource to the specified destination.
   * 
   * @param destination the path to which this resource should be moved
  
   * @throws ResourceException */
  public void move(CmsPath destination);


  /**
   * Removes this resource from the repository.
   * 
  
   * @throws ResourceException */
  public void delete();
  
  
  /**
  
   * @return true if this is a linked resource */
  public boolean isLink();

  
  //TODO: add a long getModificationStamp() method like they have in Eclipse so we can fix search

  /**
   * Indicates whether this resource is of a particular type.
   * This method can be used as an alternative to using the <code>instanceof</code> keyword.
   * The type parameter supports bitmasking, so to check if a resource is, for example, a linked folder, we can call <code>isType(IResource.LINK | IResource.FILE)</code> which will return <code>true</code> if this resource is both a link and a file.
   *
   * @param type int
   * @return <code>true</code> if this resource is of the specified type * @throws ResourceException  * @see getType() */
  public boolean isType(int type);

  /**
   * Indicates whether this resource or any of its ancestors is of a particular type by recursivly calling getParent().isType(type) until the type is matched or the root path is reached.
   * 
   * @param type int
   * @return <code>true</code> if this resource or any of its ancestors is of the specified type * @throws ResourceException  * @see isType() */
  public boolean isTypeInPath(int type);
  
  /**
   * Returns the type of this resource as a bitmask.
   * Typical return values include <code>IResource.FILE</code>, <code>IResource.FOLDER</code>, or combinations like <code>IResource.LINK | IResource.FILE</code>.
   * This method should not generally be used to test if a resource is of a particular type, except in special circumstances.
   * Instead, clients should use <code>isType(int)</code>
   * 
   * @return the type of this resource as a bitmask * @throws ResourceException
   * @see isType(int) */
  public int getType();
  
  public String getMimetype();

  /**
   * Returns the type of the node on the server representing this resource.
   * @return QualifiedName
   */
  public String getNodeType();

  /**
   * Returns <code>true</code> if this resource has the aspect specified. <code>false</code> otherwise.
   * @param aspect the aspect to check
   * @return boolean
   * @throws ResourceException */
  public boolean hasAspect(String aspect);

  /**
   * Returns the aspects that have been applied to this resource.
   * @return the aspects for this resource * @throws ResourceException */
  public List<String> getAspects();

  /**
   * Returns  <code>true</code> if this resource has the mimetype specified. <code>false</code> otherwise.
   * @param mimetype
   * @return
   */
  public boolean hasMimetype(String mimetype);


}
