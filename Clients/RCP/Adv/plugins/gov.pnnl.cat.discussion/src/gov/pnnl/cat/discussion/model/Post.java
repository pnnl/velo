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
package gov.pnnl.cat.discussion.model;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;

/**
 */
public class Post {
  private IFile file;
  private IFolder topic;
  private String content;

  /**
   * Constructor for Post.
   * @param file IFile
   * @param topic IFolder
   * @param content String
   */
  public Post(IFile file, IFolder topic, String content) {
    this.file = file;
    this.topic = topic;
    this.content = content;
  }

  /**
   * Method getFile.
   * @return IFile
   */
  public IFile getFile() {
    return file;
  }

  /**
   * Method getTopic.
   * @return IFolder
   */
  public IFolder getTopic() {
    return topic;
  }

  /**
   * Method getContent.
   * @return String
   */
  public String getContent() {
    return content;
  }
}
