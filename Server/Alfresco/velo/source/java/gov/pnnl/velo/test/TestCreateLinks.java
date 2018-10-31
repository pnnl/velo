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
package gov.pnnl.velo.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 */
public class TestCreateLinks {

  /**
   * @param args
   */
  public static void main(String[] args) {
    String sourceFile = "/home/d3k339/VELO/Build/wikiDeploy.sh";
    String linkFile = "/home/d3k339/link.sh";
    File runDir = new File("/home/d3k339");
    
    //ln -s `readlink -f alf_data/contentstore/2013/4/4/16/41/4fdd836b-82da-4ea5-8a85-d8e7daf9049d.bin` 
    //String[] cmdArray = {"ln", "-s", "`readlink", "-f", sourceFile, linkFile};

    //String[] cmdArray = {"ln", "-s", "`", "readlink", "-f", sourceFile, "`", linkFile};
    String cmd[] = {"bash", "-c", "ln -s $(readlink -f " + sourceFile + ") " + linkFile };
    
    try {
      execCommand(cmd, runDir);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    

  }
  
  /**
   * Method execCommand.
   * @param cmdArray String[]
   * @param runDir File
   * @throws Exception
   */
  public static void execCommand(String[] cmdArray, File runDir)
      throws Exception {
System.out.println();
    System.out.println("executing command:");
    try {
      for (String arg : cmdArray) {
        System.out.println(arg);
      }
      System.out.println(cmdArray);
      
      Runtime rt = Runtime.getRuntime();
      Process proc = rt.exec(cmdArray, null, runDir);

      StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
      StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());

      errorGobbler.start();
      outputGobbler.start();

      int exitVal = proc.waitFor();
      if (exitVal != 0) {
        errorGobbler.join();
        String msg = errorGobbler.getMessage();
        System.err.println(msg);
        // throw new RuntimeException(msg);
      }
      System.out.println(outputGobbler.getMessage());
      System.out.println(errorGobbler.getMessage());

    } catch (IOException e) {
      if (e.toString().contains("The system cannot find the file specified")) {
        System.err.println(e.toString());
      } else {
        throw e;
      }
    }

  }

  /**
   */
  public static class StreamGobbler extends Thread {
    private InputStream is;
    private StringBuffer msg = new StringBuffer();

    /**
     * Constructor for StreamGobbler.
     * @param is InputStream
     */
    public StreamGobbler(InputStream is) {
      this.is = is;
    }

    /**
     * Method getMessage.
     * @return String
     */
    public String getMessage() {
      return msg.toString();
    }

    /**
     * Method run.
     * @see java.lang.Runnable#run()
     */
    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
          msg.append(line + "\n");
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

}
