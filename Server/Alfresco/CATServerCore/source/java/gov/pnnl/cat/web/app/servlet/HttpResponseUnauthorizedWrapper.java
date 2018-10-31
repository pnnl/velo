package gov.pnnl.cat.web.app.servlet;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.IOUtils;
import org.osaf.cosmo.util.BufferedServletOutputStream;

/**
 * HttpServletResponseWrapper that catches 401 Unauthorized
 *  or 403 responses so we can retry as guest before flushing the output
 */
public class HttpResponseUnauthorizedWrapper extends HttpServletResponseWrapper {
    private String errorMsg = null;
    private BufferedServletOutputStream bufferedOutput = null;
    private boolean hasError = false;
    private int errorCode;
    
    public HttpResponseUnauthorizedWrapper(HttpServletResponse response) throws IOException {
        super(response);
    }

    @Override
    public void sendError(int code, String msg) throws IOException {
        // Trap 401's
        if(code==SC_UNAUTHORIZED || code == SC_FORBIDDEN) {
            hasError = true;
            errorMsg = msg;
            errorCode = code;
        } else {
            super.sendError(code, msg);
        }
    }

    @Override
    public void sendError(int code) throws IOException {
        // Trap 401's
        if(code==SC_UNAUTHORIZED  || code == SC_FORBIDDEN) {
            hasError = true;
            errorCode = code;
        } else {
            super.sendError(code);
        }
    }
    
    public boolean flushError() throws IOException {
      return flushError(null);
    }
    
    /**
     * If a 401/403 error was trapped, then flush it.  This can involve invoking
     * sendError() or setStatus() along with writing any data that
     * was buffered.
     * @returns true if an error was flushed, otherwise false
     * @throws IOException
     */
    public boolean flushError(Integer responseCode) throws IOException {
        if(hasError) {
            if(bufferedOutput!=null && !bufferedOutput.isEmpty()) {
                if(responseCode != null) {
                  super.setStatus(responseCode);
                  
                } else {
                  super.setStatus(errorCode);
                }
                IOUtils.copy(bufferedOutput.getBufferInputStream(), super.getOutputStream()); 
            }
            else if (errorMsg!=null) {
              if(responseCode != null) {
                super.sendError(responseCode);
                
              } else {
                super.sendError(errorCode, errorMsg);
              }
            }
            else
                super.sendError(errorCode);
            
            clearError();
            return true;
        }
        
        return false;
    }
    
    /**
     * Clear error, voiding any 401 response sent.
     */
    public void clearError() {
        bufferedOutput = null;
        errorMsg = null;
        hasError = false;
    }
    
    public boolean isUnauthorized() {
      return hasError && errorCode == SC_UNAUTHORIZED;
    }
    
    public boolean isForbidden() {
      return hasError && errorCode == SC_FORBIDDEN;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        
        // If an error was trapped, then return our custom outputstream
        // so that we can buffer any data sent and be able to send
        // it later on.
        if(hasError) {
            if(bufferedOutput==null)
                bufferedOutput = new BufferedServletOutputStream();
            
            return bufferedOutput;
        }
        
        return super.getOutputStream();
    }

    @Override
    public void setStatus(int sc, String sm) {
        // trap 401  
        if(sc==SC_UNAUTHORIZED || sc == SC_FORBIDDEN) {
            hasError = true;
            errorMsg = sm;
            errorCode = sc;
        } else {
            super.setStatus(sc, sm);
        }
    }

    @Override
    public void setStatus(int sc) {
        // trap 401
        if(sc==this.SC_UNAUTHORIZED || sc == SC_FORBIDDEN) {
            hasError = true;
            errorCode = sc;
        } else {
            super.setStatus(sc);
        }
    }
    
    /**
     * @return buffered outputstream, only has a value if
     *         an error has been trapped and getOutputStream()
     *         was called, meaning the request handler was
     *         trying to send error data.
     */
    public BufferedServletOutputStream getBufferedOutputStream() {
        return bufferedOutput;
    }
}