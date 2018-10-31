package gov.pnnl.velo.sapphire.landingpage;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Fact;
import org.eclipse.sapphire.modeling.annotations.InitialValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

public interface ImageGalleryViewer extends Viewer {

  ElementType TYPE = new ElementType(ImageGalleryViewer.class);

  @Label(standard = "Type")
  @Required
  @XmlBinding(path = "viewerType")
  @Fact(statement = "Type of Viewer")
  @InitialValue(text = "ImageVideoGallery")
  ValueProperty PROP_TYPE = new ValueProperty(TYPE, "Type");

  // *** File List ***
  @Type(base = FileInfo.class)
  @Label(standard = "Files")
  @Unique
  @XmlListBinding(path = "imageFiles", mappings = @XmlListBinding.Mapping(element = "fileInfo", type = FileInfo.class) )
  ListProperty PROP_FILES = new ListProperty(TYPE, "Files");

  ElementList<FileInfo> getFiles();

}
