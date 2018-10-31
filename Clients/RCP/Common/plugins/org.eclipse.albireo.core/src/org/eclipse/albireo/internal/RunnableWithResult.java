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
package org.eclipse.albireo.internal;

/**
 * A Runnable that produces a result, but nevertheless can be used as a
 * {@link Runnable}.
 * @version $Revision: 1.0 $
 */
public abstract class RunnableWithResult implements Runnable {
    private Object result;

    /**
     * Executes the user-defined code.
     * It should call {@link #setResult} to assign a result.
     * @see java.lang.Runnable#run()
     */
    abstract public void run();

    /**
     * Returns the result.
     * This method can be called after {@link run()} was executed.
     * @return Object
     */
    public Object getResult() {
        return result;
    }

    /**
     * Assigns a result. This method should be called once during
     * {@link #run}.
     * @param result Object
     */
    protected void setResult(Object result) {
        this.result = result;
    }

}
