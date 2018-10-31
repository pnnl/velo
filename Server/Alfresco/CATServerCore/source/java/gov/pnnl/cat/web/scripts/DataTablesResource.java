package gov.pnnl.cat.web.scripts;

class DataTablesResource {
  private String name;
  private String size;
  private String type;
  private String contentUrl;
  private String modifiedDate;

  public DataTablesResource() {
  }

  public DataTablesResource(String name, String size, String type, String contentUrl, String modifiedDate) {
    this.name = name;
    this.size = size;
    this.type = type;
    this.contentUrl = contentUrl;
    this.modifiedDate = modifiedDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getContentUrl() {
    return contentUrl;
  }

  public void setContentUrl(String contentUrl) {
    this.contentUrl = contentUrl;
  }

  public String getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(String modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

}
