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
package gov.pnnl.cat.ui.rcp.views.preview;

import gov.pnnl.cat.core.util.exception.CATRuntimeException;

import java.lang.reflect.Field;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * When {@link StyledText#setWordWrap(boolean)} is set to true, resizing is CPU intensive.
 * <p>
 * Later if setting back to false, resizing is still CPU intensive as the component is not reset.
 * </p>
 * <p>
 * This custom {@link StyledText} overrides this behavior to reset the component.
 * </p>
 * @version $Revision: 1.0 $
 */
public class CatStyledText extends StyledText {
  private static Field FIXED_LINE_HEIGHT;

  static {
    try {
      FIXED_LINE_HEIGHT = StyledText.class.getDeclaredField("fixedLineHeight");
      FIXED_LINE_HEIGHT.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new CATRuntimeException(e);
    }
  }

  /**
   * Constructor for CatStyledText.
   * @param parent Composite
   * @param style int
   */
  public CatStyledText(Composite parent, int style) {
    super(parent, style);

    Listener[] resizeListeners = getListeners(SWT.Resize);

    for (Listener listener : resizeListeners) {
      removeListener(SWT.Resize, listener);
    }

    final boolean[] running = new boolean[1];

    addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event e) {
        if (running[0]) {
          return;
        }

        running[0] = true;

        final boolean originalFixedLineHeight = getFixedLineHeight();
        setFixedLineHeight(true);

        getDisplay().asyncExec(new Runnable() {
          public void run() {
            if (isDisposed()) {
              return;
            }

            setFixedLineHeight(originalFixedLineHeight);
            running[0] = false;
          }
        });
      }
    });

    for (Listener listener : resizeListeners) {
      addListener(SWT.Resize, listener);
    }
  }

  /**
   * When false, reset the component to correct for slow resizing.
   * <p>
   * {@inheritDoc}
   * </p>
   * 
   * @see org.eclipse.swt.custom.StyledText#setWordWrap(boolean)
   */
  @Override
  public void setWordWrap(boolean wrap) {
    super.setWordWrap(wrap);

    if (!wrap) {
      setFixedLineHeight(true);
    }
  }

  /**
   * Use reflection to get the super class' fixedLineHeight field value.
   * 
  
   * @return fixedLineHeight value */
  protected boolean getFixedLineHeight() {
    try {
      return FIXED_LINE_HEIGHT.getBoolean(this);
    } catch (IllegalAccessException e) {
      throw new CATRuntimeException(e);
    }
  }

  /**
   * Use reflection to set the super class' fixedLineHeight field value.
   * 
   * @param fixedLineHeight
   *          boolean value to set
   */
  protected void setFixedLineHeight(boolean fixedLineHeight) {
    try {
      FIXED_LINE_HEIGHT.setBoolean(this, fixedLineHeight);
    } catch (IllegalAccessException e) {
      throw new CATRuntimeException(e);
    }
  }
}
