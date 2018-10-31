package gov.pnnl.velo.dataset.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.util.AbstractWebScriptClient;
import gov.pnnl.cat.core.util.WebServiceUrlUtility;

public class DatasetService extends AbstractWebScriptClient {

  private static DatasetService instance = new DatasetService();

  public DatasetService() {
    super(ResourcesPlugin.getResourceManager().getRepositoryUrlBase(), ResourcesPlugin.getProxyConfig());
  }

  public static DatasetService getInstance() {
    return instance;
  }

  public ArrayList<Long> getDatasetFileStats(String uuid) {
    //http://localhost:8082/alfresco/service/cat/getFileChildrenStats?uuid=97a6344d-8dda-486f-9854-18a38c7f5bc1
    //returns
    //[112,12978752]  112,12937
    StringBuilder url = getCatWebScriptUrl();
    WebServiceUrlUtility.appendPaths(url, "getFileChildrenStats");
    WebServiceUrlUtility.appendParameter(url, "uuid", uuid);

    CloseableHttpResponse response = null;

    try {
      HttpGet httpget = new HttpGet(url.toString());
      response = executeMethod(httpClient, httpget);
      ObjectMapper mapper = new ObjectMapper();
      ArrayList<Long> stats = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<Long>>() {});
      EntityUtils.consumeQuietly(response.getEntity());
      return stats;

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);

    } finally {
      closeQuietly(response);
    }
    return null;
  }

}
