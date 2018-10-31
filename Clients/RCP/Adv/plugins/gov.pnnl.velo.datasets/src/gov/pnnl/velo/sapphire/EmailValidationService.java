package gov.pnnl.velo.sapphire;

import org.apache.commons.validator.routines.EmailValidator;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

public final class EmailValidationService extends ValidationService {
  @Text("{0} is not a valid e-mail")
  private static LocalizableText messageFormat;

  static {
    LocalizableText.init(EmailValidationService.class);
  }

  @Override

  protected Status compute() {
    final String text = context(Value.class).text();

    if (text != null) {
      EmailValidator validator = EmailValidator.getInstance();

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
