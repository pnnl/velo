package gov.pnnl.cat.ui.rcp.editors;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

//Based on org.eclipse.ui.ide.FileStoreEditorInputFactory
/**
 * Factory for saving and restoring a <code>ResourceFileStoreEditorInput</code>.
 * The stored representation of a <code>ResourceFileStoreEditorInput</code> remembers
 * the path of the editor input.
 * <p>
 * The workbench will automatically create instances of this class as required.
 * It is not intended to be instantiated or subclassed by the client.</p>
 *
 * @since 3.3
 */
public class ResourceFileStoreEditorInputFactory implements IElementFactory {
  
  /**
   * This factory's ID.
   * <p>
   * The editor plug-in registers a factory by this name with
   * the <code>"org.eclipse.ui.elementFactories"<code> extension point.
   */
  static final String ID = "gov.pnnl.cat.ui.rcp.editors.ResourceFileStoreEditorInputFactory";
  
  /**
   * Tag for the URI string.
   */
  private static final String TAG_CMS_PATH= "cmspath"; //$NON-NLS-1$
  
  /**
   * Saves the state of the given editor input into the given memento.
   *
   * @param memento the storage area for element state
   * @param input the file editor input
   */
  static void saveState(IMemento memento, ResourceFileStoreEditorInput input) {
    IFile file = input.getFile();
    //file.getPath()
    memento.putString(TAG_CMS_PATH, file.getPath().toAssociationNamePath());
  }


  /*
   * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
   */
  @Override
  public IAdaptable createElement(IMemento memento) {
    // Get the file name.
    String cmsPath = memento.getString(TAG_CMS_PATH);
    if (cmsPath == null)
      return null;
    try {
      IResourceManager mgr = ResourcesPlugin.getResourceManager();
      IFile file = (IFile)mgr.getResource(new CmsPath(cmsPath));
      File localFile = mgr.getContentPropertyAsFile(file.getPath(), VeloConstants.PROP_CONTENT);
      IFileStore fileStore = EFS.getStore(localFile.toURI());
      return new ResourceFileStoreEditorInput(fileStore, file);
    } catch (CoreException e) {
      return null;
    }
  }
}
