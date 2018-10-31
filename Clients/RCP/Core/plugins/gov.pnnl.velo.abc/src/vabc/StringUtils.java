package vabc;

import abc.containers.ABCComponent;
import datamodel.DataItem;
import datamodel.collections.RecursiveDataItemMap;

public class StringUtils {

  public static String collectionToHTML(ABCComponent parent, RecursiveDataItemMap map, IABCDataProvider dataProvider, String space) {
    
    StringBuffer text = new StringBuffer();
    
    for(DataItem item: map.getCollection()) {
      String linkedObject = item.getLinkedObject() != null ? dataProvider.getLabel(item.getLinkedObject()) : null;
      if(linkedObject != null && linkedObject.equals(item.getAlias()))
        continue;
      if(parent.rIsActive(item.getKey()))
          text.append(space + item.getHTMLView(item.getLinkedObject() != null ? dataProvider.getLabel(item.getLinkedObject()) : null));
    }

    for(DataItem item: map.keySet()) {
      String linkedObject = item.getLinkedObject() != null ? dataProvider.getLabel(item.getLinkedObject()) : null;
      text.append(space + item.getHTMLView(linkedObject));
      text.append(collectionToHTML(parent, map.get(item), dataProvider, space + "&nbsp&nbsp"));
    }

    return text.toString();
  }
	
	public static boolean equals(String a, String b) {
		if(a == null && b == null)
			return true;
		if(a != null && b != null)
			return a.equals(b);
		return false;		
	}
}
