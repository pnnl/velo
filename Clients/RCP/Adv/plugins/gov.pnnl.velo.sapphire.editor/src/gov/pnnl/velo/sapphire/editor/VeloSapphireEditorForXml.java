package gov.pnnl.velo.sapphire.editor;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.InitialValueService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.RequiredConstraintService;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.ui.def.DefinitionLoader.Reference;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;
import org.eclipse.sapphire.ui.forms.swt.MasterDetailsEditorPage;
import org.eclipse.sapphire.ui.swt.xml.editor.SapphireEditorForXml;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

//import org.eclipse.sapphire.workspace.WorkspaceFileResourceStore;
import gov.pnnl.cat.ui.rcp.editors.ResourceFileStoreEditorInput;
import gov.pnnl.cat.ui.rcp.editors.VeloEditorUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;

public class VeloSapphireEditorForXml extends SapphireEditorForXml {


  @Override
  protected void createFormPages() throws PartInitException {
    super.createFormPages();// org.eclipse.ui.part.FileEditorInput
   
    setPageText(1, "source");

    // for now, removing the 'source' page as most likely if users see it and go to it they'll get confused :) will add
    // back later when they are ready

    // wow, after I did this sapphire wouldn't save any changes to the xml file :(
    // this.removePage(1);
  }

  /**
   * Method isSaveAsAllowed.
   * 
   * @return boolean
   * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
   */
  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  /**
   * Method doSaveAs.
   * 
   * @see org.eclipse.ui.ISaveablePart#doSaveAs()
   */
  @Override
  public void doSaveAs() {
    String errMsg = "Save As not yet supported.";
    ToolErrorHandler.handleError(errMsg, null, true);
  }

  /**
   * Method doSave.
   * 
   * @param progressMonitor
   *          IProgressMonitor
   * @see org.eclipse.ui.ISaveablePart#doSave(IProgressMonitor)
   */
  @Override
  public void doSave(IProgressMonitor progressMonitor) {
    super.doSave(progressMonitor);
    ResourceFileStoreEditorInput myInput = (ResourceFileStoreEditorInput) this.getEditorInput();
    VeloEditorUtil.saveToServer(myInput);
  }

  private boolean hideOutline;
  private boolean initialized =false;


  @Override
  protected Element createModel() {
    Element model = super.createModel();
    initializeMandatoryFields(model);

    if(initialized){
      //if initializeMandatoryFields method initialized some property 
      //the editor will have unsaved changes. Force a save
      this.doSave(null);
    }
    return model;
  }

  @Override
  protected IEditorPart createPage(final Reference<EditorPageDef> definition) {
    IEditorPart page = super.createPage(definition);

    if (page instanceof MasterDetailsEditorPage) {
      MasterDetailsEditorPage masterDetailsEditorPage = (MasterDetailsEditorPage) page;
      // Reduce the default width of the outline view in order to give more real estate to the form
      masterDetailsEditorPage.getPart().state().getContentOutlineState().setRatio("0.3d");
      if (hideOutline) {
        MasterDetailsContentNodePart root = masterDetailsEditorPage.outline().getRoot();
        masterDetailsEditorPage.outline().setSelectedNode(root.nodes().get(0));
        masterDetailsEditorPage.getPart().state().getContentOutlineState().setVisible(false);
      }
      masterDetailsEditorPage.getPart().expandAllNodes();
      //always open with form page instead of source page
      setActiveEditor(masterDetailsEditorPage);
    }
    return page;
  }

  @Override
  public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data) {
    super.setInitializationData(config, propertyName, data);
    // this.pageDefinitionId = (String)(((Map<?,?>) data).get( "pageDefinitionId" ));
    final Map<?, ?> properties = (Map<?, ?>) data;
    Object param = properties.get("hideOutline");
    if (param != null && ((String) param).equalsIgnoreCase("true"))
      this.hideOutline = true;

  }

  // Code based on org.eclipse.sapphire.ElementImpl.initialize()
  // initializes only ValueProperty and ImpliedElementProperty. does not not initialize list property
  @SuppressWarnings("unchecked")
  public final <T extends Element> T initializeMandatoryFields(Element model) {
    for (Property instance : model.properties()) {

      final PropertyDef property = instance.definition();

      if (property instanceof ValueProperty) {
        Object content = ((Value<?>) instance).content(false);
        // Object defaultContent = ((Value<?>)instance).getDefaultContent();
        boolean required = instance.service(RequiredConstraintService.class).required();
        final InitialValueService initialValueService = instance.service(InitialValueService.class);
        // if a required field is empty fill it with initial value
        if (content == null && initialValueService != null && required) {
          ((Value<?>) instance).write(initialValueService.value());
          initialized = true;
        }

      } else if (property instanceof ImpliedElementProperty) {
        initializeMandatoryFields(model.property(((ImpliedElementProperty) property)).content());
      }
      // Little too confusing to initialize list
      // else if (property instanceof ListProperty) {
      // ElementList<Element> list = model.property((ListProperty)property);
      // int size = list.size();
      // Element element = list.get(0);
      // SortedSet<Property> content = element.content();
      // boolean required = list.service(RequiredConstraintService.class).required();
      // final InitialValueService initialValueService = list.service(InitialValueService.class);
      // //LengthValidationServiceForList service = instance.service(LengthValidationServiceForList.class);
      // if(size==0 && required && initialValueService != null){
      // model.property((ListProperty)property).add(new Value<String>())
      // }
      // }
    }
    return (T) model;
  }
  
 
  
  /**
   * Sets the currently active page.
   *
   * @param pageIndex
   *            the index of the page to be activated; the index must be valid
   */
  @Override
  public void setActivePage(int pageIndex) {
    Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
    ((CTabFolder)getContainer()).setSelection(pageIndex);
    pageChange(pageIndex);
  }

}
