package gov.pnnl.velo.dataset.smartfolder.provider;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;

import gov.pnnl.cat.search.advanced.query.StandaloneSearchQuery;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.ISmartFolder;
import gov.pnnl.velo.dataset.util.DatasetConstants;

//Returns files that have been edited
//in the last 2 weeks by the currently logged in user
public class MyDatasets implements ISmartFolder {

  @Override
  public String getName() {
    return "My Datasets";
  }

  @Override
  public Image getImage() {
    return SWTResourceManager.getPluginImage("gov.pnnl.cat.ui", "icons/16x16/smartFolder.png");
  }
    
  @Override
  public ImageDescriptor getImageDescriptor() {
    return SWTResourceManager.getPluginImageDescriptor("gov.pnnl.cat.ui", "icons/16x16/smartFolder.png");
  }


  @Override
  public String getQuery() throws Exception {
  
    StandaloneSearchQuery queryObject = new StandaloneSearchQuery();
    queryObject.setSearchString("");
    queryObject.addIncludeAspect(DatasetConstants.ASPECT_DATASET);
    return queryObject.buildSearchQuery();
  }

}
