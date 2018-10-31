package gov.pnnl.velo.model;

import java.io.Serializable;

public class Relationship implements Serializable{
  private static final long serialVersionUID = 1L;
  
  // Only store uuids since that's what get stored in alfresco
  // we may not be able to resolve them because of permissions issues
  private String sourceResourceUuid;
  private String sourcePath;
  private String destinationResourceUuid;
  private String destinationPath;
  private String relationshipType; // fully qualified association type 
  
  
  public Relationship(){
    
  }
  
  public Relationship(String sourceResourceUuid, String destinationResourceUuid, String relationshipType) {
    super();
    this.sourceResourceUuid = sourceResourceUuid;
    this.destinationResourceUuid = destinationResourceUuid;
    this.relationshipType = relationshipType;
  }
  
  public String getSourceResourceUuid() {
    return sourceResourceUuid;
  }

  public void setSourceResourceUuid(String sourceResourceUuid) {
    this.sourceResourceUuid = sourceResourceUuid;
  }

  public String getDestinationResourceUuid() {
    return destinationResourceUuid;
  }

  public void setDestinationResourceUuid(String destinationResourceUuid) {
    this.destinationResourceUuid = destinationResourceUuid;
  }

  public String getSourcePath() {
    return sourcePath;
  }

  public void setSourcePath(String sourcePath) {
    this.sourcePath = sourcePath;
  }

  public String getDestinationPath() {
    return destinationPath;
  }

  public void setDestinationPath(String destinationPath) {
    this.destinationPath = destinationPath;
  }

  public String getRelationshipType() {
    return relationshipType;
  }

  public void setRelationshipType(String relationshipType) {
    this.relationshipType = relationshipType;
  }
  
}
