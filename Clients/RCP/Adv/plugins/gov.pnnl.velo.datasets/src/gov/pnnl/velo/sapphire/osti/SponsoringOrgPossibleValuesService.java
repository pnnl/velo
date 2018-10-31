package gov.pnnl.velo.sapphire.osti;

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
import org.eclipse.sapphire.modeling.Status;

import gov.pnnl.velo.dataset.DatasetsPlugin;

public class SponsoringOrgPossibleValuesService extends PossibleValuesService {

  private static Logger logger = Logger.getLogger(SponsoringOrgPossibleValuesService.class);

  @Text("\"${SponsorOrganization}\" is not in the list of organizations at https://www.osti.gov/elink/authorities.jsp  . The primary DOE sponsor should be listed first, followed by any others. Please input the spelled-out, full name of the sponsoring organization")
  private static LocalizableText message;

  // private static boolean initialized = false;
  // private static LinkedHashSet<String> codes = new LinkedHashSet<String>();
  private static LinkedHashSet<String> sponsorOrgs = new LinkedHashSet<String>();

  private static HashMap<String, String> sponsorOrgMap = new HashMap<String, String>();

  static {
    LocalizableText.init(SponsoringOrgPossibleValuesService.class);
  }

  protected void initPossibleValuesService() {
    this.invalidValueMessage = message.text();
    this.invalidValueSeverity = Status.Severity.WARNING;// user can enter values not in possible value list
    readFile("osti/sponsor-organizations.txt");
    readFile("osti/historical-sponsor-organizations.txt");
  }

  private void readFile(String fileName) {

    final URL sponsorOrgUrl = FileLocator.find(DatasetsPlugin.getDefault().getBundle(), new Path(fileName), null);
    try {
      final InputStream in = sponsorOrgUrl.openStream();

      try {
        final BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        for (String line = r.readLine(); line != null; line = r.readLine()) {
          line = line.trim();
          int index = line.indexOf(":");
          String sponserOrg = line.substring(index + 1);
          // String code = line.substring(0, index);
          // codes.add(code);
          sponsorOrgs.add(sponserOrg);
          // sponsorOrgMap.put(code, sponserOrg);
        }
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          logger.warn("Unable to close input stream for sponsor organizations " + e);
        }
      }
    } catch (IOException e) {
      // DatasetMetadataPlugin.log( e );
      // Sapphire does - Platform.getLog( getBundle() ); - where does this log?
      logger.warn("Unable to read list of sponsor organizations:" + e);
    }
  }

  @Override
  protected void compute(Set<String> values) {
    values.addAll(sponsorOrgs);
  }

  @Override
  public boolean ordered() {
    return true;
  }

  protected static String getResearchOrg(String code) {
    return sponsorOrgMap.get(code);
  }

}
