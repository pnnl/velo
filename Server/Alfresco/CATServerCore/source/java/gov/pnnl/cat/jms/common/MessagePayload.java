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
package gov.pnnl.cat.jms.common;

import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;
import gov.pnnl.cat.util.XmlUtility;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A base class for message payloads.  Extend to include
 * attributes and properties for specific messages.
 * @author D3G574
 *
 * @version $Revision: 1.0 $
 */
public class MessagePayload implements Serializable {
	private transient Date timestamp;
	private transient Map<String,String> properties;
	private transient static final Log logger = LogFactory.getLog(MessagePayload.class); 
	
	private static transient boolean useXmlPayload = false;
	
	static {
    String xmlPayloadParam = System.getProperty("jms.message.payload.format");
   
    if(xmlPayloadParam != null) {
      if(xmlPayloadParam.equals("xml")) {
        useXmlPayload = true;
      }
    }

	}
	
	/**
	 * Method toString.
	 * @return String
	 */
	@Override
	public String toString() {
	  String str = "";
	  try {
	    
	    if(useXmlPayload) {
	      str = XmlUtility.serialize(this);
	      
	    } else {
	      ObjectMapper mapper = new ObjectMapper();
	      str =  mapper.writeValueAsString(this);
	    }
	    
	  } catch (Throwable e) {
	    logger.error("Exception encoding json from JMS message.", e);	    
	  }
	  return str;
	}
		
	/**
	 * Method fromString.
	 * @param payloadText String
	 * @param classs Class<?>
	 * @return MessagePayload
	 */
	public static MessagePayload fromString(String payloadText, Class<?> classs) {
	  MessagePayload ret = null;
	  
		try {      
		  // first, to be backwards compatible, we need to see if the string is xml or json format
		  if(payloadText.startsWith("<gov.pnnl.cat")) {
		    ret = (MessagePayload) XmlUtility.deserialize(payloadText);
		    
		  } else {		  
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.registerSubtypes(RepositoryEventMessage.class);
		    ret = (MessagePayload) mapper.readValue(payloadText, classs);
		  }

		} catch (Exception e) {
		  throw new RuntimeException("Exception decoding json from JMS message.", e);
		}
		
		return ret;
	}
	
	/**
	 * Method getProperties.
	 * @return Map<String,String>
	 */
	public Map<String,String> getProperties() {
		if (properties == null) {
			properties = new HashMap<String, String>();
		}
		return properties;
	}
	
	/**
	 * Method setProperties.
	 * @param properties Map<String,String>
	 */
	public void setProperties(Map<String,String> properties) {
		this.properties = properties;
	}
	
	/**
	 * Method setProperty.
	 * @param key String
	 * @param value String
	 */
	public void setProperty(String key, String value) {
		if (properties == null) {
			properties = new HashMap<String,String>();
		}
		properties.put(key, value);
	}
}
