package gov.pnnl.velo.model;

import org.alfresco.webservice.action.Action;
import org.alfresco.webservice.types.Predicate;


public class ExecuteActionsRequest {
  private Predicate predicate;
  private Action[] webServiceActions;
  
  
  public ExecuteActionsRequest(){
    
  }
  
  public ExecuteActionsRequest(Predicate predicate, Action[] webServiceActions) {
    super();
    this.predicate = predicate;
    this.webServiceActions = webServiceActions;
  }
  public Predicate getPredicate() {
    return predicate;
  }
  public void setPredicate(Predicate predicate) {
    this.predicate = predicate;
  }
  public Action[] getWebServiceActions() {
    return webServiceActions;
  }
  public void setWebServiceActions(Action[] webServiceActions) {
    this.webServiceActions = webServiceActions;
  }
}
