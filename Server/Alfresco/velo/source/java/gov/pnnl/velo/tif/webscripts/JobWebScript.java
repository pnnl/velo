package gov.pnnl.velo.tif.webscripts;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.web.scripts.WebScriptUtils;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.webscripts.AbstractVeloWebScript;

public class JobWebScript extends AbstractVeloWebScript {

  // One class for all new jobs.get, jobs.delete, etc. (might need another to list all/search)
  //
  // Get one job given job ID
  // Get all jobs per user

  protected LaunchJobWebScript launchJobWebScript;

  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {

    // Gets the path extension beyond the path registered for this service e.g.
    // a) service registered path = /search/engine
    // b) request path = /search/engine/external => /external
    String extensionPath = req.getExtensionPath();  //for the URL http://localhost:8082/alfresco/service/velo/joblaunching/jobs/123 this was 123
    HttpServletRequest httpReq = ((WebScriptServletRequest) req).getHttpServletRequest();
    String method = getScriptMethod(httpReq);
    String queryString = req.getQueryString();

    List<String> pathSegments = splitAllPaths(extensionPath);

    // POST - create and other non-idempotent operations.
    // PUT - update.
    // GET - read a resource or collection.
    // DELETE - remove a resource or collection.

    // see file:///C:/Users/zoe/Downloads/RESTful%20Best%20Practices-v1_2.pdf

    // https://github.com/RestCheatSheet/api-cheat-sheet#api-design-cheat-sheet

    if (method.equalsIgnoreCase("get")) {
      // http://localhost:8082/alfresco/services/velo/joblaunching/jobs - returns all jobs (spec says to just return list of single job URL's) and to support pagination, filtering, sorting
      if (pathSegments.size() == 0) {
        if (req.getParameter("filterBy") != null && req.getParameter("filterValue") != null) {
          return getFilteredJobs(req.getParameter("filterBy"), req.getParameter("filterValue"), res); // TODO support paging/sorting later
        } else {
          return getFilteredJobs(null, null, res);
        }
      }

      // http://localhost:8082/alfresco/services/velo/joblaunching/jobs/{jobNodeUuid}
      if (pathSegments.size() == 1) {
        return getJobById(pathSegments.get(0), res);
      }
    } else if (method.equalsIgnoreCase("put")) {
      // http://localhost:8082/alfresco/services/velo/joblaunching/jobs/{jobNodeUuid}
      if (pathSegments.size() == 1) {
        updateJobById(pathSegments.get(0), req, res, requestContent);
      }
    } else if (method.equalsIgnoreCase("post")) {
      // http://localhost:8082/alfresco/services/velo/joblaunching/jobs
      return createNewJob(req, res, requestContent);
    } else if (method.equalsIgnoreCase("delete")) {
      // http://localhost:8082/alfresco/services/velo/joblaunching/jobs/{jobNodeUuid}
      if (pathSegments.size() == 1) {
        return deleteJobById(pathSegments.get(0), res);
      }
    }

    return null;
  }

  private Object deleteJobById(String jobNodeUuid, WebScriptResponse res) {
    NodeRef jobNode = getJobNodeRefById(jobNodeUuid, res);
    if(nodeService.exists(jobNode)){
      nodeService.deleteNode(jobNode);
    }else{
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    return null;
  }

  private Object createNewJob(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    return launchJobWebScript.executeImpl(req, res, requestContent);
  }

  private Object getFilteredJobs(String filterName, String filterValue, WebScriptResponse res) throws Exception {
    // TODO support filter by owner, status, createdAfterDate - right now ignoring filters
    ResultSet results = null;
    try {
      SearchParameters sp = new SearchParameters();
      sp.addStore(CatConstants.SPACES_STORE);
      // TODO fix this query syntax
      sp.setQuery("ASPECT:\"velo:jobProps\"");
      sp.setLanguage(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO);
      results = searchService.query(sp);

      List<NodeRef> jobNodes = results.getNodeRefs();

      List<Resource> jobs = new ArrayList<Resource>();
      for (NodeRef nodeRef : jobNodes) {
        Resource resource = WebScriptUtils.getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService);
        if (resource != null) {
          // add the resource to the overall results
          jobs.add(WebScriptUtils.getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService));
        }
      }
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(res.getOutputStream(), jobs);

    } finally {
      if (results != null) {
        results.close();
      }
    }
    return null;
  }

  private void updateJobById(String jobNodeUuid, WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // oops - should just use Resource and
    // NodeRef nodeRef = WebScriptUtils.getNodeRef(resourceToCreate, nodeService);
    // etc. like CreateUpdateResource does instead of making a new object...? it will be ginormous but will be quickest/easiest to get working
    Resource jobToUpdate;
    ObjectMapper mapper = new ObjectMapper();

    // TODO: Until we can figure out how to get jQuery.ajax to submit json in request body, we have to be able to accept
    // json as a parameter OR in the request body
    if (requestContent.length() == 0) {
      String jsonString = req.getParameter("jsonString");
      jobToUpdate = mapper.readValue(jsonString, Resource.class);

    } else {
      jobToUpdate = mapper.readValue(requestContent, Resource.class);
    }

    NodeRef nodeRef = WebScriptUtils.getNodeRef(jobToUpdate, nodeService);
    WebScriptUtils.setProperties(nodeRef, jobToUpdate, nodeService, dictionaryService);

    Resource updatedResource = WebScriptUtils.getResource(nodeRef, nodeService, namespaceService, dictionaryService, contentService);
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    mapper = new ObjectMapper();
    mapper.writeValue(res.getOutputStream(), updatedResource);
  }

  private Object getJobById(String jobNodeUuid, WebScriptResponse res) throws Exception {
    NodeRef jobNode = getJobNodeRefById(jobNodeUuid, res);
    if(jobNode != null){
      Resource updatedResource = WebScriptUtils.getResource(jobNode, nodeService, namespaceService, dictionaryService, contentService);
      res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(res.getOutputStream(), updatedResource);
    }
    return null;
  }

  private NodeRef getJobNodeRefById(String jobNodeUuid, WebScriptResponse res) {
//    ResultSet results = null;
//    try {
//      SearchParameters sp = new SearchParameters();
//      sp.addStore(CatConstants.SPACES_STORE);
//      sp.setQuery("@sys\\:node-uuid\\:\"" + jobNodeUuid + "\""); 
//
//      
//      // luceneQuery.append(" +@cm\\:creator:\"" + username + "\"");
//      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//      results = searchService.query(sp);
//      if (results.length() == 0) {
//        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//        return null; // return error code for not found
//      }
//
//      if (results.length() > 1) {
//        writeErrorCause(res, new Throwable("more than one job found with jobNodeUuid of " + jobNodeUuid));
//        return null; // return 500 server error code with message more than one job with jobNodeUuid of x found
//      }
//
//      return results.getNodeRefs().get(0);
//
//    } finally {
//      if (results != null) {
//        results.close();
//      }
//    }
    NodeRef jobNode = NodeUtils.getNodeByUuid(jobNodeUuid);
    if(nodeService.exists(jobNode)){
      return jobNode;
    }else{
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
    
    
  }

  // copied from WebScriptServletRuntime in case the alf_method or X-HTTP-Method-Override header is used:
  protected String getScriptMethod(HttpServletRequest req) {
    // Is this an overloaded POST request?
    String method = req.getMethod();
    if (method.equalsIgnoreCase("post")) {
      boolean overloadParam = false;
      String overload = req.getHeader("X-HTTP-Method-Override");
      if (overload == null || overload.length() == 0) {
        overload = req.getParameter("alf_method");
        overloadParam = true;
      }
      if (overload != null && overload.length() > 0) {
        if (logger.isDebugEnabled())
          logger.debug("POST is tunnelling method '" + overload + "' as specified by " + (overloadParam ? "alf_method parameter" : "X-HTTP-Method-Override header"));

        method = overload;
      }
    }

    return method;
  }

  public static List<String> splitAllPaths(final String path) {
    if ((path == null) || (path.length() == 0)) {
      return Collections.emptyList();
    }

    // split the path
    final StringTokenizer token = new StringTokenizer(path, "/");
    final List<String> results = new ArrayList<String>(10);
    while (token.hasMoreTokens()) {
      results.add(token.nextToken());
    }
    return results;
  }

  // class SimpleJob{
  // Map<String, Serializable> veloProps = new HashMap<String, Serializable>();
  // }
}
