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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.webservice.types.CMLAddAspect;
import org.alfresco.webservice.types.CMLCopy;
import org.alfresco.webservice.types.CMLCreate;
import org.alfresco.webservice.types.CMLDelete;
import org.alfresco.webservice.types.CMLMove;
import org.alfresco.webservice.types.CMLRemoveAspect;
import org.alfresco.webservice.types.CMLUpdate;
import org.alfresco.webservice.types.CMLWriteContent;

/**
 */
public class CMLSet {
  private List<CMLCreate>       creates = new ArrayList<CMLCreate>();
  private List<CMLAddAspect>    addAspects = new ArrayList<CMLAddAspect>();
  private List<CMLRemoveAspect> removeAspects = new ArrayList<CMLRemoveAspect>();
  private List<CMLUpdate>       updates = new ArrayList<CMLUpdate>();
  private List<CMLCopy>         copies  = new ArrayList<CMLCopy>();
  private List<CMLMove>         moves   = new ArrayList<CMLMove>();
  private List<CMLDelete>       deletes = new ArrayList<CMLDelete>();
  private List<CMLWriteContent> writeContents = new ArrayList<CMLWriteContent>();
  
  /**
   * Method add.
   * @param create CMLCreate
   */
  public void add(CMLCreate create) {
    creates.add(create);
  }
  /**
   * Method add.
   * @param addAspect CMLAddAspect
   */
  public void add(CMLAddAspect addAspect) {
    addAspects.add(addAspect);
  }
  /**
   * Method add.
   * @param removeAspect CMLRemoveAspect
   */
  public void add(CMLRemoveAspect removeAspect) {
    removeAspects.add(removeAspect);
  }
  /**
   * Method add.
   * @param update CMLUpdate
   */
  public void add(CMLUpdate update) {
    updates.add(update);
  }
  /**
   * Method add.
   * @param copy CMLCopy
   */
  public void add(CMLCopy copy) {
    copies.add(copy);
  }
  /**
   * Method add.
   * @param move CMLMove
   */
  public void add(CMLMove move) {
    moves.add(move);
  }
  /**
   * Method add.
   * @param delete CMLDelete
   */
  public void add(CMLDelete delete) {
    deletes.add(delete);
  }
  /**
   * Method add.
   * @param writeContent CMLWriteContent
   */
  public void add(CMLWriteContent writeContent) {
    writeContents.add(writeContent);
  }
  
  /**
   * Method getCreates.
   * @return List<CMLCreate>
   */
  public List<CMLCreate> getCreates() {
    return creates;
  }
  
  /**
   * Method getAddAspects.
   * @return List<CMLAddAspect>
   */
  public List<CMLAddAspect> getAddAspects() {
    return addAspects;
  }
  
  /**
   * Method getRemoveAspects.
   * @return List<CMLRemoveAspect>
   */
  public List<CMLRemoveAspect> getRemoveAspects() {
    return removeAspects;
  }
  
  /**
   * Method getUpdates.
   * @return List<CMLUpdate>
   */
  public List<CMLUpdate> getUpdates() {
    return updates;
  }
  
  /**
   * Method getCopies.
   * @return List<CMLCopy>
   */
  public List<CMLCopy> getCopies() {
    return copies;
  }
  
  /**
   * Method getMoves.
   * @return List<CMLMove>
   */
  public List<CMLMove> getMoves() {
    return moves;
  }
  
  /**
   * Method getDeletes.
   * @return List<CMLDelete>
   */
  public List<CMLDelete> getDeletes() {
    return deletes;
  }

  /**
   * Method getWriteContents.
   * @return List<CMLWriteContent>
   */
  public List<CMLWriteContent> getWriteContents() {
    return writeContents;
  }
  
  /**
   * Method getSetSize.
   * @return int
   */
  public int getSetSize() {
    return this.creates.size() + this.addAspects.size() + this.updates.size() + this.copies.size() + this.moves.size() + this.deletes.size() + this.removeAspects.size() + this.writeContents.size();
  }

}
