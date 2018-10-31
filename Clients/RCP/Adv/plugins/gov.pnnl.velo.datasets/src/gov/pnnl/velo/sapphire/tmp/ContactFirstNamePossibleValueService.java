package gov.pnnl.velo.sapphire.tmp;

import java.util.Set;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.Status;

import gov.pnnl.velo.sapphire.dataset.Contact;


/******
 * Thought of using this class+ middle and lastname possible values to popuplate first, middle, and lastname
 * in contacts forms but this does not work because middle name is optional field
 * For example - this will not validate correctly the case where there is a author by name
 * "John M Doe" and a contact by name "John Doe" 
 * @author Chandrika Sivaramakrishnan
 *
 */

public class ContactFirstNamePossibleValueService extends PossibleValuesService {

  // private static Logger logger = Logger.getLogger(ContactFirstNamePossibleValueService.class);

  @Text("\"${FirstName}\" is not in the list of authors")
  private static LocalizableText message;

  static {
    LocalizableText.init(ContactFirstNamePossibleValueService.class);
  }

  protected void initPossibleValuesService() {
    this.invalidValueMessage = message.text();
    this.invalidValueSeverity = Status.Severity.ERROR; // user can enter values not in possible value list

    final Contact contact = context(Contact.class);
    final Property property = context(Property.class);

//    final Listener listener = new FilteredListener<PropertyContentEvent>() {
//      @Override
//      protected void handleTypedEvent(final PropertyContentEvent event) {
//        // refresh the firstName list by calling compute()
//        refresh();
//
//        final Set<String> values = values();
//
//        // if there is only 1 firstName for the current middle and lastName
//        // set it as the value of the firstName field
//        if (values.size() == 1) {
//          final String firstName = values.iterator().next();
//
//          if (!firstName.equalsIgnoreCase(contact.getFirstName().text())) {
//            contact.setFirstName(firstName);
//          }
//        }
//      }
//    };
//
//    // attach the listener to authors' name fields
//    property.attach(listener, DatasetConstants.PATH_AUTHOR_FIRSTNAME);
//    property.attach(listener, DatasetConstants.PATH_AUTHOR_MIDDLENAME);
//    property.attach(listener, DatasetConstants.PATH_AUTHOR_LASTNAME);
//
//    // listen to changes in middleName and lastName and refresh
//    contact.getMiddleName().attach(listener);
//    contact.getLastName().attach(listener);

  }

  @Override
  protected void compute(Set<String> values) {

//    final Contact contact = context(Contact.class);
//
//    final Element element = context(Element.class);
//    Set<Property> properties = element.root().properties(DatasetConstants.PATH_AUTHOR_FIRSTNAME);
//
//    String lastName = contact.getLastName().content(false);
//    String middleName = contact.getMiddleName().content(false);
//
//    for (Property prop : properties) {
//
//      Author author = (Author) prop.element();
//      String authorFirstName = author.getFirstName().content(false);
//      if (authorFirstName == null)
//        continue;
//
//      // narrow based on last and middle name
//      String authorMiddleName = author.getMiddleName().content(false);
//      String authorLastName = author.getLastName().content(false);
//
//      if (middleName == null || (middleName.equalsIgnoreCase(authorMiddleName))) {
//        if (lastName == null || (lastName.equalsIgnoreCase(authorLastName))) {
//          values.add(authorFirstName);
//        }
//      }
//
//    }
  }

}
