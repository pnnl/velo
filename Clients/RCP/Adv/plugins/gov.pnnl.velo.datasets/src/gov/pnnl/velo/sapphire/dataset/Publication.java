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

public interface Publication extends Element {

  ElementType TYPE = new ElementType(Publication.class);

  // *** Name ***

  @Label(standard = "Citation")
  @Required
  @Unique
  @XmlBinding(path = "")

  ValueProperty PROP_CITATION = new ValueProperty(TYPE, "Citation");

  Value<String> getCitation();

  void setCitation(String citationString);
  
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
