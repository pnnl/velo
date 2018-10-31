package gov.pnnl.velo.sapphire.support;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

@XmlBinding(path = "supportingDocs")
public interface SupportingDocsWrapper extends Element {
  ElementType TYPE = new ElementType(SupportingDocsWrapper.class);

  // *** GeneralDatasetMetadata ***

  @Type(base = DataQuality.class)
  @Label(standard = "Supporting Documents")
  @XmlBinding(path = "")

  ImpliedElementProperty PROP_SUPPORTING_DOCS = new ImpliedElementProperty(TYPE, "SupportingDocs");

  DataQuality getSupportingDocs();
}