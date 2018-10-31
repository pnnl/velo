package gov.pnnl.velo.sapphire.tmp;

import org.eclipse.sapphire.services.ValueLabelService;

//OSTI needs country code
//This class is used when user needs to search based on country name and value to be set in the model is country code
//if using this, in the possibleValuesService class add the codes(the value that should actually be saved in the model)
//Update - this doesn't work well as valuelabelservice is not called when you do Ctrl+space to see the list of values
//value label service is only called when you hit the browse button!!!!!
public final class CountryValueLabelService extends ValueLabelService {
  @Override
  public String provide(final String value) {
    if (value != null) {
     // return CountryPossibleValuesService.getCountryByCode(value);
    }
    return value;
  }
}
