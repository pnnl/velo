package gov.pnnl.cat.core.resources.tests.myemsl;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloTifConstants;

import java.util.HashMap;
import java.util.Map;

public class TestGetDateProperty extends AbstractVeloTest {

  public TestGetDateProperty(String[] commandLineArgs) throws Exception {
    super(commandLineArgs);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void run() throws Exception {
//    // get property
//    CmsPath path = new CmsPath("/Velo/projects/test-Dec-16/test-Dec16-2013/UQ-Dec16/Outputs/run99");
//    QualifiedName qname = VeloConstants.PROP_parseQNameStringSafe("{http://www.pnl.gov/velo/model/content/1.0}stop_time");
//
//    // No time zoe
//    // Tue Dec 17 14:17:58 2013
//    // Sun Dec 15 19:51:42 2013
//    String dateStr = resourceManager.getProperty(path, qname);
//    System.out.println(dateStr);
//   
//    // convert to date
//    Date jobStartTime = new Date(dateStr);
//    System.out.println(jobStartTime.toString());
//    Date jobStopTime = new Date();
//    
//      long runtimems = (jobStopTime.getTime()) - (jobStartTime.getTime());
//      System.out.println("runtime "+ runtimems);
//      long hours = TimeUnit.MILLISECONDS.toHours(runtimems);
//      System.out.println("hours "+ hours);
//      long minutes = TimeUnit.MILLISECONDS.toMinutes(runtimems) - (60 * hours);
//      System.out.println("minutes "+ minutes);
//      long seconds = TimeUnit.MILLISECONDS.toSeconds(runtimems) - (60* minutes);
//      System.out.println("seconds "+ seconds);
//
//      String jobTime = String.format("%d hrs %d mins %d secs", 
//          hours, 
//          minutes, 
//          seconds);
//
//      System.out.println(jobTime);

    CmsPath path = new CmsPath("/Velo");
    Map<String, String> properties = new HashMap<String, String>();
    for(String property : VeloTifConstants.JOB_PROPERTIES) {
      properties.put(property, null);
    }
    resourceManager.setProperties(path, properties);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    try {
      TestGetDateProperty test = new TestGetDateProperty(args);
      test.run();

    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Done");
    System.exit(0);
  
  }

}
