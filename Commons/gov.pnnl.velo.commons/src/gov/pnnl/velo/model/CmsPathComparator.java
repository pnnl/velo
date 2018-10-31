package gov.pnnl.velo.model;

import java.util.Comparator;

public class CmsPathComparator implements Comparator<CmsPath> {
    public int compare(CmsPath a, CmsPath b) {
      int difference = a.getSegments().size() - b.getSegments().size();
      return difference == 0 ? 1 : difference < 0 ? difference : difference + 1;    
    }
  }