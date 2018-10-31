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
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

/**
 * This class contains utility functions for debugging focus issues relating
 * to the SWT_AWT bridge.
 * <p>
 * Debugging focus issues cannot be done in a debugger running on the same
 * machine, because the interactions with the debugger often cause the
 * application window to be deactivated. Therefore a println based approach
 * has been adopted.
 * <p>
 * There are four kinds of events:
 * <ul>
 *   <li>SWT focus events relating to the IlvSwingControl.</li>
 *   <li>AWT window focus events relating to the topmost window under the
 *       IlvSwingControl.</li>
 *   <li>AWT focus events relating to components inside that window.</li>
 *   <li>Property change events of the AWT
 *       <code>KeyboardFocusManager</code>.</li>
 * </ul>
 * @version $Revision: 1.0 $
 */
public class FocusDebugging {

   /**
     * Adds listeners for debugging the three first kinds of focus events.
     * @param control org.eclipse.swt.widgets.Composite
    * @param topLevelComponent Container
    */
    public static void addFocusDebugListeners(org.eclipse.swt.widgets.Composite control,
                                              Container topLevelComponent) {
        control.addFocusListener(_SWTFocusListener);
        control.addListener(SWT.Activate, _SWTActivationListener);
        control.addListener(SWT.Deactivate, _SWTActivationListener);
        if (topLevelComponent instanceof Window)
            ((Window)topLevelComponent).addWindowFocusListener(_AWTWindowFocusListener);
        addFocusListenerToTree(topLevelComponent);
    }

    /**
     * Shows focus events on the SWT side.
     * @version $Revision: 1.0 $
     */
    private static class SWTFocusListener implements org.eclipse.swt.events.FocusListener {
        /**
         * Method focusGained.
         * @param event org.eclipse.swt.events.FocusEvent
         * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
         */
        public void focusGained(org.eclipse.swt.events.FocusEvent event) {
            System.err.println("@"+System.currentTimeMillis()
                               +" SWT focus gained "+event.getSource().hashCode());
        }
        /**
         * Method focusLost.
         * @param event org.eclipse.swt.events.FocusEvent
         * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
         */
        public void focusLost(org.eclipse.swt.events.FocusEvent event) {
            System.err.println("@"+System.currentTimeMillis()
                               +" SWT focus lost "+event.getSource().hashCode());
        }
    }
    private static SWTFocusListener _SWTFocusListener = new SWTFocusListener();

    /**
     * Shows activation events on the SWT side. Note: events that are eaten by the filter
     * in FocusHander will not be displayed here.
     * @version $Revision: 1.0 $
     */
    private static class SWTActivationListener implements org.eclipse.swt.widgets.Listener {
        /**
         * Method handleEvent.
         * @param event Event
         * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
         */
        public void handleEvent(Event event) {
            String name = null;
            switch (event.type) {
            case SWT.Deactivate:
                name = "Deactivate";
                break;
                
            case SWT.Activate:
                name = "Activate";
                break;
            }
            System.err.println("@"+System.currentTimeMillis() + 
                    " SWT Event: " + name + " " + System.identityHashCode(event.widget));
        }
    }
    private static SWTActivationListener _SWTActivationListener = new SWTActivationListener();
    
    /**
     * Shows focus events on the top-level window on the AWT side.
     * @version $Revision: 1.0 $
     */
    private static class AWTWindowFocusListener implements WindowFocusListener {
        /**
         * Method showKFMStatus.
         * @param window Window
         */
        private void showKFMStatus(Window window) {
            KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            System.err.println("               permanentFocusOwner: "+kfm.getPermanentFocusOwner());
            System.err.println("               focusOwner:          "+kfm.getFocusOwner());
            System.err.println("               window's focusOwner: "+window.getFocusOwner());
        }
        /**
         * Method windowGainedFocus.
         * @param event WindowEvent
         * @see java.awt.event.WindowFocusListener#windowGainedFocus(WindowEvent)
         */
        public void windowGainedFocus(WindowEvent event) {
            System.err.println("@"+System.currentTimeMillis()
                               +" AWT focus gained by window "+event.getWindow());
            showKFMStatus(event.getWindow());
        }
        /**
         * Method windowLostFocus.
         * @param event WindowEvent
         * @see java.awt.event.WindowFocusListener#windowLostFocus(WindowEvent)
         */
        public void windowLostFocus(WindowEvent event) {
            System.err.println("@"+System.currentTimeMillis()
                               +" AWT focus lost by window "+event.getWindow());
            showKFMStatus(event.getWindow());
        }
    }
    private static AWTWindowFocusListener _AWTWindowFocusListener = new AWTWindowFocusListener();

    /**
     * Shows focus events on a given component on the AWT side.
     * @version $Revision: 1.0 $
     */
    private static class AWTFocusListener implements FocusListener {
        /**
         * Method focusGained.
         * @param event FocusEvent
         * @see java.awt.event.FocusListener#focusGained(FocusEvent)
         */
        public void focusGained(FocusEvent event) {
            System.err.println("@"+System.currentTimeMillis()
                               +" AWT focus gained "+event.getComponent());
        }
        /**
         * Method focusLost.
         * @param event FocusEvent
         * @see java.awt.event.FocusListener#focusLost(FocusEvent)
         */
        public void focusLost(FocusEvent event) {
            System.err.println("@"+System.currentTimeMillis()
                               +" AWT focus lost "+event.getComponent());
        }
    }
    private static AWTFocusListener _AWTFocusListener = new AWTFocusListener();

    /**
     * Attaches the AWTFocusListener on each of the components in the component
     * tree under the given component.
     * @version $Revision: 1.0 $
     */
    private static class AWTContainerListener implements ContainerListener {
        /**
         * Method componentAdded.
         * @param event ContainerEvent
         * @see java.awt.event.ContainerListener#componentAdded(ContainerEvent)
         */
        public void componentAdded(ContainerEvent event) {
            addFocusListenerToTree(event.getChild());
        }

        /**
         * Method componentRemoved.
         * @param event ContainerEvent
         * @see java.awt.event.ContainerListener#componentRemoved(ContainerEvent)
         */
        public void componentRemoved(ContainerEvent event) {
            removeFocusListenerFromTree(event.getChild());
        }
    }
    private static AWTContainerListener _AWTContainerListener = new AWTContainerListener();
    /**
     * Method addFocusListenerToTree.
     * @param comp Component
     */
    static void addFocusListenerToTree(Component comp) {
        comp.addFocusListener(_AWTFocusListener);
        if (comp instanceof Container) {
            Container cont = (Container)comp;
            // Remember to add the listener to child components that are added later.
            cont.addContainerListener(_AWTContainerListener);
            // Recurse across all child components that are already in the tree now.
            int n = cont.getComponentCount();
            for (int i = 0; i < n; i++)
                addFocusListenerToTree(cont.getComponent(i));
        }
    }
    /**
     * Method removeFocusListenerFromTree.
     * @param comp Component
     */
    static void removeFocusListenerFromTree(Component comp) {
        // The exact opposite of addFocusListenerToTree.
        comp.removeFocusListener(_AWTFocusListener);
        if (comp instanceof Container) {
            Container cont = (Container)comp;
            cont.removeContainerListener(_AWTContainerListener);
            int n = cont.getComponentCount();
            for (int i = 0; i < n; i++)
                removeFocusListenerFromTree(cont.getComponent(i));
        }
    }

    // ------------------------------------------------------------------------

    /**
     * Enables logging of events,
     * from the AWT <code>KeyboardFocusManager</code> singleton.
     */
    public static void enableKeyboardFocusManagerLogging() {
        enableFinest("java.awt.focus.KeyboardFocusManager");
        enableFinest("java.awt.focus.DefaultKeyboardFocusManager");
    }
    /**
     * Method enableFinest.
     * @param name String
     */
    private static void enableFinest(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(Level.FINEST);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        logger.addHandler(handler);
    }
}
