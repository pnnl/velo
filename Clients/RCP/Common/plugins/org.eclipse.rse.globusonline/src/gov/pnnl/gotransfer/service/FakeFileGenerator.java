package gov.pnnl.gotransfer.service;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class FakeFileGenerator implements Runnable{
  String source = "Z:/APS3"; //Z:/APS3
  String destination = "Z:/zoetesting";
  int wait = 1000;

  public void run() {
    recurse(new File(source), destination);
  }
  
  //top down recusively create files/folders in destination
  public void recurse(File file, String destination){
      if(file.isDirectory()){
        File newDir = new File(destination + "/" + file.getName()); 
        newDir.mkdirs();
        System.out.println("created dir " + newDir);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) { }
        
        int size = file.listFiles().length;
        if(size > 0) {
            for(int idx = 0; idx < size; idx++) {
            	File child = file.listFiles()[idx];
                recurse(child, newDir.getAbsolutePath());
            }
        }
      }else{
        try {
          FileUtils.copyFile(file, new File(destination + "/" + file.getName()));
          System.out.println("copied file " + destination + "/" + file.getName());
          Thread.sleep(1000);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
  }

  public static void main(String[] args) {
    FakeFileGenerator test = new FakeFileGenerator();
    Thread testThread = new Thread(test);
    testThread.start();
  }
}
