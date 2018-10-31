package gov.pnnl.velo.tools.util;

import java.util.regex.Pattern;

public class TestRegex {

  public static void main(String[] args) {
    
    Pattern pattern1 = Pattern.compile("href=\"platform:/plugin/.*\"");
    
    String input = 
    "<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"platform:/plugin/gov.pnnl.velo.tools.agni/config/xui/pe_methods.xml\" parse=\"xml\" />" +
    "<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"platform:/plugin/gov.pnnl.velo.tools.agni/config/xui/pe_options.xml\" parse=\"xml\" />" +
    "<xi:include xmlns:xi=\"http://www.w3.org/2001/XInclude\" href=\"platform:/plugin/gov.pnnl.velo.tools.agni/config/xui/uq_methods.xml\" parse=\"xml\" />";

//    
//    if(pattern1.matcher(input).()) { 
//     System.out.println("is SA");
//    } 
    
 
  }

}
