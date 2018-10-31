package gov.pnnl.velo.sapphire.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Text;

import gov.pnnl.velo.dataset.DatasetsPlugin;

public class LicensePossibleValuesService extends PossibleValuesService {

  private static Logger logger = Logger.getLogger(LicensePossibleValuesService.class);

  @Text("\"${License}\" is not a valid License.")
  private static LocalizableText message;

  // private static boolean initialized = false;
  private static LinkedHashSet<String> licenses = new LinkedHashSet<String>();

  static {
    LocalizableText.init(LicensePossibleValuesService.class);
  }

  protected void initPossibleValuesService() {
    this.invalidValueMessage = message.text();
    final URL fileUrl = FileLocator.find(DatasetsPlugin.getDefault().getBundle(), new Path("dataset/licenses.txt"), null);
    try {
      final InputStream in = fileUrl.openStream();

      try {
        final BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        for (String line = r.readLine(); line != null; line = r.readLine()) {
          String license = line.trim();
          licenses.add(license);
        }
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          logger.warn("Unable to close input stream for dataset/licenses.txt");
        }
      }
    } catch (IOException e) {
      // DatasetMetadataPlugin.log( e );
      // Sapphire does - Platform.getLog( getBundle() ); - where does this log?
      logger.warn("Unable to read list of licenses:" + e);
    }
  }

  @Override
  protected void compute(Set<String> values) {
    values.addAll(licenses);
  }

  @Override
  public boolean ordered() {
    return true;
  }

}
