package gov.pnnl.velo.model;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class User extends Resource {
  private String firstName;
  private String lastName;
  private String email;
  private String username;
  private String password;
  
  public User() {
    
  }
  
  public User(String firstName, String lastName, String email, String password) {
    super();
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
  }
  
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.password = password;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    try { 
      String orgProp = "{http://www.alfresco.org/model/content/1.0}organization";
      // Spit out the User in json format
      User user = new User("Bucket", "Lansing", "bucket.lansing@gmail.com", "change-me");
      user.setProperty(orgProp, "PNNL");

      ObjectMapper mapper = new ObjectMapper();
      String json;
      json = mapper.writeValueAsString(user);
      System.out.println(json);
      System.out.println("\n");
      
      String userStr = 
       "{\"properties\":{\"" + orgProp + "\":[\"PNNL\"]},\"firstName\":\"Bucket\",\"lastName\":\"Lansing\",\"email\":\"bucket.lansing@gmail.com\",\"password\":\"change-me\"}";

      User user2 = mapper.readValue(userStr, User.class);
      System.out.println(user2.getFirstName());
      System.out.println(user2.getLastName());
      System.out.println(user2.getEmail());
      System.out.println(user2.getPassword());
      System.out.println(user2.getPropertyAsString(orgProp));
      
      
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
