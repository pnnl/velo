package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

//class level label(standard) is used in the "add child" link of ElementList<Tag> field
@Label(full = "Tag", standard = "Tag")
public interface Tag extends Element {
  ElementType TYPE = new ElementType(Tag.class);
  // if xmlbinding path="" value property will not get its own element such as <Tag>.
  // The string value of PROP_ variable will be text within the parent's xml element
  @XmlBinding(path = "")
  @Unique
  // this label is used in info/error text. If label is not present ValueProperty name (i.e. DOEContract) will be used
  @Label(standard = "Tag")
  ValueProperty PROP_TAG = new ValueProperty(TYPE, "Tag");

  Value<String> getTag();

  void setTag(String value);

}
