package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

import gov.pnnl.velo.sapphire.PathListEntry;
import gov.pnnl.velo.sapphire.support.DataQuality;

@Image(path = "table.png")
@Label(standard = "Dataset Metadata")
@XmlBinding(path = "dataset")
public interface Dataset extends Element {

  ElementType TYPE = new ElementType(Dataset.class);

  // *** General Information ***

  @Type(base = CitationInformation.class)
  @Label(standard = "Citation Information")
  @XmlBinding(path = "citationInformation")
  ImpliedElementProperty PROP_CITATION_INFORMATION = new ImpliedElementProperty(TYPE, "CitationInformation");

  CitationInformation getCitationInformation();

  // *** DataAccess ***

  @Type(base = DataAccess.class)
  @Label(standard = "Data Access")
  @XmlBinding(path = "dataAccess")
  ImpliedElementProperty PROP_DATA_ACCESS = new ImpliedElementProperty(TYPE, "DataAccess");

  DataAccess getDataAccess();

  // Coverage of Dataset
  @Type(base = DatasetCoverage.class)
  @Label(standard = "Dataset Coverage")
  @XmlBinding(path = "datasetCoverage")
  ImpliedElementProperty PROP_DATASET_COVERAGE = new ImpliedElementProperty(TYPE, "DatasetCoverage");

  DatasetCoverage getDatasetCoverage();

  // *** Publications ***
  @Type(base = Publication.class)
  @Label(standard = "Publications")
  @Unique
  @XmlListBinding(path = "publications", mappings = @XmlListBinding.Mapping(element = "publication", type = Publication.class) )
  ListProperty PROP_PUBLICATIONS = new ListProperty(TYPE, "Publications");

  ElementList<Publication> getPublications();

  // *** Contact Information ***
  @Type(base = Contact.class)
  @XmlListBinding(path = "contacts", mappings = @XmlListBinding.Mapping(element = "contact", type = Contact.class) )
  ListProperty PROP_CONTACTS = new ListProperty(TYPE, "Contacts");
  ElementList<Contact> getContacts();
  
  // *** documents describing methods
  @Type(base = PathListEntry.class)
  @Unique
  @MustExist
  @XmlListBinding(path = "methods", mappings = @XmlListBinding.Mapping(element = "path", type = PathListEntry.class) )
  @Label(standard = "Methods")

  ListProperty PROP_METHODS = new ListProperty(TYPE, "Methods");

  ElementList<PathListEntry> getMethods();

  // DataQuality
  @Type(base = DataQuality.class)
  @Label(standard = "Data Quality")
  @XmlBinding(path = "dataQuality")
  ImpliedElementProperty PROP_DATA_QUALITY = new ImpliedElementProperty(TYPE, "DataQuality");

  DataQuality getDataQuality();

}
