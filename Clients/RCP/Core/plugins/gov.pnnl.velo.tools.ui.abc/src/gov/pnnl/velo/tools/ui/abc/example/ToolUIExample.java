package gov.pnnl.velo.tools.ui.abc.example;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import abc.test.ABCExample;
import datamodel.DataItem;
import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.tools.Tool;
import gov.pnnl.velo.tools.ui.abc.ToolUIABCDefault;
import gov.pnnl.velo.util.ClasspathUtils;
import gov.pnnl.velo.util.SpringContainerInitializer;
import vabc.ABCDocument;
import vabc.IABCActionProvider;
import vabc.IABCDataProvider;

public class ToolUIExample extends ToolUIABCDefault {

	private static final long serialVersionUID = 1L;
	
	Map<String, List<DataItem>> dataModel;

  ABCExample example = new ABCExample(this);  
  
  public static void main(String[] args) {
    try {
      // configure spring beans and repository.properties
      initializeServices();
      
      // Set System L&F
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    Tool tool = new TestTool();
    ToolUIExample toolUI = new ToolUIExample();
    CmsPath toolInstancePath = new CmsPath("/User Documents/admin/testTool");
    IFolder instanceDir = (IFolder) CmsServiceLocator.getResourceManager().getResource(toolInstancePath);
    List<IResource> selectedResources = new ArrayList<IResource>();
    selectedResources.add(instanceDir);
    toolUI.initializeContext(tool, selectedResources);
    
    toolUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
  private static void initializeServices() throws Exception {
    
    // Configure system properties for Velo to work properly (can also be provided as runtime args)
    
    System.setProperty("logfile.path", "./velo.log");
    System.setProperty("repository.properties.path", "D:\\Akuna\\repository.properties.akunaDev");
    
    SpringContainerInitializer.loadBeanContainerFromClasspath(null);
    ISecurityManager securityManager = CmsServiceLocator.getSecurityManager();    
    securityManager.login("Carina", "Carina1");

  }  

  @Override
  public JPanel getSummaryView() {
    return new JPanel(); // Not used
  }

  @Override
  protected Map<String, Object> getMenuItems() {
    return new HashMap<String, Object>();
  }

  @Override
  public Map<File, CmsPath> getFilesToSave() {
    Map<File, CmsPath> files = super.getFilesToSave();

    return files;
  }

  @Override
  protected File getAbcConfigFile() {
    return ClasspathUtils.getFileFromClassFolder( ABCDocument.class, "geochemistry.xml");
  }

  @Override
  public IABCDataProvider getDataProvider() {
    return example;
  }

  @Override
  public IABCActionProvider getActionProvider() {
    return example;
  }
  
  

}
