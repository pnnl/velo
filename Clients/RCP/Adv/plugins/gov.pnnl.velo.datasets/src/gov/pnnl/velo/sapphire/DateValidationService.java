package gov.pnnl.velo.sapphire;

import org.apache.commons.validator.routines.DateValidator;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

public final class DateValidationService extends ValidationService {
  @Text("{0} is not a valid date")
  private static LocalizableText messageFormat;

  static {
    LocalizableText.init(DateValidationService.class);
  }

  @Override

  protected Status compute() {
    final String text = (String) context(Value.class).content();

    if (text != null) {
      DateValidator validator = DateValidator.getInstance();

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
