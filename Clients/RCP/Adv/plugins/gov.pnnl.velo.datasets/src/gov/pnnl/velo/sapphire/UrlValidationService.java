package gov.pnnl.velo.sapphire;

import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

public final class UrlValidationService extends ValidationService {

  @Text("{0} is not a URL. Valid URLs should start with a \"http://\" or \"https://\" ")
  private static LocalizableText messageFormat;

  static {
    LocalizableText.init(UrlValidationService.class);
  }

  @Override
  protected Status compute() {
    final String text = context(Value.class).text();

    if (text != null) {
      String[] schemes = { "http", "https" }; // DEFAULT schemes = "http", "https", "ftp"
      UrlValidator validator = new UrlValidator(schemes);
      if (validator.isValid(text)) {
        return Status.createOkStatus();
      } else {
        final String message = messageFormat.format(text);
        return Status.createErrorStatus(message);
      }
    }

    return Status.createOkStatus();
  }

}
