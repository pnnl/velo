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
package gov.pnnl.cat.ui.rcp;

import gov.pnnl.cat.core.resources.IResource;

import java.util.ArrayList;
import java.util.List;


/**
 */
public class CatClipboard {

  private static CatClipboard catClip = null;
  private List<IResource> contents;
  
  private CatClipboard() {
    contents = new ArrayList<IResource>();
  }

  /**
   * Method getInstance.
   * @return CatClipboard
   */
  public static CatClipboard getInstance() {
    if (catClip == null) {
      catClip = new CatClipboard();
    }
    return catClip;
  }
  
  /**
   * Method setContents.
   * @param contents List<IResource>
   */
  public void setContents(List<IResource> contents) {
    this.contents = contents;
  }
  
  /**
   * Method getContents.
   * @return List<IResource>
   */
  public List<IResource> getContents() {
    return contents;
  }
  
  /**
   * Method isEmpty.
   * @return boolean
   */
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }
  
  public void clearContents() {
    contents.clear();
  }
  
}
