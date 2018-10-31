/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.util;

import gov.pnnl.cat.policy.thumbnail.ThumbnailPolicy;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class ImageUtils {
  private static final Log LOG = LogFactory.getLog(ThumbnailPolicy.class);

  /**
   * Find the dimensions of the image represented by the given {@link NodeRef}.
   * 
   * @param nodeRef
   *          {@link NodeRef} of image
   * @param contentService
   *          {@link ContentService} instance to use to retrieve image
  
   * @return int array with width x height */
  public static int[] getDimensions(NodeRef nodeRef, ContentService contentService) {
    int[] dimensions = new int[] { 0, 0 };

    ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

    if (contentReader != null) {
      InputStream inputStream = contentReader.getContentInputStream();

      try {
        BufferedImage image = ImageIO.read(inputStream);

        if (image != null) {
          dimensions[0] = image.getWidth();
          dimensions[1] = image.getHeight();
        } else {
          LOG.warn("Unable to read thumbnail to determine width/height");
        }
      } catch (IOException e) {
        LOG.warn("Unable to read thumbnail to determine width/height", e);
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
    } else {
      LOG.warn("Unable to read thumbnail to determine width/height");
    }

    return dimensions;
  }

  /**
   * Set the width & height dimension properties on the given {@link NodeRef} representing an image.
   * 
   * @param nodeRef
   *          {@link NodeRef} of image
   * @param contentService
   *          {@link ContentService} instance to use to retrieve image
   * @param nodeService
   *          {@link NodeService} instance to use to set properties
   */
  public static void setDimensions(NodeRef nodeRef, ContentService contentService, NodeService nodeService) {
    int[] dimensions = getDimensions(nodeRef, contentService);
    setDimensions(nodeRef, dimensions, nodeService);
  }

  /**
   * Set the width & height dimension properties on the given {@link NodeRef} representing an image.
   * 
   * @param nodeRef
   *          {@link NodeRef} of image
   * @param dimensions
   *          int array with width x height
   * @param nodeService
   *          {@link NodeService} instance to use to set properties
   */
  public static void setDimensions(NodeRef nodeRef, int[] dimensions, NodeService nodeService) {
    nodeService.setProperty(nodeRef, CatConstants.PROP_THUMBNAIL_WIDTH, dimensions[0]);
    nodeService.setProperty(nodeRef, CatConstants.PROP_THUMBNAIL_HEIGHT, dimensions[1]);
  }

  /**
   * Cannot instantiate
   */
  private ImageUtils() {
    super();
  }
}
