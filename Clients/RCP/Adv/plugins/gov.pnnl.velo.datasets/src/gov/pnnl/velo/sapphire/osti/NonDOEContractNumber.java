package gov.pnnl.velo.sapphire.osti;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

//class level label is used in the "add child" link of ElementList<DOEContractNumber> field
@Label(full = "DOE Contract Number", standard = "DOE Contract #")
@XmlBinding(path = "nonDOEContractNumber")
public interface NonDOEContractNumber extends Element {
  ElementType TYPE = new ElementType(NonDOEContractNumber.class);

  @Fact(statement = "Contract or award number not assigned by DOE (for example, an NSF award number)")
  // if xmlbinding path="" value property will not get its own element such as <NonDOEContract>.
  // The string value of PROP_NONDOE_CONTRACT will be text within the parent's xml element
  @XmlBinding(path = "")
  @Unique
  // this label is used in info/error text. If label is not present ValueProperty name (i.e. NonDOEContract) will be used and the auto format of that is ugly
  @Label(standard = "Non DOE Contract Number")
  ValueProperty PROP_NONDOE_CONTRACT = new ValueProperty(TYPE, "NonDOEContract");

  Value<String> getNonDOEContract();

  void setNonDOEContract(String value);

}
