package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

import gov.pnnl.velo.sapphire.EmailValidationService;

@XmlBinding(path = "author")
public interface Author extends Element {
  ElementType TYPE = new ElementType(Author.class);

  // *** First Name ***
  @Label(standard = "First Name")
  @Required
  @XmlBinding(path = "firstName")
  ValueProperty PROP_FIRST_NAME = new ValueProperty(TYPE, "FirstName");

  Value<String> getFirstName();

  void setFirstName(String value);

  //*** Middle Name ***
  @Label(standard = "Middle Name/Initial")
  @XmlBinding(path = "middleName")
  ValueProperty PROP_MIDDLE_NAME = new ValueProperty(TYPE, "MiddleName");

  Value<String> getMiddleName();

  void setMiddleName(String value);

  //*** Last Name ***
  @Label(standard = "Last Name")
  @Required
  @XmlBinding(path = "lastName")
  ValueProperty PROP_LAST_NAME = new ValueProperty(TYPE, "LastName");

  Value<String> getLastName();

  void setLastName(String value);


  // *** EMail ***

  @Label(standard = "E-Mail")
  @XmlBinding(path = "eMail")
  @Required
  @Service(impl = EmailValidationService.class)
  ValueProperty PROP_E_MAIL = new ValueProperty(TYPE, "EMail");

  Value<String> getEMail();

  void setEMail(String email);

  // *** ORCID ***
  @Label(standard = "ORCID")
  @XmlBinding(path = "ORCID")

  ValueProperty PROP_ORCID = new ValueProperty(TYPE, "ORCID");

  Value<String> getORCID();

  // *** PhoneNumber ***
  @Label(standard = "phone number")
  @XmlBinding(path = "phoneNumber")

  ValueProperty PROP_PHONE_NUMBER = new ValueProperty(TYPE, "PhoneNumber");

  Value<String> getPhoneNumber();

  void setPhoneNumber(String phoneNumber);

  // *** institution ***
  @Label(standard = "institution")
  @XmlBinding(path = "institution")

  ValueProperty PROP_INSTITUTION = new ValueProperty(TYPE, "Institution");

  Value<String> getInstitution();

  void setInstitution(String institution);

  // *** BooleanProperty ***

  @Type(base = Boolean.class)
  @Label(standard = "Primary Author")
  @XmlBinding(path = "primaryAuthor")

  ValueProperty PROP_PRIMARY_AUTHOR = new ValueProperty(TYPE, "PrimaryAuthor");

  Value<Boolean> getPrimaryAuthor();

  void setPrimaryAuthor(String value);

  void setPrimaryAuthor(Boolean value);

}
