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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer;

import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;

/**
 * IPostSelectionProvider implementation that delegates to another ISelectionProvider or IPostSelectionProvider. The selection provider used for delegation can be exchanged dynamically. Registered listeners are adjusted accordingly. This utility class may be used in workbench parts with multiple viewers.
 * 
 * @author Marc R. Hoffmann
 * @version $Revision: 1.0 $
 */
public class SelectionProviderIntermediate implements IPostSelectionProvider {

  private static final SelectionProviderIntermediate INSTANCE = new SelectionProviderIntermediate();

  private final ListenerList selectionListeners = new ListenerList();

  private final ListenerList postSelectionListeners = new ListenerList();

  private ISelectionProvider delegate;

  /**
  
   * @return {@link SelectionProviderIntermediate} singleton */
  public static SelectionProviderIntermediate getInstance() {
    return INSTANCE;
  }

  /**
   * Enforce singleton
   */
  private SelectionProviderIntermediate() {
    super();
  }

  private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
    public void selectionChanged(final SelectionChangedEvent event) {
      if (event.getSelectionProvider() == delegate) {
        Display.getDefault().syncExec(new Runnable() {
          public void run() {
            fireSelectionChanged(RCPUtil.handleEmptySelectionForTable(event.getSelection(), delegate));
          }
        });
      }
    }
  };

  private ISelectionChangedListener postSelectionListener = new ISelectionChangedListener() {
    public void selectionChanged(final SelectionChangedEvent event) {
      if (event.getSelectionProvider() == delegate) {
        Display.getDefault().syncExec(new Runnable() {
          public void run() {
            firePostSelectionChanged(RCPUtil.handleEmptySelectionForTable(event.getSelection(), delegate));
          }
        });
      }
    }
  };

  /**
   * Sets a new selection provider to delegate to. Selection listeners registered with the previous delegate are removed before.
   * 
   * @param newDelegate
   *          new selection provider (will be a treeviewer or tableviewer which is a contentprovider)
   */
  public void setSelectionProviderDelegate(ISelectionProvider newDelegate) {
    if (newDelegate == delegate) {
      return;
    }
    if (delegate != null) {
      delegate.removeSelectionChangedListener(selectionListener);
      if (delegate instanceof IPostSelectionProvider) {
        ((IPostSelectionProvider) delegate).removePostSelectionChangedListener(postSelectionListener);
      }
    }
    delegate = newDelegate;
    if (newDelegate != null) {
      delegate.addSelectionChangedListener(selectionListener);
      if (delegate instanceof IPostSelectionProvider) {
        ((IPostSelectionProvider) delegate).addPostSelectionChangedListener(postSelectionListener);
      }
      fireSelectionChanged(delegate.getSelection());

    }
  }

  /**
   * Method fireSelectionChanged.
   * @param selection ISelection
   */
  protected void fireSelectionChanged(ISelection selection) {
    fireSelectionChanged(selectionListeners, selection);
  }

  /**
   * Method firePostSelectionChanged.
   * @param selection ISelection
   */
  protected void firePostSelectionChanged(ISelection selection) {
    fireSelectionChanged(postSelectionListeners, selection);
  }

  /**
   * Method fireSelectionChanged.
   * @param list ListenerList
   * @param selection ISelection
   */
  private void fireSelectionChanged(ListenerList list, ISelection selection) {
    if (selection != null && list != null && !list.isEmpty()) {
      SelectionChangedEvent event = new SelectionChangedEvent(delegate, selection);
      Object[] listeners = list.getListeners();
      for (int i = 0; i < listeners.length; i++) {
        ISelectionChangedListener listener = (ISelectionChangedListener) listeners[i];
        listener.selectionChanged(event);
      }
    }
  }

  // IPostSelectionProvider Implementation

  /**
   * Method addSelectionChangedListener.
   * @param listener ISelectionChangedListener
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
   */
  public void addSelectionChangedListener(ISelectionChangedListener listener) {
    selectionListeners.add(listener);
  }

  /**
   * Method removeSelectionChangedListener.
   * @param listener ISelectionChangedListener
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
   */
  public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    selectionListeners.remove(listener);
  }

  /**
   * Method addPostSelectionChangedListener.
   * @param listener ISelectionChangedListener
   * @see org.eclipse.jface.viewers.IPostSelectionProvider#addPostSelectionChangedListener(ISelectionChangedListener)
   */
  public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
    postSelectionListeners.add(listener);
  }

  /**
   * Method removePostSelectionChangedListener.
   * @param listener ISelectionChangedListener
   * @see org.eclipse.jface.viewers.IPostSelectionProvider#removePostSelectionChangedListener(ISelectionChangedListener)
   */
  public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
    postSelectionListeners.remove(listener);
  }

  /**
   * Method getCurrentDelegate.
   * @return ISelectionProvider
   */
  public ISelectionProvider getCurrentDelegate() {
    return delegate;
  }

  /**
   * Method getSelection.
   * @return ISelection
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  public ISelection getSelection() {
    return delegate == null ? null : RCPUtil.handleEmptySelectionForTable(delegate.getSelection(), delegate);
  }

  /**
   * Method setSelection.
   * @param selection ISelection
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(ISelection)
   */
  public void setSelection(ISelection selection) {
    if (delegate != null) {
      delegate.setSelection(selection);
    }
  }

}
