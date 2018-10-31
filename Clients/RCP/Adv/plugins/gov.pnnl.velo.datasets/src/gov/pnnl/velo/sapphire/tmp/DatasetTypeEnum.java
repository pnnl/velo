package gov.pnnl.velo.sapphire.tmp;

import org.eclipse.sapphire.modeling.annotations.Label;

@Label(standard = "Dataset Type")

public enum DatasetTypeEnum {
  @Label(standard = "Experiment")

  EXPERIMENT,

  @Label(standard = "Simulation")

  SIMULATION,

  @Label(standard = "Observation")

  OBSERVATION,

  @Label(standard = "Analysis")

  ANALYSIS,

  @Label(standard = "Software")

  SOFTWARE,

  @Label(standard = "Dataset Publication Package")

  DATASET_PUBLICATION_PACKAGE

}
