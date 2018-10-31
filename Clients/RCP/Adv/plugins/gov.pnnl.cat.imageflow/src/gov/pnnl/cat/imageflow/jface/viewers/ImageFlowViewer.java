package gov.pnnl.cat.imageflow.jface.viewers;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.util.WebServiceUrlUtility;
import gov.pnnl.cat.imageflow.model.ImageContainer;
import gov.pnnl.cat.imageflow.swing.event.ImageFlowPreloader;
import gov.pnnl.cat.imageflow.swt.events.CleanResizeListener;
import gov.pnnl.cat.imageflow.views.ImageFlowView;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.util.VeloConstants;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.httpclient.old.URIUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.codebeach.ui.ImageFlow;
import com.codebeach.ui.ImageFlowItem;
import com.codebeach.ui.StackLayout;

public class ImageFlowViewer extends Viewer implements ISelectionListener, ListSelectionListener {

  private static final String ATOM_CONTENT_TYPE = "application/atom+xml";

  static {
    // Reduce embeddeded Swing resize flicker in Windows
    System.setProperty("sun.awt.noerasebackground", "true");
  }

  private Composite embeddedComposite;

  private JPanel imageFlowPanel;

  private ImageFlow imageFlow;

  private ISelection curSelection;

  private ImageContainer curImageContainer;

  private ImageFlowItem currentImageFlowItem;

  public ImageFlowViewer(Composite parent) {

    // Embedded Swing Frame
    this.embeddedComposite = new Composite(parent, SWT.EMBEDDED);

    // Listen for resize events to clean up (reducing flicker)
    embeddedComposite.addControlListener(new CleanResizeListener());

    embeddedComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    java.awt.Frame awtFrame = SWT_AWT.new_Frame(embeddedComposite);
    java.awt.Panel panel = new java.awt.Panel(new java.awt.BorderLayout());
    awtFrame.add(panel);

    // Add the ImageFlow to Swing panel
    imageFlowPanel = new JPanel(new StackLayout());
    // imageFlowPanel.add(new GradientPanel(), StackLayout.BOTTOM);
    panel.add(imageFlowPanel, BorderLayout.CENTER);

    // TODO: can we find out the current folder that is selected when we create this view?
  }

  /**
   * Returns the primary control associated with this viewer.
   * 
   * @return the SWT control which displays this viewer's content
   */
  @Override
  public Control getControl() {
    return embeddedComposite;
  }

  /**
   * Returns the input.
   * 
   * @return the input object
   */
  @Override
  public Object getInput() {
    return null;
  }

  /**
   * Returns the current selection for this provider.
   * 
   * @return the current selection
   */
  @Override
  public ISelection getSelection() {
    return curSelection;
  }

  /**
   * Refreshes this viewer completely with information freshly obtained from this viewer's model.
   */
  @Override
  public void refresh() {
    setSelection(getSelection());
  }

  /**
   * Sets or clears the input for this viewer.
   * 
   * @param input
   *          the input of this viewer, or <code>null</code> if none
   */
  @Override
  public void setInput(Object input) {

  }

  /**
   * Scrolls the ImageFlow to the appropriate selection.
   * 
   * @param selection
   *          the new selection
   * @param reveal
   *          <code>true</code> if the selection is to be made visible, and <code>false</code> otherwise
   */
  @Override
  public void setSelection(ISelection selection, boolean reveal) {
    // we ignore our own selection or null selection
    if (selection == null) {
      return;
    }

    // a slight optimization to handle people selecting the same thing over and
    // over
    if (curSelection != null && curSelection.equals(selection)) {
      return;
    }
    curSelection = selection;

    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structSelection = (IStructuredSelection) selection;
      Object element = structSelection.getFirstElement();

      if (element != null) {
        // TODO which adapter will be used if a gang member folder is selected, the folder adapter or the gang member's?
        ImageContainer container = (ImageContainer) Platform.getAdapterManager().getAdapter(element, ImageContainer.class);
        if (container == null) {
          container = getImageContainer(element);
        }

        if (container != null) {
          if (getImageFlow() != null && curImageContainer != null && container.getImageFolder().equals(curImageContainer.getImageFolder()) && element instanceof IResource) {
            // sync selection in the image flow (within the folder)
            IResource resource = (IResource) element;
            String label = resource.getName();

            int index = getImageFlow().getSelectedIndex();

            for (int i = 0; i < getImageFlow().getAvatars().size(); i++) {
              ImageFlowItem item = getImageFlow().getAvatars().get(i);

              if (StringUtils.equals(item.getLabel(), label)) {
                index = i;
              }
            }

            getImageFlow().setSelectedIndex(index);
          } else {
            // refresh the image flow for the entire folder
            curImageContainer = container;
            refreshImageFlow(container);
          }
        }
      }
    }
  }

  private void refreshImageFlow(final ImageContainer container) {
    try {
      // File imageFolder = new File("C:/Documents and Settings/d3k339/My Documents/My Pictures/gang");
      // ImageFlow imageFlow = new ImageFlow(imageFolder);

      // since selection occurs via SWT, but the image flow is in Swing, we have to run this async or else
      // we will get deadlock on selection events
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          try {
            if (getImageFlow() != null) {
              imageFlowPanel.remove(getImageFlow());
            }
            setImageFlow(new ImageFlow(loadFromCat(container)));
            imageFlowPanel.add(getImageFlow(), StackLayout.TOP);
            imageFlowPanel.validate();

            // TODO fire a SelectionChangedEvent on load so the correct row is selected in the table view
            // updateSelection(getImageFlow());
          } catch (Throwable e) {
            CatLogger.getLogger(ImageFlowView.class).error("Failed to retrieve image", e);
          }
        }

      });

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public List<ImageFlowItem> loadFromCat(ImageContainer container) {
    List<ImageFlowItem> list = new ArrayList<ImageFlowItem>();
    try {
      // /cat/thumbnails/uuid?includeSubfolders=true&imagesOnly=true
      StringBuilder url = WebServiceUrlUtility.getService("cat");
      WebServiceUrlUtility.appendPaths(url, "thumbnails", container.getImageFolder().getPropertyAsString(VeloConstants.PROP_UUID));

      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("includeSubfolders", String.valueOf(container.isIncludeAllSubfolders()));
      parameters.put("imagesOnly", String.valueOf(container.isImagesOnly()));
      parameters.put("thumbnailName", "imgpreview");

      WebServiceUrlUtility.appendParameters(url, parameters);

      ClientConfig config = new ClientConfig();
      BasicAuthSecurityHandler basicAuthHandler = new BasicAuthSecurityHandler();
      String username = ResourcesPlugin.getDefault().getSecurityManager().getUsername();
      String password = ResourcesPlugin.getDefault().getSecurityManager().getPassword();
      basicAuthHandler.setUserName(username);
      basicAuthHandler.setPassword(password);
      config.handlers(basicAuthHandler);
      RestClient client = new RestClient(config);
      Resource resource = client.resource(url.toString());
      AtomFeed atomFeed = resource.accept(ATOM_CONTENT_TYPE).get(AtomFeed.class);

      //sort by title to match default sorting in explorer
      List<AtomEntry> sortedEntries = new ArrayList<AtomEntry>();
      for (AtomEntry atomEntry : atomFeed.getEntries()) {
        sortedEntries.add(atomEntry);
      }
      Collections.sort(sortedEntries, new MyAtomEntryComparable());
      for (AtomEntry atomEntry : sortedEntries) {
        String[] ids = atomEntry.getId().split("\\|");
        String[] dimensions = atomEntry.getSummary().getValue().split("x");

        ImageFlowItem item = new ImageFlowItem(ids[0], ids[1], URIUtil.decode(atomEntry.getTitle().getValue()), Long.parseLong(dimensions[0]), Long.parseLong(dimensions[1]));
        list.add(item);
      }

      // throw any exceptions back out to caller
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
    return list;
  }

  public class MyAtomEntryComparable implements Comparator<AtomEntry> {
    @Override
    public int compare(AtomEntry o1, AtomEntry o2) {
      try {
        String o1Title = URIUtil.decode(o1.getTitle().getValue());
        String o2Title = URIUtil.decode(o2.getTitle().getValue());
        return o1Title.compareTo(o2Title);
      } catch (Throwable e) {
      }
      return 1;
    }
  }
  
  private ImageContainer getImageContainer(Object adaptableObject) {
    if (adaptableObject instanceof IFolder) {
      return new ImageContainer((IFolder) adaptableObject);
    } else if (adaptableObject instanceof IFile) {
      IResource parent;
      try {
        parent = ((IFile) adaptableObject).getParent();
        if (parent instanceof IFolder) {
          return new ImageContainer((IFolder) parent);
        }
      } catch (Exception e) {
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    setSelection(selection);
  }

  /**
   * {@inheritDoc}
   * 
   * Listen for "selection" changes in the ImageFlow and fire off those selections to other listeners.
   * 
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    updateSelection((ImageFlow) e.getSource());
  }

  /**
   * Fire a {@link SelectionChangedEvent} for selection changes in the {@link ImageFlow}.
   * 
   * @param imageFlow
   *          {@link ImageFlow} with selection changes
   */
  private void updateSelection(ImageFlow imageFlow) {
    ImageFlowItem imageFlowItem = imageFlow.getSelectedValue();

    if (imageFlowItem != null && currentImageFlowItem != imageFlowItem) {
      this.currentImageFlowItem = imageFlowItem;
      String uuid = imageFlowItem.getParentId();
      IResource resource = ResourcesPlugin.getDefault().getResourceManager().getResource(uuid);
      StructuredSelection selection = new StructuredSelection(resource);
      SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
      fireSelectionChanged(event);
    }
  }

  /**
   * @return the imageFlow
   */
  private ImageFlow getImageFlow() {
    return imageFlow;
  }

  /**
   * @param imageFlow
   *          the imageFlow to set
   */
  private void setImageFlow(ImageFlow imageFlow) {
    this.imageFlow = imageFlow;

    imageFlow.addListSelectionListener(this);
    imageFlow.addListSelectionListener(new ImageFlowPreloader(imageFlow));
  }
}
