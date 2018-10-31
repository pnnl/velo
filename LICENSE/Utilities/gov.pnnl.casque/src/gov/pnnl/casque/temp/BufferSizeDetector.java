package gov.pnnl.casque.temp;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class BufferSizeDetector extends BufferedInputStream {
  public BufferSizeDetector(InputStream in) {
    super(in);
  }

  public int getBufferSize() {
    return super.buf.length;
  }
}
