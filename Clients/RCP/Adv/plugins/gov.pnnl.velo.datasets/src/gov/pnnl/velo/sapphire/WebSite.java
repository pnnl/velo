package gov.pnnl.velo.sapphire;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

@Image(path = "icons/Web.png")
public interface WebSite extends Element {
  ElementType TYPE = new ElementType(WebSite.class);


  
  @Label(standard = "title")
  @Required
  @XmlBinding(path = "websiteTitle")

  ValueProperty PROP_TITLE = new ValueProperty(TYPE, "Title");

  Value<String> getTitle();

  void setTitle(String Title);
  
  
  // *** Url ***

  @XmlBinding(path = "url")
  @Label(standard = "URL")
  @Required
  @Service(impl = UrlValidationService.class)
  ValueProperty PROP_URL = new ValueProperty(TYPE, "Url");

  Value<String> getUrl();

  void setUrl(String url);

}
