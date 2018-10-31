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
package gov.pnnl.cat.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public class PrioritizedThreadFactory implements ThreadFactory {

  static final AtomicInteger poolNumber = new AtomicInteger(1);
  final ThreadGroup group;
  final AtomicInteger threadNumber = new AtomicInteger(1);
  final String namePrefix;
  
  private int threadPriority;
  private String label;
  
  public PrioritizedThreadFactory(String label, String priority) {
    this();
    setLabel(label);
    setPriority(priority);
  }
  
  /**
   * Used DefaultThreadFactory as an example.
   *  
   */
  public PrioritizedThreadFactory() {
    
    SecurityManager s = System.getSecurityManager();
    group = (s != null)? s.getThreadGroup() :
      Thread.currentThread().getThreadGroup();
    namePrefix = "pool-" + 
    poolNumber.getAndIncrement() + 
    "-thread-";
  }

  /**
   * Method getLabel.
   * @return String
   */
  public String getLabel() {
    return label;
  }

  /**
   * Method setLabel.
   * @param label String
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Method setPriority.
   * @param priority String
   */
  public void setPriority(String priority) {
    
    if(priority.equals("low")) {
      this.threadPriority = Thread.MIN_PRIORITY;
    } else if (priority.equals("medium")) {
      this.threadPriority = Thread.NORM_PRIORITY;
    } else if (priority.equals("high")) {
      this.threadPriority = Thread.MAX_PRIORITY;
    } else {
      throw new RuntimeException(priority + " is not a valid priority!");
    }    
  }


  /**
   * Method newThread.
   * @param r Runnable
   * @return Thread
   * @see java.util.concurrent.ThreadFactory#newThread(Runnable)
   */
  public Thread newThread(Runnable r) {
    Thread t = new Thread(group, r, 
        namePrefix + threadNumber.getAndIncrement(),
        0);
    if (t.isDaemon())
      t.setDaemon(false);
    if (t.getPriority() != threadPriority)
      t.setPriority(threadPriority);
    
    // Set the name so we know what it is
    t.setName(label);
    t.setDaemon(true); // make sure this doesn't hold up the JVM being shut down
    return t;
  }
}
