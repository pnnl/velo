package gov.pnnl.velo.sapphire.osti;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.InitialValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

//class level label is used in the "add child" link of ElementList<ResearchOrganization> field
@Label(full = "Originating Research Organization", standard = "Research Organization")
public interface ResearchOrganization extends Element {
  ElementType TYPE = new ElementType(ResearchOrganization.class);

  @Service(impl = ResearchOrgPossibleValuesService.class)
  @InitialValue(text = "Pacific Northwest National Laboratory (PNNL), Richland, WA (United States)")
  // if xmlbinding path="" value property will not get its own element such as <ResearchOrganization>.
  // The string value of PROP_RESEARCH_ORGANIZATION will be text within the parent's xml element
  @XmlBinding(path = "")
  @Unique
  @Required
  @Fact(statement = "The primary DOE organization should be listed first, followed by any others.  If non-DOE orgs are included, input the spelled-out, full name of the organization")
  ValueProperty PROP_RESEARCH_ORGANIZATION = new ValueProperty(TYPE, "ResearchOrganization");

  Value<String> getResearchOrganization();

  void setResearchOrganization(String value);

}
