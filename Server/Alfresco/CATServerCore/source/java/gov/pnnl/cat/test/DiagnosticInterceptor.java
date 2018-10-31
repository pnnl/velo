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
package gov.pnnl.cat.test;


import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class DiagnosticInterceptor implements MethodInterceptor {

  private static Log logger = LogFactory.getLog(DiagnosticInterceptor.class);
  private static final String COUNT = "gov.pnnl.cat.test.DiagnosticInterceptor.count";
  
  /**
   * Method invoke.
   * @param mi MethodInvocation
   * @return Object
   * @throws Throwable
   * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
   */
  public Object invoke(MethodInvocation mi) throws Throwable {
    AlfrescoTransactionSupport.bindListener(new TransListener());
    incrementCount();
    return mi.proceed();
  }

  private void incrementCount() {
    Integer count = (Integer)AlfrescoTransactionSupport.getResource(COUNT);

    if (count == null) {
      count = new Integer(0);
    }
    count = new Integer(count.intValue() + 1);
    AlfrescoTransactionSupport.bindResource(COUNT, count);
  }
  
  /**
   */
  private class TransListener implements TransactionListener
  {

    /**
     * Method afterCommit.
     * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
     */
    public void afterCommit() {
      Integer count = (Integer)AlfrescoTransactionSupport.getResource(COUNT);
      logger.error("NodeService called " + count + " times in thread " + Thread.currentThread().getName());     
    }

    /**
     * Method afterRollback.
     * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
     */
    public void afterRollback() {
      // TODO Auto-generated method stub
      
    }

    /**
     * Method beforeCommit.
     * @param readOnly boolean
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
     */
    public void beforeCommit(boolean readOnly) {
      // TODO Auto-generated method stub
      
    }

    /**
     * Method beforeCompletion.
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
     */
    public void beforeCompletion() {
      // TODO Auto-generated method stub
      
    }

    /**
     * Method flush.
     * @see org.alfresco.repo.transaction.TransactionListener#flush()
     */
    public void flush() {
      // TODO Auto-generated method stub
      
    }
    
  }
}
