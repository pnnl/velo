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
/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package gov.pnnl.cat.intercept.transformfileappender;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * This class is used to hold FileInfo data for phantom transform nodes that are created for the
 * CIFS view.  
 * We have to create our own version of FileInfoImpl, since alfresco's implementation was package protected
 * so we couldn't call the constructor.
 * 
 * @author David Gillen
 * @version $Revision: 1.0 $
 */
public class TransformedFileInfoImpl implements FileInfo
{
    private static final long serialVersionUID = 1915018521764853537L;

    private NodeRef nodeRef;
    private NodeRef linkNodeRef;
    private boolean isFolder;
    private boolean isLink;
    private Map<QName, Serializable> properties;

    /**
     * Package-level constructor
     * @param nodeRef NodeRef
     * @param isFolder boolean
     * @param properties Map<QName,Serializable>
     */
    /* package */ TransformedFileInfoImpl(NodeRef nodeRef, boolean isFolder, Map<QName, Serializable> properties)
    {
        this.nodeRef = nodeRef;
        this.isFolder = isFolder;
        this.properties = properties;
        
        // Check if this is a link node
        if ( properties.containsKey( ContentModel.PROP_LINK_DESTINATION))
        {
        	isLink = true;
        	linkNodeRef = (NodeRef) properties.get( ContentModel.PROP_LINK_DESTINATION);
        }
    }
    
    /**
     * Method toString.
     * @return String
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("FileInfo")
          .append("[name=").append(getName())
          .append(", isFolder=").append(isFolder)
          .append(", nodeRef=").append(nodeRef);
        
        if ( isLink())
        {
        	sb.append(", linkref=");
        	sb.append(linkNodeRef);
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Method getNodeRef.
     * @return NodeRef
     * @see org.alfresco.service.cmr.model.FileInfo#getNodeRef()
     */
    @Override
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    /**
     * Method isFolder.
     * @return boolean
     * @see org.alfresco.service.cmr.model.FileInfo#isFolder()
     */
    @Override
    public boolean isFolder()
    {
        return isFolder;
    }

    /**
     * Method isLink.
     * @return boolean
     * @see org.alfresco.service.cmr.model.FileInfo#isLink()
     */
    @Override
    public boolean isLink()
    {
    	return isLink;
    }
  
    /**
     * Method getLinkNodeRef.
     * @return NodeRef
     * @see org.alfresco.service.cmr.model.FileInfo#getLinkNodeRef()
     */
    @Override
    public NodeRef getLinkNodeRef()
    {
    	return linkNodeRef;
    }
    
    /**
     * Method getName.
     * @return String
     * @see org.alfresco.service.cmr.model.FileInfo#getName()
     */
    @Override
    public String getName()
    {
        return (String) properties.get(ContentModel.PROP_NAME);
    }

    /**
     * Method getCreatedDate.
     * @return Date
     * @see org.alfresco.service.cmr.model.FileInfo#getCreatedDate()
     */
    @Override
    public Date getCreatedDate()
    {
        return DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_CREATED));
    }

    /**
     * Method getModifiedDate.
     * @return Date
     * @see org.alfresco.service.cmr.model.FileInfo#getModifiedDate()
     */
    @Override
    public Date getModifiedDate()
    {
        return DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_MODIFIED));
    }
    
    /**
     * Method getContentData.
     * @return ContentData
     * @see org.alfresco.service.cmr.model.FileInfo#getContentData()
     */
    @Override
    public ContentData getContentData()
    {
        return DefaultTypeConverter.INSTANCE.convert(ContentData.class, properties.get(ContentModel.PROP_CONTENT));
    }

    /**
     * Method getProperties.
     * @return Map<QName,Serializable>
     * @see org.alfresco.service.cmr.model.FileInfo#getProperties()
     */
    @Override
    public Map<QName, Serializable> getProperties()
    {
        return properties;
    }

    /**
     * Method isHidden.
     * @return boolean
     * @see org.alfresco.service.cmr.model.FileInfo#isHidden()
     */
    @Override
    public boolean isHidden() {
      // phantom transform nodes are not hidden
      return false;
    }

    /**
     * Method getType.
     * @return QName
     * @see org.alfresco.service.cmr.model.FileInfo#getType()
     */
    @Override
    public QName getType() {
      // phantom transform nodes are of type content
      return ContentModel.TYPE_CONTENT;
    }

}
