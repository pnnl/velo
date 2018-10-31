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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 */
public class ActiveMQBrokerLaunch extends AbstractLifecycleBean implements InitializingBean {

  private String activeMQCommand;
  private Process process;
  private static final Log logger = LogFactory.getLog(ActiveMQBrokerLaunch.class);

  /**
   * Method setActiveMQCommand.
   * @param activeMQCommand String
   */
  public void setActiveMQCommand(String activeMQCommand) {
    this.activeMQCommand = activeMQCommand;
  }

  /**
   * Method afterPropertiesSet.
   * @throws Exception
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  public void afterPropertiesSet() throws Exception {
    // TODO Auto-generated method stub

    process = Runtime.getRuntime().exec(activeMQCommand);
    logger.debug("ActiveMQ Broker Launched.");

//    ProcessHandler stdout = new ProcessHandler(process.getInputStream(), "STDOUT");
//    ProcessHandler stderr = new ProcessHandler(process.getErrorStream(), "STDERR");
//    stdout.start();
//    stderr.start();


  }

  /**
   * Method onBootstrap.
   * @param event ApplicationEvent
   */
  protected void onBootstrap(ApplicationEvent event) {
    // do nothing
  }

  /**
   * Method onShutdown.
   * @param event ApplicationEvent
   */
  protected void onShutdown(ApplicationEvent event) {
    process.destroy();
  }



  /**
   * A class to read the STDOUT and STDERR from the spawned process
   * If we don't continually read them, the launched process may deadlock.
   * 
   * Adapted from http://vyvaks.wordpress.com/2006/05/27/does-runtimeexec-hangs-in-java/
   * 
   * @author D3G574
   *
   * @version $Revision: 1.0 $
   */
  public class ProcessHandler extends Thread {

    InputStream inpStr;
    String strType;

    /**
     * Constructor for ProcessHandler.
     * @param inpStr InputStream
     * @param strType String
     */
    public ProcessHandler(InputStream inpStr, String strType) {
      this.inpStr = inpStr;
      this.strType = strType;
      this.setName("ActveMQ " + strType + " Reader");

    }

    /**
     * Method run.
     * @see java.lang.Runnable#run()
     */
    public void run() {
      try {
        InputStreamReader inpStrd = new InputStreamReader(inpStr);
        BufferedReader buffRd = new BufferedReader(inpStrd);
        String line = null;
        while((line = buffRd.readLine()) != null) {
          logger.debug(strType + " -> " + line);
        }
        buffRd.close();

      } catch(Exception e) {
        System.out.println(e);
      }

    }
  }


}


