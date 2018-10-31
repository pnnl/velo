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
package gov.pnnl.cat.search.advanced;

import gov.pnnl.cat.logging.CatLogger;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 */
public class AdvancedSearchExtensions {

  private Logger logger = CatLogger.getLogger(AdvancedSearchExtensions.class);
  private static String OPEN_WITH_EXTENSION_POINT_ID = "gov.pnnl.cat.search.advanced.advancedSearchOptions";
  private IConfigurationElement[] elementExtensions;
  private List<AdvancedSearchOptions> advOptionsExtensions;
  
  /**
   * Method getAdvOptionsExtensions.
   * @return List<AdvancedSearchOptions>
   */
  public List<AdvancedSearchOptions> getAdvOptionsExtensions() {
    return advOptionsExtensions;
  }

  public void loadExtensions(){
    if(elementExtensions != null){
      return;
    }
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    elementExtensions = registry.getConfigurationElementsFor( OPEN_WITH_EXTENSION_POINT_ID );
    this.advOptionsExtensions = new ArrayList<AdvancedSearchOptions>(elementExtensions.length);
    for (int i = 0; i < elementExtensions.length; i++ ) {
      try {
        AdvancedSearchOptions newOpenWithAction = (AdvancedSearchOptions) elementExtensions[i].createExecutableExtension("class");
        this.advOptionsExtensions.add( newOpenWithAction );
      } catch (CoreException e) {
        logger.error(e);
      }
    }
  }
}
