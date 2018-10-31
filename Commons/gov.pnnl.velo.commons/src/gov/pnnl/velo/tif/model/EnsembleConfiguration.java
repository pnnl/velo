package gov.pnnl.velo.tif.model;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ensembleConfiguration")
public class EnsembleConfiguration{
  private List<Fileset> outputs;
  private boolean recordRuns = true;
  protected String runDirPrefix = "";

  public EnsembleConfiguration() {}
  
  public EnsembleConfiguration(List<Fileset> outputs) {
    this.outputs = outputs;
  }

  /**
   * @return the outputs
   */
  public List<Fileset> getOutputs() {
    return outputs;
  }

  /**
   * @param outputs the outputs to set
   */
  public void setOutputs(List<Fileset> outputs) {
    this.outputs = outputs;
  }

  /**
   * @return the recordRuns
   */
  public boolean isRecordRuns() {
    return recordRuns;
  }

  public String getRunDirPrefix() {
    return runDirPrefix;
  }

  public void setRunDirPrefix(String runDirPrefix) {
    this.runDirPrefix = runDirPrefix;
  }

  /**
   * @param recordRuns the recordRuns to set
   */
  public void setRecordRuns(boolean recordRuns) {
    this.recordRuns = recordRuns;
  }

  public void merge(EnsembleConfiguration parentEnsembleConfiguration) {
    
    if(outputs==null || outputs.isEmpty()) {
      setOutputs(parentEnsembleConfiguration.getOutputs());
      
    } else {
      for(Fileset output : parentEnsembleConfiguration.getOutputs()) {
        // only add parent output if it is not already in my list
        if(!outputs.contains(output)) {
          outputs.add(output);
        }
      }
    }
    
    
  }
  
}