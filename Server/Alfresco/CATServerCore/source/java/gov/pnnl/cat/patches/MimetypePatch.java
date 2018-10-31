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
package gov.pnnl.cat.patches;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.mimetype.MimetypeDAO;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Since Alfresco has this bug:  http://issues.alfresco.com/jira/browse/ALF-6750
 * we need to go through the mimetype map and make sure one of each type of mimetype
 * is created when the server starts up.
 *
 * @version $Revision: 1.0 $
 */
public class MimetypePatch extends AbstractPatch {

  // Logger
  private static final Log logger = LogFactory.getLog(MimetypePatch.class);
  private MimetypeDAO mimetypeDAO;
  private MimetypeService mimetypeService;

  /**
   * Ensure that required properties have been set
   * @throws Exception
   */
  protected void checkRequiredProperties() throws Exception
  {
      checkPropertyNotNull(nodeService, "nodeService");
  }
  
  
  /**
   * Method applyInternal.
   * @return String
   * @throws Exception
   */
  @Override
  protected String applyInternal() throws Exception {
    
    // Create all the mimetypes in the mimetype map
    List<String> mimetypes = mimetypeService.getMimetypes();
    for(String mimetype : mimetypes) {
      mimetypeDAO.getOrCreateMimetype(mimetype);
    }
    return "mimetype patch completed";
  }


  /**
   * Method setMimetypeDAO.
   * @param mimetypeDAO MimetypeDAO
   */
  public void setMimetypeDAO(MimetypeDAO mimetypeDAO) {
    this.mimetypeDAO = mimetypeDAO;
  }


  /**
   * Method setMimetypeService.
   * @param mimetypeService MimetypeService
   */
  public void setMimetypeService(MimetypeService mimetypeService) {
    this.mimetypeService = mimetypeService;
  }
 
}
