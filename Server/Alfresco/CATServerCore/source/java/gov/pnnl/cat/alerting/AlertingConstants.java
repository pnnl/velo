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
package gov.pnnl.cat.alerting;

import org.alfresco.service.namespace.QName;

/**
 */
public interface AlertingConstants {

  /** Namespace constants */
  public static final String NAMESPACE_ALERT = "http://www.pnl.gov/dmi/model/alert/1.0";
  public static final String NAMESPACE_SUBSCRIPTION = "http://www.pnl.gov/dmi/model/subscription/1.0";
  public static final String NAMESPACE_SUBSCRIPTION_REPOSITORY = "http://www.pnl.gov/dmi/model/subscription/repository/1.0";
  public static final String NAMESPACE_SUBSCRIPTION_REPOSITORY_FACTSHEET = "http://www.pnl.gov/dmi/model/subscription/repository/factsheet/1.0";
  public static final String NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH = "http://www.pnl.gov/dmi/model/subscription/repository/search/1.0";
  public static final String NAMESPACE_SUBSCRIPTION_RSS = "http://www.pnl.gov/dmi/model/subscription/rss/1.0";

  /** Prefix constants */
  static final String ALERT_MODEL_PREFIX = "alrt";
  static final String SUBSCRIPTION_MODEL_PREFIX = "sub";
  static final String SUBSCRIPTION_REPOSITORY_MODEL_PREFIX = "rep";
  static final String SUBSCRIPTION_REPOSITORY_SEARCH_MODEL_PREFIX = "srch";
  
  /** Alert node types */
  public static final QName TYPE_ALERT = QName.createQName(NAMESPACE_ALERT, "alert");
  public static final QName TYPE_TEMPORARY_ALERT = QName.createQName(NAMESPACE_ALERT, "temporaryAlert");
  
  /** Subscription node types */
  public static final QName TYPE_SUBSCRIPTION = QName.createQName(NAMESPACE_SUBSCRIPTION, "subscription");
  public static final QName TYPE_SUBSCRIPTION_REPOSITORY_SEARCH = QName.createQName(NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH, "subscription");
  public static final QName TYPE_SUBSCRIPTION_REPOSITORY = QName.createQName(NAMESPACE_SUBSCRIPTION_REPOSITORY, "subscription");
  public static final QName TYPE_SUBSCRIPTION_REPOSITORY_FACTSHEET = QName.createQName(NAMESPACE_SUBSCRIPTION_REPOSITORY_FACTSHEET, "subscription");
  public static final QName TYPE_SUBSCRIPTION_RSS = QName.createQName(NAMESPACE_SUBSCRIPTION_RSS, "subscription");

  
  /** Delivery channels */
  /** This is also the RSS channel **/
  public static final QName CHANNEL_REPOSITORY = QName.createQName(NAMESPACE_SUBSCRIPTION, "repository");
  public static final QName CHANNEL_EMAIL = QName.createQName(NAMESPACE_SUBSCRIPTION, "email");

  /** Property values */
  public static final QName PROP_SUB_REP_SUBSCRIPTION_NODE = QName.createQName(NAMESPACE_SUBSCRIPTION_REPOSITORY, "node");
  public static final QName PROP_SUB_REP_INCLUDE_CHILDREN = QName.createQName(NAMESPACE_SUBSCRIPTION_REPOSITORY, "includeChildren");
  public static final QName PROP_SUB_REP_CHANGE_TYPE = QName.createQName(NAMESPACE_SUBSCRIPTION_REPOSITORY, "changeType");
  
  public static final QName PROP_SUB_SEARCH_QUERY = QName.createQName(NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH, "query");
  public static final QName PROP_SUB_SEARCH_CHANGE_TYPE = QName.createQName(NAMESPACE_SUBSCRIPTION_REPOSITORY_SEARCH, "changeType");

  public static final QName PROP_SUB_RSS_URL = QName.createQName(NAMESPACE_SUBSCRIPTION_RSS, "url");
  public static final QName PROP_SUB_RSS_CHANGE_TYPE = QName.createQName(NAMESPACE_SUBSCRIPTION_RSS, "changeType");


  public static final QName PROP_SUB_DELIVERY_CHANNELS = QName.createQName(NAMESPACE_SUBSCRIPTION, "deliveryChannels");
  public static final QName PROP_SUB_LAST_ALERT_SENT = QName.createQName(NAMESPACE_SUBSCRIPTION, "lastAlertSent");
  
  public static final String PROP_SUB_FREQUENCY_LOCALNAME = "frequency";
  
  public static final QName PROP_ALERT_SENDER = QName.createQName(NAMESPACE_ALERT, "sender");
  public static final QName PROP_ALERT_RECIPIENTS = QName.createQName(NAMESPACE_ALERT, "recipients");
  public static final QName PROP_ALERT_SUBSCRIPTION_TYPE = QName.createQName(NAMESPACE_ALERT, "subscriptionType");
  public static final QName PROP_ALERT_FREQUENCY = QName.createQName(NAMESPACE_ALERT, "frequency");
  public static final QName PROP_ALERT_SUMMARY = QName.createQName(NAMESPACE_ALERT, "summary");
  public static final QName PROP_ALERT_SUBSCRIPTION = QName.createQName(NAMESPACE_ALERT, "subscription");
  public static final QName PROP_ALERT_WAS_READ = QName.createQName(NAMESPACE_ALERT, "wasRead");
  public static final QName PROP_ALERT_BKMS_DISPLAY = QName.createQName(NAMESPACE_ALERT, "bkmsDisplay");
  public static final QName PROP_CONTENT = QName.createQName(NAMESPACE_ALERT, "content");
  
  /** Name Constants */
  public static final QName NAME_ALERTS_FOLDER = QName.createQName(NAMESPACE_ALERT, "Alerts");
  public static final QName NAME_SUBSCRIPTIONS_FOLDER = QName.createQName(NAMESPACE_SUBSCRIPTION, "Subscriptions");
   public static final QName NAME_TEMPORARY_ALERTS_FOLDER = QName.createQName(NAMESPACE_SUBSCRIPTION, "TemporaryAlerts");
  
  /** Change Types */
  public static final String CHANGE_TYPE_NEW = "new";
  public static final String CHANGE_TYPE_MODIFIED = "modified";
  public static final String CHANGE_TYPE_DELETED = "deleted";
  public static final String CHANGE_TYPE_TAGTIMER_EXPIRED = "expired";
  public static final String CHANGE_TYPE_TAGTIMER_ABOUT_TO_EXPIRE = "expiring";
  
  /** Associations */
  public static final QName ASSOC_TEMP_ALERTS_CONTAINER = QName.createQName(NAMESPACE_SUBSCRIPTION, "tempAlertsContainer");
  
  
  public static final QName MODEL_ALERT = QName.createQName(NAMESPACE_ALERT, "alertmodel");
  
  
  /** Mime Types */
  public static final String MIME_TYPE_DELIVERY_EMAIL = "delivery/email";
  public static final String MIME_TYPE_DELIVERY_REPOSITORY = "delivery/repository";

  /** Constants for Transform options map */
  public static final String TRANSFORM_OPTION_ALERT = "alert";
}
