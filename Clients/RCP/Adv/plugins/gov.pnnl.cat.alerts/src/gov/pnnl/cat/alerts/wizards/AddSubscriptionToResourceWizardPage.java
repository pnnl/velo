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

import gov.pnnl.cat.alerts.model.ISubscription.ChangeType;
import gov.pnnl.cat.alerts.model.ISubscription.Channel;
import gov.pnnl.cat.alerts.model.ISubscription.Frequency;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.ui.images.SharedImages;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchWindow;

/**
 */
public class AddSubscriptionToResourceWizardPage extends WizardPage implements Listener {

	private final static String MISSING_SUB_NAME = "Please enter a subscription name";
	private final static String MISSING_DELIVERY = "Please select how you want your alerts to be sent";
	private final static String MISSING_FREQUENCY = "Please select how often you want the alerts to be sent";
	private final static String MISSING_ACTION = "Please select what actions you want to be alerted for";
	private final static String INVALID_CHARS = "The subscription name cannot contain any of the following characters: %s";
	
	private String currentMessage = "";
	private IResource selection = null;
	private IWorkbenchWindow workbench = null;
	private Font boldFont = null;
	
	//Buttons
	private Text subscriptionNameText;
	private Button catAlertsChk;
	private Button emailAlertsChk;
	private Button hourlyRadio;
	private Button weeklyRadio;
	private Button dailyRadio;
	private Button createdChk;
	private Button modifiedChk;
	private Button deletedChk;
	
	//User
	IUser user;
	
	//Subscription data
	private Set<Channel> channels = new HashSet<Channel>();
	private Frequency frequency;
	private Set<ChangeType> changeTypes = new HashSet<ChangeType>();
	private boolean resourceFolder;

	/**
	 * Constructor for AddSubscriptionToResourceWizardPage.
	 * @param selection IResource
	 * @param workbenchWindow IWorkbenchWindow
	 * @param user IUser
	 */
	public AddSubscriptionToResourceWizardPage(IResource selection,
			IWorkbenchWindow workbenchWindow, IUser user) {
		super("addSubscriptionToResource");
		setTitle("Resource Subscription");
		setDescription("Add a subscription to the selected resource to get alerts");
		setImageDescriptor(SharedImages.getInstance().getImageDescriptor(
				SharedImages.CAT_IMG_SEARCH_SUB, SharedImages.CAT_IMG_SIZE_64));

		this.workbench = workbenchWindow;
		this.selection = selection;
		this.user = user;
		
		setResourceFolder(selection.isType(IResource.FOLDER));
	}

	/**
	 * Create composite that will contain all of the wizard page elements
	 * @param parent Composite
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL));

		setControl(container);
		
		initFont();
		
		createPage(container);
		
		updatePageCompletion();
		setPageComplete(false);
	}
	
	/**
	 * Initialize the bold font to use in display
	 */
	public void initFont(){
		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		fontData.setHeight(11);
		boldFont = new Font(Display.getCurrent(), fontData);
	}
	
	/**
	 * Creates the elements for the wizard page
	 * @param parent
	 */
	public void createPage(Composite parent){

		/****Subscription Name Section***/
	    Composite subscriptionNameComposite = new Composite(parent, SWT.NONE);
	    subscriptionNameComposite.setLayout(new GridLayout(2, false));
	    subscriptionNameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
	    Label subscriptionNameLabel = new Label(subscriptionNameComposite, SWT.NONE);
		subscriptionNameLabel.setText("Subscription Name: ");
		
		subscriptionNameText = new Text(subscriptionNameComposite, SWT.BORDER);
		GridData textPathData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		subscriptionNameText.setText(getSelection().getName() + " repository subscription");
		subscriptionNameText.setLayoutData(textPathData);
		
		Composite content = new Composite(parent, SWT.NONE);
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 2;
	    layout.makeColumnsEqualWidth = true;
	    layout.marginWidth = 0;
	    content.setLayout(layout);
	    content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
		/****Selected Resource Section***/
		//Create composite that aligns to the right
		Composite selectedResourceComposite = new Composite(content, SWT.NONE);
		GridData selectedResourceGridData = new GridData();
		
		//Make horizontal span 2 so it stretches the parent columns
		selectedResourceGridData.horizontalSpan = 2;
		//Right align
		selectedResourceGridData.horizontalAlignment = SWT.BEGINNING;
		GridLayout selectedResourceGridLayout = new GridLayout();
		selectedResourceGridLayout.horizontalSpacing = 0;
		selectedResourceGridLayout.marginTop =  -8;
		selectedResourceGridLayout.marginBottom =  15;
		selectedResourceGridLayout.makeColumnsEqualWidth = false;
		selectedResourceGridLayout.numColumns = 3;
		selectedResourceComposite.setLayout(selectedResourceGridLayout);
		selectedResourceComposite.setLayoutData(selectedResourceGridData);
		
		Label selectedResourceLabel = new Label(selectedResourceComposite, SWT.NONE);
		selectedResourceLabel.setText("Resource selected:");
		
		//Custom label to hold the image of the selected resource
		CLabel resourceImage = new CLabel(selectedResourceComposite, SWT.NONE);
		resourceImage.setImage(SharedImages.getInstance().getImageForResource(getSelection(), SharedImages.CAT_IMG_SIZE_16));
		
		//The name of the selected resource
		Label selectedResourceNameLabel = new Label(selectedResourceComposite, SWT.NONE);
		selectedResourceNameLabel.setText(getSelection().getName());
		selectedResourceNameLabel.setFont(boldFont);
	    
		/****Alerts Delivery Section***/
		Group alertsDeliveryGroup = new Group(content, SWT.NONE | SWT.WRAP);
		alertsDeliveryGroup.setLayout(new GridLayout(1, true));
		alertsDeliveryGroup.setText("Send Alerts To");
		alertsDeliveryGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
		
		catAlertsChk = new Button(alertsDeliveryGroup, SWT.CHECK);
		catAlertsChk.setText("Alerts Dashboard");
		
		emailAlertsChk = new Button(alertsDeliveryGroup, SWT.CHECK);
		emailAlertsChk.setText("My Email (" + getUser().getEmail() + ")");
		
		/****Alerts Frequency Section***/
		Group alertsFrequencyGroup = new Group(content, SWT.NONE);
		alertsFrequencyGroup.setLayout(new GridLayout(1, true));
		alertsFrequencyGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
		alertsFrequencyGroup.setText("Send Alerts");
		
		hourlyRadio = new Button(alertsFrequencyGroup, SWT.RADIO);
		hourlyRadio.setText("Hourly");
		
		dailyRadio = new Button(alertsFrequencyGroup, SWT.RADIO);
		dailyRadio.setText("Daily");

		weeklyRadio = new Button(alertsFrequencyGroup, SWT.RADIO);
		weeklyRadio.setText("Weekly");
		
		/****Alerts Actions Section***/
		//Change group description text based on if the selected resource is a file or folder
		String alertsActionGroupText = isResourceFolder() ? "Alert me when folder contents are" : "Alert me when file is";
		
		Group alertsActionGroup = new Group(parent, SWT.NONE);
		alertsActionGroup.setText(alertsActionGroupText);
		alertsActionGroup.setLayout(new GridLayout(3, true));
		alertsActionGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
		
		//Check if resource is a file or folder
		//If resource is a folder show "Created" check box, otherwise don't
		if(isResourceFolder()){
			createdChk = new Button(alertsActionGroup, SWT.CHECK);
			createdChk.setText("Created");
			createdChk.addListener(SWT.Selection, this);
		}
		
		modifiedChk = new Button(alertsActionGroup, SWT.CHECK);
		modifiedChk.setText("Modified");
		
		deletedChk = new Button(alertsActionGroup, SWT.CHECK);
		deletedChk.setText("Deleted");
		
		//Listeners
		subscriptionNameText.addListener(SWT.Modify, this);
		catAlertsChk.addListener(SWT.Selection, this);
		emailAlertsChk.addListener(SWT.Selection, this);
		hourlyRadio.addListener(SWT.Selection, this);
		dailyRadio.addListener(SWT.Selection, this);
		weeklyRadio.addListener(SWT.Selection, this);
		modifiedChk.addListener(SWT.Selection, this);
		deletedChk.addListener(SWT.Selection, this);
		
	}
	
	/**
	 * Handles all of the events for the widgets on the page.
	 * 
	 * This includes modification of text in a text field and when a button is
	 * pressed
	 * 
	 * @param e Event
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
	 */
	@Override
	public void handleEvent(Event e) {
		Widget source = e.widget;
		
		if(source == catAlertsChk){
			if(catAlertsChk.getSelection()){
				channels.add(Channel.REPOSITORY);
			}else{
				channels.remove(Channel.REPOSITORY);
			}
		}
		if(source == emailAlertsChk){
			if(emailAlertsChk.getSelection()){
				channels.add(Channel.EMAIL);
			}else{
				channels.remove(Channel.EMAIL);
			}
		}
		if(source == hourlyRadio){
			if(hourlyRadio.getSelection()){
				frequency = Frequency.HOURLY;
			}
		}
		if(source == dailyRadio){
			if(dailyRadio.getSelection()){
				frequency = Frequency.DAILY;
			}
		}
		if(source == weeklyRadio){
			if(weeklyRadio.getSelection()){
				frequency = Frequency.WEEKLY;
			}
		}
		if(source == createdChk){
			if(createdChk.getSelection()){
				changeTypes.add(ChangeType.NEW);
			}else{
				changeTypes.remove(ChangeType.NEW);
			}
		}
		if(source == modifiedChk){
			if(modifiedChk.getSelection()){
				changeTypes.add(ChangeType.MODIFIED);
			}else{
				changeTypes.remove(ChangeType.MODIFIED);
			}
		}
		if(source == deletedChk){
			if(deletedChk.getSelection()){
				changeTypes.add(ChangeType.DELETED);
			}else{
				changeTypes.remove(ChangeType.DELETED);
			}
		}
		
		updatePageCompletion();
	}

	/**
	 * Update the page completion by validating actions(updated, created, deleted), 
	 * subscription frequency (weekly, daily, hourly) and delivery method (CAT, email) 
	 * 
	 * Sets the dialog error message and page completion for the component
	 */
	private void updatePageCompletion() {
		boolean complete = validateSubscriptionNameLength() && validateDelivery() 
			&& validateFrequency() && validateActions() && validateSubscriptionName();
		if (complete) {
			setErrorMessage(null);
			// Setting the page complete to true will enable the "Finish" button
			setPageComplete(true);
		} else {
			setErrorMessage(currentMessage);
			// Setting the page complete to false will disable the "Finish"
			// button
			setPageComplete(false);
		}
	}
	
	/**
	 * Validate the subscription name control to make sure the user 
	 * has entered text
	
	 * @return boolean */
	private boolean validateSubscriptionNameLength(){
		if(subscriptionNameText.getText().trim().length() > 0){
			return true;
		}
		
		setCurrentMessage(MISSING_SUB_NAME);
		return false;
	}
	
	/**
	 * Validate subscription name for any invalid characters
	
	 * @return boolean */
	private boolean validateSubscriptionName(){
		if(subscriptionNameText.getText().matches(IResource.invalidCharactersRegex)){
			setCurrentMessage(String.format(INVALID_CHARS, IResource.INVALID_CHARS));
			return false;
		}
		
		return true;
	}
	
	/**
	 * Validate that the user has selected a delivery option
	
	 * @return boolean */
	private boolean validateDelivery(){
		if(catAlertsChk.getSelection() || emailAlertsChk.getSelection()){
			return true;
		}
		
		setCurrentMessage(MISSING_DELIVERY);
		return false;
	}
	
	/**
	 * Validate that the user has selected a delivery frequency
	
	 * @return boolean */
	private boolean validateFrequency(){
		if(hourlyRadio.getSelection() || dailyRadio.getSelection() || weeklyRadio.getSelection()){
			return true;
		}
		
		setCurrentMessage(MISSING_FREQUENCY);
		return false;
	}
	
	/**
	 * Method validateActions.
	 * @return boolean
	 */
	private boolean validateActions(){
		//Check to see if Resource is a file or folder
		//this determines which checkboxes to validate
		if((createdChk != null && createdChk.getSelection()) || modifiedChk.getSelection() || deletedChk.getSelection()){
			return true;
		}
		
		setCurrentMessage(MISSING_ACTION);
		
		return false;
	}
	
	/**
	 * Get the current error message for the page
	 * 
	
	 * @return a string describing the current error message on the page */
	public String getCurrentMessage() {
		return currentMessage;
	}

	/**
	 * Set the current error message for the page
	 * 
	 * @param currentMessage
	 *            String
	 */
	public void setCurrentMessage(String currentMessage) {
		this.currentMessage = currentMessage;
	}

	/**
	 * Method getSelection.
	 * @return IResource
	 */
	public IResource getSelection(){
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
	 * Method isResourceFolder.
	 * @return boolean
	 */
	public boolean isResourceFolder(){
		return this.resourceFolder;
	}

	/**
	 * Method setResourceFolder.
	 * @param resourceFolder boolean
	 */
	public void setResourceFolder(boolean resourceFolder){
		this.resourceFolder = resourceFolder;
	}
	
	/**
	 * Method getSubscriptionName.
	 * @return String
	 */
	public String getSubscriptionName() {
		return subscriptionNameText.getText().trim();
	}

	/**
	 * Method getChannels.
	 * @return Set<Channel>
	 */
	public Set<Channel> getChannels() {
		return channels;
	}

	/**
	 * Method setChannels.
	 * @param channels Set<Channel>
	 */
	public void setChannels(Set<Channel> channels) {
		this.channels = channels;
	}

	/**
	 * Method getFrequency.
	 * @return Frequency
	 */
	public Frequency getFrequency() {
		return frequency;
	}

	/**
	 * Method setFrequency.
	 * @param frequency Frequency
	 */
	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}

	/**
	 * Method getChangeTypes.
	 * @return Set<ChangeType>
	 */
	public Set<ChangeType> getChangeTypes() {
		return changeTypes;
	}

	/**
	 * Method setChangeTypes.
	 * @param changeTypes Set<ChangeType>
	 */
	public void setChangeTypes(Set<ChangeType> changeTypes) {
		this.changeTypes = changeTypes;
	}
	
}
