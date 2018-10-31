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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Customizable thread pool executor bean used for throttling
 * threads, since there are so many.  Priority can be set at
 * low, normal, or max.
 *
 * We may need to tweak the settings if threads are being throttled
 * too much.
 * @version $Revision: 1.0 $
 */
public class PrioritizedThreadPoolExecutor extends ThreadPoolExecutor implements BeanNameAware, InitializingBean {
  
  /**
   * Default pool values
   */
//  private static final int CORE_POOL_SIZE = 2;
//
//  private static final int MAX_POOL_SIZE = 5;
//
//  private static final long KEEP_ALIVE = 30;
//
//  private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
//
//  private static final int MAX_QUEUE_SIZE = 500;
//  
  // logger
  private static final Log logger = LogFactory.getLog(PrioritizedThreadPoolExecutor.class);
  
  // bean name
  private String name;
  
  /**
   * Try using a fixed pool size with an unbounded queue
   * @param threadFactory ThreadFactory
   * @param poolSize int
   */
  public PrioritizedThreadPoolExecutor(ThreadFactory threadFactory, int poolSize) {
    
//    super(CORE_POOL_SIZE, 
//        MAX_POOL_SIZE, 
//        KEEP_ALIVE, 
//        TIME_UNIT, 
//        new ArrayBlockingQueue<Runnable>(MAX_QUEUE_SIZE, true),
//        threadFactory);
    
    super(poolSize, poolSize,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(), threadFactory);
  }

  /**
   * Method beforeExecute.
   * @param t Thread
   * @param r Runnable
   */
  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    logger.debug(this.getActiveCount()+ " threads running " + this.name);   
    logger.debug("queue size = " + this.getQueue().size() + " in " + this.name);
    logger.debug("thread priority = " + t.getPriority() + " in " + this.name);
    super.beforeExecute(t, r);
  }

  /**
   * The name is taken from the bean name
   * @param name
   * @see org.springframework.beans.factory.BeanNameAware#setBeanName(String)
   */
  public void setBeanName(String name) {
    this.name = name;
  }

  /**
   * Method afterPropertiesSet.
   * @throws Exception
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  public void afterPropertiesSet() throws Exception {
    logger.debug (this.name + " initialized");
  }
  
}
