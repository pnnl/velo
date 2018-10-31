package org.eclipse.rse.globusonline;

import org.eclipse.rse.core.model.IPropertySet;

public interface IGlobusOnlineConnectorService {

  // Globus Online Endpoint ID
  public static final String PARAMETER_GLOBUS_ENDPOINT_ID = "endpointId";
  // Globus Online port
  public static final String PARAMTER_GLOBUS_ENDPOINT_MY_PROXY_HOST = "myProxyHost";
  // Path on network filesystem that is the same on globus endpoint and host machine
  // (e.g., /pic is same path on olympus and pic#dtn)
  public static final String PARAMETER_GLOBUS_ACCESSIBLE_PATH = "globalPath"; // TODO: maybe need better name for this - rename to globusAccessiblePath
  
  public static final String LABEL_GLOBUS_ONLINE_PROPERTIES = "Globus Online Properties";
  
  public static final String PROPERTY_SET_GLOBUS = "Globus Online Settings";

  
  /**
   * @param endpointId
   * @param myProxyHost - need this in order to authenticate - could be a different server than where endpoint resides
   * @param globusAccessiblePath - TODO: this should be a list, as there could be multiple mounted shared paths
   * @return
   */
  public IPropertySet setGlobusOnlineProperties(String endpointId, String myProxyHost, String globusAccessiblePath);
}
