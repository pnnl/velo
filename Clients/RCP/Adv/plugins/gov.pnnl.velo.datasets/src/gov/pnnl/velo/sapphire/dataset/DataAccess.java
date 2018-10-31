package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Length;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.PossibleValues;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

import gov.pnnl.velo.sapphire.DateValidationService;

@Image(path = "access.png")

public interface DataAccess extends Element {

  ElementType TYPE = new ElementType(DataAccess.class);

  // *** Access Type ***

  @XmlBinding(path = "accessLevel")
  @Label(standard = "Access Level")
  @Required
  @PossibleValues(values={"Public","Restricted Public","Non-Public"})
  ValueProperty PROP_LEVEL = new ValueProperty(TYPE, "Level");

  Value<String> getType();

  void setType(String value);


  // *** Access Period ***

  @Label(standard = "Access Period Ends")
  @XmlBinding(path = "accessEndDate")
  @Service(impl = DateValidationService.class)
  @Fact(statement = "Date till this dataset will be available from landing page(format mm/dd/yyyy)")
  ValueProperty PROP_ACCESS_END_DATE = new ValueProperty(TYPE, "AccessEndDate");

  Value<String> getAccessEndDate();

  void setAccessEndDate(String value);

  // *** Rights ***

  @Label(standard = "Rights")
  @Unique
  @XmlBinding(path = "rights")
  @LongString
  @Length(max = 255)
  ValueProperty PROP_RIGHTS = new ValueProperty(TYPE, "Rights");

  Value<String> getRights();

  void setName(String rights);

  // *** License ***

  @Label(standard = "License")
  @XmlBinding(path = "license")
  @Fact(statement = "*Please acciqure the selected license before publishing the dataset")
  @Service(impl = LicensePossibleValuesService.class)
  ValueProperty PROP_LICENSE = new ValueProperty(TYPE, "License");

  Value<String> getLicense();

  void setLicense(String license);

  // ***** Required Software *****
  // optional field
  @Type(base = RequiredSoftware.class)
  @Label(standard = "Required Softwares")
  @Unique
  @Fact(statement = "Softwares required to read/interpret the data in this dataset")
  @XmlListBinding(path = "requiredSoftwares", mappings = @XmlListBinding.Mapping(element = "requiredSoftware", type = RequiredSoftware.class) )
  ListProperty PROP_REQUIRED_SOFTWARES = new ListProperty(TYPE, "RequiredSoftwares");

  ElementList<RequiredSoftware> getRequiredSoftwares();

}
