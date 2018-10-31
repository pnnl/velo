package gov.pnnl.velo.dataset.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.dataset.util.DatasetConstants;
import gov.pnnl.velo.dataset.util.DatasetUtil;

public class PublishDataset extends AbstractHandler {
  private Logger logger = CatLogger.getLogger(this.getClass());

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute(ExecutionEvent event) throws ExecutionException {
    IFolder dataset = DatasetUtil.getSelectedDatasetInActiveWindow();

    
    //code to add/remove publishing props for testing landing page only
//    List<String> props = new ArrayList<String>();
//    props.add(DatasetConstants.PROP_DOI_STATE);
//    props.add(DatasetConstants.PROP_DOI); //publishDate
//    props.add(DatasetConstants.createQNameString(DatasetConstants.NAMESPACE_DS, "publishDate")); //publishDate
//    
//    ResourcesPlugin.getResourceManager().removeProperties(dataset.getPath(), props);
//    ResourcesPlugin.getResourceManager().removeAspect(dataset.getPath(), DatasetConstants.ASPECT_DOI);
//    ResourcesPlugin.getResourceManager().setProperty(dataset.getPath(), DatasetConstants.PROP_DOI_STATE, DatasetConstants.DOI_STATE_FINAL);
//    ResourcesPlugin.getResourceManager().setProperty(dataset.getPath(), DatasetConstants.PROP_DOI, "abcdefg-123456-hijklmnop-789-qrstuvwxyz");
//    ResourcesPlugin.getResourceManager().addAspect(dataset.getPath(), DatasetConstants.ASPECT_DOI);

    MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Publish Dataset", "Datasets cannot yet be published.");
    return null;
  }
}
