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

import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.cat.ui.rcp.SystemManager;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 */
public abstract class JobAction extends ViewerAction {

  /**
   * Method runJob.
   * @param monitor IProgressMonitor
   * @return IStatus
   * @throws Exception
   */
  public abstract IStatus runJob(IProgressMonitor monitor) throws Exception;
  /**
   * Method getJobName.
   * @return String
   */
  public abstract String getJobName();
  public void setupJob() {}

  /**
   * Method isStandardJob.
   * @return boolean
   */
  public boolean isStandardJob() {
  	return true;
  }

  // test comment
  /**
   * Constructor for JobAction.
   * @param text String
   */
  public JobAction(String text) {
    super(text);
  }

  
  /**
   * Method run.
   * @see org.eclipse.jface.action.IAction#run()
   */
  final public void run() {
    
    setupJob();
    
    if (isStandardJob()) {
      final Job actionJob = new Job(getJobName()) {//before this was "Uploading Files"
        protected IStatus run(IProgressMonitor monitor) {
          IStatus result = null;
          try {
            result = runJob(monitor);
          } catch (Exception e) {
            int severity = IStatus.ERROR;  
            result = new Status(severity, CatRcpPlugin.PLUGIN_ID, 1, "Error running job.", e);         
          }
          return result;
        }
      };
      
      actionJob.addJobChangeListener(SystemManager.getInstance());
      actionJob.setUser(true);
      actionJob.setPriority(Job.LONG);
      actionJob.schedule();
    
    } else {
      IProgressService service = PlatformUI.getWorkbench().getProgressService();
      try {
        service.run(true, false, new IRunnableWithProgress(){
          public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            try {
              runJob(monitor); 
            } catch (Exception e) {
              throw new InvocationTargetException(e);
            }
          }
        });
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        logger.error(e);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        logger.error(e);
      }
    }
  }
 
  
}
