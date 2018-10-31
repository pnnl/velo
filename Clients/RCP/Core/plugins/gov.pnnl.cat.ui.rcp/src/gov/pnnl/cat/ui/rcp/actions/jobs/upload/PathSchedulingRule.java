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
package gov.pnnl.cat.ui.rcp.actions.jobs.upload;

import gov.pnnl.velo.model.CmsPath;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * TODO: Consider moving this into the resources plugin.
 *
 * @author Eric Marshall
 * @version $Revision: 1.0 $
 */
public class PathSchedulingRule implements ISchedulingRule {
  CmsPath path;

  /**
   * Constructor for PathSchedulingRule.
   * @param path CmsPath
   */
  public PathSchedulingRule(CmsPath path) {
    this.path = path;
  }

  /**
   * Method getPath.
   * @return CmsPath
   */
  public CmsPath getPath() {
    return path;
  }

  /**
   * Method contains.
   * @param rule ISchedulingRule
   * @return boolean
   * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(ISchedulingRule)
   */
  public boolean contains(ISchedulingRule rule) {
    if (this == rule) {
      return true;
    }

    if (rule instanceof MultiRule) {
      MultiRule multi = (MultiRule) rule;
      ISchedulingRule[] children = multi.getChildren();
      for (int i = 0; i < children.length; i++) {
        if (!contains(children[i])) {
          return false;
        }
      }
      return true;
    }

    if (!(rule instanceof PathSchedulingRule)) {
      return false;
    }

    return path.isPrefixOf(((PathSchedulingRule) rule).getPath());
  }

  /**
   * Method isConflicting.
   * @param rule ISchedulingRule
   * @return boolean
   * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(ISchedulingRule)
   */
  public boolean isConflicting(ISchedulingRule rule) {
    if (!(rule instanceof PathSchedulingRule)) {
      return false;
    }

    CmsPath otherPath = ((PathSchedulingRule) rule).getPath();
    return path.isPrefixOf(otherPath) || otherPath.isPrefixOf(path);
  }
}
