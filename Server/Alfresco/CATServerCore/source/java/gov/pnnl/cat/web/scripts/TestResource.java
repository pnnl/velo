package gov.pnnl.cat.web.scripts;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Resource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestResource {

  /**
   * @param args
   */
  public static void main(String[] args) {
    List<Resource> resources = new ArrayList<Resource>();
    CmsPath path = new CmsPath("/company_home/Velo/project/test");
    Resource resource = new Resource();
    resource.setPath(path.toAssociationNamePath());
    resources.add(resource);
    List<String>values = new ArrayList<String>();
    values.add("cmsfile/sr");
    resource.getProperties().put("{http://www.pnl.gov/velo/model/content/1.0}mimetype", values);
    ObjectMapper mapper = new ObjectMapper();
    mapper = new ObjectMapper();
    StringWriter writer = new StringWriter();
    try {
      mapper.writeValue(writer, resources);
      System.out.println(writer.toString());
    } catch (JsonGenerationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonMappingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


  }

}
