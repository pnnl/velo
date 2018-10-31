package gov.pnnl.velo.tif.ui;

import gov.pnnl.velo.tif.model.JobConfig;

import java.util.List;

import javax.swing.JPanel;


public abstract class CustomCodeParametersUI {
  
  public abstract void resetLayout(JPanel parentCodeSection, int startingRow);
  public abstract void loadState(JobConfig config);
  public abstract void pushStateToDataModel(JobConfig config);
  public abstract void validateUserInputs(List<String> errors);

}
