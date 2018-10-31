package gov.pnnl.velo.tif.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("scheduler")
public class Scheduler {
  
  @XStreamAsAttribute
  private String name = "Fork";
  
  @XStreamAsAttribute
  private boolean allocation = false;
  
  @XStreamAsAttribute
  private String path;
  
  @XStreamAsAttribute
  private String kill;
  
  @XStreamImplicit(itemFieldName="queue")
  private List<Queue> queues  = new ArrayList<Queue>();

  public Scheduler(){
    
  }
  
  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public boolean isAllocation() {
    return allocation;
  }

  
  public void setAllocation(boolean allocation) {
    this.allocation = allocation;
  }

  
  public String getPath() {
    return path;
  }

  
  public void setPath(String path) {
    this.path = path;
  }

  
  public String getKill() {
    return kill;
  }

  
  public void setKill(String kill) {
    this.kill = kill;
  }

  
  public List<Queue> getQueues() {
//    if(queues == null) {
//      // we can't initialize in the constructor since xstream overrides via reflection
//      return new ArrayList<Queue>();
//    }
    return queues;
  }

  
  public void setQueues(List<Queue> queue) {
    this.queues = queue;
  }

}
