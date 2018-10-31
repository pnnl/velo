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
package gov.pnnl.velo.exception;

/**
 * Exception indicating some kind of provenance violation 
 * has occurred.  Later we can subclass this if we need more
 * info than what can be stored in the message.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class ProvenanceException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor for ProvenanceException.
   * @param message String
   * @param cause Throwable
   */
  public ProvenanceException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  /**
   * Constructor for ProvenanceException.
   * @param message String
   */
  public ProvenanceException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  /**
   * Constructor for ProvenanceException.
   * @param cause Throwable
   */
  public ProvenanceException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

  
}
