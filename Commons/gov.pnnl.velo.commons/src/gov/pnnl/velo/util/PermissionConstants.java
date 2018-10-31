package gov.pnnl.velo.util;

public class PermissionConstants extends VeloConstants {
  /** Permission prefixes for role's and group's */
  public static final String ROLE_PREFIX      = "ROLE_";    
  public static final String GROUP_PREFIX     = "GROUP_";
  
  /** Standard authorities */
  public static final String ALL_AUTHORITIES          = "GROUP_EVERYONE";
  public static final String OWNER_AUTHORITY          = "ROLE_OWNER";
  public static final String LOCK_OWNER_AUTHORITY     = "ROLE_LOCK_OWNER";
  public static final String ADMINISTRATOR_AUTHORITY  = "ROLE_ADMINISTRATOR";

  /** Common permissions */
  public static final String ALL_PERMISSIONS          = "All";
  public static final String FULL_CONTROL             = "FullControl";
  public static final String READ                     = "Read";
  public static final String WRITE                    = "Write";
  public static final String DELETE                   = "Delete";
  public static final String ADD_CHILDREN             = "AddChildren";
  public static final String READ_PROPERTIES          = "ReadProperties";
  public static final String READ_CHILDREN            = "ReadChildren";
  public static final String WRITE_PROPERTIES         = "WriteProperties";
  public static final String DELETE_NODE              = "DeleteNode";
  public static final String DELETE_CHILDREN          = "DeleteChildren";
  public static final String CREATE_CHILDREN          = "CreateChildren";
  public static final String LINK_CHILDREN            = "LinkChildren";
  public static final String DELETE_ASSOCIATIONS      = "DeleteAssociations";
  public static final String READ_ASSOCIATIONS        = "ReadAssociations";
  public static final String CREATE_ASSOCIATIONS      = "CreateAssociations";
  public static final String READ_PERMISSIONS         = "ReadPermissions";
  public static final String CHANGE_PERMISSIONS       = "ChangePermissions";
  public static final String EXECUTE                  = "Execute";
  public static final String READ_CONTENT             = "ReadContent";
  public static final String WRITE_CONTENT            = "WriteContent";
  public static final String EXECUTE_CONTENT          = "ExecuteContent";
  public static final String TAKE_OWNERSHIP           = "TakeOwnership";
  public static final String SET_OWNER                = "SetOwner";
  public static final String COORDINATOR              = "Coordinator";
  public static final String CONTRIBUTOR              = "Contributor";
  public static final String EDITOR                   = "Editor";
  public static final String GUEST                    = "Guest";
  public static final String LOCK                     = "Lock";   
  public static final String UNLOCK                   = "Unlock";
  public static final String CHECK_OUT                = "CheckOut";
  public static final String CHECK_IN                 = "CheckIn";
  public static final String CANCEL_CHECK_OUT         = "CancelCheckOut";

}
