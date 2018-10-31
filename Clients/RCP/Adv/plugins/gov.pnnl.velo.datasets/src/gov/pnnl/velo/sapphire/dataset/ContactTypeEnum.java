package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.sapphire.modeling.annotations.Label;

@Label(standard = "Dataset Type")
public enum ContactTypeEnum {
  //Value of enum stored in sapphire xml is the variable name and not the label
  //@label is used to display in the radio group/list
  //sapphire ignore @EnumValue annotation
  @Label(standard = "Author")

  Author,

  @Label(standard = "Other")

  Other

}