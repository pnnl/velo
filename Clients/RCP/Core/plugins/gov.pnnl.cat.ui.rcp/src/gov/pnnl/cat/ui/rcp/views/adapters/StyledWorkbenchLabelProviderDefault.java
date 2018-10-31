package gov.pnnl.cat.ui.rcp.views.adapters;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloTifConstants;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

public class StyledWorkbenchLabelProviderDefault implements StyledWorkbenchLabelProvider {

  private Styler boldStyler;
  private Styler boldRedStyler;
  private Styler greenStyler;
  private Styler redStyler;
  private Styler grayStyler = StyledString.DECORATIONS_STYLER;
  private Styler aquaStyler;
  private Styler blueStyler;

  public StyledWorkbenchLabelProviderDefault() {
    this.boldStyler= new Styler() {
      public void applyStyles(TextStyle textStyle) {
        textStyle.font= JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);//SWTResourceManager.getBoldFont(baseFont) //boldFont;
      }
    };
    
    this.boldRedStyler= new Styler() {
      public void applyStyles(TextStyle textStyle) {
        textStyle.font= JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);//SWTResourceManager.getBoldFont(baseFont) //boldFont;
        textStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
      }
    };
    
    this.greenStyler= new Styler() {
      public void applyStyles(TextStyle textStyle) {
        textStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
      }
    };
  
    this.redStyler= new Styler() {
      public void applyStyles(TextStyle textStyle) {
        textStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
      }
    };

    this.aquaStyler= new Styler() {
      public void applyStyles(TextStyle textStyle) {
        textStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
      }
    };

    this.blueStyler= new Styler() {
      public void applyStyles(TextStyle textStyle) {
        textStyle.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
      }
    };
  }

  @Override
  public StyledString getStyledText(Object element, StyledString styledString) {
    if(element instanceof IResource){
      IResource resource = (IResource)element;

      // remote link styling
      if(resource.hasAspect(VeloConstants.ASPECT_REMOTE_LINK)) {
        // First make the original file string be aqua
        styledString.setStyle(0, styledString.length(), aquaStyler);
        styledString.append(" --> ");
        String host = resource.getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_MACHINE);
        String path = resource.getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_PATH);
        if(host != null && path != null) {
          styledString.append(host.toLowerCase() + path, blueStyler);
        } else {
          String url = resource.getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_URL);
          if(url != null) {
            styledString.append(url, blueStyler);
          }
        }
      }
      
      // Job status styling
      String status = resource.getPropertyAsString(VeloTifConstants.JOB_STATUS);  
      if(status != null) {
        if(status.equals(VeloTifConstants.STATUS_SUCCESS) || status.equals(VeloTifConstants.STATUS_SUBMITTING) 
            || status.equals(VeloTifConstants.STATUS_WAIT) || status.equals(VeloTifConstants.STATUS_START)
            || status.equals(VeloTifConstants.STATUS_POSTPROCESS) || status.equals(VeloTifConstants.STATUS_RECONNECT)
           ) {
          styledString = styledString.append(" [" + status + "] ",  greenStyler);        
        
        } else if( status.equals(VeloTifConstants.STATUS_KILLED) || status.equals(VeloTifConstants.STATUS_CANCELLED)
            || status.equals(VeloTifConstants.STATUS_ERROR)) {
          styledString = styledString.append(" [" + status + "] ",  redStyler);        
        
        } else if(status.equals(VeloTifConstants.STATUS_DISCONNECTED)) {
          styledString = styledString.append(" [" + status + "] ",  grayStyler);  
          
        } else {
          styledString = styledString.append(" [" + status + "] ",  StyledString.COUNTER_STYLER);  
        }
      }

      String failedRuns = resource.getPropertyAsString(VeloTifConstants.JOB_RUNS_FAILED);  
      if(failedRuns != null) {
         styledString = styledString.append(" - has failed runs ", redStyler);  
      }
            
//      //sample of prepending - make a new styledString with the text to prepend, then append the orig styledString
//      StyledString  styledString2 = new StyledString(" [COUNTER_STYLER] ", boldStyler);
//      styledString = styledString2.append(styledString);


    }
    return styledString;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.StyledWorkbenchLabelProvider#getDecorationStyle(java.lang.Object, org.eclipse.jface.viewers.StyledString.Styler)
   */
  @Override
  public Styler getDecorationStyle(Object element, Styler styler) {
    // looks like this is never called
    if(element instanceof IResource) {
      IResource resource = (IResource)element;
      if(resource.hasAspect(VeloConstants.ASPECT_REMOTE_LINK)) {
        return aquaStyler;
      }
    }
    return styler;
  }


}
