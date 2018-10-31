package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

@Image(path = "table.png")

public interface CitationInformation extends Element {

  ElementType TYPE = new ElementType(CitationInformation.class);

  // *** Details other than Author ***
  // @Type(base = Description.class)
  // @Label(standard = "description")
  // @XmlBinding(path = "")
  // ImpliedElementProperty PROP_DESCRIPTION = new ImpliedElementProperty(TYPE, "Description");
  //
  // Description getDescription();

  // *** Title ***

  @Label(standard = "title")
  @Required
  @XmlBinding(path = "title")
  ValueProperty PROP_TITLE = new ValueProperty(TYPE, "Title");

  Value<String> getTitle();

  void setTitle(String Title);

  // *** Description ***

  @Label(standard = "description")
  @Required
  @LongString
  @XmlBinding(path = "description")
  ValueProperty PROP_DESCRIPTION = new ValueProperty(TYPE, "Description");

  Value<String> getDescription();

  void setDescription(String Description);

  // *** Publisher ***

  @Label(standard = "Publisher")
  @Required
  @XmlBinding(path = "publisher")
  @Fact(statement = "The publishing entity")
  ValueProperty PROP_PUBLISHER = new ValueProperty(TYPE, "Publisher");

  Value<String> getPublisher();

  void setPublisher(String value);

  // *** Tags ***
  // @Type(base = Tag.class)
  @Label(standard = "Tags/Keywords")
  @Unique
  // @XmlListBinding(path = "tags", mappings = @XmlListBinding.Mapping(element = "tag", type = Tag.class) )
  @XmlBinding(path = "tags")
  ValueProperty PROP_TAGS = new ValueProperty(TYPE, "Tags");

  Value<String> getTags();

  void setTags(String value);

  // *** Authors ***
  @Type(base = Author.class)
  @Label(standard = "Authors")
  @Unique
  @XmlListBinding(path = "authors", mappings = @XmlListBinding.Mapping(element = "author", type = Author.class) )
  ListProperty PROP_AUTHORS = new ListProperty(TYPE, "Authors");

  ElementList<Author> getAuthors();
}
