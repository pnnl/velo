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
package gov.pnnl.cat.alerts.wizards;

import gov.pnnl.cat.alerts.AlertService;
import gov.pnnl.cat.alerts.AlertsPlugin;
import gov.pnnl.cat.alerts.model.ISubscription.ChangeType;
import gov.pnnl.cat.alerts.model.ISubscription.Channel;
import gov.pnnl.cat.alerts.model.RepositorySubscription;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

/**
 */
public class AddSubscriptionToResourceWizard extends Wizard implements INewWizard {

	private IWorkbenchWindow workbench;
	private AddSubscriptionToResourceWizardPage page;
	private IResource selection;
	private IUser user;
	
	public AddSubscriptionToResourceWizard(){
		super();
	}
	
	/**
	 * Method init.
	 * @param workbench IWorkbench
	 * @param selection IStructuredSelection
	 * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench.getActiveWorkbenchWindow();
		this.selection = (IResource)selection.getFirstElement();
	}

	/**
	 * Method performFinish.
	 * @return boolean
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		boolean success = true;
		try{
			String dateStr = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date());
			Set<Channel> channels = page.getChannels();
			Set<ChangeType> changeTypes = page.getChangeTypes();
			
			RepositorySubscription subscription = new RepositorySubscription();
			subscription.setTitle(page.getSubscriptionName());
			subscription.setName(getUser().getUsername() + " " + dateStr);
			subscription.setChannels(channels.toArray(new Channel[channels.size()]));
			subscription.setFrequency(page.getFrequency());
			subscription.setUser(getUser());
			subscription.setChangeTypes(changeTypes.toArray(new ChangeType[changeTypes.size()]));
			subscription.setResource(getSelection());
			
			AlertService alertsService = AlertsPlugin.getDefault().getAlertsService();
		    alertsService.addSubscription(subscription);
		    
		}catch(Exception e){
		  ToolErrorHandler.handleError("An error occurred trying to create the subscription.", e, true);
			success = false;
		}
		return success;
	}

	/**
	 * Method addPages.
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	@Override
	public void addPages() {
		page = new AddSubscriptionToResourceWizardPage(getSelection(), getWorkbench(), getUser());
		addPage(page);
	}

	/**
	 * Method setSelection.
	 * @param selection IResource
	 */
	public void setSelection(IResource selection) {
		this.selection = selection;
	}

	/**
	 * Method getSelection.
	 * @return IResource
	 */
	public IResource getSelection() {
		return this.selection;
	}
	
	/**
	 * Method setUser.
	 * @param user IUser
	 */
	public void setUser(IUser user){
		this.user = user;
	}
	
	/**
	 * Method getUser.
	 * @return IUser
	 */
	public IUser getUser(){
		return this.user;
	}
	
	/**
	 * Method setWorkbench.
	 * @param workbench IWorkbenchWindow
	 */
	public void setWorkbench(IWorkbenchWindow workbench){
		this.workbench = workbench;
	}
	
	/**
	 * Method getWorkbench.
	 * @return IWorkbenchWindow
	 */
	public IWorkbenchWindow getWorkbench(){
		return this.workbench;
	}

}
