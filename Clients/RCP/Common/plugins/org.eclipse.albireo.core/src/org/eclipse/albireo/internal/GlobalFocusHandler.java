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
package org.eclipse.albireo.internal;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.albireo.core.SwingControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

// Handler for global focus events. Maintains global state information like current and 
// previous active widgets. When actions based on global state need to be done, they are
// implemented in this class. See also the FocusHandler class for additional handling based 
// on a single SwingControl.
/**
 */
public class GlobalFocusHandler {
    private static final String SAVED_FOCUS_OWNER_KEY = "org.eclipse.albireo.savedFocusOwner";
    private final Display display;
    private final SwtEventFilter swtEventFilter;
    private final List listeners = new ArrayList();
    private static final boolean verboseFocusEvents = FocusHandler.verboseFocusEvents;
    
    /**
     * Constructor for GlobalFocusHandler.
     * @param display Display
     */
    public GlobalFocusHandler(Display display) {
        this.display = display;
        swtEventFilter = new SwtEventFilter();
        display.addFilter(SWT.Activate, swtEventFilter);
        display.addFilter(SWT.Deactivate, swtEventFilter);
        display.addFilter(SWT.Traverse, swtEventFilter);
    }

    /**
     * Method getCurrentSwtTraversal.
     * @return int
     */
    public int getCurrentSwtTraversal() {
        assert Display.getCurrent() != null; // On SWT event thread
        return swtEventFilter.currentSwtTraversal;
    }

    /**
     * Method getActiveWidget.
     * @return Widget
     */
    public Widget getActiveWidget() {
        assert Display.getCurrent() != null; // On SWT event thread
        return swtEventFilter.activeWidget;
    }

    /**
     * Method getActiveShell.
     * @return Shell
     */
    public Shell getActiveShell() {
        assert Display.getCurrent() != null; // On SWT event thread
        return swtEventFilter.activeShell;
    }

    /**
     * Method getActiveEmbedded.
     * @return SwingControl
     */
    public SwingControl getActiveEmbedded() {
        assert Display.getCurrent() != null; // On SWT event thread
        return swtEventFilter.activeEmbedded;
    }

    /**
     * Method getLastActiveWidget.
     * @return Widget
     */
    public Widget getLastActiveWidget() {
        assert Display.getCurrent() != null; // On SWT event thread
        return swtEventFilter.lastActiveWidget;
    }

    /**
     * Method getLastActiveEmbedded.
     * @return SwingControl
     */
    public SwingControl getLastActiveEmbedded() {
        assert Display.getCurrent() != null; // On SWT event thread
        return swtEventFilter.lastActiveEmbedded;
    }
    
    /**
     * Method getLastActiveFocusCleared.
     * @return boolean
     */
    public boolean getLastActiveFocusCleared() {
        assert Display.getCurrent() != null; // On SWT event thread
        return swtEventFilter.lastActiveFocusCleared;
    }
    
    /**
     * Method setLastActiveFocusCleared.
     * @param lastActiveFocusCleared boolean
     */
    public void setLastActiveFocusCleared(boolean lastActiveFocusCleared) {
        assert Display.getCurrent() != null; // On SWT event thread
        swtEventFilter.lastActiveFocusCleared = lastActiveFocusCleared;
    }
    
    /**
     * Method addEventFilter.
     * @param filter Listener
     */
    public void addEventFilter(Listener filter) {
        listeners.add(filter);
    }
    
    /**
     * Method removeEventFilter.
     * @param filter Listener
     */
    public void removeEventFilter(Listener filter) {
        listeners.remove(filter);
    }
    
    /**
     * Method fireEvent.
     * @param event Event
     */
    protected void fireEvent(Event event) {
        for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            Listener listener = (Listener)iterator.next();
            listener.handleEvent(event);
        }
    }
    
    public void dispose() {
        display.removeFilter(SWT.Activate, swtEventFilter);
        display.removeFilter(SWT.Deactivate, swtEventFilter);
        display.removeFilter(SWT.Traverse, swtEventFilter);
    }

    /**
     * Method isBorderlessSwingControl.
     * @param widget Widget
     * @return boolean
     */
    protected boolean isBorderlessSwingControl(Widget widget) {
        return (widget instanceof SwingControl) && ((widget.getStyle() & SWT.EMBEDDED) != 0);
    }

    /**
     * Method clearFocusOwner.
     * @param swingControl SwingControl
     */
    protected void clearFocusOwner(SwingControl swingControl) {
        assert Display.getCurrent() != null; // On SWT event thread
        
        if (!swingControl.isAWTPermanentFocusLossForced()) {
            return;
        }
        
        // It appears safe to call getFocusOwner on SWT thread
        final Component owner = ((Frame)swingControl.getAWTHierarchyRoot()).getFocusOwner();
        if (owner != null) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    // Clear the AWT focus owner so that a permanent focus lost event is 
                    // generated. Where possible, we use the KeyboardFocusManager, but
                    // it has no method to clear the focus owner within a particular frame, 
                    // if that frame is no longer active. In that case, we use a hack of 
                    // disabling and re-enabling the window's focus owner. The hack has
                    // the drawback of a brief visual movement of the cursor (or other 
                    // focus indicator), so it is good to avoid it whenever possible, as 
                    // we do here. 
                    KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                    if (owner == kfm.getFocusOwner()) {
                        if (verboseFocusEvents) {
                            trace("clearing focus thru kfm: " + owner);
                        }
                        kfm.clearGlobalFocusOwner();
                    } else {
                        if (verboseFocusEvents) {
                            trace("clearing focus thru hack: " + owner);
                        }
                        owner.setEnabled(false);
                        owner.setEnabled(true);
                    }
                }
            });
            swingControl.setData(SAVED_FOCUS_OWNER_KEY, owner);
        }
    }
    
    /**
     * Method restoreFocusOwner.
     * @param swingControl SwingControl
     */
    protected void restoreFocusOwner(SwingControl swingControl) {
        assert Display.getCurrent() != null; // On SWT event thread

        final Component savedOwner = (Component)swingControl.getData(SAVED_FOCUS_OWNER_KEY);
        if (savedOwner != null) {
            swingControl.setData(SAVED_FOCUS_OWNER_KEY, null);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    // Restore focus to any AWT component that lost focus due to 
                    // clearFocusOwner(). 
                    if (verboseFocusEvents) {
                        trace("restoring focus: " + savedOwner);
                    }
                    savedOwner.requestFocus();
                }
            });
        }
    }
    
    /**
     * Method trace.
     * @param msg String
     */
    private void trace(String msg) {
        System.err.println(header() + ' ' + msg);
    }
    /**
     * Method header.
     * @return String
     */
    private String header() {
        return "@" + System.currentTimeMillis() + " " + System.identityHashCode(this);
    }
    
    /**
     */
    protected class SwtEventFilter implements Listener {
        
        int currentSwtTraversal = SWT.TRAVERSE_NONE;
        Widget activeWidget;
        Shell activeShell;
        SwingControl activeEmbedded;

        Widget lastActiveWidget = null;
        SwingControl lastActiveEmbedded = null;
        boolean lastActiveFocusCleared = false;
        
        public SwtEventFilter() {
            activeWidget = display.getFocusControl();
            activeShell = display.getActiveShell();
            if (isBorderlessSwingControl(activeWidget)) {
                activeEmbedded = (SwingControl)activeWidget;
            }
        }
        
        /**
         * Method handleEvent.
         * @param event Event
         * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
         */
        public void handleEvent(Event event) {
            Widget widget = event.widget;
            switch (event.type) {
            case SWT.Activate:
                activeWidget = widget;
                
                // Track the currently active shell. This is more reliable than
                // depending on Display.getActiveShell() which sometimes returns an 
                // inactive shell. 
                if (widget instanceof Shell) {
                    activeShell = (Shell)widget;
                }
                
                // If we have moved from a SwingControl to another control in the same
                // shell, clear its current focus owner so that a permanent focus
                // lost event is generated. 
                if ((lastActiveEmbedded != null) && (!lastActiveEmbedded.isDisposed()) && (lastActiveEmbedded != widget) &&  
                        !lastActiveFocusCleared && 
                        (widget instanceof Control) &&  // (need a getShell() method)
                        (lastActiveEmbedded.getShell() == ((Control)widget).getShell())) {
                    clearFocusOwner(lastActiveEmbedded);
                    lastActiveFocusCleared = true;
                }
                
                // If we have moved to a SwingControl, restore the current focus owner
                // that was cleared above during a previous Activate event.
                if (isBorderlessSwingControl(widget)) {
                    activeEmbedded = (SwingControl)widget;
                    restoreFocusOwner(activeEmbedded);
                }
                break;

            case SWT.Deactivate:
                if (activeWidget != null) {
                    lastActiveWidget = activeWidget;
                    activeWidget = null;
                }
                
                if (event.widget instanceof Shell) {
                    activeShell = null;
                }
                
                if (isBorderlessSwingControl(widget)) {
                    if (activeEmbedded != null) {
                        lastActiveEmbedded = activeEmbedded;
                        lastActiveFocusCleared = false;
                        activeEmbedded = null;
                    }
                }

                break;
                
            case SWT.Traverse:
                currentSwtTraversal = event.detail;
                
                break;
            }
            
            // Propagate to any listeners
            fireEvent(event);
            
            // If there is a current traversal, it is now complete
            // with the activation of a control. Reset the value
            // to indicate no current traversal. 
            if (event.type == SWT.Activate) {
                currentSwtTraversal = SWT.TRAVERSE_NONE;
            }
        }
    }
}
