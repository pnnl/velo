package gov.pnnl.velo.sapphire.landingpage;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

@XmlBinding(path = "fileInfo")
public interface FileInfo extends Element {

  ElementType TYPE = new ElementType(FileInfo.class);
  // *** Video File ***
  @Type(base = Path.class)
  @Label(standard = "File")
  @XmlBinding(path = "filePath")
  @ValidFileSystemResourceType(FileSystemResourceType.FILE)
  // TODO: get the list of supported video formats
  // @FileExtensions(expr = "mp4,avi,mov,flv,swf") - this doesn't seem to. try in sdef
  ValueProperty PROP_FILE = new ValueProperty(TYPE, "File");

  Value<Path> getFile();

  void setFile(String value);

  void setFile(Path value);

  // *** Title ***
  @Label(standard = "Title")
  @XmlBinding(path = "fileTitle")
  @Required
  ValueProperty PROP_TITLE = new ValueProperty(TYPE, "Title");

  Value<String> getTitle();

  void setTitle(String value);

  // *** Description ***
  @Label(standard = "Description")
  @XmlBinding(path = "fileDescription")
  @Required
  @LongString
  ValueProperty PROP_DESCRIPTION = new ValueProperty(TYPE, "Description");

  Value<String> getDescription();

  void setDescription(String value);

}