package gov.pnnl.velo.sapphire;

import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.forms.SectionDef;
import org.eclipse.ui.PlatformUI;

public class VeloHelpActionHandler extends SapphireActionHandler {
  public static final String ID = "Sapphire.Help";

  public VeloHelpActionHandler() {
    setId(ID);
  }

  @Override
  protected Object run(final Presentation context) {
    // final IContext documentationContext = getPart().getDocumentationContext();
    //
    // if ( documentationContext != null )
    // {
    // PlatformUI.getWorkbench().getHelpSystem().displayHelp( documentationContext );
    // }
    String contextId = null;
    ((SectionDef) getPart().definition()).getDocumentation();
    getPart().definition().getDocumentation().definition();
    if (contextId != null) {
      PlatformUI.getWorkbench().getHelpSystem().displayHelp(contextId);
    }
    return null;
  }

}