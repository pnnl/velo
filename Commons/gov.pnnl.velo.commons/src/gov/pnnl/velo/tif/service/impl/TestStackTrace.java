package gov.pnnl.velo.tif.service.impl;

import java.util.Arrays;


public class TestStackTrace {

  /**
   * @param args
   */
  public static void main(String[] args) {
   try{
     try{
       String t=null;
       throw new CustomException("inner most exception");
     }catch(Exception e){
       throw new CustomException2(e);
     }
   }catch(Exception e){
     StackTraceElement[] stackTrace = e.getStackTrace();
     System.out.println("len"+ stackTrace.length);
     System.out.println(Arrays.toString(stackTrace));
     e.printStackTrace();
     System.out.println("Message");
     
     System.out.println(e.getMessage());
     System.out.println(e.getClass());
   }
  }

}

class CustomException extends Exception{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  CustomException(Exception e){
    super(e);
  }
  CustomException(String msg){
    super(msg);
  }
}

class CustomException2 extends Exception{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  CustomException2(Exception e){
    super(e);
  }
  CustomException2(String msg){
    super(msg);
  }
}