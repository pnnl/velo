package gov.pnnl.cat.core.internal.resources.events;

public interface JmsConnectionListener {
  public final int CONNECTED = 1;
  public final int DISCONNECTED = 2;
  public final int RECONNECTED = 3;
  
  public void connectionStatusChanged(int status, Throwable exception);

}
