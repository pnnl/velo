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

import gov.pnnl.cat.alerts.model.ISubscription.Channel;
import gov.pnnl.cat.alerts.model.ISubscription.Frequency;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.ui.rcp.CatPerspectiveIDs;
import gov.pnnl.cat.ui.rcp.CatViewIDs;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 */
public class NewSearchSubscriptionWizardPage extends WizardPage {

  private Text titleText;
  private String errorMessage;
  private Button alertsSummaryCheckbox;
  private Button emailCheckbox;
  private String defaultTitle;
  private IUser user;

  // user input
  private String title = "";
  private Set<Channel> channels = new HashSet<Channel>();
  private Frequency frequency;

  /**
   * Create the wizard
   * @param errorMessage String
   * @param defaultTitle String
   * @param user IUser
   */
  public NewSearchSubscriptionWizardPage(String errorMessage, String defaultTitle, IUser user) {
    super("searchSubscriptionPage");
    setTitle("New Search Subscription");
    setDescription("Select a name and delivery options for the new subscription.");
    this.defaultTitle = defaultTitle;
    this.errorMessage = errorMessage;
    this.user = user;
    if (defaultTitle != null) {
      title = defaultTitle;
    }
  }

  /**
   * Create contents of the wizard
   * @param parent
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    
    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    container.setLayout(gridLayout);

    boolean validState = errorMessage == null;

    if (!validState) {
      final Label errorLabel = new Label(container, SWT.WRAP);
      errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      errorLabel.setText("In order to subscribe to search results, you must switch to " +
      		"the Search Perspective and perform a search.");
  
      final Link searchLink = new Link(container, SWT.NONE);
      searchLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      searchLink.setText("<a>Switch to the Search Perspective now and perform a search.</a>");
      searchLink.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          getWizard().performCancel();
          getWizard().getContainer().getShell().close();

          // spin the UI thread to make sure we don't become unresponsive
          while (Display.getCurrent().readAndDispatch());

          try {
            PlatformUI.getWorkbench().showPerspective(CatPerspectiveIDs.SEARCH,
                PlatformUI.getWorkbench().getActiveWorkbenchWindow());

            // spin a little more
            while (Display.getCurrent().readAndDispatch());
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(CatViewIDs.SEARCH);
          } catch (WorkbenchException ex) {
            ToolErrorHandler.handleError("An error occurred switching to the Search perspective.", ex, true);
          }
        }
      });
  
      final Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
      final GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
      gd_label.verticalIndent = 10;
      label.setLayoutData(gd_label);

      setErrorMessage("Unable to create Subscription: " + errorMessage);
      setPageComplete(false);
    }

    final Label titleLabel = new Label(container, SWT.NONE);
    titleLabel.setText("Name:");

    titleText = new Text(container, SWT.BORDER);
    titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    titleText.setEnabled(validState);
    titleText.setFocus();
    titleText.setSelection(0, titleText.getText().length());
    titleText.setTextLimit(64);
    if (defaultTitle != null) {
      titleText.setText(defaultTitle);
    }
    titleText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        title = titleText.getText();
        validateInput();
      }
    });

    final Label channelLabel = new Label(container, SWT.NONE);
    final GridData gd_channelLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
    gd_channelLabel.verticalIndent = 10;
    channelLabel.setLayoutData(gd_channelLabel);
//    channelLabel.setFont(
//        JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
    channelLabel.setText("Send Alerts to:");

    alertsSummaryCheckbox = new Button(container, SWT.CHECK);
    final GridData gd_alertsSummaryButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
    gd_alertsSummaryButton.horizontalIndent = 10;
    alertsSummaryCheckbox.setLayoutData(gd_alertsSummaryButton);
    alertsSummaryCheckbox.setText("CAT Alerts Summary");
    alertsSummaryCheckbox.setEnabled(validState);
    alertsSummaryCheckbox.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (alertsSummaryCheckbox.getSelection()) {
          channels.add(Channel.REPOSITORY);
        } else {
          channels.remove(Channel.REPOSITORY);
        }
        validateInput();
      }
    });

    emailCheckbox = new Button(container, SWT.CHECK);
    final GridData gd_emailButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
    gd_emailButton.horizontalIndent = 10;
    emailCheckbox.setLayoutData(gd_emailButton);

    String emailText;
    if (user != null && user.getEmail() != null && user.getEmail().length() > 0) {
      emailText = String.format("My Email (%s)", user.getEmail());
      emailCheckbox.setEnabled(validState);
    } else {
      emailText = String.format("My Email (no email on record)");
      emailCheckbox.setEnabled(false);
    }

    emailCheckbox.setText(emailText);

    emailCheckbox.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (alertsSummaryCheckbox.getSelection()) {
          channels.add(Channel.EMAIL);
        } else {
          channels.remove(Channel.EMAIL);
        }
        validateInput();
      }
    });

    final Label frequencyLabel = new Label(container, SWT.NONE);
    final GridData gd_frequencyLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
    gd_frequencyLabel.verticalIndent = 10;
    frequencyLabel.setLayoutData(gd_frequencyLabel);
//    frequencyLabel.setFont(
//        JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
    frequencyLabel.setText("How often:");

    final Button dailyButton = new Button(container, SWT.RADIO);
    final GridData gd_dailyButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
    gd_dailyButton.horizontalIndent = 10;
    dailyButton.setLayoutData(gd_dailyButton);
    dailyButton.setText("Daily");
    dailyButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        frequency = Frequency.DAILY;
      }
    });
    dailyButton.setEnabled(validState);

    final Button weeklyButton = new Button(container, SWT.RADIO);
    final GridData gd_weeklyButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
    gd_weeklyButton.horizontalIndent = 10;
    weeklyButton.setLayoutData(gd_weeklyButton);
    weeklyButton.setText("Weekly");
    weeklyButton.setEnabled(validState);
    weeklyButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        frequency = Frequency.WEEKLY;
      }
    });

    // set default values
    alertsSummaryCheckbox.setSelection(true);
    channels.add(Channel.REPOSITORY);

    dailyButton.setSelection(true);
    frequency = Frequency.DAILY;

    //
    setControl(container);
  }

  private void validateInput() {
    if (!alertsSummaryCheckbox.getSelection() &&
        !emailCheckbox.getSelection()) {
      setErrorMessage("You must select at least one alert delivery option");
      setPageComplete(false);
    } else if (title.matches(IResource.invalidCharactersRegex)) {
      setErrorMessage(String.format("The subscription name cannot contain any of the following characters: %s", IResource.INVALID_CHARS));
      setPageComplete(false);
    } else {
      setErrorMessage(null);
      setPageComplete(true);
    }
  }

  /**
  
   * @return the title * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
   */
  public String getTitle() {
    return title;
  }

  /**
  
   * @return the channels */
  public Set<Channel> getChannels() {
    return channels;
  }

  /**
  
   * @return the frequency */
  public Frequency getFrequency() {
    return frequency;
  }
}
