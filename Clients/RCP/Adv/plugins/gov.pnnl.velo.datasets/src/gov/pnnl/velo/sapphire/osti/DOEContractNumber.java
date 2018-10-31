package gov.pnnl.velo.sapphire.osti;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

//class level label(standard) is used in the "add child" link of ElementList<DOEContractNumber> field
@Label(full = "DOE Contract Number", standard = "DOE Contract #")
public interface DOEContractNumber extends Element {
  ElementType TYPE = new ElementType(DOEContractNumber.class);
  // if xmlbinding path="" value property will not get its own element such as <DOEContract>.
  // The string value of PROP_DOECONTRACT will be text within the parent's xml element
  @XmlBinding(path = "")
  @Required
  @Unique
  // this label is used in info/error text. If label is not present ValueProperty name (i.e. DOEContract) will be used
  @Label(standard = "DOE Contract Number")
  @Fact(statement = "Use the format of the contract \"as is\", but please leave off any preceding \"DE\"")
  ValueProperty PROP_DOECONTRACT = new ValueProperty(TYPE, "DOEContract");

  Value<String> getDOEContract();

  void setDOEContract(String value);

}
