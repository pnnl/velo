package gov.pnnl.velo.sapphire.osti;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.Value;

import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.util.AbstractWebScriptClient;
import gov.pnnl.cat.core.util.ProxyConfig;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.dataset.util.DatasetConstants;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.sapphire.CountryPossibleValuesService;
import gov.pnnl.velo.sapphire.dataset.Author;
import gov.pnnl.velo.sapphire.dataset.DatasetMetadata;
import gov.pnnl.velo.sapphire.dataset.RequiredSoftware;
import gov.pnnl.velo.util.VeloConstants;

public class OSTIPublicationService extends AbstractWebScriptClient {

  private static final String serverUrl = "https://www.osti.gov/elinktest/2416api";
  // private static final String serverUrl = "https://www.osti.gov/elink/2416api";
  private static OSTIPublicationService instance = new OSTIPublicationService();
  private String responseString;
  private String status;

  private OSTIPublicationService() {
    super(serverUrl, new ProxyConfig(serverUrl));
  }

  public static OSTIPublicationService getInstance() {
    return instance;
  }

  public String publishToOSTI(CmsPath datasetPath, DatasetMetadata metadata, boolean isFinal) {
    return publishToOSTI(datasetPath, null, metadata, isFinal);
  }

  public String publishToOSTI(CmsPath datasetPath, String datasetSize, DatasetMetadata metadata, boolean isFinal) {
    // 1) convert form data object to xml
    String xml = createOSTIXml(datasetPath, datasetSize, metadata, isFinal);
    //BUG in OSTI test server. For now add author name as Jannean Elliott and orcid as below
    //xml = xml.replace("<orcid_id></orcid_id>","<orcid_id>0000-0002-9962-3894</orcid_id>");
     xml = xml.replace("https://sbrsfa.velo.pnnl.gov:443/datasets?UUID=8fccf487-604a-4aa0-924a-0015ea22000b", "http://nsidc.org/data/g01130");
    System.out.println(xml);

    // 2) invoke web service

    CloseableHttpResponse response = null;
    try {

      HttpPost httppost = new HttpPost(serverUrl);
      StringEntity reqEntity = new StringEntity(xml);
      httppost.setEntity(reqEntity);
      //response = executeMethod(httpClient, httppost, "repos2416websvs", "Hm#sB84ie");
      response = executeMethod(httpClient, httppost, "msdp2416websvs", "Hm#54nintt");
      responseString = getResponseBodyAsString(response);
      System.out.println(responseString);
      EntityUtils.consumeQuietly(response.getEntity());

    } catch (Throwable e) {
      handleException("Failed to execute method.", e);

    } finally {
      closeQuietly(response);
    }

    // 3) parse web service response
    System.out.println("Response string:::::\n" + responseString);
    String statusSubstring = responseString.split("<status>")[1];
    status = statusSubstring.split("</status>")[0];
    String failureMsg = null;
    try {
      if (status.equalsIgnoreCase("failure")) {
        String failureMsgSub = responseString.split("<status_message>")[1];
        failureMsg = failureMsgSub.split("</status_message>")[0];
        throw new Exception();

      } else if (status.equalsIgnoreCase("success")) {
        // 4) set all metadata
        IResourceManager mgr = ResourcesPlugin.getResourceManager();
        Map<String, String> propertyMap = new HashMap<String, String>();
        
        if(mgr.getProperty(datasetPath, DatasetConstants.PROP_OSTI_ID)==null){
          //format <doi status="RESERVED">doinumber</doi>
          int index = responseString.indexOf("<doi");
          int doiBeginIndex = responseString.indexOf(">",index+4)+1;
          int doiEndIndex=responseString.indexOf("</doi>");
          String doi = responseString.substring(doiBeginIndex,doiEndIndex);
          
          System.out.println("DOI:" + doi);

          String idSubstring = responseString.split("<osti_id>")[1];
          String ostiId = idSubstring.split("</osti_id>")[0];
          System.out.println(ostiId);

          // GET the OSTI ID - save this for future get or edit requests <osti_id>

          propertyMap.put(DatasetConstants.PROP_DOI, doi);
          propertyMap.put(DatasetConstants.PROP_OSTI_ID, ostiId);
        }
        if (isFinal) {
          propertyMap.put(DatasetConstants.PROP_DOI_STATE, DatasetConstants.DOI_STATE_FINAL);
        } else {
          propertyMap.put(DatasetConstants.PROP_DOI_STATE, DatasetConstants.DOI_STATE_DRAFT);
          
        }
        mgr.setProperties(datasetPath, propertyMap);

      }

    } catch (Exception e) {

      ToolErrorHandler.handleError("Failed publishing dataset. " + failureMsg, e, true);
    }

    return status;

  }

  private String createOSTIXml(CmsPath datasetPath, String datasetSize, DatasetMetadata metadata, boolean isFinal) {
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    StringBuilder sb = new StringBuilder(400);

    String defaultValues = "<dataset_type>SM</dataset_type>\n";
    
    sb.append("<?xml version=\"1.0\" ?>\n<records>\n<record>");
    sb.append(defaultValues);

    // contact_name
    // TODO: should probably be just hard coded to Tommian
    addElement(sb, "contact_name", "Chandrika Sivaramakrishnan");
    addElement(sb, "contact_org", "Pacific Northwest National Laboratory");
    addElement(sb, "contact_email", "chandrika@pnnl.gov");
    addElement(sb, "contact_phone", "5093726032");

    // Setting it to DOI for now
    // TODO: use ERICA id once integrated with ERICA
    addElement(sb, "product_nos", mgr.getUUID(datasetPath));
    if (isFinal) {
      addElement(sb, "site_url", mgr.getProperty(datasetPath, VeloConstants.PROP_WEB_VIEW_URL));
    } else {
      sb.append("<set_reserved/>\n");
      addElement(sb, "site_url", "");
    }

    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    addElement(sb, "publication_date", sdf.format(new Date()));

    // optional fields
    List<String> propKeys = Arrays.asList(DatasetConstants.PROP_OSTI_ID, DatasetConstants.PROP_DOI);
    List<String> propValues = mgr.getPropertiesAsString(datasetPath, propKeys);
    if (propValues.get(0) != null) {
      addElement(sb, "osti_id", propValues.get(0));
      addElement(sb, "doi", propValues.get(1));
    }

    // TODO: <doi_infix> - is there a project or pnnl specific doi infix ?

    if (datasetSize != null) {
      // <dataset_size/>
      addElement(sb, "dataset_size", datasetSize);
    }

    // Now get fields by parsing sapphire tree
    parseSapphireTree(sb, metadata);

    sb.append("</record>\n</records>");
    return sb.toString();
  }

  private void parseSapphireTree(StringBuilder sb, DatasetMetadata metadata) {

    addElement(sb, "title", metadata.getDataset().getCitationInformation().getTitle());

    addAuthors(sb, metadata.getDataset().getCitationInformation().getAuthors());

    // DOE contracts
    OSTI ostiMetadata = metadata.getOSTIMetadata();
    ElementList<DOEContractNumber> doeContracts = ostiMetadata.getDOEContracts();
    String fieldValue = "";
    // Use for loop to preserve order
    for (int i = 0; i < doeContracts.size(); i++) {
      DOEContractNumber doeContractNumber = doeContracts.get(i);
      if (doeContractNumber.getDOEContract().content() != null)
        fieldValue = fieldValue + doeContractNumber.getDOEContract().text() + "; ";
    }
    if (!fieldValue.isEmpty()) {
      fieldValue = fieldValue.substring(0, fieldValue.length() - 2);
      addElement(sb, "contract_nos", fieldValue);
    }
    // NON DOE contract numbers - <othnondoe_contract_nos>
    ElementList<NonDOEContractNumber> nonDOEContracts = ostiMetadata.getNonDOEContracts();
    fieldValue = "";
    // Use for loop to preserve order
    for (int i = 0; i < nonDOEContracts.size(); i++) {
      NonDOEContractNumber element = nonDOEContracts.get(i);
      if (element.getNonDOEContract().content() != null)
        fieldValue = fieldValue + element.getNonDOEContract().text() + "; ";
    }
    if (!fieldValue.isEmpty()) {
      fieldValue = fieldValue.substring(0, fieldValue.length() - 2);
      addElement(sb, "othnondoe_contract_nos", fieldValue);
    }

    // Originating research Organization
    ElementList<ResearchOrganization> researchOrganizations = ostiMetadata.getResearchOrganizations();
    fieldValue = "";
    // Use for loop to preserve order
    for (int i = 0; i < researchOrganizations.size(); i++) {
      ResearchOrganization element = researchOrganizations.get(i);
      if (element.getResearchOrganization().content() != null)
        fieldValue = fieldValue + element.getResearchOrganization().text() + "; ";
    }
    if (!fieldValue.isEmpty()) {
      fieldValue = fieldValue.substring(0, fieldValue.length() - 2);
      addElement(sb, "originating_research_org", fieldValue);
    }

    addElement(sb, "language", ostiMetadata.getLanguage());
    
    Value<String> country = ostiMetadata.getCountry();
    addElement(sb, "country", CountryPossibleValuesService.getCodeByCountry(country.text()));
    

    // sponsor Organization
    // Use for loop to preserve order
    ElementList<SponsorOrganization> sponsorOrgs = ostiMetadata.getSponsorOrgs();
    fieldValue = "";
    for (int i = 0; i < sponsorOrgs.size(); i++) {
      SponsorOrganization element = sponsorOrgs.get(i);
      if (element.getSponsorOrg().content() != null)
        fieldValue = fieldValue + element.getSponsorOrg().text() + "; ";
    }
    if (!fieldValue.isEmpty()) {
      fieldValue = fieldValue.substring(0, fieldValue.length() - 2);
      addElement(sb, "sponsor_org", fieldValue);
    }

    // Optional fields
    // keywords
    if (metadata.getDataset().getCitationInformation().getTags().content() != null) {
      fieldValue = metadata.getDataset().getCitationInformation().getTags().text().replaceAll("\\s*,\\s*", "; ");
      addElement(sb, "keywords", fieldValue);
    }
    // description
    if (metadata.getDataset().getCitationInformation().getDescription().content() != null)
      addElement(sb, "description", metadata.getDataset().getCitationInformation().getDescription().text());

    // <software_needed/>
    ElementList<RequiredSoftware> requiredSoftwares = metadata.getDataset().getDataAccess().getRequiredSoftwares();
    fieldValue = "";
    for (RequiredSoftware software : requiredSoftwares) {
      if (software.getURL().content() != null) {
        fieldValue = fieldValue + software.getURL().text() + "; ";
      }
    }
    if (!fieldValue.isEmpty()) {
      fieldValue = fieldValue.substring(0, fieldValue.length() - 2);
      addElement(sb, "software_needed", fieldValue);
    }
  }

  private void addAuthors(StringBuilder sb, ElementList<Author> authors) {
    // Add authors
    sb.append("<creatorsblock>\n");
    // add Author
    for (Author author : authors) {
      sb.append("<creators_detail>\n");
      addElement(sb, "first_name", author.getFirstName(), true);
      addElement(sb, "middle_name", author.getMiddleName(), true);
      addElement(sb, "last_name", author.getLastName(), true);
      addElement(sb, "affiliation", author.getInstitution(), true);
      addElement(sb, "private_email", author.getEMail(), true);
      addElement(sb, "orcid_id", author.getORCID(), true);
      sb.append("</creators_detail>\n");
    }
    sb.append("</creatorsblock>\n");
  }

  private void addElement(StringBuilder sb, String fieldName, Value<String> valueObject) {
    addElement(sb, fieldName, valueObject, false);
  }

  private void addElement(StringBuilder sb, String fieldName, Value<String> valueObject, boolean indent) {

    if (valueObject.content() == null)
      return;
    addElement(sb, fieldName, valueObject.text(), indent);
  }

  private void addElement(StringBuilder sb, String fieldName, String value) {
    if(value==null)
      return;
    addElement(sb, fieldName, value, false);
  }

  private void addElement(StringBuilder sb, String fieldName, String value, boolean indent) {
    if (indent)
      sb.append("  ");
    sb.append("<");
    sb.append(fieldName);
    sb.append(">");
    sb.append(value);
    sb.append("</");
    sb.append(fieldName);
    sb.append(">");
    sb.append("\n");
  }

}
