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
package gov.pnnl.cat.ui.rcp.handlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

/**
 */
public class FileCompareInput extends CompareEditorInput {
  File leftFile;
  File rightFile;

  /**
   * Constructor for FileCompareInput.
   * @param configuration CompareConfiguration
   * @param leftFile File
   * @param rightFile File
   */
  public FileCompareInput(CompareConfiguration configuration, File leftFile, File rightFile) {
    super(configuration);
    this.leftFile = leftFile;
    this.rightFile = rightFile;
  }

  /**
   * Method prepareInput.
   * @param monitor IProgressMonitor
   * @return Object
   * @throws InvocationTargetException
   * @throws InterruptedException
   */
  @Override
  protected Object prepareInput(IProgressMonitor monitor)
      throws InvocationTargetException, InterruptedException {
    Differencer d= new Differencer() {
      protected Object visit(Object parent, int description, Object ancestor, Object left, Object right) {
        return new MyDiffNode((IDiffContainer) parent, description, (ITypedElement)ancestor, (ITypedElement)left, (ITypedElement)right);
      }
    };
    MyCompareNode left=new MyCompareNode(leftFile);
    MyCompareNode right=new MyCompareNode(rightFile);
    return d.findDifferences(false, null, null, null, left, right);
  }
  
  /**
   * Method saveChanges.
   * @param monitor IProgressMonitor
   * @throws CoreException
   */
  @Override
  public void saveChanges(IProgressMonitor monitor) throws CoreException {
    // TODO Auto-generated method stub
    super.saveChanges(monitor);
  }
  
  /**
   * Method isDirty.
   * @return boolean
   */
  @Override
  public boolean isDirty() {
    // TODO Auto-generated method stub
    return super.isDirty();
  }
  
  /**
   */
  class MyDiffNode extends DiffNode {

    private boolean fDirty= false;
    private ITypedElement fLastId;
    private String fLastName;

    /**
     * Constructor for MyDiffNode.
     * @param parent IDiffContainer
     * @param description int
     * @param ancestor ITypedElement
     * @param left ITypedElement
     * @param right ITypedElement
     */
    public MyDiffNode(IDiffContainer parent, int description, ITypedElement ancestor, ITypedElement left, ITypedElement right) {
      super(parent, description, ancestor, left, right);
    }
    public void fireChange() {
      super.fireChange();
      setDirty(true);
      fDirty= true;

    }
    void clearDirty() {
      fDirty= false;
    }
    /**
     * Method getName.
     * @return String
     * @see org.eclipse.compare.structuremergeviewer.ICompareInput#getName()
     */
    public String getName() {
      if (fLastName == null)
        fLastName= super.getName();
      if (fDirty)
        return '<' + fLastName + '>';
      return fLastName;
    }

    /**
     * Method getId.
     * @return ITypedElement
     */
    public ITypedElement getId() {
      ITypedElement id= super.getId();
      if (id == null)
        return fLastId;
      fLastId= id;
      return id;
    }
  }

  /**
   */
  class MyCompareNode extends BufferedContent implements ITypedElement {

    private File fResource;

    /**
     * Constructor for MyCompareNode.
     * @param resource File
     */
    MyCompareNode (File resource) {
      fResource = resource;
    }

    /**
     * Method createStream.
     * @return InputStream
     * @throws CoreException
     */
    protected InputStream createStream() throws CoreException {
      InputStream is = null;
      try {
        is = new BufferedInputStream(new FileInputStream(fResource));
        return is;
        
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Method getImage.
     * @return Image
     * @see org.eclipse.compare.ITypedElement#getImage()
     */
    public Image getImage() {
      return null;
    }

    /**
     * Method getName.
     * @return String
     * @see org.eclipse.compare.ITypedElement#getName()
     */
    public String getName() {
      return null;
    }

    /**
     * Method getType.
     * @return String
     * @see org.eclipse.compare.ITypedElement#getType()
     */
    public String getType() {
      return ITypedElement.TEXT_TYPE;
    }
  }

}
