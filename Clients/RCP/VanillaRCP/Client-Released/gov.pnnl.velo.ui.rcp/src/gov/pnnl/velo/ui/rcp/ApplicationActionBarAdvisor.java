package gov.pnnl.velo.ui.rcp;

//import org.eclipse.ui.IWorkbenchWindow;
//import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.IActionBarConfigurer;

import gov.pnnl.cat.ui.common.rcp.AbstractCatApplicationActionBarAdvisor;

public class ApplicationActionBarAdvisor extends AbstractCatApplicationActionBarAdvisor {

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
        showNewMenu = false;
        showImportMenu = false;
        showExportMenu = false;
        
    }
    
//    protected void makeActions(IWorkbenchWindow window) {
//      super.makeActions(window);
//      register(ActionFactory.HELP_SEARCH.create(window));
//      register(ActionFactory.DYNAMIC_HELP.create(window));
//    }

    
    
}
