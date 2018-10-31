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
package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.ImageUtils;
import gov.pnnl.cat.util.TaxonomyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ISO9075;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 */
public class GetThumbnailsWebScript extends DeclarativeWebScript {
  // The max thumbnails we want to return for a folder (otherwise we can overload
  // image flow with too many pics and slow it down)
  private static final int MAX_THUMBNAILS = 99;

  // web script params
  private static final String REQ_PARAM_INCLUDE_SUBFOLDERS = "includeSubfolders";

  private static final String REQ_PARAM_THUMBNAIL_NAME = "thumbnailName";

  private static final String REQ_PARAM_IMAGES_ONLY = "imagesOnly";

  private static final String REQ_PARAM_START_INDEX = "startIndex";

  private static final String REQ_PARAM_END_INDEX = "endIndex";

  // model property keys
  private static final String MODEL_PROP_KEY_THUMBNAILS = "thumbnails";

  private NodeService nodeService;

  private NamespaceService namespaceService;

  private SearchService searchService;

  private ContentService contentService;

  /*
   * (non-Javadoc)
   * 
   * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl( org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
   */
  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param status Status
   * @param cache Cache
   * @return Map<String,Object>
   */
  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    //TODO links inside normal (non-taxonomy) folders are not getting thumbnails returned. 
    String uuid = req.getExtensionPath();
    boolean imagesOnly = Boolean.valueOf(req.getParameter(REQ_PARAM_IMAGES_ONLY));
    boolean includeSubfolders = Boolean.valueOf(req.getParameter(REQ_PARAM_INCLUDE_SUBFOLDERS));
    String thumbnailName = "medium";
    if (req.getParameter(REQ_PARAM_THUMBNAIL_NAME) != null) {
      thumbnailName = req.getParameter(REQ_PARAM_THUMBNAIL_NAME);
    }
    NodeRef nodeRef = new NodeRef(CatConstants.SPACES_STORE, uuid);

    String startIndexParam = req.getParameter(REQ_PARAM_START_INDEX);
    String endIndexParam = req.getParameter(REQ_PARAM_END_INDEX);
    int startIndex = 0;
    int endIndex = MAX_THUMBNAILS;
    if (startIndexParam != null) {
      startIndex = Integer.valueOf(startIndexParam);
    }
    if (endIndexParam != null) {
      endIndex = Integer.valueOf(endIndexParam);
    }
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_TAXONOMY_FOLDER) || nodeService.hasAspect(nodeRef, CatConstants.ASPECT_TAXONOMY_ROOT)) {
      return getTaxonomyThumbnails(nodeRef, imagesOnly, includeSubfolders, thumbnailName, startIndex, endIndex);
    } else {
      return getFolderThumbnails(nodeRef, imagesOnly, includeSubfolders, thumbnailName, startIndex, endIndex);
    }
  }

  /**
   * Method getTaxonomyThumbnails.
   * @param nodeRef NodeRef
   * @param imagesOnly boolean
   * @param includeSubfolders boolean
   * @param thumbnailName String
   * @param startIndex int
   * @param endIndex int
   * @return Map<String,Object>
   */
  protected Map<String, Object> getTaxonomyThumbnails(NodeRef nodeRef, boolean imagesOnly, boolean includeSubfolders, String thumbnailName, int startIndex, int endIndex) {
    // Get the category associated with the tax folder
    NodeRef category = TaxonomyUtils.getTaxonomyCategory(nodeRef, nodeService);
    String categoryName = (String) nodeService.getProperty(category, ContentModel.PROP_NAME);

    // Get all the files in that category
    StringBuilder categoryQuery = new StringBuilder();
    if (includeSubfolders) {
      categoryQuery.append("PATH:\"/tax:classification//tax:" + ISO9075.encode(categoryName) + "//member\"");
    } else {
      categoryQuery.append("PATH:\"/tax:classification//tax:" + ISO9075.encode(categoryName) + "/member\"");
    }

    List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
    ResultSet results = null;
    try {
      results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, categoryQuery.toString());
      nodeRefs = results.getNodeRefs();
    } finally {
      if (results != null) {
        results.close();
      }
    }

    // Get all the thumbnails from those files
    StringBuilder parentQuery = new StringBuilder();
    if (nodeRefs.size() > 0) {
      parentQuery.append("(");
      endIndex = Math.min(nodeRefs.size(), endIndex);

      for (int i = startIndex; i < endIndex; i++) {
        NodeRef node = nodeRefs.get(i);
        if (i > startIndex) {
          parentQuery.append(" OR ");
        }
        parentQuery.append("PARENT:\"");
        parentQuery.append(node.toString());
        parentQuery.append("\"");
      }
      parentQuery.append(")");
    }

    String thumbnailTypeQuery = " TYPE:\"" + ContentModel.TYPE_THUMBNAIL + "\"";
    String thumbnailNameQuery = "((@\\{http\\://www.alfresco.org/model/content/1.0\\}thumbnailName:"
      + thumbnailName + ") OR (@\\{http\\://www.alfresco.org/model/content/1.0\\}name:" + thumbnailName +"))";
    String query = parentQuery + " AND " + thumbnailNameQuery + " AND " + thumbnailTypeQuery;

    if (imagesOnly) {
      query += " AND (@\\{http\\://www.pnl.gov/cat/model/content/1.0\\}parentMimetype:image*)";
    }

    return getThumbnailModel(query, startIndex, endIndex);
  }

  /**
   * Method getFolderThumbnails.
   * @param nodeRef NodeRef
   * @param imagesOnly boolean
   * @param includeSubfolders boolean
   * @param thumbnailName String
   * @param startIndex int
   * @param endIndex int
   * @return Map<String,Object>
   */
  protected Map<String, Object> getFolderThumbnails(NodeRef nodeRef, boolean imagesOnly, boolean includeSubfolders, String thumbnailName, int startIndex, int endIndex) {

    String path = nodeService.getPath(nodeRef).toPrefixString(namespaceService);

    // Perform the Search

    // Do a search for all thumbnail nodes under this folder
    if (!path.endsWith("/")) {
      path += "/";
    }
    // We want to query only nodes that are two hops away from our folder (since thumbnail nodes are a child of the file)
    if (includeSubfolders) {
      path += "*//*";
    } else {
      path += "*/*";
    }
    String pathQuery = " PATH:\"" + path + "\"";
    String thumbnailTypeQuery = " TYPE:\"" + ContentModel.TYPE_THUMBNAIL + "\"";
    String thumbnailNameQuery = "((@\\{http\\://www.alfresco.org/model/content/1.0\\}thumbnailName:"
      + thumbnailName + ") OR (@\\{http\\://www.alfresco.org/model/content/1.0\\}name:" + thumbnailName +"))";
    String imagesOnlyQuery = "";
    if (imagesOnly) {
      imagesOnlyQuery = " (@\\{http\\://www.pnl.gov/cat/model/content/1.0\\}parentMimetype:image*)";
    }
    String query = thumbnailNameQuery + " AND " + thumbnailTypeQuery + " AND " + pathQuery + imagesOnlyQuery;

    return getThumbnailModel(query, startIndex, endIndex);
  }

  /**
   * Method getThumbnailModel.
   * @param query String
   * @param startIndex int
   * @param endIndex int
   * @return Map<String,Object>
   */
  protected Map<String, Object> getThumbnailModel(String query, int startIndex, int endIndex) {
    List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
    ResultSet results = null;
    try {

      results = searchService.query(CatConstants.SPACES_STORE, SearchService.LANGUAGE_LUCENE, query.toString());
      List<NodeRef> nodeRefs = results.getNodeRefs();
      endIndex = Math.min(nodeRefs.size(), endIndex);

      for (int i = startIndex; i < endIndex; i++) {
        NodeRef node = nodeRefs.get(i);
        NodeRef parent = nodeService.getPrimaryParent(node).getParentRef();
        String name = (String) nodeService.getProperty(parent, ContentModel.PROP_NAME);
        Integer width = (Integer) nodeService.getProperty(node, CatConstants.PROP_THUMBNAIL_WIDTH);
        Integer height = (Integer) nodeService.getProperty(node, CatConstants.PROP_THUMBNAIL_HEIGHT);

        // If the image doesn't have the dimension properties, find and save them for next time
        if (width == null || height == null) {
          int[] dimensions = ImageUtils.getDimensions(node, contentService);
          ImageUtils.setDimensions(node, dimensions, nodeService);

          width = Integer.valueOf(dimensions[0]);
          height = Integer.valueOf(dimensions[1]);
        }

        try {
          thumbnails.add(new Thumbnail(node.getId(), URIUtil.encodeWithinQuery(name), parent.getId(), width, height));
        } catch (URIException e) {
          e.printStackTrace();
          thumbnails.add(new Thumbnail(node.getId(), name, parent.getId(), width, height));
        }
      }

    } finally {
      if (results != null) {
        results.close();
      }
    }

    // add objects to model for the template to render
    Map<String, Object> model = new HashMap<String, Object>();
    model.put(MODEL_PROP_KEY_THUMBNAILS, thumbnails);

    return model;
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method setNamespaceService.
   * @param namespaceService NamespaceService
   */
  public void setNamespaceService(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  /**
   * Method setSearchService.
   * @param searchService SearchService
   */
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   */
  public class Thumbnail {
    private String id;

    private String name;

    private String parentId;

    private Integer width;

    private Integer height;

    /**
     * Constructor for Thumbnail.
     * @param id String
     * @param name String
     * @param parentId String
     * @param width Integer
     * @param height Integer
     */
    public Thumbnail(String id, String name, String parentId, Integer width, Integer height) {
      super();

      this.id = id;
      this.name = name;
      this.parentId = parentId;
      this.width = width;
      this.height = height;
    }

    /**
     * Method getId.
     * @return String
     */
    public String getId() {
      return id;
    }

    /**
     * Method setId.
     * @param id String
     */
    public void setId(String id) {
      this.id = id;
    }

    /**
     * Method getName.
     * @return String
     */
    public String getName() {
      return name;
    }

    /**
     * Method setName.
     * @param name String
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Method getParentId.
     * @return String
     */
    public String getParentId() {
      return parentId;
    }

    /**
     * Method setParentId.
     * @param parentId String
     */
    public void setParentId(String parentId) {
      this.parentId = parentId;
    }

    /**
     * Method getWidth.
     * @return Integer
     */
    public Integer getWidth() {
      return width;
    }

    /**
     * Method setWidth.
     * @param width Integer
     */
    public void setWidth(Integer width) {
      this.width = width;
    }

    /**
     * Method getHeight.
     * @return Integer
     */
    public Integer getHeight() {
      return height;
    }

    /**
     * Method setHeight.
     * @param height Integer
     */
    public void setHeight(Integer height) {
      this.height = height;
    }
  }

}
