package gov.pnnl.cat.ui.rcp.security;

/**
 * Converts between alfresco permissions and the display name and description that should be associated with it.
 * @author D3K339
 *
 */
public class PermissionDescriptor {
  
  private String alfrescoPermission;
  private String displayDescription;
  private String displayLabel;
  
  public PermissionDescriptor(String alfrescoPermission, String displayDescription, String displayLabel) {
    super();
    this.alfrescoPermission = alfrescoPermission;
    this.displayDescription = displayDescription;
    this.displayLabel = displayLabel;
  }

  public String getAlfrescoPermission() {
    return alfrescoPermission;
  }

  public void setAlfrescoPermission(String alfrescoPermission) {
    this.alfrescoPermission = alfrescoPermission;
  }

  public String getDisplayDescription() {
    return displayDescription;
  }

  public void setDisplayDescription(String displayDescription) {
    this.displayDescription = displayDescription;
  }

  public String getDisplayLabel() {
    return displayLabel;
  }

  public void setDisplayLabel(String displayLabel) {
    this.displayLabel = displayLabel;
  }
  
  
}
