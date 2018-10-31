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
package gov.pnnl.cat.alerting.transform;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.util.XmlUtility;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 */
public abstract class AbstractDeliveryContentTransformer extends AbstractContentTransformer2 {

  /**
   * Method transformInternal.
   * @param reader ContentReader
   * @param writer ContentWriter
   * @param options TransformationOptions
   * @throws Exception
   */
  @Override
  protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception {
    Alert alert = (Alert) options.toMap().get(AlertingConstants.TRANSFORM_OPTION_ALERT);

    String eventXml = reader.getContentString();
    List<Event> events = XmlUtility.deserialize(eventXml);

    transformAlert(alert, events, writer, options.toMap());

  }

  /**
   * Method transformAlert.
   * @param alert Alert
   * @param events List<Event>
   * @param writer ContentWriter
   * @param options Map<String,Object>
   */
  protected abstract void transformAlert(Alert alert, List<Event> events, ContentWriter writer, Map<String, Object> options);

}
