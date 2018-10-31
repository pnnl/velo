package com.codebeach.ui;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.velo.util.VeloConstants;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

public class ImageFlowItem
{
    private String id;
    private BufferedImage image = null;
    private String label;
    private String parentId;
    private Long width;
    private Long height;

    public ImageFlowItem(String parentId, String id, String label, Long width, Long height)
    {
      this.parentId = parentId;
      this.id = id;
      this.label = label;
      this.width = width;
      this.height = height;
    }

    public synchronized void loadImage()
    {
      if (image == null) {
        InputStream inputStream = null;
        
        try {
          inputStream = ResourcesPlugin.getDefault().getResourceManager().getContentProperty(getId(), VeloConstants.PROP_CONTENT);
          this.image = ImageIO.read(inputStream);
        } catch (IOException e) {
          throw new RuntimeException("Unable to load image", e);
        } finally {
          IOUtils.closeQuietly(inputStream);
        }
      } 
    }
    
    public synchronized void unloadImage() 
    {
      this.image = null; 
    }

    public Image getImage()
    {
      return image;
    }
    
    public String getId() 
    {
      return id;
    }
    
    public String getLabel()
    {
        return label;
    }

    public String getParentId() 
    {
      return parentId;
    }

    public Long getWidth() 
    {
      return width;
    }

    public Long getHeight() 
    {
      return height;
    }

}
