package gov.pnnl.cat.core.resources.tests.myemsl;

import gov.pnnl.cat.core.internal.resources.search.JsonCatQueryResult;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;

import java.io.File;

public class SearchTest extends AbstractVeloTest {
  public static String PROP_QUERY = "query";
  public static String PROP_TIMEOUT = "timeout";
  public static String PROP_BATCH_SIZE = "batch.size";
  public static String PROP_EXPECTED_HITS = "expected.hits";

  private File jsonSearchResultsFile;

  public SearchTest(String[] commandLineArgs) throws Exception {
    super(commandLineArgs);
    jsonSearchResultsFile = new File("SearchTestOutput.json");
    appendToFile(getOutputFile(), "Query #," + "Query Time (ms)," + "Total Hits");
  }

  @Override
  protected void run() throws Exception {
    int batchSize = Integer.valueOf(properties.getProperty(PROP_BATCH_SIZE));
    long expectedHits = Long.valueOf(properties.getProperty(PROP_EXPECTED_HITS));
    String query = properties.getProperty(PROP_QUERY);
    long timeoutMs = Long.valueOf(properties.getProperty(PROP_TIMEOUT)) * 1000;
    long currentTimeMs = System.currentTimeMillis();
    long endTime = currentTimeMs + timeoutMs;
    String json = null;
    int count = 1;
    
    while(currentTimeMs <= endTime) {
      long start = System.currentTimeMillis();
      // return only one page
      ICatQueryResult results = searchManager.query(query, false, null, null, batchSize, 1);
      long end = System.currentTimeMillis();

      String line = String.valueOf(count) + "," + String.valueOf(end-start) + "," + String.valueOf(results.getTotalHits());
      appendToFile(getOutputFile(), line);
      
      if(results.getTotalHits() >= expectedHits) {
        json = ((JsonCatQueryResult)results).getRawJson();
        break;
      }
      
      currentTimeMs = System.currentTimeMillis();
      count++;
    }
    appendToFile(jsonSearchResultsFile, json);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    try {
      SearchTest test = new SearchTest(args);
      test.run();

    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Done");
    System.exit(0);

  }

}
