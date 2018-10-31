package gov.pnnl.cat.ui.rcp.editors;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.statushandlers.StatusManager;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.perspectives.EditorPerspective;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.util.VeloConstants;

/**
 * @author zoe
 *
 */
public class VeloEditorUtil {

  public static String VELO_EDITOR_ATTRIBUTE = "enabledForVelo";
  public static List<String> VELO_EDITOR_IDS;
  protected static IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();

  
  
  static {
    loadVeloEditors();
  }
  

  /**
   * 
   */
  private static void loadVeloEditors() {
    VELO_EDITOR_IDS = new ArrayList<String>();

    // we only want to include editors that have the enabledForVelo attribute in the open with menu.
    // so we look at all registered editors and find only the ones with the "VeloEditor" annotation
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] editorElementExtensions = registry.getConfigurationElementsFor(PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_EDITOR);
    for (IConfigurationElement extension : editorElementExtensions) {
      try {
        String enabledForVelo = extension.getAttribute(VeloEditorUtil.VELO_EDITOR_ATTRIBUTE);
        String id = extension.getAttribute("id");
        if (enabledForVelo != null) {
          VELO_EDITOR_IDS.add(id);
        }
      } catch (Exception e) {// ignore exceptions, we just won't load that editor
        e.printStackTrace();
      }
    }
  }
  
  

  /**
   * getVeloEditors 
   * @param resourceFileName
   * @return returns all editors based on filename that have added in the plugin.xml for the editor the attribute enabledForVelo=true
   */
  public static List<IEditorDescriptor> getVeloEditors(String resourceFileName){
    IEditorDescriptor[] editors = editorRegistry.getEditors(resourceFileName);
    Collections.sort(Arrays.asList(editors), comparer);
    
    // Check that we don't add it twice. This is possible
    // if the same editor goes to two mappings.
    List<IEditorDescriptor> veloEditors = new ArrayList<>();

    for (int i = 0; i < editors.length; i++) {
      // only include editors that have the enabledForVelo xml attribute added in their declaration in plugin.xml
      IEditorDescriptor editor = editors[i];
      if (VeloEditorUtil.VELO_EDITOR_IDS.contains(editor.getId())) {
        if (!veloEditors.contains(editor)) {
          veloEditors.add(editor);
        }
      }
    }
    return veloEditors;
  }
  


  /**
   * @param monitor
   * @param editorDescriptor
   * @param files
   * 
   * open each file in the given editor
   */
  public static void openFilesInEditor(boolean openNewWindow, IProgressMonitor monitor, IEditorDescriptor editorDescriptor, IResource... files) {
    try {
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

      //open in new window if the editor area is not visable AND if no other editors are already opened in this window but are minimized
      if(openNewWindow && !PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().isEditorAreaVisible() && 
          window.getActivePage().getEditorReferences().length == 0){
        // First open editor perspective in new window
        window = PlatformUI.getWorkbench().openWorkbenchWindow(EditorPerspective.ID, null);
        window.getShell().forceActive();
      }
      
      
      IResourceManager mgr = ResourcesPlugin.getResourceManager();

      for (IResource resource : files) {
        IFile theFile = (IFile) resource;
        File localFile = mgr.getContentPropertyAsFile(theFile.getPath(), VeloConstants.PROP_CONTENT);

        IFileStore fileStore = EFS.getStore(localFile.toURI());
        // use the CAT Resource editor & input (that extends the defaults) so the 'save' will save contents back to alfresco
//        IEditorPart editor = ((WorkbenchPage) window.getActivePage()).openEditorFromDescriptor(new ResourceFileStoreEditorInput(fileStore, theFile), editorDescriptor, true, null);
        
        window.getActivePage().openEditor(new ResourceFileStoreEditorInput(fileStore, theFile),
            editorDescriptor.getId(), true, WorkbenchPage.MATCH_ID);
        
        if (monitor != null) {
          monitor.worked(1);
        }
      }

    } catch (Throwable e) {
      StatusUtil.handleStatus("Failed to open editor.", e, StatusManager.SHOW);
    }
  }

  
  
  /**
   * @param window
   * @param theFile
   * 
   * finds the editor to open using the file's name, then opens it. 
   */
  public static void openFilesInDefaultEditor(boolean openNewWindow, IWorkbenchWindow window, IFile theFile) {
    try {

      //open in new window if the editor area is not visable AND if no other editors are already opened in this window but are minimized
      if(openNewWindow && !PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().isEditorAreaVisible() && 
          window.getActivePage().getEditorReferences().length == 0){
        // First open editor perspective in new window
        window = PlatformUI.getWorkbench().openWorkbenchWindow(EditorPerspective.ID, null);
        window.getShell().forceActive();
      }
      
      IResourceManager mgr = ResourcesPlugin.getResourceManager();
      
      List<IEditorDescriptor> editors = getVeloEditors(theFile.getName());

      IEditorDescriptor editorDescriptor = null;
      if (editors.size() == 0) {
        editorDescriptor = editorRegistry.findEditor(ResourceTextEditor.RESOURCE_TEXT_EDITOR_ID);
      } else {
        editorDescriptor = editors.get(0);
      }

      File localFile = mgr.getContentPropertyAsFile(theFile.getPath(), VeloConstants.PROP_CONTENT);

      IFileStore fileStore = EFS.getStore(localFile.toURI());
      // use the CAT Resource editor & input (that extends the defaults) so the 'save' will save contents back to alfresco
      ((WorkbenchPage) window.getActivePage()).openEditorFromDescriptor(new ResourceFileStoreEditorInput(fileStore, theFile), editorDescriptor, true, null);

    } catch (Throwable e) {
      StatusUtil.handleStatus("Failed to open editor.", e, StatusManager.SHOW);
    }

  }

  
  
  
  /**
   * @param myInput
   * 
   * 
   */
  public static void saveToServer(ResourceFileStoreEditorInput myInput) {
    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
    try {
      mgr.createFile(myInput.getFile().getPath(), new File(myInput.getURI()));
    } catch (Throwable e) {
      ToolErrorHandler.handleError("Save failed.", e, true);
    }
  }
  
  
  
  
  /**
   * Compares the labels from two IEditorDescriptor objects
   */
  private static final Comparator<IEditorDescriptor> comparer = new Comparator<IEditorDescriptor>() {
    private Collator collator = Collator.getInstance();

    @Override
    public int compare(IEditorDescriptor arg0, IEditorDescriptor arg1) {
      String s1 = arg0.getLabel();
      String s2 = arg1.getLabel();
      return collator.compare(s1, s2);
    }
  };


}
