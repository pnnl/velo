package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.ClearOnDisable;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.InitialValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

import gov.pnnl.velo.sapphire.EmailValidationService;

@XmlBinding(path = "contact")
public interface Contact extends Element {

  ElementType TYPE = new ElementType(Contact.class);

  @Type(base = Boolean.class)
  @Label(standard = "Primary Contact")
  // @InitialValue(expr = ${ PrimaryContact == true ? true : primaryContact })
  @XmlBinding(path = "primaryContact")
  ValueProperty PROP_PRIMARY_CONTACT = new ValueProperty(TYPE, "PrimaryContact");

  Value<Boolean> getPrimaryContact();

  void setPrimaryContact(String value);

  void setPrimaryContact(Boolean value);

  // *** Type ***

  @Label(standard = "Type")
  @Required
  @XmlBinding(path = "contactType")
  @Type(base=ContactTypeEnum.class)
  @InitialValue(text="Author")  
  ValueProperty PROP_TYPE = new ValueProperty(TYPE, "Type");

  Value<String> getType();

  void setType(String value);

  @Label(standard = "Author Name")
  @Required
  @XmlBinding(path = "authorName")
  @ClearOnDisable
  @Enablement(expr="${Type==\"Author\"}")
  @Service(impl = AuthorNamePossibleValueService.class, params = @Service.Param(name = "property", value = "/Dataset/CitationInformation/Authors/Name") )
  ValueProperty PROP_AUTHOR_NAME = new ValueProperty(TYPE, "AuthorName");

  Value<String> getAuthorName();

  void setAuthorName(String value);
  
  // *** Contact Role***

  @Label(standard = "Contact Role")
  @XmlBinding(path = "contactRole")
  @Enablement(expr="${Type!=\"Author\"}")
  @ClearOnDisable
  @Fact(statement = "Role of contact. For example - project admin, data specialist, system administrator, web master etc.")
  ValueProperty PROP_CONTACT_ROLE = new ValueProperty(TYPE, "ContactRole");

  Value<String> getContactRole();

  void setContactRole(String value);

  // *** ContactName***

  @Label(standard = "Contact Name")
  @Required
  @XmlBinding(path = "contactName")
  @Enablement(expr="${Type!=\"Author\"}")
  @ClearOnDisable
  ValueProperty PROP_CONTACT_NAME = new ValueProperty(TYPE, "ContactName");

  Value<String> getContactName();

  void setContactName(String value);
  

  // *** EMail ***

  @Label(standard = "E-Mail")
  @XmlBinding(path = "eMail")
  @Required
  @Service(impl = EmailValidationService.class)
  @Enablement(expr="${Type!=\"Author\"}")
  @ClearOnDisable
  ValueProperty PROP_E_MAIL = new ValueProperty(TYPE, "EMail");

  Value<String> getEMail();

  void setEMail(String email);

  // *** PhoneNumber ***
  @Label(standard = "phone number")
  @XmlBinding(path = "phoneNumber")
  @Enablement(expr="${Type!=\"Author\"}")
  @ClearOnDisable
  ValueProperty PROP_PHONE_NUMBER = new ValueProperty(TYPE, "PhoneNumber");

  Value<String> getPhoneNumber();

  void setPhoneNumber(String phoneNumber);

  // *** institution ***
  @Label(standard = "institution")
  @XmlBinding(path = "institution")
  @Enablement(expr="${Type!=\"Author\"}")
  @ClearOnDisable
  ValueProperty PROP_INSTITUTION = new ValueProperty(TYPE, "Institution");

  Value<String> getInstitution();

  void setInstitution(String institution);

}
