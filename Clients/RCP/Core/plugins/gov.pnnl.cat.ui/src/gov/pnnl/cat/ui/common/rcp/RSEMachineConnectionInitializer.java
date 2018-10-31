package gov.pnnl.cat.ui.common.rcp;

import gov.pnnl.cat.rse.RSEUtils;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.IRSEModelInitializer;

public class RSEMachineConnectionInitializer implements IRSEModelInitializer {

  @Override
  public IStatus run(IProgressMonitor monitor) {
    
    // Initialize RSE Connections
    // TODO: this is hooked in via the org.eclipse.rse.core.modelInitializers extension point, but
    // this extension only gets called once on bootstrap.
    // We need to copy this code to the application workbench advisor on startup so that it 
    // gets called every time, so we can initialize new servers that get added at a later date
    // We need to have this code in both places because it MUST be here or else the local connection
    // won't initialize properly.
    try {
      RSEUtils.initializeRSEConnections();
      
    } catch (Throwable e) {
      ToolErrorHandler.handleError("Unable to initialize remote connections.", e, true);
    }

    return Status.OK_STATUS;

  }

}
