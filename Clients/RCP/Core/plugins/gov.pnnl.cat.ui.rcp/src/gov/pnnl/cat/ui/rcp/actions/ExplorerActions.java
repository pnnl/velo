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
package gov.pnnl.cat.ui.rcp.actions;

import org.eclipse.jface.viewers.ContentViewer;

// test comment
/**
 */
public class ExplorerActions {

  /**
   * Method openWithAppAction.
   * @param strExt String
   * @param label String
   * @param view ContentViewer
   * @return OpenWithAppAction
   */
  public static OpenWithAppAction openWithAppAction(final String strExt, final String label, final ContentViewer view) {
    OpenWithAppAction action = new OpenWithAppAction(strExt, label);
    action.setViewer(view);
    return action;
  }


  /**
   * Method refreshFolder.
   * @param view ContentViewer
   * @return RefreshFolderAction
   */
  public static RefreshFolderAction refreshFolder(final ContentViewer view) {
    RefreshFolderAction action = new RefreshFolderAction();
    action.setViewer(view);
    return action;
  }

  /**
   * Method collapseAll.
   * @param view ContentViewer
   * @return CollapseAllAction
   */
  public static CollapseAllAction collapseAll(final ContentViewer view) {
    CollapseAllAction action = new CollapseAllAction();
    action.setViewer(view);
    return action;
  }

  /**
   * Method openFileInSystemEditorAction.
   * @param view ContentViewer
   * @param changeName boolean
   * @return OpenFileInSystemEditorAction
   */
  public static OpenFileInSystemEditorAction openFileInSystemEditorAction(final ContentViewer view, final boolean changeName) {
    OpenFileInSystemEditorAction action = new OpenFileInSystemEditorAction(changeName);
    action.setViewer(view);
    return action;
  }


//  public static OpenInNewTabHandler createOpenInNewWindowAction(final ICatView catPerspective, final ContentViewer view) {
//    OpenInNewTabHandler action = new OpenInNewTabHandler(catPerspective);
//    action.setViewer(view);
//    return action;
//  }


//  public static CutAction createCutAction(final ContentViewer view, final IResourceManager mgr) {
//    CutAction cutAction = new CutAction();
//    cutAction.setViewer(view);
////    cutAction.setResourceTreeManager(mgr);
//    return cutAction;
//  }
//
//
//  public static DeleteAction createDeleteAction(final ContentViewer view, final IResourceManager mgr) {
//    DeleteAction action = new DeleteAction();
//    action.setViewer(view);
//    return action;
//  }
//
//  
//  public static NewFolderAction createNewFolderAction(final ContentViewer view, final IResourceManager mgr) {
//    NewFolderAction action = new NewFolderAction();
//    action.setViewer(view);
////    action.setResourceTreeManager(mgr);
//    return action;
//  }


  //
  // public static Action createShortcutAction(final ContentViewer viewer, final
  // IResourceService mgr) {
  // Action action = new Action("Create &Shortcut") { //$NON-NLS-1$
  // public void run() {
  // // Create the shortcut.
  // // Get the selected file.
  // StructuredSelection selectedFile = (StructuredSelection)
  // viewer.getSelection();
  // for (Iterator iter = selectedFile.iterator(); iter.hasNext();) {
  // IResource resource = (IResource) ((CatItemNode) iter.next()).getResource();
  // selectedFile.getFirstElement();
  //
  // IResource origResource = null;
  // if (resource instanceof ILinkedResource) {
  // System.out.println("This is a link. Name = " + resource.getName());
  // origResource = ((ILinkedResource) resource).getTarget();
  // } else {
  // System.out.println("This is NOT a link. Name = " + resource.getName());
  // origResource = resource;
  // }
  //
  // try {
  // CmsPath pathToLinksParent = new
  // Path(resource.getParent().getPath().toOSString());
  //
  // // Test if the folder already has a file with this name.
  // IFolder parent = (IFolder) resource.getParent();
  // Vector children = parent.getChildren();
  // int availableIndex = 1;
  // availableIndex = nextAvailableLinkIndex(origResource, children,
  // availableIndex);
  //
  // CmsPath proposedPathOfNewLink;
  // if (availableIndex == 1) {
  // proposedPathOfNewLink = new Path(pathToLinksParent.append("Link to " +
  // origResource.getName()).toOSString());
  // } else {
  // proposedPathOfNewLink = new Path(pathToLinksParent.append("Link (" +
  // Integer.toString(availableIndex) + ") to " +
  // origResource.getName()).toOSString());
  // }
  // mgr.addLink(proposedPathOfNewLink, origResource);
  //
  // } catch (ResourceException e) {
  // // TODO Add error code to this action event.
  // logger.error(e);
  // }
  // }
  // }
  //
  // };
  // return action;
  // }


//  public static CopyAction createCopyAction(final ContentViewer viewer) {
//    CopyAction action = new CopyAction();
//    action.setViewer(viewer);
//    return action;
//  }


  /**
   * Creates the action "&Paste Shortcut".
   * 
   * @param viewer
   * @param mgr
   * @return
   */
//  public static PasteShortcutAction createPasteShortcutAction(final ContentViewer viewer, final IResourceManager mgr) {
//    PasteShortcutAction action = new PasteShortcutAction();
//    action.setViewer(viewer);
//    return action;
//  }


  /**
   * Create the action "Paste"
   * 
   * @param viewer
   * @param mgr
   * @return
   */
//  public static PasteAction createPasteAction(final ContentViewer viewer, final IResourceManager mgr) {
//    PasteAction action = new PasteAction();
//    action.setViewer(viewer);
//    
//    return action;
//  }
//
//  public static SelectAllAction createSelectAllAction(ContentViewer viewer) {
//    SelectAllAction action = new SelectAllAction();
//    action.setViewer(viewer);
//    
//    return action;
//  }
//
//
//  public static RenameAction createRenameAction(ContentViewer viewer, IResourceManager manager) {
//    RenameAction action = new RenameAction();
//    action.setViewer(viewer);
//    return action;
//  }
}
