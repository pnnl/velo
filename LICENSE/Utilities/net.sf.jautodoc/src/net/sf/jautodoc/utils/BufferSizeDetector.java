/*
 * gov.pnnl.velo
 * 
 * Author: Cody Curry
 */
package net.sf.jautodoc.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;

public final class BufferSizeDetector extends BufferedInputStream {
    public BufferSizeDetector(InputStream in) {
        super(in);
    }
  
    public int getBufferSize() {
        return super.buf.length;
    }
}
