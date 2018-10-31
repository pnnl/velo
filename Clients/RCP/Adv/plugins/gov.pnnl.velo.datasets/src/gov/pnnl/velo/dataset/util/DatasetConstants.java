package gov.pnnl.velo.dataset.util;

public class DatasetConstants {
  /** Namespace constants */
  public static final String NAMESPACE_DS = "http://www.pnl.gov/velo/model/dataset/1.0";

  /** Prefix constants */
  static final String DS_MODEL_PREFIX = "DS";

  /** Aspects */
  public static final String ASPECT_DATASET = createQNameString(NAMESPACE_DS, "dataset");
  public static final String ASPECT_DOI = createQNameString(NAMESPACE_DS, "doi");

  /** General Properties */
  public static final String PROP_OSTI_ID = createQNameString(NAMESPACE_DS, "osti_id");
  public static final String PROP_DOI = createQNameString(NAMESPACE_DS, "doi");
  public static final String PROP_DOI_STATE = createQNameString(NAMESPACE_DS, "state");

  public static final String DOI_STATE_FINAL = "final";
  public static final String DOI_STATE_DRAFT = "draft";

  public static String createQNameString(String namespace, String name) {
    return "{" + namespace + "}" + name;
  }

  public static final String PATH_AUTHOR_FIRSTNAME = "/Dataset/CitationInformation/Authors/FirstName";
  public static final String PATH_AUTHOR_MIDDLENAME = "/Dataset/CitationInformation/Authors/MiddleName";
  public static final String PATH_AUTHOR_LASTNAME = "/Dataset/CitationInformation/Authors/LastName";

}
