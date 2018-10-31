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

import gov.pnnl.velo.model.Comment;
import gov.pnnl.velo.model.Comment.Author;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.io.FileUtils;

/**
 * Static helper functions for dealing with comments
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class CommentUtils {

  private static NodeService nodeService;
  private static ContentService contentService;
  private static PersonService personService;
  private static AuthenticationComponent authenticationComponent;

  /**
   * Returns all comment nodes for a given node.
  
   * @param nodeRef NodeRef
   * @return an array of comments. */
  public static Comment[]  getComments(NodeRef nodeRef) {
    return getComments(nodeRef, false);
  }
  
  /**
   * Returns all comment nodes for a given node.  
   * @param nodeRef
   * @param filterByCurrentUser
  
   * @return an array of comments. */
  /**
   * @return
   */
  public static Comment[]  getComments(NodeRef nodeRef, boolean filterByCurrentUser) {
    List<Comment> comments = new ArrayList<Comment>();
    String currentUser = authenticationComponent.getCurrentUserName();

    NodeRef commentsFolder = getCommentsFolder(nodeRef);
    if (commentsFolder != null) {
      List<ChildAssociationRef> children = nodeService.getChildAssocs(commentsFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

      for(ChildAssociationRef child : children) {
        // convert to comment data
        Comment comment = getCommentData(child.getChildRef(), nodeService);
        
        // if we are filtering, only get comments if current user is the creator of this comment
        if(!filterByCurrentUser || currentUser.equals(comment.getAuthor().getUsername())) {
          comments.add(comment);          
        }
      }
    }

    return comments.toArray(new Comment[comments.size()]);
  }

  /**
   * Returns the folder that contains all the comments.
   * 
   * We currently use the fm:discussable aspect where we
   * add a "Comments" topic to it.
   * @param nodeRef NodeRef
   * @return NodeRef
   */
  public static NodeRef getCommentsFolder(NodeRef nodeRef) {
    NodeRef forumFolder = getForumFolder(nodeRef);
    NodeRef topicFolder = null;

    if(forumFolder != null) {
      // Alfresco Share now puts all Comments under a special topic called Comments
      List<ChildAssociationRef> topics = nodeService.getChildAssocs(forumFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments"));
      if(topics.size() > 0) {
        topicFolder = topics.get(0).getChildRef();
      }
    }
    return topicFolder;
  }

  /**
   * Method getForumFolder.
   * @param nodeRef NodeRef
   * @return NodeRef
   */
  public static NodeRef getForumFolder(NodeRef nodeRef) {
    NodeRef forumFolder = null;
    if(nodeService.hasAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE)) {

      List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef, 
          ForumModel.ASSOC_DISCUSSION, RegexQNamePattern.MATCH_ALL);

      if (children.size() > 0) {     
        forumFolder = children.get(0).getChildRef();
      }
    }
    return forumFolder;
  }

  /**
   * Creates the comments folder if it doesn't yet exist for the given node.
   * @param nodeRef NodeRef
   * @return NodeRef
   */
  public static NodeRef createCommentsFolder(NodeRef nodeRef) {

    NodeRef commentsFolder = getCommentsFolder(nodeRef);

    if (commentsFolder == null) {    
      // Forum node is created automatically by DiscussableAspect behaviour.
      nodeService.addAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
      NodeRef forumFolder = getForumFolder(nodeRef);
      final List<ChildAssociationRef> existingTopics = nodeService.getChildAssocs(forumFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments"));

      if (existingTopics.isEmpty())
      {
        final Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, "Comments");

        commentsFolder = nodeService.createNode(forumFolder, ContentModel.ASSOC_CONTAINS, 
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments"), ForumModel.TYPE_TOPIC, properties).getChildRef();
      }
      else
      {
        commentsFolder = existingTopics.get(0).getChildRef();
      }


    }
    return commentsFolder;
  }

  /**
   * Method addComment.
   * @param nodeRef NodeRef
   * @param commentContent String
   * @return Comment
   */
  public static Comment addComment(NodeRef nodeRef, String commentContent) {

    NodeRef commentsFolder = createCommentsFolder(nodeRef);

    NodeRef commentNode = nodeService.createNode(commentsFolder, ContentModel.ASSOC_CONTAINS, QName.createQName("comment" + System.currentTimeMillis()), ForumModel.TYPE_POST).getChildRef();
    nodeService.setProperty(commentNode, ContentModel.PROP_CONTENT, new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));
    ContentWriter writer = contentService.getWriter(commentNode, ContentModel.PROP_CONTENT, true);
    writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    writer.setEncoding("UTF-8");
    writer.putContent(commentContent);

    return getCommentData(commentNode, nodeService);
  }

  /**
   * Returns the data object for a comment node
   * @param commentNode NodeRef
   * @param nodeService NodeService
   * @return Comment
   */
  public static Comment getCommentData(NodeRef commentNode, NodeService nodeService) {
    try{ 
      Map<QName, Serializable> props = nodeService.getProperties(commentNode);
      String username = (String)props.get(ContentModel.PROP_CREATOR);
      NodeRef person = personService.getPerson(username);
      String firstName = (String)nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
      String lastName = (String)nodeService.getProperty(person, ContentModel.PROP_LASTNAME);
      Author author = new Author(username, firstName, lastName);

      FileContentReader reader = (FileContentReader)contentService.getReader(commentNode, ContentModel.PROP_CONTENT);
      String content = FileUtils.readFileToString(reader.getFile());
      String uuid = commentNode.getId();
      String name = (String)props.get(ContentModel.PROP_NAME);
      Date created = (Date) props.get(ContentModel.PROP_CREATED);
      Date modified = (Date) props.get(ContentModel.PROP_CREATED);

      Comment comment = new Comment();
      comment.setAuthor(author);
      comment.setContent(content);
      comment.setCreatedOn(created);
      comment.setModifiedOn(modified);
      comment.setName(name);
      comment.setUuid(uuid);

      // TODO: add permissions later (do we use site permissions??)
      return comment;

    } catch (RuntimeException e) {
      throw e;
      
    } catch (Exception e) {
      throw new RuntimeException("Failed to create comment.", e);
    }


  }

  /**
   * Method getNodeService.
   * @return NodeService
   */
  public static NodeService getNodeService() {
    return nodeService;
  }

  /**
   * Method setAuthenticationComponent.
   * @param authenticationComponent AuthenticationComponent
   */
  public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
    CommentUtils.authenticationComponent = authenticationComponent;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    CommentUtils.nodeService = nodeService;
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService(ContentService contentService) {
    CommentUtils.contentService = contentService;
  }

  /**
   * Method setPersonService.
   * @param personService PersonService
   */
  public void setPersonService(PersonService personService) {
    CommentUtils.personService = personService;
  }


}
