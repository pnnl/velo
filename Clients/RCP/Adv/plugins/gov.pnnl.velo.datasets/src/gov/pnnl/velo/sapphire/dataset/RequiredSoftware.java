package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

import gov.pnnl.velo.sapphire.UrlValidationService;

//class level label is used in the "add child" link of ElementList<RequiredSoftware> field
@Label(standard = "Required software")
public interface RequiredSoftware extends Element {
  ElementType TYPE = new ElementType(RequiredSoftware.class);

  @XmlBinding(path = "title")
  @Unique
  // this label is used in info/error text. If label is not present ValueProperty name will be used
  // and the auto format might not work well especially if the variable names have acronym
  @Label(standard = "Title")
  @Required
  ValueProperty PROP_TITLE = new ValueProperty(TYPE, "Title");

  Value<String> getTitle();

  void setTitle(String value);

  @XmlBinding(path = "url")
  @Unique
  // this label is used in info/error text. If label is not present ValueProperty name will be used
  // and the auto format might not work well especially if the variable names have acronym
  @Label(standard = "URL")
  @Required
  @Service(impl = UrlValidationService.class)
  @Image(path = "icons/Web.png")
  ValueProperty PROP_URL = new ValueProperty(TYPE, "URL");

  Value<String> getURL();

  void setURL(String value);

}
