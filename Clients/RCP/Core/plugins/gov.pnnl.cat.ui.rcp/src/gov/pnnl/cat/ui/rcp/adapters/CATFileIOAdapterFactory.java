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

import org.eclipse.core.runtime.IAdapterFactory;

/**
 */
public class CATFileIOAdapterFactory implements IAdapterFactory {

  public static final Class[] types = { ITransferObjectAdapter.class };
  
  /**
   * Method getAdapter.
   * @param adaptableObject Object
   * @param adapterType Class
   * @return Object
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(Object, Class)
   */
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if(adaptableObject instanceof CATFileIO && adapterType == ITransferObjectAdapter.class){
      return new CATFileIOAdapter(adaptableObject);
    }
    return null;
  }

  /**
   * Method getAdapterList.
   * @return Class[]
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
   */
  public Class[] getAdapterList() {
    return types;
  }

}
