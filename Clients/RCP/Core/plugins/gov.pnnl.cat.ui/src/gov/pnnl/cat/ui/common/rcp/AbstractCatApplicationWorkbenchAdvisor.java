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
package gov.pnnl.cat.ui.common.rcp;

import gov.pnnl.cat.core.internal.resources.events.JmsConnectionListener;
import gov.pnnl.cat.core.internal.resources.events.NotificationManagerJMS;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.util.ProxyConfig;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.net.IVeloFileSystemManager;
import gov.pnnl.cat.net.VeloNetworkPlugin;
import gov.pnnl.cat.ui.UiPlugin;
import gov.pnnl.cat.ui.utils.StatusLineCLabelContributionItem;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.albireo.core.AwtEnvironment;
import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.core.internal.net.ProxyManager;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.security.storage.SecurePreferencesContainer;
import org.eclipse.equinox.internal.security.storage.SecurePreferencesWrapper;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.provider.IProviderHints;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.model.ContributionComparator;
import org.eclipse.ui.model.IComparableContribution;
import org.eclipse.ui.model.IContributionService;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * Base class for CAT-derived ApplicationWorkbenchAdvisors.  This class adds
 * support for restarting CAT (occasionally required to pick up CIFS changes).
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("restriction")
public abstract class AbstractCatApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

  protected IWorkbenchConfigurer configurer;
  protected Logger logger = CatLogger.getLogger(AbstractCatApplicationWorkbenchAdvisor.class);

  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
   */
  @Override
  public void initialize(IWorkbenchConfigurer configurer) {
    super.initialize(configurer);
    configurer.setSaveAndRestore(true);
    this.configurer = configurer;
  }

  /**
   * Method createWorkbenchWindowAdvisor.
   * @param configurer IWorkbenchWindowConfigurer
   * @return WorkbenchWindowAdvisor
   */
  @Override
  public abstract WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer);

  /**
   * Method getInitialWindowPerspectiveId.
   * @return String
   */
  @Override
  public String getInitialWindowPerspectiveId() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor#preStartup()
   */
  @Override
  public void preStartup() {
    super.preStartup();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor#postStartup()
   */
  @Override
  public void postStartup() {
    // 1) Set Eclipse proxy preferences (for auto-updates)
    setEclipseProxyPreferences();
    
    // Initialize JMS
    initializeJMS();

    // Initialize swing/swt bridge
    initializeAWT();

    // Initialize local drive
    initializeMappedDrive();
    
  }
  
  protected void initializeMappedDrive() {
    IProgressService service = PlatformUI.getWorkbench().getProgressService();
    try {
      service.run(false, false, new IRunnableWithProgress(){
        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {    
          SwingUtilities.invokeAndWait(new Runnable() {
            
            @Override
            public void run() {
              try {
                IVeloFileSystemManager mgr = VeloNetworkPlugin.getVeloFileSystemManager();
                
                // todo run this in background thread with progress, since it could take a long time
                mgr.mapLocalDrive();
                
              } catch (Throwable e) {
                ToolErrorHandler.handleError("Failed to map local Velo drive.", e, true);
              }
              
            }
          });         
        }
      });
    } catch (Exception e) {
      ToolErrorHandler.handleError("Failed to map local Velo drive.", e, true);
    } 
    
  }

  protected void removeMappedDrive() {
    
    IProgressService service = PlatformUI.getWorkbench().getProgressService();
    try {
      service.run(false, false, new IRunnableWithProgress(){
        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {    
          SwingUtilities.invokeAndWait(new Runnable() {
            
            @Override
            public void run() {
              IVeloFileSystemManager mgr = VeloNetworkPlugin.getVeloFileSystemManager();
              
              if(mgr.isMappedDriveInUse()){
                int selection = JOptionPane.showConfirmDialog(null, "Upload is in progress.  If you unmount now any pending uploads will be canceled.  Do you want to unmount still?");
                if(selection == JOptionPane.CANCEL_OPTION || selection == JOptionPane.NO_OPTION){
                  return;
                }
              }
              
              mgr.unmapLocalDrive();
              
            }
          });

        }
      });
    } catch (Exception e) {
      ToolErrorHandler.handleError("Failed to unmap local Velo drive.", e, true);
    }       

  }
  
  protected void initializeAWT() {
    // To avoid deadlocks on mac, run this from background thread
    // instead of SWT event thread
    Runnable runnable = new Runnable() {
      
      @Override
      public void run() {
        AwtEnvironment.setSyncModalDialogs(false);
        AwtEnvironment.getInstance(Display.getDefault());        
      }
    };
    Thread thread = new Thread(runnable);
    thread.start();
  }
  
  protected void initializeJMS() {

    // Try to make the JMS connection
    // Start the JMS connect in a background job
    Job startNotificationsJob = new Job("Connecting to Messaging Server") {

      @Override
      protected IStatus run(IProgressMonitor monitor) {

        monitor.beginTask("Connecting to Messaging Server...", IProgressMonitor.UNKNOWN);
        JmsConnectionListener listener = new VeloJmsConnectionListener();
        ResourcesPlugin.getNotificationManager().connectToServer(listener);
        monitor.done();


        return Status.OK_STATUS;
      }
    };
    startNotificationsJob.setUser(true);
    startNotificationsJob.setPriority(Job.SHORT);
    startNotificationsJob.schedule();

  }

  protected void setEclipseProxyPreferences() {
    // make sure the secure store does not prompt for password recovery information
    ISecurePreferences basePrefs = SecurePreferencesFactory.getDefault();
    SecurePreferencesContainer prefsContainer = ((SecurePreferencesWrapper) basePrefs).getContainer();
    prefsContainer.setOption(IProviderHints.PROMPT_USER, new Boolean(false));        
    
    
    ProxyConfig proxyConfig = ResourcesPlugin.getProxyConfig();
    if(proxyConfig.getHost() != null) {
      try {

        // if proxy detected, we need to set the proxy settings in eclipse so the update manager will be able to connect
        String type = proxyConfig.getRepositoryProtocol().toUpperCase();
        ProxyData proxy = new ProxyData(type, proxyConfig.getHost(), proxyConfig.getPort(), 
            proxyConfig.isProxyAuthenticationRequired(), "velo");
        IProxyData[] proxies = new ProxyData[]{proxy};
        if(proxyConfig.isProxyAuthenticationRequired()) {
          proxy.setUserid(proxyConfig.getProxyUsername());
          proxy.setPassword(proxyConfig.getProxyPassword());
        }

        IProxyService service = ProxyManager.getProxyManager();
        service.setProxiesEnabled(true);
        service.setSystemProxiesEnabled(false);
        service.setProxyData(proxies);

      } catch (Throwable e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

    }
  }


  /* (non-Javadoc)
   * @see org.eclipse.ui.application.WorkbenchAdvisor#postShutdown()
   */
  @Override
  public void postShutdown() {
    try {
    	
      // remove our JMS connection
      ResourcesPlugin.getNotificationManager().shutdown();
      
    } catch (Exception e) {
    	logger.error("Failed to shut down notification manager", e);
    }
    
    // unmap our local velo drive
    
  }

  /** 
   * We are overriding this method so we can set the sort order for Property Pages!
   * @see org.eclipse.ui.application.WorkbenchAdvisor#getComparatorFor(java.lang.String)
   */
  @Override
  public ContributionComparator getComparatorFor(String contributionType) {
    ContributionComparator cc;
    
    if (contributionType.equals(IContributionService.TYPE_PROPERTY)) {
      cc = new PropertyPageComparator();
    } else {
      cc = super.getComparatorFor(contributionType);
    }

    return cc;
  }

  public class PropertyPageComparator extends ContributionComparator {

    @Override
    public int compare(IComparableContribution c1, IComparableContribution c2) {
      int result;
      
      IPluginContribution pc1 = (IPluginContribution)c1;
      IPluginContribution pc2 = (IPluginContribution)c2;
   
      String id1 = pc1.getLocalId().substring(0,3);
      String id2 = pc2.getLocalId().substring(0,3);      
      
      Integer idx1 = null;
      Integer idx2 = null;
      try {
        idx1 = Integer.valueOf(id1);
      } catch (Throwable e) {
      }
      
      try {
        idx2 = Integer.valueOf(id2);
      } catch (Throwable e) {
        
      }
      
      if(idx1 == null && idx2 == null) {
        result = super.compare(c1, c2);     
      } else if (idx1 == null && idx2 != null) {
        result = 1;
      } else if (idx1 != null && idx2 == null) {
        result = -1;
      } else {
        result = idx1.compareTo(idx2);
      }
   
      return result;
    }
    
  }

  public class VeloJmsConnectionListener implements JmsConnectionListener {
    MessageDialog lastDisplayedDialog;
    int lastMessagingStatus = 0;

    private MessageDialog getMessageDialog(String title, String message) {
      // close any previous message dialogs before we show this latest one
      if(lastDisplayedDialog != null && lastDisplayedDialog.getShell() != null && !lastDisplayedDialog.getShell().isDisposed()) {
        lastDisplayedDialog.getShell().dispose();
      }

      String[] labels = new String[] { IDialogConstants.OK_LABEL };
      MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), title, null, message,
          MessageDialog.INFORMATION, labels, 0);

      lastDisplayedDialog = dialog;
      return dialog;
    }

    @Override
    public void connectionStatusChanged(final int status, final Throwable exception) {

      if(status != lastMessagingStatus) {
        lastMessagingStatus = status;

        // Update the window title to indicate messaging enabled
        Display.getDefault().asyncExec(new Runnable() {

          @Override
          public void run() {
            if (PlatformUI.getWorkbench() != null) {
              String jmsMsg = "messaging enabled";
              Image image = SWTResourceManager.getImage(UiPlugin.getDefault().getAbsolutePath("icons/16x16/information.gif"));
              if(!NotificationManagerJMS.isJMS_ENABLED()) {
                jmsMsg = "messaging disabled";
                image = SWTResourceManager.getImage(UiPlugin.getDefault().getAbsolutePath("icons/16x16/error.gif"));
              }

              for(IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                StatusLineManager slm = ((WorkbenchWindow)window).getStatusLineManager();
                for(IContributionItem item : slm.getItems()) {
                  if(item.getId().equals("veloMessagingStatus")) {
                    ((StatusLineCLabelContributionItem)item).setText(jmsMsg);
                    ((StatusLineCLabelContributionItem)item).setImage(image);
                  }
                }
              }
            }
            // do not reshow error box if it's already been displayed once
            if(status == JmsConnectionListener.DISCONNECTED) {
              // Display error message to user
              String message = "Unable to connect to messaging server.  You will need to manually refresh in order to pick up content changes.";
              String title = "Messaging Server Connection Broken";
              getMessageDialog(title, message).open();

            } else if (status == JmsConnectionListener.RECONNECTED) {
              String message = "The connection to the messaging server has been re-established.  You no longer need to manually refresh to see content changes.";
              String title = "Messaging Server Connection Restored";
              getMessageDialog(title, message).open();
            }
          }
        });

      }
    }

  }

}
