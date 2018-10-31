package abc.dataentry;

import java.util.List;
import java.util.ArrayList;

public class DERow {

  protected List<String> items; // TODO change to DataItem?
  //int tmpsize = 3;
  int tmpsize = 6;

  public DERow() {
      items = new ArrayList();
      for (int idx=0; idx<tmpsize; idx++) {
        items.add("");
      }
  }

  public String getItem(int column) {
    return items.get(column-1);
  }
  public void setItem(int column, String value) {
    items.set(column-1,value);
  }

  public boolean isEmpty() {
      boolean flag = true;
      for (int idx=0; idx<tmpsize; idx++) {
        flag = flag && items.get(idx).trim().isEmpty();
      }
      return flag;
  }
}