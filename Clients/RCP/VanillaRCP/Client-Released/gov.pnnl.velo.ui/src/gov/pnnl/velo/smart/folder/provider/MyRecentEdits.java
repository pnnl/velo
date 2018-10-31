package gov.pnnl.velo.smart.folder.provider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.search.advanced.query.StandaloneSearchQuery;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.ISmartFolder;
import gov.pnnl.velo.uircp.VeloUIPlugin;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloUIConstants;

//Returns files that have been edited
//in the last 2 weeks by the currently logged in user
public class MyRecentEdits implements ISmartFolder {

  @Override
  public String getName() {
    return "My Recent Edits";
  }

  @Override
  public Image getImage() {
    return SWTResourceManager.getPluginImage("gov.pnnl.velo.ui", "icons/16x16/smartFolder.png");
  }
  
  @Override
  public ImageDescriptor getImageDescriptor() {
    return SWTResourceManager.getPluginImageDescriptor("gov.pnnl.velo.ui", "icons/16x16/smartFolder.png");
  }

  @Override
  public String getQuery() throws Exception {
    final int NUMBER_OF_DAYS_AGO = -14;
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
    
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, NUMBER_OF_DAYS_AGO);
    
    StandaloneSearchQuery queryObject = new StandaloneSearchQuery();
    queryObject.setSearchString("");
    queryObject.addAttributeQuery(VeloConstants.PROP_MODIFIER, ResourcesPlugin.getSecurityManager().getUsername() );
    queryObject.addDateRange(VeloConstants.PROP_MODIFIED, cal.getTime(), new Date());
    queryObject.setContentType(StandaloneSearchQuery.CONTENT_TYPE_FILE);
    return queryObject.buildSearchQuery();
  }

}
