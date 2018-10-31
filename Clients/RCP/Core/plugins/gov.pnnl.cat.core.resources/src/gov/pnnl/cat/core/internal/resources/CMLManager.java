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

import gov.pnnl.cat.logging.CatLogger;

import java.util.Vector;

import org.alfresco.webservice.types.CMLAddAspect;
import org.alfresco.webservice.types.CMLCopy;
import org.alfresco.webservice.types.CMLCreate;
import org.alfresco.webservice.types.CMLDelete;
import org.alfresco.webservice.types.CMLMove;
import org.alfresco.webservice.types.CMLRemoveAspect;
import org.alfresco.webservice.types.CMLUpdate;
import org.alfresco.webservice.types.CMLWriteContent;
import org.apache.log4j.Logger;

/**
 */
public class CMLManager {
  
  private final static int MAX_CML = 50;
  private int iCounter = 0;
  private int iCMLTotal = 0;
  private CMLSet currentSet = new CMLSet();
  private Vector<CMLSet> vecCMLSets = new Vector<CMLSet>();  
  protected static Logger logger = CatLogger.getLogger(CMLManager.class);

  /**
   * Method getCurCMLSetAndCount.
   * @return CMLSet
   */
  private CMLSet getCurCMLSetAndCount() {

    if (iCounter == MAX_CML) {
      vecCMLSets.add(currentSet);
      iCounter = 0;
      currentSet = new CMLSet();
    }
    iCounter++;
    iCMLTotal++;
    return currentSet;
  }
  
  /**
   * Method getCMLTotal.
   * @return int
   */
  public int getCMLTotal() {
    return iCMLTotal;
  }
  
  /**
   * Method add.
   * @param create CMLCreate
   */
  public void add(CMLCreate create) {
    getCurCMLSetAndCount().add(create);
  }
  
  /**
   * Method add.
   * @param addAspect CMLAddAspect
   */
  public void add(CMLAddAspect addAspect) {
    getCurCMLSetAndCount().add(addAspect);
  }
  
  /**
   * Method add.
   * @param removeAspect CMLRemoveAspect
   */
  public void add(CMLRemoveAspect removeAspect) {
    getCurCMLSetAndCount().add(removeAspect);
  }
  
  /**
   * Method add.
   * @param update CMLUpdate
   */
  public void add(CMLUpdate update) {
    getCurCMLSetAndCount().add(update);
  }
  
  /**
   * Method add.
   * @param copy CMLCopy
   */
  public void add(CMLCopy copy) {
    getCurCMLSetAndCount().add(copy);
  }
  
  /**
   * Method add.
   * @param move CMLMove
   */
  public void add(CMLMove move) {
    getCurCMLSetAndCount().add(move);
  }
  
  /**
   * Method add.
   * @param delete CMLDelete
   */
  public void add(CMLDelete delete) {
    getCurCMLSetAndCount().add(delete);
  }
  
  /**
   * Method add.
   * @param writeContent CMLWriteContent
   */
  public void add(CMLWriteContent writeContent) {
    getCurCMLSetAndCount().add(writeContent);
  }

  /**
   * Method getCMLSets.
   * @return Vector<CMLSet>
   */
  public Vector<CMLSet> getCMLSets() {
    
    Vector<CMLSet> vecSets = (Vector<CMLSet>) vecCMLSets.clone();
   
    // Only add the current set if it has data in it
    if(currentSet.getSetSize() > 0) {
      vecSets.add(currentSet);
    }

    return vecSets;
  }

}

