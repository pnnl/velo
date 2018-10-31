package gov.pnnl.velo.installer;

public class CheckJava {

  /**
   * Checks for java version to make sure it matches the specified parameters
   * @param args
   */
  public static void main(String[] args) {  
    //pass two params: arch type (32/64) and platform (6, 7, etc.) and it returns 0 or 1
    String arch = null;
    String version = null;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-version") && i < args.length-1) {
        version = args[i+1];
      }
      if(args[i].equals("-arch") && i < args.length-1) {
        arch = args[i+1];
      }
    }
    
    System.out.println("Checking java version: " + version);
    System.out.println("Checking architecture: " + arch);
    
    if(arch == null || version == null) {
      printUsageAndExit();
    }

    String sysArch = System.getProperty("sun.arch.data.model");
    //System.out.println(System.getProperty("os.arch"));
    String sysVersion = System.getProperty("java.version");

    System.out.println("Found java version: " + sysVersion);
    System.out.println("Found architecture: " + sysArch);

    int retCode = 0;
    
    if(!sysVersion.startsWith(version)) {
      System.err.println("Version doesn't match");
      retCode = -1;
    }
    if(!sysArch.equals(arch)) {
      System.err.println("Architecture doesn't match");
      retCode = -1;
    }
    System.exit(retCode);
    
  }

  private static void printUsageAndExit() {
    // print usage
    System.out.println("Usage:");
    System.out
    .println("CheckJava -version <java version (1.6, 1.7, etc.)> -arch <architecture (32 or 64)>");
    System.exit(1);
  }

}
