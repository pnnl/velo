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
/**
 * 
 */
package gov.pnnl.cat.web.scripts;

import gov.pnnl.velo.model.Resource;

import java.io.File;
import java.util.ArrayList;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TODO: Load all user and team profiles in one call, so we can preload them
 * quickly.  THIS IS JUST A STUB - THIS WEB SCRIPT NOT IMPLEMENTED YET
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class GetUsersAndTeams extends AbstractCatWebScript {
  

  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {

    // write the results to the output stream
    ArrayList<Resource> resources = getUsersAndTeams();

    // serialize children via json
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), resources);

    return null;

  }
  
  /**
   * Method getUsersAndTeams.
   * @return ResourceList
   * @throws Exception
   */
  protected ArrayList<Resource> getUsersAndTeams() throws Exception {
    
    ArrayList<Resource> resourceList = new ArrayList<Resource>();

   
    return resourceList;
  }
  
//  public UserQueryResults execute(ServiceRegistry serviceRegistry)
//  {
//      PersonService personService = serviceRegistry.getPersonService();
//      NodeService nodeService = serviceRegistry.getNodeService();
//
//      Set<NodeRef> nodeRefs = personService.getAllPeople();
//      
//      // Filter the results
//      List<NodeRef> filteredNodeRefs = null;
//      if (userFilter != null && userFilter.getUserName() != null && userFilter.getUserName().length() != 0)
//      {
//          String userNameFilter = userFilter.getUserName();
//          
//          filteredNodeRefs = new ArrayList<NodeRef>(nodeRefs.size());
//          for (NodeRef nodeRef : nodeRefs)
//          {
//              String userName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
//              if (userName.matches(userNameFilter) == true)
//              {
//                  filteredNodeRefs.add(nodeRef);
//              }
//          }
//      }
//      else
//      {
//          filteredNodeRefs = new ArrayList<NodeRef>(nodeRefs);
//      }
//      
//      UserDetails[] results = new UserDetails[filteredNodeRefs.size()];
//      int index = 0;
//      for (NodeRef nodeRef : filteredNodeRefs)
//      {
//          String userName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
//          results[index] = AdministrationWebService.createUserDetails(nodeService, userName, nodeRef);
//          index++;
//      }
//
//      UserQueryResults queryResults = new UserQueryResults(null, results);
//      
//      // Done
//      return queryResults;
//  }
  
}
