package gov.pnnl.cat.rse;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.utils.PerspectiveOpener;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.tif.model.ConnectionProtocol;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.service.MachineRegistry;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.globusonline.IGlobusOnlineConnectorService;
import org.eclipse.rse.internal.ui.actions.ShowInSystemsViewDelegate;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.jcraft.jsch.Session;


/**
 * Utility methods for dealing with all the RSE APIs
 * @author D3K339
 *
 */
public class RSEUtils {
  private static final Logger logger = Logger.getLogger(RSEUtils.class);
  
  public static final String SYSTEM_TYPE_SSH = "org.eclipse.rse.systemtype.ssh";
  public static final String SYSTEM_TYPE_GLOBUS_ONLINE_ONLY = "org.eclipse.rse.systemtype.globusonline";
  public static final String SYSTEM_TYPE_SSH_PLUS_GLOBUS_ONLINE = "org.eclipse.rse.systemtype.sshglobus";
  public static final String SYSTEM_TYPE_LOCAL = "org.eclipse.rse.systemtype.local";  
 
  
  // TODO:
  public static String getGlobusPath(String rsePath) {
    // If system type is sshglobus, then we know that the rsePath will be the absoulte filesystem path,
    // so we need to see if we need to parse off the globus path
    throw new NotImplementedException();
  }
  
  // TODO:
  public static String getFileSystemPath(String rsePath) {
    // If system type is globusonline, then we know that the rsePath is a globus online path, which may
    // not be the absolute file system path - we need to see if we need to prepend the globus path mapping
    throw new NotImplementedException();
  }
  
  public static IRemoteFile getRemoteResource(String hostName, String path) throws Exception {
    IRemoteFile file = null;
    
    IHost host = getHost(hostName);
    if(host == null) { // for some reason it hasn't been initialized yet
      host = initializeRSEConnection(hostName);
    }
    
    if(host != null && connectToHost(host)) {
      
      IFileServiceSubSystem ss = getFileSubSystem(host);
      Object[] searchResults = ss.resolveFilterString(path, new NullProgressMonitor());
      // TODO: why is it finding multiple results for files that don't have the same path??
      for(Object obj : searchResults ) {
        if(obj instanceof IRemoteFile && ((IRemoteFile)obj).getAbsolutePath().equals(path)) {
          file = (IRemoteFile)obj;
          break;
        }
      }
    }
    return file;
  }
  
  public static IHost initializeRSEConnection(String hostName) throws Exception {
    Machine machine = null;
    IHost host = null;
    MachineRegistry mReg = TifServiceLocator.getMachineRegistry();
    for(String machineId : mReg.getMachineIDs()) {
      Machine temp = mReg.get(machineId); 
      if(temp.getFullDomainName().equals(hostName)) {
        machine = temp;
        break;
      }
    }
    
    if(machine != null) {
      host = initializeRSEConnection(machine);
    } else {
      logger.error("Could not find a machine description for: " + hostName + " so unable to initialize RSE connection.");
    }
    
    return host;
  }
  
  public static IHost initializeRSEConnection(Machine machine) throws Exception {
    IHost host = null;
    List<ConnectionProtocol> connectionProtocols = machine.getConnectionProtocols();
    boolean supportsSsh = false;
    boolean supportsGlobusOnline = false;
    if(connectionProtocols.size() > 0) {
      for(ConnectionProtocol connectionProtocol : connectionProtocols) {
        if(connectionProtocol.getType().equals(ConnectionProtocol.TYPE_SSH)) {
          supportsSsh = true;
        } else if (connectionProtocol.getType().equals(ConnectionProtocol.TYPE_GLOBUS_ONLINE)) {
          supportsGlobusOnline = true;
        }
      }
    }
    String systemTypeId = null;
    
    if(supportsSsh && !supportsGlobusOnline) {
      // ssh only
      systemTypeId = SYSTEM_TYPE_SSH;
      
    } else if (supportsGlobusOnline && !supportsSsh) {
      // globus online only
      systemTypeId = SYSTEM_TYPE_GLOBUS_ONLINE_ONLY;
    
    } else if (supportsSsh && supportsGlobusOnline) {
      // hybrid
      systemTypeId = SYSTEM_TYPE_SSH_PLUS_GLOBUS_ONLINE; // TODO: switch to hybrid type
    }
    
    if(systemTypeId != null) {
      IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(systemTypeId);
      String hostName = machine.getFullDomainName();
      host = registerHost(hostName, systemType, machine);
    }
    return host;
  }
  
  public static void initializeRSEConnections() throws Exception {
    MachineRegistry mReg = TifServiceLocator.getMachineRegistry();
    if(mReg != null) {
      mReg.init();
      for(String machineId : mReg.getMachineIDs()) {
        Machine machine = mReg.get(machineId); 
        initializeRSEConnection(machine);
      }
    }
  }
  
  public static IHost registerHost(String hostName, IRSESystemType systemType, Machine machine) throws Exception {
    ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
    ISystemProfile profile = registry.getSystemProfileManager().getDefaultPrivateSystemProfile();
    IHost host = registry.getHost(profile, hostName);
    if (host == null) {      
      host = registry.createHost(systemType, hostName, hostName, "Connection to " + hostName);
    }
    
    // See if we need to configure globus online properties
    IFileServiceSubSystem fss = getFileSubSystem(host);
    IConnectorService cs = fss.getConnectorService();

    if(cs instanceof IGlobusOnlineConnectorService) {
      
      // set Globus properties
      ConnectionProtocol cp = machine.getConnectionProtocol(ConnectionProtocol.TYPE_GLOBUS_ONLINE);
      String endpointId = cp.getParameter(ConnectionProtocol.PARAMETER_GLOBUS_ENDPOINT_ID);
      String myProxyHost = cp.getParameter(ConnectionProtocol.PARAMTER_GLOBUS_ENDPOINT_MY_PROXY_HOST);
      String globalPath = cp.getParameter(ConnectionProtocol.PARAMETER_GLOBUS_ACCESSIBLE_PATH);
      ((IGlobusOnlineConnectorService)cs).setGlobusOnlineProperties(endpointId, myProxyHost, globalPath);
      
      // create filter to globus folder!
      
      
    }
    return host;
  }

  public static String getUsername(String hostName){
    IHost host = getHost(hostName);
    return getFileSubSystem(host).getUserId();
  }
  
  public static IHost getHost(String hostName) {
    ISystemRegistry registry = RSECorePlugin.getDefault().getSystemRegistry();
    ISystemProfile profile = registry.getSystemProfileManager().getDefaultPrivateSystemProfile();
    IHost host = registry.getHost(profile, hostName);
    return host;
  }
  
  /**
   * Get all the currently registered hosts - may or may not be connected.
   * @return
   */
  public static List<IHost> getRegisteredHosts() {
    IHost[] hostsArr = RSECorePlugin.getTheSystemRegistry().getHosts();
    List<IHost> hosts = new ArrayList<IHost>();
    for(IHost host : hostsArr) {
      hosts.add(host);
    }
    return hosts;
  }
  
  public static List<ISystemFilterReference> getFilters(Object selection, IRemoteFileSubSystem fss) {
    ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
    List<ISystemFilterReference> filterRefs = registry.findFilterReferencesFor(selection, fss, false);
    return filterRefs;
  }
  
  public static File createTemporaryFolder() throws Exception {
    File tempDir = File.createTempFile("tempFolder", "");
    tempDir.delete();
    tempDir.mkdir();
    return tempDir;
  }
 
  public static File createTemporaryFile(File parentFolder, String fileName) throws Exception {
    File file = new File(parentFolder, fileName);
    return file;
  }
  
  /**
   * Get contents of a remote file as a local temp file.  Note that this can't be done with files that
   * are located on a globus online endpoint.
   * This doesn't use cache checking - if you want to view/edit cached file, then use SystemEditableRemoteFile instead.
   * 
   * @param file
   * @return
   */
  public static void getRemoteFileContents(IRemoteFile remoteFile, File destinationFile) throws Exception {
    IFileServiceSubSystem fss = getFileSubSystem(remoteFile.getHost());
    fss.download(remoteFile, destinationFile.getAbsolutePath(), remoteFile.getEncoding(), new NullProgressMonitor());
  }
  
  /**
   * Look up the file subsystem for the given host
   * @param hostso 
   * @return
   */
  public static IFileServiceSubSystem getFileSubSystem(IHost host) {
    
    IRemoteFileSubSystem ss = RemoteFileUtility.getFileSubSystem(host);
    if(ss instanceof IFileServiceSubSystem) {
      IFileServiceSubSystem fss = (IFileServiceSubSystem)ss;
      return fss;     
    } else {
      // this better not happen
      throw new RuntimeException("remote file system: " + ss.getName() + " is not an instance of IFileServiceSubsystem");
    }

  }
  
  /**
   * Adapt the given object to an RSE object so we can get the right properties for label providers.
   * @param rseObject
   * @return
   */
  public static ISystemViewElementAdapter getRseViewAdapter(Object selected) {
  
    if (selected != null && selected instanceof IAdaptable) {
      return (ISystemViewElementAdapter)((IAdaptable)selected).getAdapter(ISystemViewElementAdapter.class);
    }
    return null;
  }
  
  public static void openInRemoteSystemsExplorer(IResource remoteLink) throws Exception {
    
    // 1) check if host exists
    String host = remoteLink.getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_MACHINE);
    String path = remoteLink.getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_PATH);

    if(host == null) {

      // this is probably the old remote link - need to parse the host from the url
      // ssh://carinal@hopper.nersc.gov:/scratch/scratchdirs/carinal/Argentina_load/SR2_copy_081514_165456/agni-out.xml
      String linkUrl = remoteLink.getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_URL);
      String hostStr = linkUrl.substring(linkUrl.indexOf('@') + 1);
      int colon = hostStr.indexOf(':');
      host = hostStr.substring(0, colon);
      path = hostStr.substring(colon + 1);

    }
    
    // 2) activate RSE perspective
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    String perspectiveID = "org.eclipse.rse.ui.view.SystemPerspective";
    PerspectiveOpener perspectiveOpener = new PerspectiveOpener(perspectiveID, null, window);      
    int returnCode = perspectiveOpener.openPerspectiveWithPrompt();
    if (returnCode == IDialogConstants.CANCEL_ID) {
      return;
    }
    IWorkbenchPage activePage = window.getActivePage();

    // 3) browse to file in tree
    IRemoteFile remoteFile = RSEUtils.getRemoteResource(host, path);
    if(remoteFile != null) {
      ShowInSystemsViewDelegate delegate = new ShowInSystemsViewDelegate();
      IAction dummyAction = new DummyAction();
      delegate.selectionChanged(dummyAction, new StructuredSelection(remoteFile));
      delegate.run(dummyAction);
    
    } else {
      ToolErrorHandler.handleError("File " + path + " no longer exists on " + host, null, true);
    }
  }
  
  public static void openInRemoteSystemsExplorer(IRemoteFile remoteFile) throws Exception {
    
    // 1) check if host exists
    IHost host = remoteFile.getHost();
    
    // 2) activate RSE perspective
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    String perspectiveID = "org.eclipse.rse.ui.view.SystemPerspective";
    PerspectiveOpener perspectiveOpener = new PerspectiveOpener(perspectiveID, null, window);      
    int returnCode = perspectiveOpener.openPerspectiveWithPrompt();
    if (returnCode == IDialogConstants.CANCEL_ID) {
      return;
    }
    IWorkbenchPage activePage = window.getActivePage();
    
    // browse to file in tree
    ShowInSystemsViewDelegate delegate = new ShowInSystemsViewDelegate();
    IAction dummyAction = new DummyAction();
    delegate.selectionChanged(dummyAction, new StructuredSelection(remoteFile));
    delegate.run(dummyAction);
    
  }

  public static Session getJschConnection(IHost host) {
    IFileServiceSubSystem fss = getFileSubSystem(host);
    IConnectorService cs = fss.getConnectorService();
    
    // TODO: we need to refactor the common ssh service APIs out and put them in a separate base plugin
    // so we don't have two copies :(
    Session session = null;
    if(cs instanceof org.eclipse.rse.internal.services.sshglobus.ISshSessionProvider) {
      session = ((org.eclipse.rse.internal.services.sshglobus.ISshSessionProvider)cs).getSession();
      
    } else if (cs instanceof org.eclipse.rse.internal.services.ssh.ISshSessionProvider) {
      session = ((org.eclipse.rse.internal.services.ssh.ISshSessionProvider)cs).getSession();
    }
    
    return session;
  }

  public static boolean connectToHost(final IHost host) {
    IProgressService service = PlatformUI.getWorkbench().getProgressService();
    final boolean[] connected = {false};
    try {
      service.run(false, false, new IRunnableWithProgress(){
        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {    
          try {
            // make sure the host is connected
            IRemoteFileSubSystem fileSS = RSEUtils.getFileSubSystem(host);
            //ISubSystem fileSS = host.getConnectorServices()[0].getSubSystems()[0];
            if(!fileSS.isConnected()) {
              monitor.beginTask("Connecting to server " + host.getName(), IProgressMonitor.UNKNOWN);
              fileSS.connect(new NullProgressMonitor(), false);
              connected[0] = true;
              monitor.done();
            } else {
              connected[0] = true;
            }
          } catch (Exception ex) {
            ToolErrorHandler.handleError("Failed to connect to server: " + host.getName(), ex, true);
          }
        }
      });
    } catch (Exception e) {
      ToolErrorHandler.handleError("Failed to connect to server: " + host.getName(), e, true);
    } 
    return connected[0];
  }

}

