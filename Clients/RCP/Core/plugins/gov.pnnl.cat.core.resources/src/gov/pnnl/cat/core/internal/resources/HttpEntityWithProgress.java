/**
 * 
 */
package gov.pnnl.cat.core.internal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author d3k339
 *
 */
public class HttpEntityWithProgress implements HttpEntity {
  private HttpEntity wrappedEntity;
  private IProgressMonitor monitor;
  private int workedByteBatchSize;
  
  public HttpEntityWithProgress(HttpEntity wrappedEntity, IProgressMonitor monitor, int workedByteBatchSize) {
    super();
    this.wrappedEntity = wrappedEntity;
    this.monitor = monitor;
    this.workedByteBatchSize = workedByteBatchSize;
  }

  /* (non-Javadoc)
   * @see org.apache.http.HttpEntity#isRepeatable()
   */
  @Override
  public boolean isRepeatable() {
    return wrappedEntity.isRepeatable();
  }

  /* (non-Javadoc)
   * @see org.apache.http.HttpEntity#isChunked()
   */
  @Override
  public boolean isChunked() {
    return wrappedEntity.isChunked();
  }

  /* (non-Javadoc)
   * @see org.apache.http.HttpEntity#getContentLength()
   */
  @Override
  public long getContentLength() {
    return wrappedEntity.getContentLength();
  }

  /* (non-Javadoc)
   * @see org.apache.http.HttpEntity#getContentType()
   */
  @Override
  public Header getContentType() {
    return wrappedEntity.getContentType();
  }

  /* (non-Javadoc)
   * @see org.apache.http.HttpEntity#getContentEncoding()
   */
  @Override
  public Header getContentEncoding() {
    return wrappedEntity.getContentEncoding();
  }

  /* (non-Javadoc)
   * @see org.apache.http.HttpEntity#getContent()
   */
  @Override
  public InputStream getContent() throws IOException, IllegalStateException {
    return wrappedEntity.getContent();
  }

  /* (non-Javadoc)
   * @see org.apache.http.HttpEntity#writeTo(java.io.OutputStream)
   */
  @Override
  public void writeTo(OutputStream outstream) throws IOException {
    wrappedEntity.writeTo(new OutputStreamWithProgress(outstream, monitor, workedByteBatchSize));
  }

  /* (non-Javadoc)
   * @see org.apache.http.HttpEntity#isStreaming()
   */
  @Override
  public boolean isStreaming() {
    return wrappedEntity.isStreaming();
  }

  /* (non-Javadoc)
   * @see org.apache.http.HttpEntity#consumeContent()
   */
  @Override
  public void consumeContent() throws IOException {
    wrappedEntity.consumeContent();
  }

  public static class OutputStreamWithProgress extends ProxyOutputStream {

    private final IProgressMonitor monitor;
    private long bytesTransferred = 0;
    private int workedByteBatchSize;

    public OutputStreamWithProgress(final OutputStream out, final IProgressMonitor monitor, int workedByteBatchSize) {
      super(out);
      this.monitor = monitor;
      this.workedByteBatchSize = workedByteBatchSize;
    }

    public void write(byte[] b, int off, int len) throws IOException {
      out.write(b, off, len);
      bytesTransferred += len;
      evaluateProgress();
    }

    public void write(int b) throws IOException {
      out.write(b);
      bytesTransferred++;
      evaluateProgress();
    }
    
    private void evaluateProgress() {
      // Because Eclipse only lets you measure total work as an int, we have
      // to equate 1 worked interval as a batch of bytes transferred
      while(bytesTransferred > workedByteBatchSize) {
        bytesTransferred = bytesTransferred - workedByteBatchSize;
        if(monitor != null)
        	monitor.worked(1);
      }
    }
  }

}
