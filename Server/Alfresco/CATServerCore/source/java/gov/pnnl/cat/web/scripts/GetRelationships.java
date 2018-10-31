package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.model.Relationship;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * returns list of all nodes connected to the given node via any relationships other than "contains"
 * 
 * @author D3K339
 * 
 * @version $Revision: 1.0 $
 */
public class GetRelationships extends AbstractCatWebScript {

  public static final String PARAM_PATH = "path";

  /**
   * Method executeImpl.
   * 
   * @param req
   *          WebScriptRequest
   * @param res
   *          WebScriptResponse
   * @param requestContent
   *          File
   * @return Object
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {

    // Get the request parameters:
    String path = req.getParameter(PARAM_PATH);
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);

    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);

    // write the results to the output stream
    ArrayList<Relationship> children = getRelationships(path);

    // serialize children via json
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), children);

    return null;
  }

  /**
   * Method getRelationships.
   * 
   * @param path
   *          String
   * @param printStream
   *          PrintStream
   * @throws Exception
   */
  public ArrayList<Relationship> getRelationships(String path) throws Exception {

    ArrayList<Relationship> relationships = new ArrayList<Relationship>();

    // look up node
    NodeRef nodeRef = NodeUtils.getNodeByName(path, nodeService);
    if (nodeRef == null) {
      return relationships;
    }
    String nodeUuid = nodeRef.getId();
    String nodePath = nodeService.getPath(nodeRef).toString();

    // Associations are directional
    // Get all associations where I am the source (i.e, me->node)
    List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);

    // Get all associations where I am the target (i.e., node->me)
    List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);

    // Now run as admin to look up paths
    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    
    // Write one assoc per line in a tab-delimited text file
    // first do the relationships where i am the source
    for (AssociationRef assoc : targetAssocs) {
      String relationshipType = assoc.getTypeQName().toString();
      NodeRef target = assoc.getTargetRef();
      String targetUuid = target.getId();
      Relationship relationship = new Relationship(nodeUuid, targetUuid, relationshipType);
      relationship.setSourcePath(nodePath);
      
      // For now we will show the paths of the other nodes in the relationships, even if user can't
      // read the target node     
      if(nodeService.exists(target)) {
        relationship.setDestinationPath(nodeService.getPath(target).toString());
      }
      relationships.add(relationship);
    }

    // now do the relationships where i am the target
    for (AssociationRef assoc : sourceAssocs) {
      String relationshipName = assoc.getTypeQName().toString();
      NodeRef source = assoc.getSourceRef();
      String sourceUuid = source.getId();
      Relationship relationship = new Relationship(sourceUuid, nodeUuid, relationshipName);
      relationship.setDestinationPath(nodePath);
      
      // For now we will show the paths of the other nodes in the relationships, even if user can't
      // read the target node     
      if(nodeService.exists(source)) {
        relationship.setSourcePath(nodeService.getPath(source).toString());
      }      
      relationships.add(relationship);
    }

    return relationships;

  }
}
