package gov.pnnl.velo.core.util;

public class ThreadUtils {
  
  public static boolean isAwtThread() {
    boolean awtThread = false;
    
    String threadName = Thread.currentThread().getName();
    if (threadName.contains("AWT-EventQueue")) {
      awtThread = true;
    }
    return awtThread;
  }
  
  
  public static boolean isSWTThread() {
    boolean swtThread = false;
    
    String threadName = Thread.currentThread().getName();
    if (threadName.contains("main")) {
      swtThread = true;
    }
    return swtThread;
  }

}
