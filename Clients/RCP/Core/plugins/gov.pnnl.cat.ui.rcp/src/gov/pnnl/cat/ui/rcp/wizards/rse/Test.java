package gov.pnnl.cat.ui.rcp.wizards.rse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {

  public static void main(String[] args) throws ParseException {
    // TODO Auto-generated method stub
    String value = "3/2/2014";
    Date expDate = new SimpleDateFormat("MM/dd/yyyy").parse(value);
    System.out.println(expDate);
  }

}
