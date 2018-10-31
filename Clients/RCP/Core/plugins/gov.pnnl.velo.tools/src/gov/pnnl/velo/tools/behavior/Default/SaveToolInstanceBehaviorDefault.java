package gov.pnnl.velo.tools.behavior.Default;

import java.awt.Component;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.dialogs.CATSaveAsDialog;
import gov.pnnl.cat.ui.rcp.dialogs.ResourceSelectionValidator;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.util.SWTUtil;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.RepositoryContainer;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.tools.Tool;
import gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior;
import gov.pnnl.velo.tools.ui.ToolProgressService;
import gov.pnnl.velo.tools.ui.ToolUI;
import gov.pnnl.velo.tools.util.ToolUtils;
import gov.pnnl.velo.util.VeloConstants;

public class SaveToolInstanceBehaviorDefault implements SaveToolInstanceBehavior {

	private IResourceManager resourceManager = ResourcesPlugin.getResourceManager();
	private Tool tool;

	/**
	 * Constructor
	 * @param tool
	 */
	public SaveToolInstanceBehaviorDefault() {
	}

	@Override
	public void setTool(Tool tool) {
		this.tool = tool;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.cat.ui.rcp.handlers.CustomSaveAsBehavior#saveAs(gov.pnnl.cat.core.resources.IResource)
	 */
	@Override
	public boolean saveAs(IResource resource) throws RuntimeException {
		String mimetype = ResourcesPlugin.getResourceManager().getProperty(resource.getPath(), VeloConstants.PROP_MIMETYPE);
		if(mimetype.equalsIgnoreCase(tool.getMimetype())) {
			try {
				Map<File, CmsPath> filesToSave = new HashMap<File, CmsPath>();
				Map<String, String> propertiesToSave = new HashMap<String, String>();
				saveAs((IFolder)resource, null, filesToSave, propertiesToSave, false);
			} catch (Throwable e) {
				ToolErrorHandler.handleError("Failed to save tool.", e, true);
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.cat.ui.rcp.handlers.CustomPasteBehavior#paste(gov.pnnl.cat.core.resources.IResource, gov.pnnl.cat.core.resources.IResource, boolean)
	 */
	@Override
	public boolean paste(IResource source, IResource destinationParent, boolean isMove) throws RuntimeException {
		// we only care if we are pasting our tool
		if(ToolUtils.hasMimetype(source, tool.getMimetype())) {
			try {
				Map<File, CmsPath> filesToSave = new HashMap<File, CmsPath>();
				Map<String, String> propertiesToSave = new HashMap<String, String>();
				saveAs((IFolder)source, null, filesToSave, propertiesToSave, isMove);

			} catch (Throwable e) {
				ToolErrorHandler.handleError("Failed to save tool.", e, true);
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior#isSaveAllowed(gov.pnnl.cat.core.resources.IFolder)
	 */
	@Override
	public boolean isSaveAllowed(IFolder toolInstanceDir) {
		return true;
	}

	protected IFolder copyToolInstanceDir(IFolder toolInstanceDir, Map<String, Object> saveAsInfo) {
		CmsPath destination = (CmsPath) saveAsInfo.get(SAVE_DESTNATION);

		// If we are saving over ourself, then we don't have to copy anything first
		if(!destination.equals(toolInstanceDir)) {
			ResourcesPlugin.getResourceManager().copy(toolInstanceDir.getPath(), destination, true);
			return (IFolder) ResourcesPlugin.getResourceManager().getResource(destination);
		}
		return toolInstanceDir;
	}

	protected IFolder moveToolInstanceDir(IFolder toolInstanceDir, Map<String, Object> saveAsInfo) {
		CmsPath destination = (CmsPath) saveAsInfo.get(SAVE_DESTNATION);

		// TODO: do we need to delete the destination if it already exists?
		ResourcesPlugin.getResourceManager().move(toolInstanceDir.getPath(), destination);
		return (IFolder) ResourcesPlugin.getResourceManager().getResource(destination);
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior#saveAs(gov.pnnl.cat.core.resources.IFolder, gov.pnnl.velo.tools.ui.ToolUIFactory, java.util.Map, java.util.Map)
	 */
	@Override
	public void saveAs(final IFolder toolInstanceDir, final ToolUI toolUI,
			final Map<File, CmsPath> filesToSave, final Map<String, String> propertiesToSave, final boolean isMove) throws Exception {

		final Map<String, Object> saveAsInfo = new HashMap<String, Object>();
		int option = promptForSaveAs(toolInstanceDir, toolUI, saveAsInfo);
		// TODO: prompt for additional info

		if(option == SAVE_AS) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {        
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("Saving", 7);

						monitor.worked(1);
						monitor.worked(1);

						// first copy/move the existing tool
						IFolder newToolFolder = null;
						if(isMove) {
							newToolFolder = moveToolInstanceDir(toolInstanceDir, saveAsInfo);
						} else {
							newToolFolder = copyToolInstanceDir(toolInstanceDir, saveAsInfo);
						}
						monitor.worked(3);
					  //TODO: fix files to save. change old path with new
						CmsPath oldToolPath = toolInstanceDir.getPath();
						int oldToolPathSize  = oldToolPath.size();
						
						for (File f : filesToSave.keySet()){
						  CmsPath filePath = filesToSave.get(f);
						  CmsPath fileNewPath = newToolFolder.getPath();
						  for (int i=oldToolPathSize; i<filePath.size() ; i++){
						    fileNewPath.append(filePath.get(i));
						  }
						  filesToSave.put(f, fileNewPath);
						}
						// now save our current state to the new tool (if we are doing a true saveAs, and the current state has not been persisted yet)
						save(newToolFolder, filesToSave, propertiesToSave, monitor);
						

						if(toolUI != null && !newToolFolder.getPath().equals(toolInstanceDir.getPath())) {
							toolUI.changeContext(newToolFolder);
						}

//						if(toolUI != null) {
//							toolUI.setDirty(false);
//						}
						
						tool.getOpenToolInstanceBehavior().updateToolReference(toolInstanceDir,newToolFolder);
					} catch(Throwable e){
					  throw new RuntimeException(e);
					}
					  finally {
						monitor.setCanceled(true);
					}

				}
			};
			ToolProgressService.threadSafeExecuteWithProgress(runnable);

		} // else cancelled
	}  

	/* (non-Javadoc)
	 * @see gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior#save(gov.pnnl.cat.core.resources.IFolder, gov.pnnl.velo.tools.ui.ToolUIFactory, java.util.Map, java.util.Map)
	 */
	@Override
	public int save(IFolder toolInstanceDir, ToolUI toolUI, 
			Map<File, CmsPath> filesToSave, Map<String, String> propertiesToSave) throws Exception {
		int option = CANCEL;

		if(toolUI.isDirty() && isSaveAllowed(toolInstanceDir)) {
			option = promptForSave(toolInstanceDir, toolUI, null);
			save(option, toolInstanceDir, toolUI, filesToSave, propertiesToSave);
		}
		return option;
	}

	@Override
	public void save(int option, IFolder toolInstanceDir, ToolUI toolUI, 
			Map<File, CmsPath> filesToSave, Map<String, String> propertiesToSave) throws Exception {
		if(option == SAVE) {
			IProgressMonitor monitor = ToolProgressService.getProgressMonitor();
			monitor.beginTask("Saving", 3);
			try {
				clearResults(toolInstanceDir);
				monitor.worked(1);
				save(toolInstanceDir, filesToSave, propertiesToSave, monitor);
				toolUI.setDirty(false);

			} finally {
				monitor.setCanceled(true);
			}

		} else if (option == SAVE_AS) {
			saveAs(toolInstanceDir, toolUI, filesToSave, propertiesToSave, false);      
		} // else cancelled
	}

	protected void save(IFolder toolInstanceDir, Map<File, CmsPath> filesToSave, 
			final Map<String, String> propertiesToSave, IProgressMonitor monitor) throws Exception { 
		IResourceManager mgr = ResourcesPlugin.getResourceManager();
		monitor.subTask("Saving files to server...");
		mgr.setProperties(toolInstanceDir.getPath(), propertiesToSave);
		monitor.worked(1);
		mgr.bulkUpload(filesToSave, monitor, true);         
		monitor.worked(1);
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior#clearResults(gov.pnnl.cat.core.resources.IFolder)
	 */
	@Override
	public void clearResults(IFolder toolInstanceFolder) {

	}

	/* (non-Javadoc)
	 * @see gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior#promptForSave(gov.pnnl.cat.core.resources.IFolder, gov.pnnl.velo.tools.ui.ToolUI, java.util.Map)
	 */
	@Override
	public int promptForSave(IFolder toolInstanceDir, ToolUI toolUI, Map<String, Object> saveInfo) {
		return SAVE;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior#promptForSaveOnClose(gov.pnnl.velo.tools.ui.ToolUI)
	 */
	@Override
	public int promptForSaveOnClose(ToolUI toolUI) {
		Component parent = null;
		if(toolUI instanceof JFrame) {
			parent = (JFrame)toolUI;
		}
		int reply = JOptionPane.showConfirmDialog(parent,
				"Save changes before closing?", "Save Changes",
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (reply == JOptionPane.YES_OPTION) {
			return SAVE;
		} 
		if (reply == JOptionPane.NO_OPTION) {
			return NO;
		}
		return CANCEL;
	}

	/* (non-Javadoc)
	 * @see gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior#promptForSaveAs(gov.pnnl.cat.core.resources.IFolder, gov.pnnl.velo.tools.ui.ToolUI, java.util.Map)
	 */
	@Override
	public int promptForSaveAs(final IFolder toolInstanceDir, ToolUI toolUI, final Map<String, Object> saveAsInfo) {
		// bring up a Velo file chooser dialog so user can pick the destination folder
		Callable<Integer> cb = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {

				int result = CANCEL;
				Shell shell = SWTUtil.getCenteredDialogShell();
				RepositoryContainer root = RCPUtil.getTreeRoot();
				ResourceSelectionValidator validator = null;     
				CATSaveAsDialog saveAsDialog = new CATSaveAsDialog(shell, false, true, root, validator);

				// Set selection to current context
				IResource selResource = null;
				CmsPath sourcePath = toolInstanceDir.getPath();
				selResource = resourceManager.getResource(sourcePath);
				if (selResource != null) {
					saveAsDialog.setOriginalFile(selResource);
				}
				saveAsDialog.setBlockOnOpen(true);
				saveAsDialog.create();  

				int openResult = saveAsDialog.open();
				shell.dispose(); // make sure the shell is disposed

				if (openResult == Dialog.OK && saveAsDialog.getResult() != null) {
					CmsPath destPath = saveAsDialog.getResult();

					saveAsInfo.put(SAVE_DESTNATION, destPath);

					if(destPath.equals(toolInstanceDir.getPath())) { // we are saving as the same tool
						result = SAVE;
					} else {
						result = SAVE_AS;
					}

					if(resourceManager.resourceExists(destPath)) { // we are saving over a resource that already exists
						String[] buttons = new String[] { IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
						String question = "The resource " + destPath.toDisplayString() + " already exists. Do you want to replace it?";
						MessageDialog d = new MessageDialog(Display.getCurrent().getActiveShell(),
								"Question",
								null, question, MessageDialog.QUESTION, buttons, 0) {
							protected int getShellStyle() {
								return super.getShellStyle() | SWT.SHEET;
							}
						};
						int overwrite = d.open();
						switch (overwrite) {
						case 0: // Yes
							break;
						case 1: // No
						case 2: // Cancel
						default:
							result = CANCEL;
						}

					}

				} 

				return result;
			}
		};

		Integer option = SWTUtil.blockingAsyncExec(cb);
		return option;
	}



}
