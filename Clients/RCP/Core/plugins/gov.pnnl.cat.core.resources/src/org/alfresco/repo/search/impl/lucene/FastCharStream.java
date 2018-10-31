/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
// FastCharStream.java
package org.alfresco.repo.search.impl.lucene;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.Reader;

/** An efficient implementation of JavaCC's CharStream interface.  <p>Note that
 * this does not do line-number counting, but instead keeps track of the
 * character position of the token in the input, as required by Lucene's {@link
 * org.apache.lucene.analysis.Token} API. * @version $Revision: 1.0 $
 */
public final class FastCharStream implements CharStream {
  char[] buffer = null;

  int bufferLength = 0;				  // end of valid chars
  int bufferPosition = 0;			  // next char to read

  int tokenStart = 0;				  // offset in buffer
  int bufferStart = 0;				  // position in file of buffer

  Reader input;					  // source of chars

  /** Constructs from a Reader. * @param r Reader
   */
  public FastCharStream(Reader r) {
    input = r;
  }

  /**
   * Method readChar.
   * @return char
   * @throws IOException
   * @see org.alfresco.repo.search.impl.lucene.CharStream#readChar()
   */
  public final char readChar() throws IOException {
    if (bufferPosition >= bufferLength)
      refill();
    return buffer[bufferPosition++];
  }

  /**
   * Method refill.
   * @throws IOException
   */
  private final void refill() throws IOException {
    int newPosition = bufferLength - tokenStart;

    if (tokenStart == 0) {			  // token won't fit in buffer
      if (buffer == null) {			  // first time: alloc buffer
	buffer = new char[2048];
      } else if (bufferLength == buffer.length) { // grow buffer
	char[] newBuffer = new char[buffer.length*2];
	System.arraycopy(buffer, 0, newBuffer, 0, bufferLength);
	buffer = newBuffer;
      }
    } else {					  // shift token to front
      System.arraycopy(buffer, tokenStart, buffer, 0, newPosition);
    }

    bufferLength = newPosition;			  // update state
    bufferPosition = newPosition;
    bufferStart += tokenStart;
    tokenStart = 0;

    int charsRead =				  // fill space in buffer
      input.read(buffer, newPosition, buffer.length-newPosition);
    if (charsRead == -1)
      throw new IOException("read past eof");
    else
      bufferLength += charsRead;
  }

  /**
   * Method BeginToken.
   * @return char
   * @throws IOException
   * @see org.alfresco.repo.search.impl.lucene.CharStream#BeginToken()
   */
  public final char BeginToken() throws IOException {
    tokenStart = bufferPosition;
    return readChar();
  }

  /**
   * Method backup.
   * @param amount int
   * @see org.alfresco.repo.search.impl.lucene.CharStream#backup(int)
   */
  public final void backup(int amount) {
    bufferPosition -= amount;
  }

  /**
   * Method GetImage.
   * @return String
   * @see org.alfresco.repo.search.impl.lucene.CharStream#GetImage()
   */
  public final String GetImage() {
    return new String(buffer, tokenStart, bufferPosition - tokenStart);
  }

  /**
   * Method GetSuffix.
   * @param len int
   * @return char[]
   * @see org.alfresco.repo.search.impl.lucene.CharStream#GetSuffix(int)
   */
  public final char[] GetSuffix(int len) {
    char[] value = new char[len];
    System.arraycopy(buffer, bufferPosition - len, value, 0, len);
    return value;
  }

  /**
   * Method Done.
   * @see org.alfresco.repo.search.impl.lucene.CharStream#Done()
   */
  public final void Done() {
    try {
      input.close();
    } catch (IOException e) {
      System.err.println("Caught: " + e + "; ignoring.");
    }
  }

  /**
   * Method getColumn.
   * @return int
   */
  public final int getColumn() {
    return bufferStart + bufferPosition;
  }
  /**
   * Method getLine.
   * @return int
   */
  public final int getLine() {
    return 1;
  }
  /**
   * Method getEndColumn.
   * @return int
   * @see org.alfresco.repo.search.impl.lucene.CharStream#getEndColumn()
   */
  public final int getEndColumn() {
    return bufferStart + bufferPosition;
  }
  /**
   * Method getEndLine.
   * @return int
   * @see org.alfresco.repo.search.impl.lucene.CharStream#getEndLine()
   */
  public final int getEndLine() {
    return 1;
  }
  /**
   * Method getBeginColumn.
   * @return int
   * @see org.alfresco.repo.search.impl.lucene.CharStream#getBeginColumn()
   */
  public final int getBeginColumn() {
    return bufferStart + tokenStart;
  }
  /**
   * Method getBeginLine.
   * @return int
   * @see org.alfresco.repo.search.impl.lucene.CharStream#getBeginLine()
   */
  public final int getBeginLine() {
    return 1;
  }
}
