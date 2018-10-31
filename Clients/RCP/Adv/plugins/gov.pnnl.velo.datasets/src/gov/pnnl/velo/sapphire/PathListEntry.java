package gov.pnnl.velo.sapphire;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

//class level label(standard) is used in the "add child" link of ElementList<this class> field
@Label(full = "File", standard = "File")
public interface PathListEntry extends Element {
  ElementType TYPE = new ElementType(PathListEntry.class);

  // *** Path ***

  @Type(base = Path.class)
  @Label(standard = "path")
  @XmlBinding(path = "")
  @ValidFileSystemResourceType(FileSystemResourceType.FILE)
  ValueProperty PROP_PATH = new ValueProperty(TYPE, "Path");

  Value<Path> getPath();

  void setPath(String value);

  void setPath(Path value);

}