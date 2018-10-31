package gov.pnnl.cat.core.internal.resources.search;

public class JsonCatQueryResult extends CatQueryResult {

  private String rawJson;
  
  public JsonCatQueryResult() {
    
  }

  /**
   * @return the rawJson
   */
  public String getRawJson() {
    return rawJson;
  }

  /**
   * @param rawJson the rawJson to set
   */
  public void setRawJson(String rawJson) {
    this.rawJson = rawJson;
  }

}
