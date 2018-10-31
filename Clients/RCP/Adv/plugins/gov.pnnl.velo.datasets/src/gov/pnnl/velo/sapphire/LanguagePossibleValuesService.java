package gov.pnnl.velo.sapphire;

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

public class LanguagePossibleValuesService extends PossibleValuesService {

  private static Logger logger = Logger.getLogger(LanguagePossibleValuesService.class);

  @Text("\"${Language}\" is not a valid Language.")
  private static LocalizableText message;

  // private static boolean initialized = false;
  private static LinkedHashSet<String> languages = new LinkedHashSet<String>();

  static {
    LocalizableText.init(LanguagePossibleValuesService.class);
  }

  protected void initPossibleValuesService() {
    this.invalidValueMessage = message.text();
    final URL launguagesUrl = FileLocator.find(DatasetsPlugin.getDefault().getBundle(), new Path("osti/languages.txt"), null);
    try {
      final InputStream in = launguagesUrl.openStream();

      try {
        final BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        for (String line = r.readLine(); line != null; line = r.readLine()) {
          languages.add(line.trim());
        }
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          logger.warn("Unable to close input stream for languages.txt");
        }
      }
    } catch (IOException e) {
      // DatasetMetadataPlugin.log( e );
      // Sapphire does - Platform.getLog( getBundle() ); - where does this log?
      logger.warn("Unable to read list of launguages:" + e);
    }
  }

  @Override
  protected void compute(Set<String> values) {
    values.addAll(languages);
  }

  @Override
  public boolean ordered() {
    return true;
  }

}
