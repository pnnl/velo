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
package gov.pnnl.velo.ui.views;

import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableViewerSorter;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.ModifiedDateComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.NameComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.PathComparator;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.comparator.TableExplorerComparator;
import gov.pnnl.velo.core.util.DateFormatUtility;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloTifConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class MyJobsView extends ViewPart implements  IResourceEventListener {
  
  public static final String ID = MyJobsView.class.getName();
  //public List<IResource> jobsToReconnectTo = new ArrayList<IResource>();
  private List<IResource> jobsDisconnected = new ArrayList<IResource>();
  private List<CmsPath> jobsDisconnectedPaths = new ArrayList<CmsPath>();
  TableViewer tViewer;
  Composite parent;
  Button running;
  Button disconnected;
  Button complete;
 
  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    this.parent = parent;
    RowLayout parentLayout = new RowLayout();
    parentLayout.fill = true;
    parentLayout.justify = false;
    parentLayout.pack = true;
    parentLayout.type = SWT.VERTICAL;
    parentLayout.wrap = false;
    parent.setLayout(parentLayout);
    parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    
    Color color = parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
    Composite checkBoxComposite = new Composite(parent, SWT.NONE);
    RowLayout checkBoxLayout = new RowLayout();
    checkBoxLayout.fill = true;
    checkBoxLayout.justify = false;
    checkBoxLayout.pack = true;
    checkBoxLayout.type = SWT.HORIZONTAL;
    checkBoxLayout.wrap = false;
    checkBoxComposite.setLayout(checkBoxLayout);
    checkBoxComposite.setBackground(color);
    running = new Button(checkBoxComposite, SWT.CHECK);
    running.setText("Running");
    running.setBackground(color);
    running.setSelection(true);
    
    disconnected =  new Button(checkBoxComposite, SWT.CHECK);
    disconnected.setText("Disconnected");
    disconnected.setBackground(color);
    disconnected.setSelection(true);
    
    complete = new Button(checkBoxComposite, SWT.CHECK);
    complete.setText("Complete");
    complete.setBackground(color);
    complete.setSelection(true);
    
    tViewer= new TableViewer(parent,  SWT.MULTI  | SWT.H_SCROLL | SWT.V_SCROLL |SWT.FULL_SELECTION);
    tViewer.setContentProvider(new ArrayContentProvider());
    this.getSite().setSelectionProvider(tViewer);
    
    createColumns(tViewer);
    updateTableContents();
    final Table table= tViewer.getTable();
    table.setHeaderVisible(true);
    
    //sort by default by first column (mimetype)
    tViewer.setSorter(new TableViewerSorter(new MimetypeComparator()));
    table.getColumn(0).setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_UP, SharedImages.CAT_IMG_SIZE_16));
    
    ResourcesPlugin.getResourceManager().addResourceEventListener(this);
    
    tViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        IStructuredSelection selectedObj = (IStructuredSelection)selection;
        IResource firstElement = (IResource)selectedObj.getFirstElement();
        if(firstElement!=null) {
          //ToolUtils.selectResourceInTree(firstElement);
        }
      }
    });
  }
  
  private void updateTableContents() {
    
    //Set the input for the table data
    String username = ResourcesPlugin.getSecurityManager().getUsername();
    StringBuilder query = new StringBuilder();
    
    query.append("("); 
      query.append("@velo\\:status:(Submitting)");
      query.append(" OR ");
      query.append("@velo\\:status:(Running)");
      query.append(" OR ");
      query.append("@velo\\:status:(In Queue)");
      query.append(" OR ");
      query.append("@velo\\:status:(Job Complete. Post-processing results)");
      query.append(" OR ");
      query.append("@velo\\:status:(Reconnecting)");
      query.append(" OR ");
      
      query.append("@velo\\:status:(Cancelled)");
      query.append(" OR ");
      query.append("@velo\\:status:(Killed)");
      query.append(" OR ");
      query.append("@velo\\:status:(Complete)");
      query.append(" OR ");
      query.append("@velo\\:status:(Success)");      
      query.append(" OR ");
      query.append("@velo\\:status:(Error)");      

      query.append(" OR ");
      query.append("@velo\\:status:(Disconnected)");       
      
    query.append(")");
    query.append(" AND ");
    query.append("(");
      query.append("@velo\\:cmsuser:\"" + username);
      query.append("\" OR  ");
      query.append("( -@velo\\:cmsuser:\"[a TO z]*\" AND @cm\\:creator:\"" + username + "\" )");
    query.append(")");
      
        
    ICatQueryResult queryResult = ResourcesPlugin.getSearchManager().
        query(query.toString());
    List<IResource>temp = queryResult.getHandles();
    for(IResource resource : temp) {
      jobsDisconnected.add(resource);
      jobsDisconnectedPaths.add(resource.getPath());
    }
    tViewer.setInput(jobsDisconnected);
   
  }


  /**
   * Method createColumns.
   * @param tViewer TableViewer
   */
  private void createColumns(TableViewer tViewer) {
    //create the columns that is going to display the individual rows based
    //on the input set using tViewer.setInput    
    TableViewerColumn col;
    
    col = new TableViewerColumn(tViewer, SWT.NONE);
    col.getColumn().setWidth(120);
    col.getColumn().setText("Name");
    col.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        return ((IResource)element).getName();
      }
    }); 
    
    col = new TableViewerColumn(tViewer, SWT.NONE);
    col.getColumn().setWidth(100);
    col.getColumn().setText("State");
    col.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        String state = ((IResource)element).getPropertyAsString(VeloTifConstants.JOB_STATUS);
        return state;
      }
    });

    col = new TableViewerColumn(tViewer, SWT.NONE);
    col.getColumn().setWidth(100);
    col.getColumn().setText("Machine");
    col.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        return ((IResource)element).getPropertyAsString(VeloTifConstants.JOB_MACHINE);
      }
    });
    
    col = new TableViewerColumn(tViewer, SWT.NONE);
    col.getColumn().setWidth(150);
    col.getColumn().setText("Last Modified");
    col.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        String strDate = ((IResource)element).getPropertyAsString(VeloConstants.PROP_MODIFIED);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
        return format.format(DateFormatUtility.parseJcrDate(strDate));
      }
    });
    
    col = new TableViewerColumn(tViewer, SWT.NONE);
    col.getColumn().setWidth(400);
    col.getColumn().setText("Job Path");
    col.setLabelProvider(new ColumnLabelProvider() {
      @Override
      public String getText(Object element) {
        return "";
       // return ToolUtils.getWikiContextPath(((IResource)element).getPath());
      }
    });
    
    TableColumn[] columns = tViewer.getTable().getColumns();
    int i =0;
    for(TableColumn item :columns){
      item.setData(false);
      item.setImage(null);
      item.addSelectionListener(new ColumnHeaderListener(tViewer,i++));
    }
   
  }


  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    // TODO Auto-generated method stub

  }
  
  /**
   */
  class ColumnHeaderListener  implements SelectionListener{

    private int colIndex;
    private TableViewer tViewer;

    /**
     * Constructor for ColumnHeaderListener.
     * @param tViewer TableViewer
     * @param colindex int
     */
    ColumnHeaderListener(TableViewer tViewer, int colindex){
      this.colIndex = colindex;
      this.tViewer = tViewer;
    }
    /**
     * Method widgetSelected.
     * @param e SelectionEvent
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
      TableColumn column = tViewer.getTable().getColumn(colIndex);
      if ((Boolean)(column.getData())) {
        column.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_UP, SharedImages.CAT_IMG_SIZE_16));
      } else {
        column.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_DOWN, SharedImages.CAT_IMG_SIZE_16));
      }
      sortObjects(colIndex);
    }

    /**
     * Method widgetDefaultSelected.
     * @param e SelectionEvent
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(SelectionEvent)
     */
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      
    }

    /**
     * Method sortObjects.
     * @param colIndex int
     */
    private void sortObjects(int colIndex) {
      Table table = tViewer.getTable();
      TableColumn column = table.getColumn(colIndex);
      table.setSortColumn(column);
      TableExplorerComparator comparator = getComparator(colIndex);
      comparator.setAscending((Boolean)column.getData());
      column.setData(!(Boolean)column.getData());
      tViewer.setSorter(new TableViewerSorter(comparator));
      for(TableColumn c: table.getColumns()){
        c.setImage(null);
      }
      if ((Boolean)column.getData()) {
        column.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_UP, SharedImages.CAT_IMG_SIZE_16));
      } else {
        column.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_DOWN, SharedImages.CAT_IMG_SIZE_16));
      }
    }
    
    /**
     * Method getComparator.
     * @param colIndex2 int
     * @return TableExplorerComparator
     */
    private TableExplorerComparator getComparator(int colIndex2) {
      // TODO Auto-generated method stub
      TableExplorerComparator comparator = new NameComparator();
      switch(colIndex2){
      case 0: 
        comparator = new MimetypeComparator();
        break;
      case 1:
        comparator = new MachineNameComparator();
        break;
      case 2:
        comparator = new ModifiedDateComparator();
        break;
      case 3:
        comparator = new NameComparator();
        break;
      case 4: 
        comparator = new PathComparator();
        break;
      }
      return comparator;
      
    }
    
  }
  
  /**
   */
  class MimetypeComparator extends TableExplorerComparator {

    /**
     * Method doCompare.
     * @param one IResource
     * @param two IResource
     * @return int
     */
    @Override
    protected int doCompare(IResource one, IResource two) {
      int value = 0;

      IResource resource1 = one;
      IResource resource2 = two;

      if (one instanceof ILinkedResource) {
        resource1 = ((ILinkedResource) one).getTarget();
      }

      if (two instanceof ILinkedResource) {
        resource2 = ((ILinkedResource) two).getTarget();
      }

      String type1 = resource1.getMimetype();
      String type2 = resource2.getMimetype();

      if (type1 != null && type2 != null) {
        value = STRING_COMPARATOR.compare(type1, type2);
      } else if (type1 != null) {
        value = 1;
      } else if (type2 != null) {
        value = -1;
      }

      return value;
    }
  }
  
  /**
   */
  class MachineNameComparator extends TableExplorerComparator {

    /**
     * Method doCompare.
     * @param one IResource
     * @param two IResource
     * @return int
     */
    @Override
    protected int doCompare(IResource one, IResource two) {
      int value = 0;

      IResource resource1 = one;
      IResource resource2 = two;

      if (one instanceof ILinkedResource) {
        resource1 = ((ILinkedResource) one).getTarget();
      }

      if (two instanceof ILinkedResource) {
        resource2 = ((ILinkedResource) two).getTarget();
      }

      String machine1 = resource1.getPropertyAsString(VeloTifConstants.JOB_MACHINE);
      String machine2 = resource2.getPropertyAsString(VeloTifConstants.JOB_MACHINE);

      if (machine1 != null && machine2 != null) {
        value = STRING_COMPARATOR.compare(machine1, machine2);
      } else if (machine1 != null) {
        value = 1;
      } else if (machine2 != null) {
        value = -1;
      }

      return value;
    }
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onEvent.
   * @param events IBatchNotification
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(IBatchNotification)
   */
  @Override
  public void onEvent(IBatchNotification events) {
    boolean needsRefresh = false;

    for (IResourceEvent event : events) {
      int idx = jobsDisconnectedPaths.indexOf(event.getPath());
      if(idx >=0) {

        if(event.hasChange(IResourceEvent.REMOVED)) {
          jobsDisconnected.remove(idx);
          jobsDisconnectedPaths.remove(idx);
          needsRefresh = true;
        
        } else if (event.hasChange(IResourceEvent.PROPERTY_CHANGED)) {
          IResource resource = jobsDisconnected.get(idx);
          String status = resource.getPropertyAsString(VeloTifConstants.JOB_STATUS);
          if( !status.equals(VeloTifConstants.STATUS_DISCONNECTED)) {
            jobsDisconnected.remove(idx);
            jobsDisconnectedPaths.remove(idx);
            needsRefresh = true;
          }
        }
      }
    }

    if (needsRefresh) {
      // refresh the view
      Display.getDefault().asyncExec(new Runnable() {
        @Override
        public void run() {
          if(tViewer != null) {
            tViewer.refresh();
            parent.layout(true, true);
          }
        }
      });
    }
  }

}
