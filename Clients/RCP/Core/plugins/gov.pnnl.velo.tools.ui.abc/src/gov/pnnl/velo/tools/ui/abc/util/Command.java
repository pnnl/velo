package gov.pnnl.velo.tools.ui.abc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

public abstract class Command {
  private Map<String, Object> parameters;
  private Serializable receiver;
  private Serializable receiverClone;
  private boolean undoable;

  public Command(Serializable receiver) {
    // by default, command is undoable
    this(receiver, true);
  }
  
  public Command(Serializable receiver, boolean undoable) {
    this.receiver = receiver;
    this.undoable = undoable;
    if(undoable) {
      this.receiverClone = cloneReceiver(receiver);
    }
    
  }
  
  protected abstract void execute();
  
  /**
   * @return the undoable
   */
  public boolean isUndoable() {
    return undoable;
  }

  public void undo() {
    // use reflection to restore all fields in the receiver to the 
    // original values from the clone
    try {
      // In order for this to work, the receiver must be a java bean and
      // have getter and setter methods defined for all fields
 // TODO     PropertyUtils.copyProperties(receiver, receiverClone);
      
    } catch (Throwable e) {
      throw new RuntimeException("Failed to undo " + getName(), e);
    }
  }
  
  /**
   * Get the name of the command for display/logging purposes
   * @return
   */
  public abstract String getName();

  /**
   * Set additional parameters (other than the receiver) which
   * are needed by this command.
   * @param parameters
   */
  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;    
  }

  /**
   * Get a value for a specified parameter.
   * @param name
   * @return
   */
  public Object getParameter(String name) {
    Object param = null;
    if(parameters != null) {
      param = parameters.get(name);
    }
    return param;
  }
  
  public Map<String, Object> getParameters() {
    return parameters;
  }
  
  /**
   * @return the receiver
   */
  public Serializable getReceiver() {
    return receiver;
  }

  /**
   * Use serialization to deep clone this object.
   * Subclasses can override if they don't want a deep copy.
   * @param obj
   * @return
   */
  protected Serializable cloneReceiver (Serializable obj) {
    try {
      // Serialize to byte array
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(outStream);
      out.writeObject(obj);
      out.flush();
      out.close();
      
      ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
      ObjectInputStream in = new ObjectInputStream(inStream);

      return (Serializable)in.readObject();

    } catch (Throwable e) {
      e.printStackTrace();
      throw new RuntimeException("Unable to clone receiver!" , e);
    }

  }

}
