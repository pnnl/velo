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
import java.net.URI;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

/**
 * Provides an adaptable CAT wrapper around a java.io.File
 * @version $Revision: 1.0 $
 */
public class CATFileIO extends File implements IAdaptable {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor for CATFileIO.
   * @param pathname String
   */
  public CATFileIO(String pathname) {
    super(pathname);
  }
  
  /**
   * Constructor for CATFileIO.
   * @param parent String
   * @param child String
   */
  public CATFileIO(String parent, String child) {
    super(parent, child);
  }
  
  /**
   * Constructor for CATFileIO.
   * @param parent File
   * @param child String
   */
  public CATFileIO(File parent, String child) {
    super(parent, child);
  }
  
  /**
   * Constructor for CATFileIO.
   * @param uri URI
   */
  public CATFileIO(URI uri) {
    super(uri);
  }
  
  /**
   * Method getAdapter.
   * @param adapter Class
   * @return Object
   */
  public Object getAdapter(Class adapter) {
    return Platform.getAdapterManager().getAdapter(this, adapter);
  }

}
