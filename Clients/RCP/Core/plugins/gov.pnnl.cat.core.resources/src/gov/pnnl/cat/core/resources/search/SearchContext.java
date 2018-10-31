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
package gov.pnnl.cat.core.resources.search;

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

import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.impl.lucene.QueryParser;
import org.apache.log4j.Logger;

/**
 * Holds the context required to build a search query and can return the populated query.
 * <p>
 * Builds a lucene format search string from each of the supplied attributes and terms.
 * Can be serialized to and from XML format for saving and restoring of previous searches.
 * 
 * @author Kevin Roast
 * @version $Revision: 1.0 $
 */
public final class SearchContext implements Serializable
{
   private static final long serialVersionUID = 6730844584074229969L;
   
   /** XML serialization elements */
   private static final String ELEMENT_VALUE = "value";
   private static final String ELEMENT_FIXED_VALUES = "fixed-values";
   private static final String ELEMENT_INCLUSIVE = "inclusive";
   private static final String ELEMENT_UPPER = "upper";
   private static final String ELEMENT_LOWER = "lower";
   private static final String ELEMENT_RANGE = "range";
   private static final String ELEMENT_RANGES = "ranges";
   private static final String ELEMENT_NAME = "name";
   private static final String ELEMENT_ATTRIBUTE = "attribute";
   private static final String ELEMENT_ATTRIBUTES = "attributes";
   private static final String ELEMENT_MIMETYPE = "mimetype";
   private static final String ELEMENT_CONTENT_TYPE = "content-type";
   private static final String ELEMENT_FOLDER_TYPE = "folder-type";
   private static final String ELEMENT_CATEGORY = "category";
   private static final String ELEMENT_CATEGORIES = "categories";
   private static final String ELEMENT_LOCATION = "location";
   private static final String ELEMENT_MODE = "mode";
   private static final String ELEMENT_TEXT = "text";
   private static final String ELEMENT_SEARCH = "search";
   private static final String ELEMENT_QUERY = "query";
   
   /** advanced search term operators */
   private static final char OP_WILDCARD = '*';
   private static final char OP_AND = '+';
   private static final char OP_NOT = '-';
   private static final String STR_OP_WILDCARD = "" + OP_WILDCARD;
   
   /** Search mode constants */
   public final static int SEARCH_ALL = 0;
   public final static int SEARCH_FILE_NAMES_CONTENTS = 1;
   public final static int SEARCH_FILE_NAMES = 2;
   public final static int SEARCH_SPACE_NAMES = 3;
   public final static int SEARCH_CONTENT = 4;
   public final static int SEARCH_METADATA = 5;
   
   /** the search text string */
   private String text = "";
   
   /** mode for the search */
   private int mode = SearchContext.SEARCH_ALL;
   
   /** folder XPath location for the search */
   private Collection<CmsPath> locations = new ArrayList<CmsPath>();
   
   /** categories to add to the search */
   private String[] categories = new String[0];
   
   /** folder type to restrict search against */
   private String folderType = null;
   
   /** content type to restrict search against */
   private String contentType = null;
   
   /** content mimetype to restrict search against */
   private String mimeType = null;
   
   /** any extra query attributes to add to the search */
   private Map<String, String> queryAttributes = new HashMap<String, String>(5, 1.0f);

   /** any additional range attribute to add to the search */
   private Map<String, RangeProperties> rangeAttributes = new HashMap<String, RangeProperties>(5, 1.0f);
   
   /** any additional fixed value attributes to add to the search, such as boolean or noderef */
   private Map<String, String> queryFixedValues = new HashMap<String, String>(5, 1.0f);
   
   /** set true to force the use of AND between text terms */
   private boolean forceAndTerms = false;

  private String aspectToInclude;

  private String aspectToExclude;
  
  private List<String> typesToExclude = new ArrayList<String>();

  private List<String> typesToInclude = new ArrayList<String>();;
   
   /** logger */
   private static Logger logger = CatLogger.getLogger(SearchContext.class);
   
   /**
    * Build the search query string based on the current search context members.
    * 
    * @param minimum       small possible textual string used for a match
    *                      this does not effect fixed values searches (e.g. boolean, int values) or date ranges
    * 
   
    * @return prepared search query string */
   public String buildQuery(int minimum)
   {
     boolean validQuery = true;
     
     String text = this.text.trim();

     StringBuilder fullTextBuf = new StringBuilder(64);
     fullTextBuf.append("ALL:(" + text + ")" );
     
     //location = /User Documents/zoe" convert to
     //e.g., pathQuery= PATH:"/app:company_home/cm:User_x0020_Documents/cm:zoe//*" 
     // match a specific PATH for space location or categories
     StringBuilder pathQuery = null;
     if (!locations.isEmpty())
     {
       pathQuery = new StringBuilder(128);
       int count = 0;
       pathQuery.append("("); // opening "(" for non-category locations
       pathQuery.append("("); // opening "(" for array of locations
       for (CmsPath path : locations) {
         String loc = path.toPrefixString(true) + "//*";

         if(count > 0) 
         {
           pathQuery.append(" OR ");
         }

         pathQuery.append(" PATH:\"").append(loc).append("\" ");

         count++;
       }
       pathQuery.append(")"); //ending ")" for array of locations
     }

     //dictionary directory should be excluded
     if(pathQuery != null && pathQuery.length() > 0)
     {
       //pathQuery.append(" AND -PATH:\"/app:company_home/app:dictionary//*\" AND -PATH:\"/app:company_home/app:guest_home//*\"");
//       pathQuery.append(" AND -PATH:\"/app:company_home/app:*//*\")");
       pathQuery.append(")");
//     ZG 6/18/2007: removed the part of the query that eliminates matches directly under company home since we're not specify path anymore (above path 'AND')
     }
     // on 5/16/2007, found PATH increases the search time dramatically
     /*
     else
     {
       pathQuery = new StringBuilder(128);
       //for some reasons, pathQuery needs the first PATH:/* to work
       //pathQuery.append("PATH:\"//*\" AND -PATH:\"/app:company_home/app:dictionary//*\" AND -PATH:\"/app:company_home/app:guest_home//*\"");
       pathQuery.append("(PATH:\"/app:company_home//*\" AND -PATH:\"/app:company_home/app:*//*\")");
     }
     */

     if (categories != null && categories.length != 0) {
       //if category is not null, put "(" at the beginning to wrap: (regularPath OR categoryPath)
       pathQuery.insert(0, "(");
       
       pathQuery.append(" OR ("); //beginning "(" for categories

       IResourceManager mgr = ResourcesPlugin.getResourceManager();

       for (int i = 0; i < categories.length; i++) {
         if (i > 0) {
           pathQuery.append(" OR");
         }
         pathQuery.append(" PATH:\"/tax:classification//tax:");
         pathQuery.append(mgr.encodeISO9075(categories[i]));
         pathQuery.append("//member\"");
       }
       pathQuery.append(") "); //ending ")" for categories

       pathQuery.append(") ");  //ending ")" for (regularPath OR categoryPath)
     }


     // match any extra query attribute values specified
     StringBuilder attributeQuery = null;
     if (queryAttributes.size() != 0)
     {
        attributeQuery = new StringBuilder(queryAttributes.size() << 6);
        for (String qname : queryAttributes.keySet())
        {
           String value = queryAttributes.get(qname).trim();
           if (value.length() >= minimum)
           {
              processSearchAttribute(qname, value, attributeQuery);
           }
        }
        
        // handle the case where we did not add any attributes due to minimum length restrictions
        if (attributeQuery.length() == 0)
        {
           attributeQuery = null;
        }
     }
     
     // match any extra fixed value attributes specified
     if (queryFixedValues.size() != 0)
     {
        if (attributeQuery == null)
        {
           attributeQuery = new StringBuilder(queryFixedValues.size() << 6);
        }
        for (String qname : queryFixedValues.keySet())
        {
           //String escapedName = Repository.escapeQualifiedName(qname);
           String escapedName = escapeQualifiedName(qname);
           String value = queryFixedValues.get(qname);
           attributeQuery.append(" +@").append(escapedName)
                         .append(":\"").append(value).append('"');
        }
     }
     
     // range attributes are a special case also
     if (rangeAttributes.size() != 0)
     {
        if (attributeQuery == null)
        {
           attributeQuery = new StringBuilder(rangeAttributes.size() << 6);
        }
        
        for (String qname : rangeAttributes.keySet())
        {
          String escapedName = escapeQualifiedName(qname);
           RangeProperties rp = rangeAttributes.get(qname);
           String value1 = QueryParser.escape(rp.lower);
           String value2 = QueryParser.escape(rp.upper);
           attributeQuery.append(" +@").append(escapedName)
                         .append(":").append(rp.inclusive ? "[" : "{").append(value1)
                         .append(" TO ").append(value2).append(rp.inclusive ? "]" : "}");
        }
     }
     
     // mimetype is a special case - it is indexed as a special attribute it comes from the combined
     // ContentData attribute of cm:content - ContentData string cannot be searched directly
     if (mimeType != null && mimeType.length() != 0)
     {
        if (attributeQuery == null)
        {
           attributeQuery = new StringBuilder(64);
        }
        // static final QName PROP_CONTENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "content");
        String escapedName = escapeQualifiedName(VeloConstants.PROP_CONTENT + ".mimetype");
        attributeQuery.append(" +@").append(escapedName)
                      .append(":").append(mimeType);
     }

     //include ASPECT if either AspectToInclude or AspectToExclude is specified
     boolean aspectInclude = aspectToInclude != null && aspectToInclude.length() > 0;
     boolean aspectExclude = aspectToExclude != null && aspectToExclude.length() > 0;
     if(aspectInclude || aspectExclude)
     {
       StringBuilder queryBuilder = new StringBuilder();
       if (attributeQuery == null)
       {
          attributeQuery = new StringBuilder(64);
       }
       if(aspectInclude)
       {
         // include anything with taxonomy root aspect
         queryBuilder.append(" +(ASPECT:\"");
         queryBuilder.append(aspectToInclude);
         queryBuilder.append("\")");
       }

       if(aspectExclude)
       {
         if(aspectInclude)
         {
           queryBuilder.append(" AND ");
         }
         queryBuilder.append(" -(ASPECT:\"");
         queryBuilder.append(aspectToExclude);
         queryBuilder.append("\")");
       }

       attributeQuery.append(queryBuilder.toString());
     }
     
     // exclude types if typesToExclude is specified
     String typeExcludeQuery = null;
     if(typesToExclude.size() > 0) {
       StringBuilder builder = new StringBuilder();
       for(String type : typesToExclude) {
         builder.append(" AND -(TYPE:\"");
         builder.append(type);
         builder.append("\")");
       }
       typeExcludeQuery = builder.toString();
     }
     //include types if typesToInclude is specified
     String typeIncludeQuery = null;
     if(typesToInclude .size() > 0) {
       StringBuilder builder = new StringBuilder();
       for(String type : typesToInclude) {
         builder.append(" AND (TYPE:\"");
         builder.append(type);
         builder.append("\")");
       }
       typeIncludeQuery = builder.toString();
     }
     
     
     
     String fullTextQuery = fullTextBuf.toString();
     String query = "";
     
     if (text.length() != 0 && text.length() >= minimum)
     {
       query = fullTextQuery;
     }
     
     // match entire query against any additional attributes specified
     if (attributeQuery != null)
     {
       if(!query.isEmpty()) {
         query = attributeQuery + " AND (" + query + ')';
       } else {
         query = attributeQuery.toString();
       }
     }
     
     // match entire query against any specified paths
     if (pathQuery != null)
     {
       query = "(" + query + ") AND " + pathQuery;
     }
          
     // check that we have a query worth executing - if we have no attributes, paths or text/name search
     // then we'll only have a search against files/type TYPE which does nothing by itself!
     validQuery = validQuery | (attributeQuery != null) | (pathQuery != null);
     if (validQuery == false)
     {
        query = null;
     } else {
       // Remove any excluded types
       if(typeExcludeQuery != null) {
         query = query + typeExcludeQuery;
       }
       if(typeIncludeQuery != null) {
         query = query + typeIncludeQuery;
       }
     }
     
     logger.debug("query:"+query);
     return query;
   }
   
   
   /**
    * Build the lucene search terms required for the specified attribute and append to a buffer.
    * Supports text values with a wildcard '*' character as the prefix and/or the suffix. 
    * 
    * @param qname      QualifiedName of the attribute
    * @param value      QualifiedNameull value of the attribute
    * @param buf        Buffer to append lucene terms to
    */
   private static void processSearchAttribute(String qname, String value, StringBuilder buf)
   {
      if (value.indexOf(' ') == -1)
      {
         String safeValue;
         String prefix = "";
         String suffix = "";
         
         // look for a wildcard suffix
         if (value.charAt(value.length() - 1) != OP_WILDCARD)
         {
            // look for wildcard prefix
            if (value.charAt(0) != OP_WILDCARD)
            {
               safeValue = QueryParser.escape(value);
            }
            else
            {
               safeValue = QueryParser.escape(value.substring(1));
               prefix = STR_OP_WILDCARD;
            }
         }
         else
         {
            // found a wildcard suffix - append it again after escaping the other characters
            suffix = STR_OP_WILDCARD;
            
            // look for wildcard prefix
            if (value.charAt(0) != OP_WILDCARD)
            {
               safeValue = QueryParser.escape(value.substring(0, value.length() - 1));
            }
            else
            {
               safeValue = QueryParser.escape(value.substring(1, value.length() - 1));
               prefix = STR_OP_WILDCARD;
            }
         }
         
         buf.append(" +@").append(escapeQualifiedName(qname)).append(":")
         //buf.append(" +@").append("\\{http\\://www.alfresco.org/model/content/1.0\\}name").append(":")
            .append(prefix).append(safeValue).append(suffix);
      }
      else
      {
         // phrase multi-word search
         String safeValue = QueryParser.escape(value);
         buf.append(" +@").append(escapeQualifiedName(qname)).append(":\"").append(safeValue).append('"');
         //buf.append(" +@").append("\\{http\\://www.alfresco.org/model/content/1.0\\}name").append(":\"").append(safeValue).append('"');         
      }
   }
   
   /**
    * Build the lucene search terms required for the specified attribute and append to multiple buffers.
    * Supports text values with a wildcard '*' character as the prefix and/or the suffix. 
    * 
    * @param qname      QualifiedName.toString() of the attribute
    * @param value      Non-null value of the attribute
    * @param attrBuf    Attribute search buffer to append lucene terms to
    * @param textBuf    Text search buffer to append lucene terms to
    */
   private static void processSearchTextAttribute(String qname, String value, StringBuilder attrBuf, StringBuilder textBuf)
   {
      String safeValue;
      String suffix = "";
      String prefix = "";
      
      if (value.charAt(value.length() - 1) != OP_WILDCARD)
      {
         // look for wildcard prefix
         if (value.charAt(0) != OP_WILDCARD)
         {
            safeValue = QueryParser.escape(value);
         }
         else
         {
            // found a leading wildcard - prepend it again after escaping the other characters
            prefix = STR_OP_WILDCARD;
            safeValue = QueryParser.escape(value.substring(1));
         }
      }
      else
      {
         suffix = STR_OP_WILDCARD;
         
         // look for wildcard prefix
         if (value.charAt(0) != OP_WILDCARD)
         {
            safeValue = QueryParser.escape(value.substring(0, value.length() - 1));
         }
         else
         {
            prefix = STR_OP_WILDCARD;
            safeValue = QueryParser.escape(value.substring(1, value.length() - 1));
         }
      }
      
      textBuf.append("TEXT:").append(prefix).append(safeValue).append(suffix);
      attrBuf.append("@").append(qname).append(":")
             .append(prefix).append(safeValue).append(suffix);
   }
   
   /**
    * Generate a search XPATH pointing to the specified node, optionally return an XPATH
    * that includes the child nodes.
    *  
   
   
    * 
   
    * @return the path */
//   /*package*/ static String getPathFromSpaceRef(NodeRef ref, boolean children)
//   {
//      FacesContext context = FacesContext.getCurrentInstance();
//      Path path = Repository.getServiceRegistry(context).getNodeService().getPath(ref);
//      NamespaceService ns = Repository.getServiceRegistry(context).getNamespaceService();
//      StringBuilder buf = new StringBuilder(64);
//      for (int i=0; i<path.size(); i++)
//      {
//         String elementString = "";
//         Path.Element element = path.get(i);
//         if (element instanceof Path.ChildAssocElement)
//         {
//            ChildAssociationRef elementRef = ((Path.ChildAssocElement)element).getRef();
//            if (elementRef.getParentRef() != null)
//            {
//               Collection prefixes = ns.getPrefixes(elementRef.getQualifiedName().getNamespaceURI());
//               if (prefixes.size() >0)
//               {
//                  elementString = '/' + (String)prefixes.iterator().next() + ':' + ISO9075.encode(elementRef.getQualifiedName().getLocalName());
//               }
//            }
//         }
//         
//         buf.append(elementString);
//      }
//      if (children == true)
//      {
//         // append syntax to get all children of the path
//         buf.append("//*");
//      }
//      else
//      {
//         // append syntax to just represent the path, not the children
//         buf.append("/*");
//      }
//      
//      return buf.toString();
//   }
   
   /**
    * @return Returns the categories to use for the search
    */
   public String[] getCategories()
   {
      return this.categories;
   }
   
   /**
    * @param categories    The categories to set as a list of search XPATHs
    */
   public void setCategories(String[] categories)
   {
      if (categories != null)
      {
         this.categories = categories;
      }
   }
   
   /**
   
    * @return Returns the node XPath to search in or null for all. */
   public Collection<CmsPath> getLocations()
   {
      return this.locations;
   }
   
   /**
    * 
    * @param paths
    */
   public void setLocations(Collection<CmsPath> paths)
   {
     this.locations = paths;
   }
   
   /**
    * Method addTypeToExclude.
    * @param type String
    */
   public void addTypeToExclude(String type) {
     typesToExclude.add(type);
   }
   
   
   /**
    * Method addTypeToInclude
    * @param type String
    */
   public void addTypeToInclude(String type) {
     typesToInclude.add(type);
   }
   
   /**
    * Method setAspectToInclude.
    * @param include String
    */
   public void setAspectToInclude(String include) {
     this.aspectToInclude = include;
   }
   
   /**
    * Method setAspectToExclude.
    * @param exclude String
    */
   public void setAspectToExclude(String exclude) {
     this.aspectToExclude = exclude;
   }

   /**
   
    * @return Returns the mode to use during the search (see constants) */
   public int getMode()
   {
      return this.mode;
   }
   
   /**
    * @param mode The mode to use during the search (see constants)
    */
   public void setMode(int mode)
   {
      this.mode = mode;
   }
   
   /**
   
    * @return Returns the search text string. */
   public String getText()
   {
      return this.text;
   }
   
   /**
    * @param text       The search text string.
    */
   public void setText(String text)
   {
      this.text = text;
   }

   /**
   
    * @return Returns the contentType. */
   public String getContentType()
   {
      return this.contentType;
   }

   /**
    * @param contentType The content type to restrict attribute search against.
    */
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }
   
   /**
   
    * @return Returns the folderType. */
   public String getFolderType()
   {
      return this.folderType;
   }

   /**
    * @param folderType The folder type to restrict attribute search against.
    */
   public void setFolderType(String folderType)
   {
      this.folderType = folderType;
   }
   
   /**
   
    * @return Returns the mimeType. */
   public String getMimeType()
   {
      return this.mimeType;
   }
   /**
    * @param mimeType The mimeType to set.
    */
   public void setMimeType(String mimeType)
   {
      this.mimeType = mimeType;
   }
   
   /**
    * Add an additional attribute to search against
    * 
    * @param qname      QualifiedName of the attribute to search against
    * @param value      Value of the attribute to use
    */
   public void addAttributeQuery(String qname, String value)
   {
      this.queryAttributes.put(qname, value);
   }
   
   /**
    * Method getAttributeQuery.
    * @param qname QualifiedName
    * @return String
    */
   public String getAttributeQuery(String qname)
   {
      return this.queryAttributes.get(qname);
   }
   
   /**
    * Add an additional range attribute to search against
    * 
    * @param qname      QualifiedName of the attribute to search against
    * @param lower      Lower value for range
    * @param upper      Upper value for range
    * @param inclusive  True for inclusive within the range, false otherwise
    */
   public void addRangeQuery(String qname, String lower, String upper, boolean inclusive)
   {
     this.rangeAttributes.put(qname, new RangeProperties(qname, lower, upper, inclusive));
   }
   
   /**
    * Method getRangeProperty.
    * @param qname QualifiedName
    * @return RangeProperties
    */
   public RangeProperties getRangeProperty(String qname)
   {
      return this.rangeAttributes.get(qname);
   }
   
   /**
    * Add an additional fixed value attribute to search against
    * 
    * @param qname      QualifiedName of the attribute to search against
    * @param value      Fixed value of the attribute to use
    */
   public void addFixedValueQuery(String qname, String value)
   {
      this.queryFixedValues.put(qname, value);
   }
   
   /**
    * Method getFixedValueQuery.
    * @param qname QualifiedName
    * @return String
    */
   public String getFixedValueQuery(String qname)
   {
      return this.queryFixedValues.get(qname);
   }
   
   /**
   
    * @return Returns if AND is forced between text terms. False (OR terms) is the default. */
   public boolean getForceAndTerms()
   {
      return this.forceAndTerms;
   }

   /**
    * @param forceAndTerms Set true to force AND between text terms. Otherwise OR is the default.
    */
   public void setForceAndTerms(boolean forceAndTerms)
   {
      this.forceAndTerms = forceAndTerms;
   }

   /**
   
    * @param qName QualifiedName
    * @return this SearchContext as XML
    * 
    * Example:
    * <code>
    * <?xml version="1.0" encoding="UTF-8"?>
    * <search>
    *    <text>CDATA</text>
    *    <mode>int</mode>
    *    <location>XPath</location>
    *    <categories>
    *       <category>XPath</category>
    *    </categories>
    *    <content-type>String</content-type>
    *    <folder-type>String</folder-type>
    *    <mimetype>String</mimetype>
    *    <attributes>
    *       <attribute name="String">String</attribute>
    *    </attributes>
    *    <ranges>
    *       <range name="String">
    *          <lower>String</lower>
    *          <upper>String</upper>
    *          <inclusive>boolean</inclusive>
    *       </range>
    *    </ranges>
    *    <fixed-values>
    *       <value name="String">String</value>
    *    </fixed-values>
    *    <query>CDATA</query>
    * </search>
    * </code> */
   //for now this causes the same Namespace exception
//   public String toXML()
//   {
//      try
//      {
//         NamespaceService ns = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNamespaceService();
//         
//         Document doc = DocumentHelper.createDocument();
//         
//         Element root = doc.addElement(ELEMENT_SEARCH);
//         
//         root.addElement(ELEMENT_TEXT).addCDATA(this.text);
//         root.addElement(ELEMENT_MODE).addText(Integer.toString(this.mode));
//         if (this.location != null)
//         {
//            root.addElement(ELEMENT_LOCATION).addText(this.location);
//         }
//         
//         Element categories = root.addElement(ELEMENT_CATEGORIES);
//         for (String path : this.categories)
//         {
//            categories.addElement(ELEMENT_CATEGORY).addText(path);
//         }
//         
//         if (this.contentType != null)
//         {
//            root.addElement(ELEMENT_CONTENT_TYPE).addText(this.contentType);
//         }
//         if (this.folderType != null)
//         {
//            root.addElement(ELEMENT_FOLDER_TYPE).addText(this.folderType);
//         }
//         if (this.mimeType != null && this.mimeType.length() != 0)
//         {
//            root.addElement(ELEMENT_MIMETYPE).addText(this.mimeType);
//         }
//         
//         Element attributes = root.addElement(ELEMENT_ATTRIBUTES);
//         for (QualifiedName attrName : this.queryAttributes.keySet())
//         {
//            attributes.addElement(ELEMENT_ATTRIBUTE)
//                      .addAttribute(ELEMENT_NAME, attrName.toPrefixString(ns))
//                      .addCDATA(this.queryAttributes.get(attrName));
//         }
//         
//         Element ranges = root.addElement(ELEMENT_RANGES);
//         for (QualifiedName rangeName : this.rangeAttributes.keySet())
//         {
//            RangeProperties rangeProps = this.rangeAttributes.get(rangeName);
//            Element range = ranges.addElement(ELEMENT_RANGE);
//            range.addAttribute(ELEMENT_NAME, rangeName.toPrefixString(ns));
//            range.addElement(ELEMENT_LOWER).addText(rangeVeloConstants.PROP_lower);
//            range.addElement(ELEMENT_UPPER).addText(rangeVeloConstants.PROP_upper);
//            range.addElement(ELEMENT_INCLUSIVE).addText(Boolean.toString(rangeVeloConstants.PROP_inclusive));
//         }
//         
//         Element values = root.addElement(ELEMENT_FIXED_VALUES);
//         for (QualifiedName valueName : this.queryFixedValues.keySet())
//         {
//            values.addElement(ELEMENT_VALUE)
//                  .addAttribute(ELEMENT_NAME, valueName.toPrefixString(ns))
//                  .addCDATA(this.queryFixedValues.get(valueName));
//         }
//         
//         // outputing the full lucene query may be useful for some situations
//         Element query = root.addElement(ELEMENT_QUERY);
//         String queryString = buildQuery(0);
//         if (queryString != null)
//         {
//            query.addCDATA(queryString);
//         }
//         
//         StringWriter out = new StringWriter(1024);
//         XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
//         writer.setWriter(out);
//         writer.write(doc);
//         
//         return out.toString();
//      }
//      catch (Throwable err)
//      {
//        //To Do: need to find an appropriate existing Exception, or create one
//        System.out.println("Exception: "+err.toString());
//        return "";
//        //throw new Exception("Failed to export SearchContext to XML.", err);
//      }
//   }

   /**
    * Restore a SearchContext from an XML definition
    * 
    * @param xml     XML format SearchContext @see #toXML()
    */
//For now, this causes Exception: 
//java.lang.NoClassDefFoundError: org/alfresco/service/namespace/NamespaceService
//   public SearchContext fromXML(String xml)
//   {
//      try
//      {
//         NamespaceService ns = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNamespaceService();
//         
//         // get the root element
//         SAXReader reader = new SAXReader();
//         Document document = reader.read(new StringReader(xml));
//         Element rootElement = document.getRootElement();
//         Element textElement = rootElement.element(ELEMENT_TEXT);
//         if (textElement != null)
//         {
//            this.text = textElement.getText();
//         }
//         Element modeElement = rootElement.element(ELEMENT_MODE);
//         if (modeElement != null)
//         {
//            this.mode = Integer.parseInt(modeElement.getText());
//         }
//         Element locationElement = rootElement.element(ELEMENT_LOCATION);
//         if (locationElement != null)
//         {
//            this.location = locationElement.getText();
//         }
//         Element categoriesElement = rootElement.element(ELEMENT_CATEGORIES);
//         if (categoriesElement != null)
//         {
//            List<String> categories = new ArrayList<String>(4);
//            for (Iterator i=categoriesElement.elementIterator(ELEMENT_CATEGORY); i.hasNext(); /**/)
//            {
//               Element categoryElement = (Element)i.next();
//               categories.add(categoryElement.getText());
//            }
//            this.categories = categories.toArray(this.categories);
//         }
//         Element contentTypeElement = rootElement.element(ELEMENT_CONTENT_TYPE);
//         if (contentTypeElement != null)
//         {
//            this.contentType = contentTypeElement.getText();
//         }
//         Element folderTypeElement = rootElement.element(ELEMENT_FOLDER_TYPE);
//         if (folderTypeElement != null)
//         {
//            this.folderType = folderTypeElement.getText();
//         }
//         Element mimetypeElement = rootElement.element(ELEMENT_MIMETYPE);
//         if (mimetypeElement != null)
//         {
//            this.mimeType = mimetypeElement.getText();
//         }
//         Element attributesElement = rootElement.element(ELEMENT_ATTRIBUTES);
//         if (attributesElement != null)
//         {
//            for (Iterator i=attributesElement.elementIterator(ELEMENT_ATTRIBUTE); i.hasNext(); /**/)
//            {
//               Element attrElement = (Element)i.next();
//               QualifiedName qname = QualifiedName.createQualifiedName(attrElement.attributeValue(ELEMENT_NAME), ns);
//               addAttributeQuery(qname, attrElement.getText());
//            }
//         }
//         Element rangesElement = rootElement.element(ELEMENT_RANGES);
//         if (rangesElement != null)
//         {
//            for (Iterator i=rangesElement.elementIterator(ELEMENT_RANGE); i.hasNext(); /**/)
//            {
//               Element rangeElement = (Element)i.next();
//               Element lowerElement = rangeElement.element(ELEMENT_LOWER);
//               Element upperElement = rangeElement.element(ELEMENT_UPPER);
//               Element incElement = rangeElement.element(ELEMENT_INCLUSIVE);
//               if (lowerElement != null && upperElement != null && incElement != null)
//               {
//                  QualifiedName qname = QualifiedName.createQualifiedName(rangeElement.attributeValue(ELEMENT_NAME), ns);
//                  addRangeQuery(qname,
//                        lowerElement.getText(), upperElement.getText(),
//                        Boolean.parseBoolean(incElement.getText()));
//               }
//            }
//         }
//         
//         Element valuesElement = rootElement.element(ELEMENT_FIXED_VALUES);
//         if (valuesElement != null)
//         {
//            for (Iterator i=valuesElement.elementIterator(ELEMENT_VALUE); i.hasNext(); /**/)
//            {
//               Element valueElement = (Element)i.next();
//               QualifiedName qname = QualifiedName.createQualifiedName(valueElement.attributeValue(ELEMENT_NAME), ns);
//               addFixedValueQuery(qname, valueElement.getText());
//            }
//         }
//      }
//      catch (Throwable err)
//      {
//        //To Do: need to find a more appropriate Exception or create one
//        System.out.println("Exception: "+err.toString());
//        //throw new Exception("Failed to import SearchContext from XML.", err);
//         //throw new AlfrescoRuntimeException("Failed to import SearchContext from XML.", err);
//      }
//      return this;
//   }


   /**
    * The following copied from Repository.class in Alfresco server
    * 
    */
   public static String escapeQualifiedName(String qName)
   {
       StringBuilder buf = new StringBuilder(qName.length() + 4);
       for (int i = 0; i < qName.length(); i++)
       {
           char c = qName.charAt(i);
           if ((c == '{') || (c == '}') || (c == ':') || (c == '-'))
           {
              buf.append('\\');
           }
   
           buf.append(c);
       }
       return buf.toString();
   }

   /**
    * This by pass the QualifiedName, directly use namespaceURI and localName
    * @param namespaceURI
    * @param localName
   
    * @return String
    */
   public static String escapeQualifiedName(String namespaceURI, String localName)
   {
     final char NAMESPACE_BEGIN = '{';
     final char NAMESPACE_END = '}';
       String string = NAMESPACE_BEGIN + namespaceURI + NAMESPACE_END + localName;
       StringBuilder buf = new StringBuilder(string.length() + 4);
       for (int i = 0; i < string.length(); i++)
       {
           char c = string.charAt(i);
           if ((c == '{') || (c == '}') || (c == ':') || (c == '-'))
           {
              buf.append('\\');
           }
   
           buf.append(c);
       }
       return buf.toString();
   }

   
   /**
    * Simple wrapper class for range query attribute properties 
    * @version $Revision: 1.0 $
    */
   static class RangeProperties
   {
      String qname;
      String lower;
      String upper;
      boolean inclusive;
      
      /**
       * Constructor for RangeProperties.
       * @param qname QualifiedName
       * @param lower String
       * @param upper String
       * @param inclusive boolean
       */
      RangeProperties(String qname, String lower, String upper, boolean inclusive)
      {
         this.qname = qname;
         this.lower = lower;
         this.upper = upper;
         this.inclusive = inclusive;
      }
   }
   
}
