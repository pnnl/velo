package gov.pnnl.velo.sapphire;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Text;

import gov.pnnl.velo.dataset.DatasetsPlugin;

public class CountryPossibleValuesService extends PossibleValuesService {

  private static Logger logger = Logger.getLogger(CountryPossibleValuesService.class);

  @Text("\"${Country}\" is not a valid Country.")
  private static LocalizableText message;

  // private static boolean initialized = false;
  //private static LinkedHashSet<String> codes = new LinkedHashSet<String>();
  private static LinkedHashSet<String> countries = new LinkedHashSet<String>();
  private static HashMap<String, String> countryLookup = new HashMap<String, String>();
  private static HashMap<String, String> codeLookup = new HashMap<String, String>();

  static {
    LocalizableText.init(CountryPossibleValuesService.class);
  }

  protected void initPossibleValuesService() {
    this.invalidValueMessage = message.text();
    final URL fileUrl = FileLocator.find(DatasetsPlugin.getDefault().getBundle(), new Path("osti/countries.txt"), null);
    try {
      final InputStream in = fileUrl.openStream();

      try {
        final BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        for (String line = r.readLine(); line != null; line = r.readLine()) {
          String country = line.trim().substring(3);
           String countryCode = line.trim().substring(0, 2);
           //codes.add(countryCode);
           countries.add(country);
           //countryLookup.put(countryCode, country);
           codeLookup.put(country, countryCode);
        }
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          logger.warn("Unable to close input stream for osti/countries.txt");
        }
      }
    } catch (IOException e) {
      // DatasetMetadataPlugin.log( e );
      // Sapphire does - Platform.getLog( getBundle() ); - where does this log?
      logger.warn("Unable to read list of countries:" + e);
    }
  }

  @Override
  protected void compute(Set<String> values) {
    values.addAll(countries);
  }

  @Override
  public boolean ordered() {
    return true;
  }

  public static String getCountryByCode(String code) {
    return countryLookup.get(code);
  }
  
  public static String getCodeByCountry(String country){
    return codeLookup.get(country);
  }

}
