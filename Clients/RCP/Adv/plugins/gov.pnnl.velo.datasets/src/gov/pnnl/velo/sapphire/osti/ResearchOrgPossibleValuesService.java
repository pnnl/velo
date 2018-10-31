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

public class ResearchOrgPossibleValuesService extends PossibleValuesService {

  private static Logger logger = Logger.getLogger(ResearchOrgPossibleValuesService.class);

  @Text("\"${ResearchOrganization}\" is not in the list of organizations at https://www.osti.gov/elink/authorities.jsp  . The primary DOE organization should be listed first, followed by any others. If non-DOE orgs are included, input the spelled-out, full name of the organization")
  private static LocalizableText message;

  // private static boolean initialized = false;
  // private static LinkedHashSet<String> codes = new LinkedHashSet<String>();
  private static LinkedHashSet<String> researchOrgs = new LinkedHashSet<String>();

  private static HashMap<String, String> researchOrgMap = new HashMap<String, String>();

  static {
    LocalizableText.init(ResearchOrgPossibleValuesService.class);
  }

  protected void initPossibleValuesService() {
    this.invalidValueMessage = message.text();
    this.invalidValueSeverity = Status.Severity.WARNING; // user can enter values not in possible value list
    final URL fileUrl = FileLocator.find(DatasetsPlugin.getDefault().getBundle(), new Path("osti/research-organizations.txt"), null);
    try {
      final InputStream in = fileUrl.openStream();

      try {
        final BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        for (String line = r.readLine(); line != null; line = r.readLine()) {
          line = line.trim();
          int index = line.indexOf(" ");
          String researchOrg = line.substring(index + 1);
          String code = line.substring(0, index);
          researchOrgs.add(researchOrg);
          // codes.add(code);
          // researchOrgMap.put(code, researchOrg);
        }
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          logger.warn("Unable to close input stream for research-organizations.txt");
        }
      }
    } catch (IOException e) {
      // DatasetMetadataPlugin.log( e );
      // Sapphire does - Platform.getLog( getBundle() ); - where does this log?
      logger.warn("Unable to read list of research organizations:" + e);
    }
  }

  @Override
  protected void compute(Set<String> values) {
    values.addAll(researchOrgs);
  }

  @Override
  public boolean ordered() {
    return true;
  }

  public static String getResearchOrg(String code) {
    return researchOrgMap.get(code);
  }

}
