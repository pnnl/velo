package gov.pnnl.velo.sapphire.osti;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Length;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.InitialValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

import gov.pnnl.velo.sapphire.CountryPossibleValuesService;
import gov.pnnl.velo.sapphire.LanguagePossibleValuesService;
import gov.pnnl.velo.sapphire.dataset.RequiredSoftware;
import gov.pnnl.velo.sapphire.tmp.CountryValueLabelService;

public interface OSTI extends Element {

  // declare the element type - your class
  ElementType TYPE = new ElementType(OSTI.class);

  // ***** Language *****
  @Label(standard = "language")
  @XmlBinding(path = "language")
  @InitialValue(text = "English")
  @Service(impl = LanguagePossibleValuesService.class)
  @Required
  ValueProperty PROP_LANGUAGE = new ValueProperty(TYPE, "Language");

  Value<String> getLanguage();

  void setLanguage(String value);

  // ***** Country *****
  @Fact(statement = "Country of Origin/Publication")
  @Label(standard = "Country")
  @Required
  @XmlBinding(path = "country")
  @InitialValue(text = "United States")
//  @Services(value={
//      @Service(impl = CountryPossibleValuesService.class),
//      @Service(impl = CountryValueLabelService.class)
//  })
  @Service(impl = CountryPossibleValuesService.class)
  ValueProperty PROP_COUNTRY = new ValueProperty(TYPE, "Country");

  Value<String> getCountry();

  void setCountry(String value);

  // ***** DOE Contract Numbers *****
  @Type(base = DOEContractNumber.class)
  @Label(standard = "DOE Contract Number(s)")
  @XmlListBinding(path = "DOEContractNumbers", mappings = @XmlListBinding.Mapping(element = "DOEContractNumber", type = DOEContractNumber.class) )
  @Required
  @Length(min = 1)
  ListProperty PROP_DOE_CONTRACTS = new ListProperty(TYPE, "DOEContracts");

  ElementList<DOEContractNumber> getDOEContracts();

  // ***** Non-DOE Contract Numbers *****
  // optional field
  @Type(base = NonDOEContractNumber.class)
  @Label(standard = "Non-DOE Contract Number(s)")
  @XmlListBinding(path = "nonDOEContractNumbers", mappings = @XmlListBinding.Mapping(element = "nonDOEContractNumber", type = DOEContractNumber.class) )
  ListProperty PROP_NONDOE_CONTRACTS = new ListProperty(TYPE, "NonDOEContracts");

  ElementList<NonDOEContractNumber> getNonDOEContracts();

  // ***** Originating Research Organization (default is PNNL) *****
  @Type(base = ResearchOrganization.class)
  @Label(standard = "Research Organizations")
  @XmlListBinding(path = "researchOrganizations", mappings = @XmlListBinding.Mapping(element = "researchOrganization", type = ResearchOrganization.class) )
  @Required
  @Length(min = 1)
  ListProperty PROP_RESEARCH_ORGANIZATIONS = new ListProperty(TYPE, "ResearchOrganizations");

  ElementList<ResearchOrganization> getResearchOrganizations();

  // ***** Originating Research Organization (default is PNNL) *****
  @Type(base = SponsorOrganization.class)
  @Label(standard = "Sponsoring Organizations")
  @XmlListBinding(path = "sponsorOrganizations", mappings = @XmlListBinding.Mapping(element = "sponsorOrganitzation", type = SponsorOrganization.class) )
  @Required
  @Length(min = 1)
  ListProperty PROP_SPONSOR_ORGS = new ListProperty(TYPE, "SponsorOrgs");

  ElementList<SponsorOrganization> getSponsorOrgs();

  // ***** Required Software *****
  // optional field
  @Type(base = RequiredSoftware.class)
  @Label(standard = "Required Softwares")
  @Unique
  @Fact(statement = "Softwares required to read/interpret the data in this dataset")
  @XmlListBinding(path = "requiredSoftwares", mappings = @XmlListBinding.Mapping(element = "requiredSoftware", type = RequiredSoftware.class) )
  ListProperty PROP_REQUIRED_SOFTWARES = new ListProperty(TYPE, "RequiredSoftwares");

  ElementList<RequiredSoftware> getRequiredSoftwares();
}
