package gov.pnnl.velo.tools.ui;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.SWTUtil;
import gov.pnnl.velo.core.util.ThreadUtils;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * Utilities to run Eclipse ProgressService to avoid ugly
 * nested runnables.  You only need to use this class if you are using a Swing UI or
 * some Swing dialogs in the class that has a long running job.
 * @author d3k339
 *
 */
public class ToolProgressService {
  private static final Logger logger = CatLogger.getLogger(ToolProgressService.class);

  public static void threadSafeExecuteWithProgress(IRunnableWithProgress runnable) throws Exception {
    if(!ThreadUtils.isSWTThread()) {
      IProgressMonitor monitor = ToolProgressService.getProgressMonitor();
      runnable.run(monitor);
    
    } else {
      IProgressService service = PlatformUI.getWorkbench().getProgressService();
      try {
        service.run(false, false, runnable);
      } catch (RuntimeException e) {
        throw e;
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  /**
   * Gets a progress monitor in a thread-safe way that can be updated
   * from a non-SWT thread without ui deadlock.
   * This MUST be called from non-SWT thread or it will deadlock.
   * Callers MUST call IProgressMonitor.setCancelled(true) when the work is done
   * so this thread can complete.
   * @return
   */
  public static IProgressMonitor getProgressMonitor() {
    assert(!ThreadUtils.isSWTThread());

    final IProgressMonitor[] progressMonitor = {null};
    final ProgressMonitorDialog[] pmd = {null};
    final boolean[] done = {false};
    final Shell shells[] = {null};
  
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        try {
          // Pop up the dialog in a new shell that is on top of any swing windows
          Shell shell = SWTUtil.getCenteredDialogShell();
          shells[0] = shell;

          ProgressMonitorDialog pmdialog= new ProgressMonitorDialog(shell); 
          pmdialog.setBlockOnOpen(false);
          pmd[0] = pmdialog;
          pmdialog.open();
          progressMonitor[0] = new ThreadSafeProgressMonitor(pmdialog.getProgressMonitor());
        } finally {
          done[0] = true;
        }
      }

    });

    while (done[0] == false) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    Runnable bridge = new Runnable() {      
      @Override
      public void run() {
        while(!progressMonitor[0].isCanceled()) {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        Display.getDefault().asyncExec(new Runnable() {
          
          @Override
          public void run() {
            pmd[0].close(); 
            shells[0].dispose(); // make sure the shell is disposed
          }
        });
        
      }
    };
    
    Thread thread = new Thread(bridge);
    thread.start();
    
    return progressMonitor[0];
  }
  
  public static class ThreadSafeProgressMonitor implements IProgressMonitor {
    private IProgressMonitor monitor;
    private boolean cancelled = false;
    
    public ThreadSafeProgressMonitor(IProgressMonitor monitor) {
      this.monitor = monitor;
    }

    @Override
    public void beginTask(final String name, final int totalWork) {
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
            monitor.beginTask(name, totalWork);
        }
      });

    }

    @Override
    public void done() {
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
            monitor.done();
        }
      });    
    }

    @Override
    public void internalWorked(final double work) {
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
            monitor.internalWorked(work);
        }
      });
    }

    @Override
    public boolean isCanceled() {
      return cancelled;
    }

    @Override
    public void setCanceled(final boolean value) {
      this.cancelled = value;
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
            monitor.setCanceled(value);
        }
      });
    }

    @Override
    public void setTaskName(final String name) {
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
            monitor.setTaskName(name);
        }
      });   
    }

    @Override
    public void subTask(final String name) {
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
            monitor.subTask(name);
        }
      });   
    }

    @Override
    public void worked(final int work) {
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
            monitor.worked(work);
        }
      });  
    }
    
  }
}
