package gov.pnnl.velo.sapphire.tmp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.eclipse.sapphire.modeling.annotations.Label;

@Label(standard = "Data Update Frequency")
@XmlEnum(String.class)
public enum UpdateFrequencyEnum {
  
  @Label(standard = "Not planned" )
  //XmlEnumValue is not being handled by sapphire. Only variable name is saved in xml
  @XmlEnumValue("Not planned")
  NOTPLANNED,
  
  @Label(standard = "Continual")

  CONTINUAL,

  @Label(standard = "Daily")

  DAILY,

  @Label(standard = "Weekly")

  WEEKLY,

  @Label(standard = "Fortnightly")

  FORTNIGHTLY,

  @Label(standard = "Quaterly")

  QUATERLY,

  @Label(standard = "Biannually")

  BIANNUALLY,

  @Label(standard = "Annually")

  ANNUALLY,

  @Label(standard = "As needed")

  ASNEEDED,

  @Label(standard = "Irregular")

  IRREGULAR,

  @Label(standard = "Unknown")

  UNKNOWN

}
