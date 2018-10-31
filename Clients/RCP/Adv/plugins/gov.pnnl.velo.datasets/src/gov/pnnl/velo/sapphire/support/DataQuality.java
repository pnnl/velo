package gov.pnnl.velo.sapphire.support;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

import gov.pnnl.velo.sapphire.PathListEntry;

@XmlBinding(path = "dataQuality")
public interface DataQuality extends Element {

  ElementType TYPE = new ElementType(DataQuality.class);
  @Type(base = PathListEntry.class)
  @Unique
  @MustExist
  @XmlListBinding(path = "dataQualityFiles", mappings = @XmlListBinding.Mapping(element = "path", type = PathListEntry.class) )
  @Label(standard = "Data Quality Files")

  ListProperty PROP_DATA_QUALITY_FILES = new ListProperty(TYPE, "DataQualityFiles");

  ElementList<PathListEntry> getDataQualityFiles();

  @Label(standard = "Data Quality Summary")
  @Required
  @XmlBinding(path = "qualitySummary")
  @LongString
  ValueProperty PROP_QUALITY_SUMMARY = new ValueProperty(TYPE, "QualitySummary");

  Value<String> getQualitySummary();

  void setQualitySummary(String value);

}
