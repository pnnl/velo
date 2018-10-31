package gov.pnnl.velo.util;

import gov.pnnl.velo.model.CmsPath.Segment;

/**
 * Extend this interface to validate or rename name of nodes (files, folders, links etc.)
 * Each application can create its own implementation and declare it as a bean. Cat core 
 * methods to create File, folders and links will call the processNodeName before creating the node. 
 * @author Chandrika Sivaramakrishnan
 *
 */
public interface NodeNameProcessor {
  
  /**
   * Cat core methods to create File, folders and links will call the processNodeName before creating the node. 
   * Implementing methods should atleast check for special characters that alfresco does not allow("\" * \\ > < ? / : |"). 
   * Applications can have more restricted name policies  
   * @param name - user provided node(file/folder/link) name Segment
   * @return - same segment if Valid, modified name with same namespace based on application specific renaming rules or RuntimeException
   * if the application wants the user to rename the file
   */
  public Segment processNodeName(Segment name);
  
  /**
   * Cat core methods to create File, folders and links will call the processNodeName before creating the node. 
   * Implementing methods should atleast check for special characters that alfresco does not allow("\" * \\ > < ? / : |"). 
   * Applications can have more restricted name policies
   * @param name - user provided node(file/folder/link) name string
   * @return - same name if valid, or modified name string based on application specific renaming rules or throws RuntimeException
   * if the application wants the user to rename the file
   */
  public String processNodeName(String name);

}
