package gov.pnnl.velo.tif.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("queue")
public class Queue {

  @XStreamAsAttribute
  private String name;
  
  @XStreamAsAttribute
  @XStreamAlias("default")
  private boolean defaultQueue = false;

  private String timeLimit = "";
  private String defaultTimeLimit = "";
  private int minNodes = 1;
  private int maxNodes = 1;
  
  
  // TODO memory limits if we find a use case....

  public Queue() {
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public boolean isDefaultQueue() {
    return defaultQueue;
  }

  
  public void setDefaultQueue(boolean defaultQueue) {
    this.defaultQueue = defaultQueue;
  }

  
  public String getTimeLimit() {
    return timeLimit;
  }

  
  public void setTimeLimit(String timeLimit) {
    this.timeLimit = timeLimit;
  }

  
  public String getDefaultTimeLimit() {
    return defaultTimeLimit;
  }

  
  public void setDefaultTimeLimit(String defaultTimeLimit) {
    this.defaultTimeLimit = defaultTimeLimit;
  }

  
  public int getMinNodes() {
    return minNodes;
  }

  
  public void setMinNodes(int minNodes) {
    this.minNodes = minNodes;
  }

  
  public int getMaxNodes() {
    return maxNodes;
  }

  
  public void setMaxNodes(int maxNodes) {
    this.maxNodes = maxNodes;
  }

  public boolean hasTimeConstraint() {
    return !timeLimit.equals("");
  }

  public int[] getDefaultTimeParts() {
    // System.out.println("default time " + defaultTime);
    return parseTime(defaultTimeLimit);
  }

  public int[] getMaxTimeParts() {
    return parseTime(timeLimit);
  }

  protected int[] parseTime(String time) {
    int idx;
    int[] ret = new int[3];
    for (idx = 0; idx < 3; idx++)
      ret[idx] = 0;

    if (time != null && !time.equals("")) {
      String[] parts = time.split(":");
      for (idx = 0; idx < parts.length; idx++) {
        ret[idx] = Integer.parseInt(parts[idx]);
        // System.out.println("part " + parts[idx] + " " + ret[idx]);
      }
    }
    return ret;

  }
}