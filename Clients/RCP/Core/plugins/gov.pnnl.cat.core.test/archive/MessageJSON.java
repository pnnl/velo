package gov.pnnl.cat.core.resources.tests;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageJSON {

  /**
   * @param args
   */
  public static void main(String[] args) {
    Message msg = new Message("clansing_Case1_01212014_084512", "Preprocessing", "Data Transfer", "Transferring data from /path/on/CADES");
    ObjectMapper mapper = new ObjectMapper();
    StringWriter writer = new StringWriter();
    try {
      mapper.writeValue(writer, msg);
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
  
  public static class Message {
    private String jobID;
    private String status;
    private String step;
    private String description;
    public String getJobID() {
      return jobID;
    }
    public void setJobID(String jobID) {
      this.jobID = jobID;
    }
    public String getStatus() {
      return status;
    }
    public void setStatus(String status) {
      this.status = status;
    }
    public String getStep() {
      return step;
    }
    public void setStep(String step) {
      this.step = step;
    }
    public String getDescription() {
      return description;
    }
    public void setDescription(String description) {
      this.description = description;
    }
    public Message(String jobID, String status, String step, String description) {
      super();
      this.jobID = jobID;
      this.status = status;
      this.step = step;
      this.description = description;
    }
    
    
  }

}
