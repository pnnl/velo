package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.PossibleValues;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.InitialValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

import gov.pnnl.velo.sapphire.DateValidationService;

@XmlBinding(path = "datasetCoverage")
public interface DatasetCoverage extends Element {

  ElementType TYPE = new ElementType(DatasetCoverage.class);

  // Temporal Coverage of data

  @Label(standard = "Start date")
  @XmlBinding(path = "temporalCoverage/coverageStartDate")
  @Service(impl = DateValidationService.class)
  @Fact(statement = "End date in the format mm/dd/yyyy")
  ValueProperty PROP_START_DATE = new ValueProperty(TYPE, "StartDate");

  Value<String> getStartDate();

  void setStartDate(String value);

  @Label(standard = "End date")
  @XmlBinding(path = "temporalCoverage/coverageEndDate")
  @Service(impl = DateValidationService.class)
  @Fact(statement = "End date in the format mm/dd/yyyy")
  ValueProperty PROP_END_DATE = new ValueProperty(TYPE, "EndDate");

  Value<String> getEndDate();

  void setEndDate(String value);
  


  // *** Update Frequency

  //@Type(base = UpdateFrequencyEnum.class)
  @XmlBinding(path = "temporalCoverage/updateFrequency")
  @Label(standard = "Data Update Frequency")
  @Required
  @InitialValue(text="Not planned")
  @PossibleValues(values={"Continual","Daily","Weekly","Fortnightly","Quaterly","Biannually",
      "Annually","As needed","Irregular","Unknown","Not planned"})
  ValueProperty PROP_UPDATE_FREQUENCY = new ValueProperty(TYPE, "UpdateFrequency");

  Value<String> getUpdateFrequency();

  void setUpdateFrequency(String value);


  @Label(standard = "DCMI Bounding Box", full = "Dublin Core Bounding Box")
  @XmlBinding(path = "spatialCoverage/DCMIBox")
  @Fact(statement = "Use the below web page to draw a bounding box and copy paste the Dublin Core format bounding box information")
  ValueProperty PROP_DCMI_BOX = new ValueProperty(TYPE, "DCMIBox");

  Value<String> getDCMIBox();

  void setDCMIBox(String value);
}
