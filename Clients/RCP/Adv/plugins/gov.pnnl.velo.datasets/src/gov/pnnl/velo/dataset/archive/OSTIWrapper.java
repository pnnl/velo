package gov.pnnl.velo.dataset.archive;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

import gov.pnnl.velo.sapphire.osti.OSTI;

@XmlBinding(path = "osti")
public interface OSTIWrapper extends Element {
  ElementType TYPE = new ElementType(OSTIWrapper.class);

  // *** GeneralDatasetMetadata ***

  @Type(base = OSTI.class)
  @Label(standard = "OSTI Metadata")
  @XmlBinding(path = "")

  ImpliedElementProperty PROP_OSTI = new ImpliedElementProperty(TYPE, "OSTI");

  OSTI getOSTI();
}