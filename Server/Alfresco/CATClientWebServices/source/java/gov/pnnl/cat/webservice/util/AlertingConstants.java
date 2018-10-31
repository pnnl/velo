/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.webservice.util;

import org.alfresco.webservice.util.Constants;


/**
 */
public class AlertingConstants extends Constants {

  /** Namespace constants */
  public static final String NAMESPACE_ALERT = "http://www.pnl.gov/dmi/model/alert/1.0";
  public static final String NAMESPACE_SUBSCRIPTION = "http://www.pnl.gov/dmi/model/subscription/1.0";
  public static final String NAMESPACE_SUBSCRIPTION_REPOSITORY = "http://www.pnl.gov/dmi/model/subscription/repository/1.0";
  public static final String NAMESPACE_SUBSCRIPTION_REPOSITORY_FACTSHEET = "http://www.pnl.gov/dmi/model/subscription/repository/factsheet/1.0";
  public static final String NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH = "http://www.pnl.gov/dmi/model/subscription/repository/search1.0";
  
  
  /** Prefix constants */
  static final String ALERT_MODEL_PREFIX = "alrt";
  static final String SUBSCRIPTION_MODEL_PREFIX = "sub";
  static final String SUBSCRIPTION_REPOSITORY_MODEL_PREFIX = "rep";
  static final String SUBSCRIPTION_REPOSITORY_SEARCH_MODEL_PREFIX = "srch";
  
  /** Alert node types */
  public static final String TYPE_ALERT = createQNameString(NAMESPACE_ALERT, "alert");
  public static final String TYPE_TEMPORARY_ALERT = createQNameString(NAMESPACE_ALERT, "temporaryAlert");
  
  /** Subscription node types */
  public static final String TYPE_SUBSCRIPTION = createQNameString(NAMESPACE_SUBSCRIPTION, "subscription");
  public static final String TYPE_SUBSCRIPTION_REPOSITORY_SEARCH = createQNameString(NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH, "subscription");
  public static final String TYPE_SUBSCRIPTION_REPOSITORY = createQNameString(NAMESPACE_SUBSCRIPTION_REPOSITORY, "subscription");
  public static final String TYPE_SUBSCRIPTION_REPOSITORY_FACTSHEET = createQNameString(NAMESPACE_SUBSCRIPTION_REPOSITORY_FACTSHEET, "subscription");
  
  /** Delivery channels */
  /** This is also the RSS channel **/
  public static final String CHANNEL_REPOSITORY = createQNameString(NAMESPACE_SUBSCRIPTION, "repository");
  public static final String CHANNEL_EMAIL = createQNameString(NAMESPACE_SUBSCRIPTION, "email");

  /** Property values */
  public static final String PROP_SUB_REP_SUBSCRIPTION_NODE = createQNameString(NAMESPACE_SUBSCRIPTION_REPOSITORY, "node");
  public static final String PROP_SUB_REP_INCLUDE_CHILDREN = createQNameString(NAMESPACE_SUBSCRIPTION_REPOSITORY, "includeChildren");
  public static final String PROP_SUB_REP_CHANGE_TYPE = createQNameString(NAMESPACE_SUBSCRIPTION_REPOSITORY, "changeType");
  
  public static final String PROP_SUB_DELIVERY_CHANNELS = createQNameString(NAMESPACE_SUBSCRIPTION, "deliveryChannels");
  public static final String PROP_SUB_LAST_ALERT_SENT = createQNameString(NAMESPACE_SUBSCRIPTION, "lastAlertSent");
  
  public static final String PROP_SUB_FREQUENCY_LOCALNAME = "frequency";
  
  public static final String PROP_ALERT_SENDER = createQNameString(NAMESPACE_ALERT, "sender");
  public static final String PROP_ALERT_RECIPIENTS = createQNameString(NAMESPACE_ALERT, "recipients");
  public static final String PROP_ALERT_SUBSCRIPTION_TYPE = createQNameString(NAMESPACE_ALERT, "subscriptionType");
  public static final String PROP_ALERT_FREQUENCY = createQNameString(NAMESPACE_ALERT, "frequency");
  public static final String PROP_ALERT_SUMMARY = createQNameString(NAMESPACE_ALERT, "summary");
  public static final String PROP_ALERT_SUBSCRIPTION = createQNameString(NAMESPACE_ALERT, "subscription");
  public static final String PROP_ALERT_WAS_READ = createQNameString(NAMESPACE_ALERT, "wasRead");
  public static final String PROP_ALERT_BKMS_DISPLAY = createQNameString(NAMESPACE_ALERT, "bkmsDisplay");
  
  /** Name Constants */
  public static final String NAME_ALERTS_FOLDER = createQNameString(NAMESPACE_ALERT, "Alerts");
  public static final String NAME_SUBSCRIPTIONS_FOLDER = createQNameString(NAMESPACE_SUBSCRIPTION, "Subscriptions");
   public static final String NAME_TEMPORARY_ALERTS_FOLDER = createQNameString(NAMESPACE_SUBSCRIPTION, "TemporaryAlerts");
  
  /** Change Types */
  public static final String CHANGE_TYPE_NEW = "new";
  public static final String CHANGE_TYPE_MODIFIED = "modified";
  public static final String CHANGE_TYPE_DELETED = "deleted";
  
  /** Associations */
  public static final String ASSOC_TEMP_ALERTS_CONTAINER = createQNameString(NAMESPACE_SUBSCRIPTION, "tempAlertsContainer");
  
  
  public static final String MODEL_ALERT = createQNameString(NAMESPACE_ALERT, "alertmodel");
  
  
  /** Mime Types */
  public static final String MIME_TYPE_DELIVERY_EMAIL = "delivery/email";
  public static final String MIME_TYPE_DELIVERY_REPOSITORY = "delivery/repository";

  /** Constants for Transform options map */
  public static final String TRANSFORM_OPTION_ALERT = "alert";
}
