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
package gov.pnnl.cat.ui.rcp.adapters;



/**
 */
public interface ITransferObjectAdapter {
  
  public final static int TYPE_CAT_JAVA_FILE = 100;
  public final static int TYPE_RESOURCE_FILE = 101;
  
  /**
   * Method getChildren.
   * @return ITransferObjectAdapter[]
   */
  public ITransferObjectAdapter[] getChildren();

  /**
   * Method isFile.
   * @return boolean
   */
  public boolean isFile();
  
  /**
   * Method isFolder.
   * @return boolean
   */
  public boolean isFolder();
  
  /**
   * Method isVirtualFolder.
   * @return boolean
   */
  public boolean isVirtualFolder();
  
  /**
   * Method isLinked.
   * @return boolean
   */
  public boolean isLinked();
  
  /**
   * Method getLabel.
   * @return String
   */
  public String getLabel();

  /**
   * Method canRead.
   * @return boolean
   */
  public boolean canRead();

  /**
   * Method exists.
   * @return boolean
   */
  public boolean exists();

  /**
   * Method getType.
   * @return int
   */
  public int getType();

  /**
   * Method getObject.
   * @return Object
   */
  public Object getObject();

  /**
   * Method getPath.
   * @return String
   */
  public String getPath();

  /**
   * Method getSize.
   * @return long
   */
  public long getSize();
}
