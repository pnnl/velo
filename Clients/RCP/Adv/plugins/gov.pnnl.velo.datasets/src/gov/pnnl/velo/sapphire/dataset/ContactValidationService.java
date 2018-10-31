package gov.pnnl.velo.sapphire.dataset;

import org.apache.commons.validator.routines.DateValidator;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

public final class ContactValidationService extends ValidationService {
  @Text("Exactly one author needs to be designated as the primary contact")
  private static LocalizableText onePrimaryAuthor;

  @Text("Atleast one author needs to be designated as corresponding author")
  private static LocalizableText correspondingAuthor;

  static {
    LocalizableText.init(ContactValidationService.class);
  }

  @Override

  protected Status compute() {
    final String text = (String) context(Value.class).content();

    if (text != null) {
      DateValidator validator = DateValidator.getInstance();

      if (validator.isValid(text)) {
        return Status.createOkStatus();
      } else {
        // final String message = messageFormat.format(text);
        return Status.createErrorStatus("test");
      }
    }

    return Status.createOkStatus();
  }

}
