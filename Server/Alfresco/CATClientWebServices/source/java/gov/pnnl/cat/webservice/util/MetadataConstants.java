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
package gov.pnnl.cat.webservice.util;

import org.alfresco.webservice.util.Constants;



/**
 */
public class MetadataConstants extends Constants {

  /** namespace for Metadata related properties, aspects */
  public static final String NAMESPACE_DCTERMS =  "http://purl.org/dc/terms";
  public static final String NAMESPACE_METADATA =  "http://www.pnl.gov/cat/model/metadata/1.0";
  
  public static final String ASPECT_METADATA = createQNameString(NAMESPACE_METADATA, "metadata");
  public static final String ASPECT_DCTERMS = createQNameString(NAMESPACE_METADATA, "dcterms");


  // properties for ASPECT_DCTERMS
  public static final String PROP_ABSTRACT = createQNameString(NAMESPACE_DCTERMS, "abstract");
  public static final String PROP_ACCESSRIGHTS = createQNameString(NAMESPACE_DCTERMS, "accessRights");
  public static final String PROP_ACCRUALMETHOD = createQNameString(NAMESPACE_DCTERMS, "accrualMethod");
  public static final String PROP_ACCRUALPERIODICITY = createQNameString(NAMESPACE_DCTERMS, "accrualPeriodicity");
  public static final String PROP_ACCRUALPOLICY = createQNameString(NAMESPACE_DCTERMS, "accrualPolicy");
  public static final String PROP_ALTERNATIVE = createQNameString(NAMESPACE_DCTERMS, "alternative");
  public static final String PROP_AUDIENCE = createQNameString(NAMESPACE_DCTERMS, "audience");
  public static final String PROP_AVAILABLE = createQNameString(NAMESPACE_DCTERMS, "available");
  public static final String PROP_BIBLIOGRAPHICCITATION = createQNameString(NAMESPACE_DCTERMS, "bibliographicCitation");
  public static final String PROP_CONFORMSTO = createQNameString(NAMESPACE_DCTERMS, "conformsTo");
  public static final String PROP_CONTRIBUTOR = createQNameString(NAMESPACE_DCTERMS, "contributor");
  public static final String PROP_COVERAGE = createQNameString(NAMESPACE_DCTERMS, "coverage");
  public static final String PROP_CREATED = createQNameString(NAMESPACE_DCTERMS, "created");
  public static final String PROP_CREATOR = createQNameString(NAMESPACE_DCTERMS, "creator");
  public static final String PROP_DATE = createQNameString(NAMESPACE_DCTERMS, "date");
  public static final String PROP_DATEACCEPTED = createQNameString(NAMESPACE_DCTERMS, "dateAccepted");
  public static final String PROP_DATECOPYRIGHTED = createQNameString(NAMESPACE_DCTERMS, "dateCopyrighted");
  public static final String PROP_DATESUBMITTED = createQNameString(NAMESPACE_DCTERMS, "dateSubmitted");
  public static final String PROP_DESCRIPTION = createQNameString(NAMESPACE_DCTERMS, "description");
  public static final String PROP_EDUCATIONLEVEL = createQNameString(NAMESPACE_DCTERMS, "educationLevel");
  public static final String PROP_EXTENT = createQNameString(NAMESPACE_DCTERMS, "extent");
  public static final String PROP_FORMAT = createQNameString(NAMESPACE_DCTERMS, "format");
  public static final String PROP_HASFORMAT = createQNameString(NAMESPACE_DCTERMS, "hasFormat");
  public static final String PROP_HASPART = createQNameString(NAMESPACE_DCTERMS, "hasPart");
  public static final String PROP_HASVERSION = createQNameString(NAMESPACE_DCTERMS, "hasVersion");
  public static final String PROP_IDENTIFIER = createQNameString(NAMESPACE_DCTERMS, "identifier");
  public static final String PROP_INSTRUCTIONALMETHOD = createQNameString(NAMESPACE_DCTERMS, "instructionalMethod");
  public static final String PROP_ISFORMATOF = createQNameString(NAMESPACE_DCTERMS, "isFormatOf");
  public static final String PROP_ISPARTOF = createQNameString(NAMESPACE_DCTERMS, "isPartOf");
  public static final String PROP_ISREFERENCEDBY = createQNameString(NAMESPACE_DCTERMS, "isReferencedBy");
  public static final String PROP_ISREPLACEDBY = createQNameString(NAMESPACE_DCTERMS, "isReplacedBy");
  public static final String PROP_ISREQUIREDBY = createQNameString(NAMESPACE_DCTERMS, "isRequiredBy");
  public static final String PROP_ISSUED = createQNameString(NAMESPACE_DCTERMS, "issued");
  public static final String PROP_ISVERSIONOF = createQNameString(NAMESPACE_DCTERMS, "isVersionOf");
  public static final String PROP_LANGUAGE = createQNameString(NAMESPACE_DCTERMS, "language");
  public static final String PROP_LICENSE = createQNameString(NAMESPACE_DCTERMS, "license");
  public static final String PROP_MEDIATOR = createQNameString(NAMESPACE_DCTERMS, "mediator");
  public static final String PROP_MEDIUM = createQNameString(NAMESPACE_DCTERMS, "medium");
  public static final String PROP_MODIFIED = createQNameString(NAMESPACE_DCTERMS, "modified");
  public static final String PROP_PROVENANCE = createQNameString(NAMESPACE_DCTERMS, "provenance");
  public static final String PROP_PUBLISHER = createQNameString(NAMESPACE_DCTERMS, "publisher");
  public static final String PROP_REFERENCES = createQNameString(NAMESPACE_DCTERMS, "references");
  public static final String PROP_RELATION = createQNameString(NAMESPACE_DCTERMS, "relation");
  public static final String PROP_REPLACES = createQNameString(NAMESPACE_DCTERMS, "replaces");
  public static final String PROP_REQUIRES = createQNameString(NAMESPACE_DCTERMS, "requires");
  public static final String PROP_RIGHTS = createQNameString(NAMESPACE_DCTERMS, "rights");
  public static final String PROP_RIGHTSHOLDER = createQNameString(NAMESPACE_DCTERMS, "rightsHolder");
  public static final String PROP_SOURCE = createQNameString(NAMESPACE_DCTERMS, "source");
  public static final String PROP_SPATIAL = createQNameString(NAMESPACE_DCTERMS, "spatial");
  public static final String PROP_SUBJECT = createQNameString(NAMESPACE_DCTERMS, "subject");
  public static final String PROP_TABLEOFCONTENTS = createQNameString(NAMESPACE_DCTERMS, "tableOfContents");
  public static final String PROP_TEMPORAL = createQNameString(NAMESPACE_DCTERMS, "temporal");
  public static final String PROP_TITLE = createQNameString(NAMESPACE_DCTERMS, "title");
  public static final String PROP_TYPE = createQNameString(NAMESPACE_DCTERMS, "type");
  public static final String PROP_VALID = createQNameString(NAMESPACE_DCTERMS, "valid");
  public static final String PROP_SOURCERELIABILITY = createQNameString(NAMESPACE_DCTERMS, "sourceReliability");
  public static final String PROP_CONTENTVALIDITY = createQNameString(NAMESPACE_DCTERMS, "contentValidity");
}
