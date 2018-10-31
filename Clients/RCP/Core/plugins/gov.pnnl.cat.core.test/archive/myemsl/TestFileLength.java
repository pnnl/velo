package gov.pnnl.cat.core.resources.tests.myemsl;

import java.io.File;

public class TestFileLength {

  /**
   * @param args
   */
  public static void main(String[] args) {
    File file = new File(args[0]);
    System.out.println("File length: " + file.length());

  }

}
