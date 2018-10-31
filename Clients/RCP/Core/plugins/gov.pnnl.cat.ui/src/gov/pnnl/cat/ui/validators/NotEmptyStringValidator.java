package gov.pnnl.cat.ui.validators;

import org.eclipse.jface.dialogs.IInputValidator;

/**
 * Valid if string is not null or empty
 * @author D3K339
 *
 */
public class NotEmptyStringValidator implements IInputValidator {

  /* (non-Javadoc)
   * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
   */
  @Override
  public String isValid(String input) {
    if (input == null || input.length() == 0)
      return " "; //$NON-NLS-1$

    return null;
  }

}