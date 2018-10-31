package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

import gov.pnnl.velo.sapphire.landingpage.LandingPage;
import gov.pnnl.velo.sapphire.osti.OSTI;

@Image(path = "table.png")

@XmlBinding(path = "datasetMetadata")
public interface DatasetMetadata extends Element {

  ElementType TYPE = new ElementType(DatasetMetadata.class);

  // *** Details of dataset metadata***
  @Type(base = Dataset.class)
  @Label(standard = "Dataset Information")
  @XmlBinding(path = "")
  ImpliedElementProperty PROP_DATASET = new ImpliedElementProperty(TYPE, "Dataset");

  Dataset getDataset();

  // *** Details of landing page configuration ***
  @Type(base = LandingPage.class)
  @Label(standard = "Landingpage Configuration")
  @XmlBinding(path = "")
  ImpliedElementProperty PROP_LANDING_PAGE = new ImpliedElementProperty(TYPE, "LandingPage");

  LandingPage getLandingPage();

  // *** Details of OSTI metadata ***
  @Type(base = OSTI.class)
  @Label(standard = "OSTI Metadata")
  @XmlBinding(path = "")
  ImpliedElementProperty PROP_OSTI_METADATA = new ImpliedElementProperty(TYPE, "OSTIMetadata");

  OSTI getOSTIMetadata();

}
