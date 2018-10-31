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
package gov.pnnl.velo.ui.views.summary.sections;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.statushandlers.StatusManager;

import com.centerkey.utils.BareBonesBrowserLaunch;
import com.thoughtworks.xstream.io.path.Path;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.util.WebServiceUrlUtility;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Relationship;
import gov.pnnl.velo.tif.service.CmsService;
import gov.pnnl.velo.ui.util.FormLayoutFactory;
import gov.pnnl.velo.ui.views.SummaryView;
import gov.pnnl.velo.ui.views.SummaryViewSectionProvider;
import gov.pnnl.velo.util.VeloConstants;

/**
 */
public class GeneralSectionProvider implements SummaryViewSectionProvider {
  
  /**
   * Method createSection.
   * @param view SummaryView
   * @param resource IResource
   * @param wikiContextPath Path
   * @param localWorkingDir File
   * @param oascisService CmsService
   * @see gov.pnnl.velo.ui.views.SummaryViewSectionProvider#createSection(SummaryView, IResource, Path, File, CmsService)
   */
  @Override
  public boolean createSummarySection(SummaryView view, IResource selectedResource) {
    FormToolkit toolkit = view.getToolkit();
    Composite generalInfo = view.createSWTSection("General Information: ", "", 2);
    FontData fontData = generalInfo.getFont().getFontData()[0];
    
    Font boldFont = JFaceResources.getFontRegistry().getBold(fontData.getName());

    //name
    String name = selectedResource.getName();
    FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Name", boldFont);
    FormLayoutFactory.createPropertyValue(toolkit, generalInfo, name);
    FormLayoutFactory.createSpacerRow(toolkit, generalInfo);
    
    // description
    String description = selectedResource.getPropertyAsString(VeloConstants.PROP_DESCRIPTION);
    if(description != null) {
      FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Description", boldFont);
      FormLayoutFactory.createPropertyValue(toolkit, generalInfo, description);
      FormLayoutFactory.createSpacerRow(toolkit, generalInfo);      
    }

    //path
    FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Path", boldFont);
    FormLayoutFactory.createPropertyValue(toolkit, generalInfo, selectedResource.getPath().toDisplayString());
    FormLayoutFactory.createSpacerRow(toolkit, generalInfo); 
    
    // target path
    if(selectedResource instanceof ILinkedResource) {
      ILinkedResource link = (ILinkedResource)selectedResource;
      String targetPath = link.getTarget().getPath().toDisplayString();
      FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Target Path", boldFont);
      FormLayoutFactory.createPropertyValue(toolkit, generalInfo, targetPath);
      FormLayoutFactory.createSpacerRow(toolkit, generalInfo); 
    }

    //web view URL (currently only used for datasets)
    final String url = selectedResource.getPropertyAsString(VeloConstants.PROP_WEB_VIEW_URL);
    if(url != null) {
      FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Web View URL", boldFont);
      Link label = new Link(generalInfo, SWT.NONE | SWT.WRAP);
      label.setText("<a>"+url+"</a>");
      label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
      label.addSelectionListener(new SelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          BareBonesBrowserLaunch.openURL(url);
        }
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
      });
      FormLayoutFactory.createSpacerRow(toolkit, generalInfo);
    }
    
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    String baseUrl = mgr.getRepositoryUrlBase();

    //webdav URL (looks like http://localhost:8082/alfresco/webdav/User%20Documents/admin/blankSet1)
    FormLayoutFactory.createPropertyName(toolkit, generalInfo, "WebDAV URL", boldFont);
    final String webdavUrl = baseUrl + "/webdav" + WebServiceUrlUtility.encodeParameter(selectedResource.getPath().toDisplayString());
    Link label2 = new Link(generalInfo, SWT.NONE | SWT.WRAP);
    label2.setText("<a>"+webdavUrl + "</a>");
    label2.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    label2.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        BareBonesBrowserLaunch.openURL(webdavUrl);
      }
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
      }
    });
    
    FormLayoutFactory.createSpacerRow(toolkit, generalInfo);

    // uuid
    String uuid = selectedResource.getPropertyAsString(VeloConstants.PROP_UUID);
    if(uuid != null) {
      FormLayoutFactory.createPropertyName(toolkit, generalInfo, "UUID", boldFont);
      FormLayoutFactory.createPropertyValue(toolkit, generalInfo, uuid);
      FormLayoutFactory.createSpacerRow(toolkit, generalInfo);      
    }
    
    try {
      //creation date
      Calendar creationDate = selectedResource.getPropertyAsDate(VeloConstants.PROP_CREATED);
      if(creationDate != null) {
        FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Created On", boldFont);
        FormLayoutFactory.createPropertyValue(toolkit, generalInfo, SummaryView.DATE_FORMAT.format(creationDate.getTime()));
        FormLayoutFactory.createSpacerRow(toolkit, generalInfo);      
      }

      //creator
      String creator = selectedResource.getPropertyAsString(VeloConstants.PROP_CREATOR);
      if(creator != null) {
        FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Created By", boldFont);
        FormLayoutFactory.createPropertyValue(toolkit, generalInfo, creator);
        FormLayoutFactory.createSpacerRow(toolkit, generalInfo);      
      }      

      //created on behalf of
      String createdOnBehalfOf = selectedResource.getPropertyAsString(VeloConstants.PROP_CREATED_ON_BEHALF_OF);
      if(createdOnBehalfOf != null) {
        FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Created On Behalf Of", boldFont);
        FormLayoutFactory.createPropertyValue(toolkit, generalInfo, createdOnBehalfOf);
        FormLayoutFactory.createSpacerRow(toolkit, generalInfo);      
      }      

      // modified date
      Calendar modifiedDate = selectedResource.getPropertyAsDate(VeloConstants.PROP_MODIFIED);
      if(modifiedDate != null) {
        FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Modified On", boldFont);
        FormLayoutFactory.createPropertyValue(toolkit, generalInfo, SummaryView.DATE_FORMAT.format(modifiedDate.getTime()));
        FormLayoutFactory.createSpacerRow(toolkit, generalInfo);      
      }      

      // modifier
      String modifier = selectedResource.getPropertyAsString(VeloConstants.PROP_MODIFIER);
      if(modifier != null) {
        FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Modified By", boldFont);
        FormLayoutFactory.createPropertyValue(toolkit, generalInfo, modifier);
        FormLayoutFactory.createSpacerRow(toolkit, generalInfo);      
      }   
      
      //modified on behalf of
      String modifiedOnBehalfOf = selectedResource.getPropertyAsString(VeloConstants.PROP_MODIFIED_ON_BEHALF_OF);
      if(modifiedOnBehalfOf != null) {
        FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Modified On Behalf Of", boldFont);
        FormLayoutFactory.createPropertyValue(toolkit, generalInfo, modifiedOnBehalfOf);
        FormLayoutFactory.createSpacerRow(toolkit, generalInfo);      
      }
      
      // Copied From
      // TODO: relationships need to be cached!!
      // {http://www.alfresco.org/model/content/1.0}original association
      List<Relationship> relationships = ResourcesPlugin.getResourceManager().getRelationships(selectedResource.getPath());
      String copiedFromPathStr = null;
      String copiedFromUuid = null;
      
      for(Relationship relationship : relationships) {
        if(relationship.getRelationshipType().equals(VeloConstants.ASSOC_TYPE_ORIGINAL)) {
          if(relationship.getSourceResourceUuid().equals(uuid)) {
            copiedFromPathStr = relationship.getDestinationPath();
            copiedFromUuid = relationship.getDestinationResourceUuid();
          }
        }
      }

      if(copiedFromUuid != null) {
        FormLayoutFactory.createPropertyName(toolkit, generalInfo, "Copied From", boldFont);

        if(copiedFromPathStr != null) {
          // add link to the copied from reference
          final CmsPath copiedFromPath = new CmsPath(copiedFromPathStr);

          //final IResource copiedFrom = ResourcesPlugin.getResourceManager().getResource(copiedFromPath);

            Hyperlink link = toolkit.createHyperlink(generalInfo, copiedFromPath.toDisplayString(), SWT.NONE);
            link.setToolTipText("Navigate to resource: " + copiedFromPath.toDisplayString() );
            link.addHyperlinkListener(new HyperlinkAdapter() {
              public void linkActivated(HyperlinkEvent e) {
                // Call the open tool action
                IResource resource = ResourcesPlugin.getResourceManager().getResource(copiedFromPath);
                if(resource != null) {
                  RCPUtil.selectResourceInTree(resource);                  
                } else {
                  // let user know they don't have permissions to see this resource
                  StatusUtil.handleStatus("You do not have permissions to view " + copiedFromPath.toDisplayString(), StatusManager.SHOW);
                }
              }
            });
        
        } else {
          FormLayoutFactory.createPropertyValue(toolkit, generalInfo, "Resource no longer exists.");          
        }
        FormLayoutFactory.createSpacerRow(toolkit, generalInfo);     
      }

    } catch (Throwable e) {
      e.printStackTrace();
    }

    return true;
  }

  @Override
  public int compareTo(SummaryViewSectionProvider o) {
    // This one should always go last
    return 1;
  }
  
}
