package gov.pnnl.velo.dataset.images;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.images.ResourceImageFactory;
import gov.pnnl.velo.dataset.DatasetsPlugin;
import gov.pnnl.velo.dataset.util.DatasetConstants;

public class DatasetImageFactory implements ResourceImageFactory {

  @Override
  public Image getImage(IResource resource, int size) {
    if (resource.hasAspect(DatasetConstants.ASPECT_DATASET)) {
      String doiState = resource.getPropertyAsString(DatasetConstants.PROP_DOI_STATE);

      if (doiState == null) {
        return DatasetsPlugin.getDefault().getImage(DatasetsPlugin.IMAGE_DATASET_NO_DOI, 16);
      } else if (doiState.equalsIgnoreCase(DatasetConstants.DOI_STATE_DRAFT)) {
        return DatasetsPlugin.getDefault().getImage(DatasetsPlugin.IMAGE_DATASET_DRAFT_DOI, 16);
      } else if (doiState.equalsIgnoreCase(DatasetConstants.DOI_STATE_FINAL)) {
        return DatasetsPlugin.getDefault().getImage(DatasetsPlugin.IMAGE_DATASET_FINAL_DOI, 16);
      }
      // in case we have another state in the future, return the no DOI icon
      return DatasetsPlugin.getDefault().getImage(DatasetsPlugin.IMAGE_DATASET_NO_DOI, 16);
    }
    return null;
  }

  @Override
  public ImageDescriptor getImageDescriptor(IResource resource, int size) {
    if (resource.hasAspect(DatasetConstants.ASPECT_DATASET)) {
      String doiState = resource.getPropertyAsString(DatasetConstants.PROP_DOI_STATE);

      if (doiState == null) {
        return DatasetsPlugin.getDefault().getImageDescriptor(DatasetsPlugin.IMAGE_DATASET_NO_DOI, 16);
      } else if (doiState.equalsIgnoreCase(DatasetConstants.DOI_STATE_DRAFT)) {
        return DatasetsPlugin.getDefault().getImageDescriptor(DatasetsPlugin.IMAGE_DATASET_DRAFT_DOI, 16);
      } else if (doiState.equalsIgnoreCase(DatasetConstants.DOI_STATE_FINAL)) {
        return DatasetsPlugin.getDefault().getImageDescriptor(DatasetsPlugin.IMAGE_DATASET_FINAL_DOI, 16);
      }
      // in case we have another state in the future, return the no DOI icon
      return DatasetsPlugin.getDefault().getImageDescriptor(DatasetsPlugin.IMAGE_DATASET_NO_DOI, 16);
    }
    return null;
  }

}
