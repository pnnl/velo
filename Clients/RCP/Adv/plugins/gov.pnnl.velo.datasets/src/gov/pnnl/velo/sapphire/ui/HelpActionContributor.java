package gov.pnnl.velo.sapphire.ui;

import org.eclipse.help.IContext;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.HelpSystem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class HelpActionContributor extends PropertyEditorAssistContributor {

  // private PropertyEditorPart part;
  // public static StringBuffer helpIds = new StringBuffer(500);

  public static final String ID = "Sapphire.Help";
  private String contextId;
  private SapphirePart part;

  @Override
  public void init(final SapphirePart part) {
    this.part = part;

    if (part instanceof PropertyEditorPart) {
      // contextId = contextId + "." +((PropertyEditorPart)part).property().name();
      PropertyDef partDef = ((PropertyEditorPart) part).property().definition();
      String path = "";
      if (partDef instanceof ValueProperty || partDef instanceof ImpliedElementProperty) {
        XmlBinding annotation = partDef.getAnnotation(XmlBinding.class);
        if (annotation != null) {
          path = annotation.path();
          if (path.isEmpty()) {
            // might happen when this property is a part of a list of Object<T> where the T contain only this single property
            PropertyDef listDef = ((PropertyEditorPart) part.parent()).property().definition();
            path = listDef.getAnnotation(org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding.class).path();
          }
        }
      } else if (partDef instanceof ListProperty) {
        XmlListBinding annotation = partDef.getAnnotation(XmlListBinding.class);
        path = annotation.path();
      }

      if (path.isEmpty()){
        contextId = null;
      }else{
        if(path.contains("/")){
          String[] parts = path.split("/");
          contextId = parts[parts.length-1];
        }else {
          contextId = path;
        }
        
      }
      // System.out.println("contextId for property " + ((PropertyEditorPart)part).property().name() + " is " + contextId);
    } else {
      // System.out.println("documentation for something other than property editor");
      // set contextId to null so that documentation of non property elements (eg <section> )
      // display <documentation> element's content from sdef
      contextId = null;
    }
    // System.out.println(contextId);
    // printContextXmlContent();
  }

  @Override
  public void contribute(PropertyEditorAssistContext context) {

    final PropertyEditorAssistContribution.Factory contribution = PropertyEditorAssistContribution.factory();
    contribution.text("<p><a href=\"action\" nowrap=\"true\">Show Help</a></p>");
    // There are multiple ways to get documentation Context
    // 1.
    // Use new SapphireHelpContenxt(property.element(), property.definition()) - if you want to display @Documentation content and facts of the specific property from the .java class
    // This would NOT display the contents of the <documentation> tag in .sdef file
    // Property property = part.property();
    // final SapphireHelpContext documentationContext = new SapphireHelpContext(property.element(), property.definition());
    // Point point = context.getShell().getDisplay().getCursorLocation();
    // contribution.link("action", new Link(documentationContext, point));

    // 2.
    // Use getDocumentationContext to get the documentation content from the .sdef. I think this is better because this would
    // keep both the documentation and the specification of assist.contributor in the same file. Moreover the facts of the property are
    // are anyway declared in the assist popup. This behavior makes it similar to pressing F1 key on windows for sections and tree nodes
    // IContext documentationContext = getDocumentationContext(part.definition().getDocumentation().content());
    // Point point = context.getShell().getDisplay().getCursorLocation();
    // contribution.link("action", new Link(documentationContext, point));

    // 3. Get from other help extension point which has a contexts.xml
    // with entries such as
    // <context id="someContextId">
    // <description></description>
    // <topic label="TopicLabel" href="html/gettingstarted/faq.html"/>
    // </context>

    if (contextId != null) {
      contribution.link("action", new Link(contextId));
    } else {
      // May be it is a sectio level documentation?
      IContext documentationContext = getDocumentationContext(part.definition().getDocumentation().content());
      Point point = context.getShell().getDisplay().getCursorLocation();
      contribution.link("action", new Link(documentationContext, point));
    }
    final PropertyEditorAssistSection section = context.getSection(SECTION_ID_ACTIONS);
    section.addContribution(contribution.create());

  }

  // Original Code from org.eclipse.sapphire.ui.forms.SectionPart
  public final IContext getDocumentationContext(ISapphireDocumentation doc) {
    if (doc != null) {
      ISapphireDocumentationDef docdef = null;

      if (doc instanceof ISapphireDocumentationDef) {
        docdef = (ISapphireDocumentationDef) doc;
      } else {
        docdef = ((ISapphireDocumentationRef) doc).resolve();
      }

      if (docdef != null) {
        return HelpSystem.getContext(docdef);
      }
    }

    return null;
  }

  private final class Link implements Runnable {
    private final IContext documentationContext;
    private final String contextId;
    private final Point point;

    private Link(IContext documentationContext, Point p) {
      this.documentationContext = documentationContext;
      this.contextId = null;
      this.point = p;
    }

    private Link(String contextId) {
      this.documentationContext = null;
      this.contextId = contextId;
      this.point = null;
    }

    public void run() {

      // close the assist popup so that
      // PlatformUI.getWorkbench().getDisplay().getActiveShell() == PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
      // if the above condition is not true help displays in popup instead of the help view (dynamicHelp)
      Display.getCurrent().getActiveShell().close();

      if (contextId != null) {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp("gov.pnnl.velo.datasets." + contextId);
      }
      if (documentationContext != null) {
        PlatformUI.getWorkbench().getHelpSystem().displayContext(documentationContext, point.x - 30, point.y - 130);
      }
    }// end of run()
  }// end of Link object

  // private void printContextXmlContent() {
  // helpIds.append(" <context id=\"");
  // helpIds.append(contextId);
  // helpIds.append("\">\n");
  // helpIds.append(" <description></description>\n <topic label=\"TopicLabel\" href=\"html/");
  // helpIds.append(contextId);
  // helpIds.append(".htm\"/>\n </context>\n");
  // }
}
