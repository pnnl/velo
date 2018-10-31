package gov.pnnl.velo.sapphire.dataset;

import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;

import gov.pnnl.velo.dataset.util.DatasetConstants;

public final class AuthorNamePossibleValueService extends PossibleValuesService {

  @Override
  protected void initPossibleValuesService() {
    final Contact contact = context(Contact.class);
    final Property property = context(Property.class);

    final Listener listener = new FilteredListener<PropertyContentEvent>() {
      @Override
      protected void handleTypedEvent(final PropertyContentEvent event) {
        // refresh the firstName list by calling compute()
        refresh();

        final Set<String> values = values();

        // if there is only 1 possible value in the list
        // set it as the value of the  field
        if (values.size() == 1) {
          final String name = values.iterator().next();

          if (!name.equalsIgnoreCase(contact.getAuthorName().text())) {
            contact.setAuthorName(name);
          }
        }
      }
    };

    // attach the listener to authors' name fields
    property.attach(listener, DatasetConstants.PATH_AUTHOR_FIRSTNAME);
    property.attach(listener, DatasetConstants.PATH_AUTHOR_MIDDLENAME);
    property.attach(listener, DatasetConstants.PATH_AUTHOR_LASTNAME);
  }

  @Override
  protected void compute(final Set<String> values) {

    final Element element = context(Element.class);
    Set<Property> properties = element.root().properties(DatasetConstants.PATH_AUTHOR_FIRSTNAME);
    for (Property prop : properties) {

      Author author = (Author) prop.element();
      String authorFirstName = author.getFirstName().content(false);
      String authorMiddleName = author.getMiddleName().content(false);
      String authorLastName = author.getLastName().content(false);

      String name = (authorFirstName == null ? "" : authorFirstName) + " ";
      name += (authorMiddleName == null ? "" : authorMiddleName) + " ";
      name += authorLastName == null ? "" : authorLastName;
      if (!name.trim().isEmpty()) {
        values.add(name);
      }
    }

  }

}
