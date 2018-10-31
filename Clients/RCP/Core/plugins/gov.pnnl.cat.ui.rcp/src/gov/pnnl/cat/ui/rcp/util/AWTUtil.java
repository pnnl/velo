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
package gov.pnnl.cat.ui.rcp.util;


import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import gov.pnnl.velo.core.util.ThreadUtils;

/**
 * Utility class to simplify thread-safe AWT access
 * @version $Revision: 1.0 $
 */
public class AWTUtil {
  
  /**
   * Execute a callback in the AWT thread asynchronously, blocking until it returns.
   * @param cb  The callback containing the unit of work.
   * @return    Returns the result of the unit of work.
   * @throws    RuntimeException
   */
  public static <R> R blockingAsyncExec(final Callable<R> cb) {
    if(ThreadUtils.isSWTThread()) {
      return execBlockingSwingFromSwt(cb);
    
    } else {
      return execBlockingSwingFromSwing(cb);
    }   
  }
  
  @SuppressWarnings("unchecked")
  public static <R> R execBlockingSwingFromSwing(final Callable<R> cb) {
    final Object[] result = {null};

    try {
      SwingUtilities.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          try {
            result[0] = cb.call();

          } catch(RuntimeException e) {
            throw e;
          } catch (Exception e) {
            throw new RuntimeException(e);
          } 

        }
      });
    } catch(RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 

    return (R) result[0];
  }
  
  @SuppressWarnings("unchecked")
  public static <R> R execBlockingSwingFromSwt(final Callable<R> cb) {
    final Object[] result = {null};
    final boolean[] done = {false};
  
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          result[0] = cb.call();
          
        } catch(RuntimeException e) {
          throw e;
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          done[0] = true;
        }
      }

    });

    while (done[0] == false) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return (R) result[0];
  }
  
}
