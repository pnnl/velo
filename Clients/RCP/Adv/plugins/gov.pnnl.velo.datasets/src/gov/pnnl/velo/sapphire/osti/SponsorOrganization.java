package gov.pnnl.velo.sapphire.osti;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

//class level label is used in the "add child" link of ElementList<SponsorOrganization> field
@Label(full = "Sponsoring Organization Name", standard = "Sponsoring Organization")
public interface SponsorOrganization extends Element {
  ElementType TYPE = new ElementType(SponsorOrganization.class);

  @Service(impl = SponsoringOrgPossibleValuesService.class)
  // if xmlbinding path="" value property will not get its own element such as <SponsorOrg>.
  // The string value of PROP_SPONSOR_ORG will be text within the parent's xml element
  @XmlBinding(path = "")
  // this label is used in info/error text
  @Label(standard = "sponsor organization", full = "sponsor organization name")
  @Required
  @Unique
  @Fact(statement = "The primary DOE sponsor should be listed first, followed by any others. For non-DOE sponsors, please include the spelled-out, full name of the other sponsoring organization")
  ValueProperty PROP_SPONSOR_ORG = new ValueProperty(TYPE, "SponsorOrg");

  Value<String> getSponsorOrg();

  void setSponsorOrg(String value);

}
