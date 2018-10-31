package gov.pnnl.velo.sapphire.dataset;

import java.util.Date;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Serialization;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

@Image(path = "table.png")

public interface CitationDatasetMetadata extends Element {

  ElementType TYPE = new ElementType(CitationDatasetMetadata.class);

  // *** Title ***

  @Label(standard = "title")
  @Required
  @XmlBinding(path = "title")

  ValueProperty PROP_TITLE = new ValueProperty(TYPE, "Title");

  Value<String> getTitle();

  void setTitle(String Title);

  // *** Publisher ***

  @Label(standard = "Publisher")
  @Required
  @XmlBinding(path = "Publisher")

  ValueProperty PROP_PUBLISHER = new ValueProperty(TYPE, "Publisher");

  Value<String> getPublisher();

  void setPublisher(String value);

  // *** Update Frequency ***

  @Label(standard = "Update Frequency")
  @Required
  @XmlBinding(path = "accrualPeriodicity")
  @Documentation(content = "Frequency with which dataset is published")

  ValueProperty PROP_UPDATE_FREQUENCY = new ValueProperty(TYPE, "UpdateFrequency");

  Value<String> getUpdateFrequency();

  void setUpdateFrequency(String value);

  // *** Keywords ***

  // *** Listing Date ***

  @Type(base = Date.class)
  @Serialization(primary = "yyyy-MM-dd")
  ValueProperty PROP_LISTING_DATE = new ValueProperty(TYPE, "Listing Date");

  Value<Date> getListingDate();

  void setListingDate(String value);

  void setListingDate(Date value);

}
