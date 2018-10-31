package gov.pnnl.velo.sapphire.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.forms.swt.AbsoluteFilePathBrowseActionHandler;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.swt.widgets.FileDialog;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.dialogs.ResourceSelectionValidator;
import gov.pnnl.cat.ui.rcp.dialogs.ResourceSelectionValidatorAdapter;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.wizards.ResourceTreeDialog;

public class VeloFilePathBrowseActionHandler extends AbsoluteFilePathBrowseActionHandler {

  private FileExtensionsService fileExtensionService;
  private List<String> staticFileExtensionsList;

  @Override
  public void init(final SapphireAction action, final ActionHandlerDef def) {
    super.init(action, def);

    // VELO CHANGE:
    // had to have our own copy of fileExtensionService and staticFileExtensionsList
    // and init them since the super class has them private
    final String staticFileExtensions = def.getParam(PARAM_EXTENSIONS);

    if (staticFileExtensions == null) {
      this.fileExtensionService = property().service(FileExtensionsService.class);

      if (this.fileExtensionService == null) {
        this.staticFileExtensionsList = Collections.emptyList();
      }
    } else {
      this.staticFileExtensionsList = new ArrayList<String>();

      for (String extension : staticFileExtensions.split(",")) {
        extension = extension.trim();

        if (extension.length() > 0) {
          this.staticFileExtensionsList.add(extension);
        }
      }
    }
  }

  protected Object getTreeRoot() {
    return RCPUtil.getTreeRoot();
  }

  protected String getTitle() {
    Property property = property();
    return property.definition().getLabel(true, CapitalizationType.FIRST_WORD_ONLY, false);
  }

  protected String getMessage() {
    Property property = property();
    return property.definition().getLabel(true, CapitalizationType.FIRST_WORD_ONLY, false);
  }

  @Override
  protected String browse(Presentation context) {
    
    
    ResourceSelectionValidator validator = new ResourceSelectionValidatorAdapter() {

      @Override
      public String validateSelection(List<IResource> selectedResources) {
        //can assume there is only one selection
        
        String errorMessage = null;
        if(selectedResources.size() > 0 && selectedResources.get(0).isType(IResource.FOLDER)) {
          errorMessage = "You must select a file.";
        }
        return errorMessage;
      }
      
    };
    
    //TODO: Change the last param to the ResourceTreeDialog constructor to be 'true' to allow multiple selection once the other parts of this UI allow it
    ResourceTreeDialog dialog = new ResourceTreeDialog(((FormComponentPresentation) context).shell(), getTreeRoot(), validator, true, false);
    
//    if(this.staticFileExtensionsList != null && this.staticFileExtensionsList.size() > 0){
//      FileExtensionFilter filter = new FileExtensionFilter(this.staticFileExtensionsList);
//      dialog.setViewerFilters(filter);
//    }

    dialog.create();
    dialog.setTitle(getTitle());
    dialog.setMessage(getMessage());
    

    // TODO let users select multiple files at a time
    if (dialog.open() == Dialog.OK && dialog.getSelectedResource() != null) {
      // List<IResource> resourcesToExport = dialog.getSelectedResources();
      IResource resourceToExport = dialog.getSelectedResource();
      return resourceToExport.getPath().toDisplayString();
    }
    return null;
  }

  protected String browseOrig(Presentation context) {
    final Property property = property();

    final FileDialog dialog = new FileDialog(((FormComponentPresentation) context).shell());
    dialog.setText(property.definition().getLabel(true, CapitalizationType.FIRST_WORD_ONLY, false));

    final Value<?> value = (Value<?>) property;
    final Path path = (Path) value.content();

    if (path != null && path.segmentCount() > 1) {
      dialog.setFilterPath(path.removeLastSegments(1).toOSString());
      dialog.setFileName(path.lastSegment());
    }

    final List<String> extensions;

    if (this.fileExtensionService == null) {
      extensions = this.staticFileExtensionsList;
    } else {
      extensions = this.fileExtensionService.extensions();
    }

    if (!extensions.isEmpty()) {
      final StringBuilder buf = new StringBuilder();

      for (String extension : extensions) {
        if (buf.length() > 0) {
          buf.append(';');
        }

        buf.append("*.");
        buf.append(extension);
      }

      dialog.setFilterExtensions(new String[] { buf.toString() });
    }

    return dialog.open();
  }

}
