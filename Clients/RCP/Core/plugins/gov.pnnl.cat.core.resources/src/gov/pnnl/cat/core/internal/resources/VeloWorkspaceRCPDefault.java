package gov.pnnl.cat.core.internal.resources;

import gov.pnnl.velo.tif.service.impl.VeloWorkspaceDefault;

import java.io.File;

import org.eclipse.core.runtime.Platform;

/**
 * Default area for writing config files is the eclipse workspace's 
 * data dir.
 * @author d3k339
 *
 */
public class VeloWorkspaceRCPDefault extends VeloWorkspaceDefault {
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.impl.VeloWorkspaceDefault#getVeloFolder()
   */
  @Override
  public File getVeloFolder() {
    return new File(Platform.getInstanceLocation().getURL().getFile());
  }

}
