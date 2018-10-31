package gov.pnnl.cat.web.scripts;

import java.io.File;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
public class TestJson {

  public static void main(String[] args) {
    try{
//      JSONArray jsonArray = new JSONArray();
      String jsonStr = FileUtils.readFileToString(new File("C:/eclipse/workspaces/VeloServer/CATServerCore/source/java/gov/pnnl/cat/web/scripts/testjson.txt"));
      JSONObject jsonObject = new JSONObject(jsonStr);
      JSONArray viewersArray = jsonObject.getJSONArray("viewers");
      JSONObject viewer = viewersArray.getJSONObject(0);
      JSONArray imgFiles = viewer.getJSONArray("imageFiles");
      JSONObject imgFile = imgFiles.getJSONObject(0);
//      System.out.println("filePath: " + imgFile.getString("filePath"));
      imgFile.remove("filePath");
      imgFile.put("fileUuid", "1234");
      
//      System.out.println("imgFile: "+imgFile);
//      System.out.println("imgFiles: " + imgFiles);
      System.out.println(jsonObject);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

    public static void mainold(String[] args) {
        try{
    StringWriter sw = new StringWriter();

    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator generator = mapper.getFactory().createJsonGenerator(sw);
    generator.writeStartObject();
    
    generator.writeFieldName("draw");
    generator.writeNumber(1);
    
    generator.writeArrayFieldStart("data");
    

    generator.writeStartArray();
    
//    generator.writeStartObject();
//    generator.writeFieldName("name");
    generator.writeString( "zoe");
//    generator.writeEndArraydObject();
    
    
    
//    generator.writeFieldName("modifiedDate");
    generator.writeString("1/1/2012");
    generator.writeEndArray();
//    
//
    generator.writeStartArray();
//    generator.writeFieldName("name");
    generator.writeString( "gabe");
//    generator.writeFieldName("modifiedDate");
    generator.writeString("1/1/2012");
    generator.writeEndArray();

    generator.writeEndArray();
    generator.writeEndObject();
    
    generator.flush();
    
    System.out.println(sw.toString());
    }catch(Exception e){
      e.printStackTrace();
    }
  }

}
