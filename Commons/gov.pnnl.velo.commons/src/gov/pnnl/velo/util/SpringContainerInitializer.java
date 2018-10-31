/**
 * 
 */
package gov.pnnl.velo.util;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Initializes spring container with provided bean config files.
 *
 */
public class SpringContainerInitializer {
  public static final String BEAN_FILE_NAME_TIF_SERVICES = "tif-services.xml";
  public static final String BEAN_FILE_NAME_CMS_SERVICES = "cms-services.xml";  

  private static ApplicationContext beanContainer;
  private static final Logger logger = Logger.getLogger(SpringContainerInitializer.class);

  /**
   * Method getBeanContainer.
   * @return ApplicationContext
   */
  public static synchronized ApplicationContext getBeanContainer() {
    return beanContainer;
  }

  public static ApplicationContext loadBeanContainerFromClasspath(String[] beanFileClasspaths) {
    if(beanFileClasspaths == null || beanFileClasspaths.length == 0) {
      //if we catch an exception trying to load the extensions, assume that we're not running
      //within an RCP and so try to find the config file by the hard coded path:
      beanFileClasspaths = new String[]{BEAN_FILE_NAME_CMS_SERVICES};
    }      

    if(logger.isDebugEnabled()) {
      logger.debug("loading " + beanFileClasspaths.length + " spring config files");
      for (int i = 0; i < beanFileClasspaths.length; i++ ) {
        logger.debug("loading spring config file: " + beanFileClasspaths[i]);
      }
    }
    beanContainer = new ClassPathXmlApplicationContext(beanFileClasspaths);
    return beanContainer;
  }

  public static ApplicationContext loadBeanContainerFromFilesystem(String[] beanFilePaths) {
    if(beanFilePaths == null || beanFilePaths.length == 0) {
      //if we catch an exception trying to load the extensions, assume that we're not running
      //within an RCP and so try to find the config file by the hard coded path:
      beanFilePaths = new String[]{"./" + BEAN_FILE_NAME_CMS_SERVICES};
    }      

    if(logger.isDebugEnabled()) {
      logger.debug("loading " + beanFilePaths.length + " spring config files");
      for (int i = 0; i < beanFilePaths.length; i++ ) {
        logger.debug("loading spring config file: " + beanFilePaths[i]);
      }
    }

    beanContainer = new FileSystemXmlApplicationContext(beanFilePaths);
    
    return beanContainer;

  }

}
