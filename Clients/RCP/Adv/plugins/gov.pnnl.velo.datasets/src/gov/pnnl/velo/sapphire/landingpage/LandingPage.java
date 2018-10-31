package gov.pnnl.velo.sapphire.landingpage;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

import gov.pnnl.velo.sapphire.WebSite;

@XmlBinding(path = "landingPage")
public interface LandingPage extends Element {
  ElementType TYPE = new ElementType(LandingPage.class);

  @Type(base = Viewer.class, possible = { ImageGalleryViewer.class })
  @XmlListBinding(path = "viewers", mappings = { @XmlListBinding.Mapping(element = "imageGalleryViewer", type = ImageGalleryViewer.class) })
  ListProperty PROP_VIEWERS = new ListProperty(TYPE, "Viewers");

  ElementList<Viewer> getViewers();

  // *** WebSites ***

  @Type(base = WebSite.class)
  @Label(standard = "Related web sites")
  @XmlListBinding(path = "relatedWebsites", mappings = @XmlListBinding.Mapping(element = "website", type = WebSite.class) )

  ListProperty PROP_RELATED_WEB_SITES = new ListProperty(TYPE, "RelatedWebSites");

  ElementList<WebSite> getRelatedWebSites();

}
