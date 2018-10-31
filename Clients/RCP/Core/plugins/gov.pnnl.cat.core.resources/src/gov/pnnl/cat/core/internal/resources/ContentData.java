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
package gov.pnnl.cat.core.internal.resources;

import java.io.Serializable;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * A utility for parsing Alfresco's content property.
 * A simplified version of Alfresco's ContentData class used
 * on the server.
 * @version $Revision: 1.0 $
 */
public class ContentData implements Serializable
{
    private static final long serialVersionUID = 8979634213050121462L;

    private static char[] INVALID_CONTENT_URL_CHARS = new char[] {'|'};
    
    private final String contentUrl;
    private final String mimetype;
    private final long size;
    private final String encoding;
    
    /**
     * Constructor for ContentData.
     * @param contentPropertyStr String
     */
    public ContentData (String contentPropertyStr)
    {
        String contentUrl = null;
        String mimetype = null;
        long size = 0L;
        String encoding = null;
        Locale locale = null;
        // now parse the string
        StringTokenizer tokenizer = new StringTokenizer(contentPropertyStr, "|");
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            if (token.startsWith("contentUrl="))
            {
                contentUrl = token.substring(11);
                if (contentUrl.length() == 0)
                {
                    contentUrl = null;
                }
            }
            else if (token.startsWith("mimetype="))
            {
                mimetype = token.substring(9);
                if (mimetype.length() == 0)
                {
                    mimetype = null;
                }
            }
            else if (token.startsWith("size="))
            {
                String sizeStr = token.substring(5);
                if (sizeStr.length() > 0)
                {
                    size = Long.parseLong(sizeStr);
                }
            }
            else if (token.startsWith("encoding="))
            {
                encoding = token.substring(9);
                if (encoding.length() == 0)
                {
                    encoding = null;
                }
            }
        }
        this.contentUrl = contentUrl;
        this.mimetype = mimetype;
        this.size = size;
        this.encoding = encoding;
     }
    
    /**
     * Helper method to determine if the data represents any physical content or not.
     * 
     * @param contentData           the content to check (may be <tt>null</tt>)
    
     * @return                      <tt>true</tt> if the value is non-null */
    public static boolean hasContent(ContentData contentData)
    {
        if (contentData == null)
        {
            return false;
        }
        return contentData.contentUrl != null;
    }
    
    /**
    
     * @return Returns a string of form: <code>contentUrl=xxx|mimetype=xxx|size=xxx|encoding=xxx|locale=xxx</code> */
    public String toString()
    {
        return getInfoUrl();
    }
    
    /**
    
     * @return Returns a URL containing information on the content including the mimetype, 
     *         locale, encoding and size, the string is returned in the form:
     *         <code>contentUrl=xxx|mimetype=xxx|size=xxx|encoding=xxx|locale=xxx</code> */
    public String getInfoUrl()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("contentUrl=").append(contentUrl == null ? "" : contentUrl)
          .append("|mimetype=").append(mimetype == null ? "" : mimetype)
          .append("|size=").append(size)
          .append("|encoding=").append(encoding == null ? "" : encoding);
        return sb.toString();
    }
    
    /**
    
    
     * @return Returns a URL identifying the specific location of the content.
     *      The URL must identify, within the context of the originating content
     *      store, the exact location of the content. * @throws ContentIOException */
    public String getContentUrl()
    {
        return contentUrl;
    }
    
    /**
     * Gets content's mimetype.
     * 
    
     * @return Returns a standard mimetype for the content or null if the mimetype
     *      is unkown */
    public String getMimetype()
    {
        return mimetype;
    }
    
    /**
     * Get the content's size
     *  
    
     * @return Returns the size of the content */
    public long getSize()
    {
        return size;
    }
    
    /**
     * Gets the content's encoding.
     * 
    
     * @return Returns a valid Java encoding, typically a character encoding, or
     *      null if the encoding is unkown */
    public String getEncoding()
    {
        return encoding;
    }
    
    /**
    
     * @return hashCode */
    public int hashCode()
    {
        if(contentUrl!= null)
        {
            return contentUrl.hashCode();
        }
        return 0;
    }
}

