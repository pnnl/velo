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
package gov.pnnl.cat.ui.rcp.editors;

import gov.pnnl.cat.core.resources.IFile;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInputFactory;

/**
 */
public class ResourceFileStoreEditorInput extends FileStoreEditorInput {

  private IFile file;

  /**
   * Method getFile.
   * @return IFile
   */
  public IFile getFile() {
    return file;
  }

  /**
   * Method setFile.
   * @param file IFile
   */
  public void setFile(IFile file) {
    this.file = file;
  }

  /**
   * Constructor for ResourceFileStoreEditorInput.
   * @param fileStore IFileStore
   */
  public ResourceFileStoreEditorInput(IFileStore fileStore) {
    super(fileStore);
    // TODO Auto-generated constructor stub
  }

  /**
   * Constructor for ResourceFileStoreEditorInput.
   * @param fileStore IFileStore
   * @param file IFile
   */
  public ResourceFileStoreEditorInput(IFileStore fileStore, IFile file) {
    super(fileStore);
    this.file = file;
  }
  
  
  @Override
  public String getFactoryId() {
    return ResourceFileStoreEditorInputFactory.ID;
  }

  @Override
  public void saveState(IMemento memento) {
    ResourceFileStoreEditorInputFactory.saveState(memento, this);

  }
}
