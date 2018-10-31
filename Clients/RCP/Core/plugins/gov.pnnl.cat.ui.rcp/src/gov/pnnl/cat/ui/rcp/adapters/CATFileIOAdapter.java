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

import java.io.File;

/**
 */
public class CATFileIOAdapter implements ITransferObjectAdapter {

  File file = null;
  
  /**
   * Constructor for CATFileIOAdapter.
   * @param obj Object
   */
  public CATFileIOAdapter(Object obj) {
    file = (File) obj;
  }
  
  /**
   * Method getChildren.
   * @return ITransferObjectAdapter[]
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getChildren()
   */
  public ITransferObjectAdapter[] getChildren() {
    if (file.isDirectory()) {
      File[] fileArray = file.listFiles();
      ITransferObjectAdapter[] array = new ITransferObjectAdapter[fileArray.length];
      for (int i = 0; i < fileArray.length; i++) {
        CATFileIO catFile = new CATFileIO(fileArray[i].getPath());
        array[i] = (ITransferObjectAdapter) catFile.getAdapter( ITransferObjectAdapter.class );
      }
              
      return array;
    }
    
    return new ITransferObjectAdapter[0];
  }

  /**
   * Method isFile.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#isFile()
   */
  public boolean isFile() {
    if (file.isFile()) {
      return true;
    }
    return false;
  }

  /**
   * Method isFolder.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#isFolder()
   */
  public boolean isFolder() {
    if (file.isDirectory()) {
      return true;
    }
    return false;
  }

  /**
   * Method getLabel.
   * @return String
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getLabel()
   */
  public String getLabel() {
    return file.getName();
  }

  /**
   * Method canRead.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#canRead()
   */
  public boolean canRead() {
    return file.canRead();
  }

  /**
   * Method exists.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#exists()
   */
  public boolean exists() {
    return file.exists();
  }

  /**
   * Method getType.
   * @return int
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getType()
   */
  public int getType() {
    return ITransferObjectAdapter.TYPE_CAT_JAVA_FILE;
  }

  /**
   * Method getObject.
   * @return Object
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getObject()
   */
  public Object getObject() {
    return file;
  }

  /**
   * Method getPath.
   * @return String
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getPath()
   */
  public String getPath() {
    return file.getPath();
  }

  /**
   * Method isVirtualFolder.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#isVirtualFolder()
   */
  public boolean isVirtualFolder() {
    return false;
  }

  /**
   * Method isLinked.
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#isLinked()
   */
  public boolean isLinked() {
    return false;
  }

  /**
   * Method getSize.
   * @return long
   * @see gov.pnnl.cat.ui.rcp.adapters.ITransferObjectAdapter#getSize()
   */
  public long getSize() {
    return file.length();
  }

}
