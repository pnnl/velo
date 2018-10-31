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
package gov.pnnl.cat.search.ui;

import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchMessages;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPageDescriptor;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPluginImages;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IPluginContribution;
import org.osgi.framework.Bundle;

/**
 * @see gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPageDescriptor
 * @author Eric Marshall
 * @version $Revision: 1.0 $
 */
public class CatSearchPageDescriptor implements IPluginContribution, Comparable {

  public final static String PAGE_TAG= "page"; 
  private final static String ID_ATTRIBUTE= "id";
  private final static String ICON_ATTRIBUTE= "icon"; 
  private final static String CLASS_ATTRIBUTE= "class"; 
  private final static String LABEL_ATTRIBUTE= "label"; 
  private final static String MENU_POSITION_ATTRIBUTE= "menuPosition"; 
  
  public final static Point UNKNOWN_SIZE= new Point(SWT.DEFAULT, SWT.DEFAULT);

  // dialog store id constants
  private final static String SECTION_ID= "Search";
  private final static String STORE_ENABLED_PAGE_IDS= SECTION_ID + ".enabledPageIds"; 
  private final static String STORE_PROCESSED_PAGE_IDS= SECTION_ID + ".processedPageIds"; 
  
  private static List fgEnabledPageIds;
  
//  private static class ExtensionScorePair {
//    public String extension;
//    public int score;
//    public ExtensionScorePair(String extension, int score) {
//      this.extension= extension;
//      this.score= score;
//    }
//  }

  private IConfigurationElement fElement;
//  private List fExtensionScorePairs;
//  private int fWildcardScore= ISearchPageScoreComputer.UNKNOWN;
  private ISearchPage fCreatedPage;
  
  /**
   * Creates a new search page node with the given configuration element.
   * @param element The configuration element
   */
  public CatSearchPageDescriptor(IConfigurationElement element) {
    fElement= element;
  }

  /**
   * Creates a new search page from this node.
  
  
   * @return the created page or null if the creation failed * @throws CoreException Page creation failed */
  public ISearchPage createObject() throws CoreException {
    if (fCreatedPage == null) {
      fCreatedPage= (ISearchPage) fElement.createExecutableExtension(CLASS_ATTRIBUTE);
      //fCreatedPage.setTitle(getLabel());
      //fCreatedPage.setContainer(parent);
    }
    return fCreatedPage;
  }
  
  /**
   * Method getPage.
   * @return ISearchPage
   */
  public ISearchPage getPage() {
    return fCreatedPage;
  }
  
  
  public void dispose() {
    if (fCreatedPage != null) {
      fCreatedPage.dispose();
      fCreatedPage= null;
    }
  }
  
  //---- XML Attribute accessors ---------------------------------------------
  
  /**
   * Returns the page's id.
  
   * @return The id of the page */
  public String getId() {
    return fElement.getAttribute(ID_ATTRIBUTE);
  }
   
  /**
   * Returns the page's image
  
   * @return ImageDescriptor of the image or null if creating failed */
  public ImageDescriptor getImage() {
    String imageName= fElement.getAttribute(ICON_ATTRIBUTE);
    if (imageName == null)
      return null;
    Bundle bundle = Platform.getBundle(getPluginId());
    return SearchPluginImages.createImageDescriptor(bundle, new Path(imageName), true);
  }

  /**
  
   * @return Returns the page's label. */
  public String getLabel() {
    return fElement.getAttribute(LABEL_ATTRIBUTE);
  }
  
  /**
   * Returns the page's tab position relative to the other tabs.
  
   * @return  the tab position or <code>Integer.MAX_VALUE</code> if not defined in
   *      the plugins.xml file */
  public int getMenuPosition() {
    int position= Integer.MAX_VALUE / 2;
    String str= fElement.getAttribute(MENU_POSITION_ATTRIBUTE);
    if (str != null)
      try {
        position= Integer.parseInt(str);
    } catch (NumberFormatException ex) {
      ToolErrorHandler.handleError(SearchMessages.Search_Error_createSearchPage_message, ex, true);
    }
    return position;
  }

  /**
   * Method isEnabled.
   * @return boolean
   */
  public boolean isEnabled() {
    return getEnabledPageIds().contains(getId());
  }

//  /**
//   * Returns the help context for help shown in search view.
//   * 
//   * @return the help context id or <code>null</code> if not defined
//   */
//  public String getSearchViewHelpContextId() {
//    return fElement.getAttribute(SEARCH_VIEW_HELP_CONTEXT_ID_ATTRIBUTE);
//  }

  /**
   * Method setEnabled.
   * @param enabledDescriptors Object[]
   */
  static void setEnabled(Object[] enabledDescriptors) {
    fgEnabledPageIds= new ArrayList(5);
    for (int i= 0; i < enabledDescriptors.length; i++) {
      if (enabledDescriptors[i] instanceof SearchPageDescriptor)
        fgEnabledPageIds.add(((SearchPageDescriptor)enabledDescriptors[i]).getId());
    }
    storeEnabledPageIds();
  }

  /**
   * Method getEnabledPageIds.
   * @return List
   */
  private static List getEnabledPageIds() {
    if (fgEnabledPageIds == null) {
      List descriptors= CatRcpPlugin.getDefault().getSearchPlugin().getSearchPageDescriptors();
      
      String[] enabledPageIds= getDialogSettings().getArray(STORE_ENABLED_PAGE_IDS);
      if (enabledPageIds == null)
        fgEnabledPageIds= new ArrayList(descriptors.size());
      else
        fgEnabledPageIds= new ArrayList(Arrays.asList(enabledPageIds));
      

      List processedPageIds;
      String[] processedPageIdsArr= getDialogSettings().getArray(STORE_PROCESSED_PAGE_IDS);
      if (processedPageIdsArr == null)
        processedPageIds= new ArrayList(descriptors.size());
      else
        processedPageIds= new ArrayList(Arrays.asList(processedPageIdsArr));
      
      // Enable pages based on contribution
      Iterator iter= descriptors.iterator();
      while (iter.hasNext()) {
        SearchPageDescriptor desc= (SearchPageDescriptor)iter.next();
        if (processedPageIds.contains(desc.getId()))
          continue;
        
        processedPageIds.add(desc.getId());
        if (desc.isInitiallyEnabled())
          fgEnabledPageIds.add(desc.getId());
      }

      getDialogSettings().put(STORE_PROCESSED_PAGE_IDS, (String[])processedPageIds.toArray(new String[processedPageIds.size()]));
      storeEnabledPageIds();
    }
    return fgEnabledPageIds;
  }

  private static void storeEnabledPageIds() {
    getDialogSettings().put(STORE_ENABLED_PAGE_IDS, (String[])fgEnabledPageIds.toArray(new String[fgEnabledPageIds.size()]));
    CatRcpPlugin.getDefault().savePluginPreferences();
  }

  /**
   * Method getDialogSettings.
   * @return IDialogSettings
   */
  private static IDialogSettings getDialogSettings() {
    IDialogSettings settings= CatRcpPlugin.getDefault().getDialogSettings();
    IDialogSettings section= settings.getSection(SECTION_ID);
    if (section == null)
      // create new section
      section= settings.addNewSection(SECTION_ID);
    return section;
  }

  /* 
   * Implements a method from IComparable 
   */ 
  /**
   * Method compareTo.
   * @param o Object
   * @return int
   */
  public int compareTo(Object o) {
    int myPos= getMenuPosition();
    int objsPos= ((CatSearchPageDescriptor)o).getMenuPosition();
    if (myPos == Integer.MAX_VALUE && objsPos == Integer.MAX_VALUE || myPos == objsPos)
      return getLabel().compareTo(((CatSearchPageDescriptor)o).getLabel());
    
    return myPos - objsPos;
  }
  
  //---- Suitability tests ---------------------------------------------------
  
//  /**
//   * Returns the score for this page with the given input element.
//   * @param element The input element
//   * @return The scope for the page
//   */
//  public int computeScore(Object element) {
//    if (element instanceof IAdaptable) {
//      IResource resource= (IResource)((IAdaptable)element).getAdapter(IResource.class);
//      if (resource != null && resource.getType() == IResource.FILE) {
//        String extension= ((IFile)resource).getFileExtension();
//        if (extension != null)
//          return getScoreForFileExtension(extension);
//      } else {
//        ISearchPageScoreComputer tester= 
//          (ISearchPageScoreComputer)((IAdaptable)element).getAdapter(ISearchPageScoreComputer.class);
//        if (tester != null)
//          return tester.computeScore(getId(), element); 
//      }
//    } /* can be removed as ISearchResultViewEntry adaptes to IResource
//      else if (element instanceof ISearchResultViewEntry) {
//      ISearchResultViewEntry entry= (ISearchResultViewEntry)element;
//      return computeScore(entry.getSelectedMarker());
//    }*/
//    if (fWildcardScore != ISearchPageScoreComputer.UNKNOWN)
//      return fWildcardScore;
//      
//    return ISearchPageScoreComputer.LOWEST;
//  }
  
//  private int getScoreForFileExtension(String extension) {
//    if (fExtensionScorePairs == null)
//      readExtensionScorePairs();
//      
//    int size= fExtensionScorePairs.size();
//    for (int i= 0; i < size; i++) {
//      ExtensionScorePair p= (ExtensionScorePair)fExtensionScorePairs.get(i);
//      if (extension.equals(p.extension))
//        return p.score;
//    }
//    if (fWildcardScore != ISearchPageScoreComputer.UNKNOWN)
//      return fWildcardScore;
//      
//    return ISearchPageScoreComputer.LOWEST; 
//  }
  
//  private void readExtensionScorePairs() {
//    fExtensionScorePairs= new ArrayList(3);
//    String content= fElement.getAttribute(EXTENSIONS_ATTRIBUTE);
//    if (content == null)
//      return;
//    StringTokenizer tokenizer= new StringTokenizer(content, ","); //$NON-NLS-1$
//    while (tokenizer.hasMoreElements()) {
//      String token= tokenizer.nextToken().trim();
//      int pos= token.indexOf(':');
//      if (pos != -1) {
//        String extension= token.substring(0, pos);
//        int score= StringConverter.asInt(token.substring(pos+1), ISearchPageScoreComputer.UNKNOWN);
//        if (extension.equals("*")) { //$NON-NLS-1$
//          fWildcardScore= score;
//        } else {
//          fExtensionScorePairs.add(new ExtensionScorePair(extension, score));
//        } 
//      }
//    }
//  }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    /**
     * Method getLocalId.
     * @return String
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return fElement.getNamespace();
    }
}
