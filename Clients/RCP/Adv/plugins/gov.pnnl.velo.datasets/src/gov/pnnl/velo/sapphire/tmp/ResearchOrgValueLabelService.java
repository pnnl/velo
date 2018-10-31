package gov.pnnl.velo.sapphire.tmp;

import org.eclipse.sapphire.services.ValueLabelService;

import gov.pnnl.velo.sapphire.osti.ResearchOrgPossibleValuesService;

//NOT USED currently
//TODO: does OSTI need code or full name?
public final class ResearchOrgValueLabelService extends ValueLabelService {
  @Override
  public String provide(final String value) {
    if (value != null) {
      return ResearchOrgPossibleValuesService.getResearchOrg(value);
    }

    return value;
  }

}
