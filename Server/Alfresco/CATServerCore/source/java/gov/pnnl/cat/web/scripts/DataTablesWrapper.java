package gov.pnnl.cat.web.scripts;

import java.util.List;

class DataTablesWrapper{
    private int draw;
    private int recordsTotal; 
    private List<DataTablesResource> data;

    //not used currently but adding for possible use in future
    private int recordsFiltered;
    private String error;
    
    public DataTablesWrapper(){
      
    }
    
    public DataTablesWrapper(int draw, int recordsTotal, List<DataTablesResource> data) {
      this.draw = draw;
      this.recordsTotal = recordsTotal;
      this.data = data;
    }
    public int getDraw() {
      return draw;
    }
    public void setDraw(int draw) {
      this.draw = draw;
    }
    public int getRecordsTotal() {
      return recordsTotal;
    }
    public void setRecordsTotal(int recordsTotal) {
      this.recordsTotal = recordsTotal;
    }
    public List<DataTablesResource> getData() {
      return data;
    }
    public void setData(List<DataTablesResource> data) {
      this.data = data;
    }
    public int getRecordsFiltered() {
      return recordsFiltered;
    }
    public void setRecordsFiltered(int recordsFiltered) {
      this.recordsFiltered = recordsFiltered;
    }
    public String getError() {
      return error;
    }
    public void setError(String error) {
      this.error = error;
    }
    
    
  }
