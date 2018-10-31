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
import gov.pnnl.cat.alerts.model.ISubscription.Channel;
import gov.pnnl.cat.alerts.model.ISubscription.Frequency;
import gov.pnnl.cat.alerts.model.SearchSubscription;
import gov.pnnl.cat.alerts.model.SearchSubscription.Trigger;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.search.advanced.query.AdvancedSearchQuery;
import gov.pnnl.cat.search.basic.query.BasicSearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchQuery;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewPart;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 */
public class NewSearchSubscriptionWizard extends Wizard implements INewWizard {

  public final static String ID = "gov.pnnl.cat.alerts.newSearchSubscriptionWizard";

  private NewSearchSubscriptionWizardPage searchSubscriptionPage;

  public NewSearchSubscriptionWizard() {
    setWindowTitle("New Search Subscription");
    setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(AlertsPlugin.PLUGIN_ID, "icons/wizban/search_subscription.png"));
  }

  /**
   * Method init.
   * @param workbench IWorkbench
   * @param selection IStructuredSelection
   * @see org.eclipse.ui.IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
   */
  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    ISearchQuery query = null;
    String defaultTitle = "";
    String errorMsg = null;

    try {
      query = getValidSearchResult();
    } catch (NoSearchResultsException e) {
      errorMsg = e.getMessage();
    }

    if (query != null) {
      if (query instanceof BasicSearchQuery) {
        BasicSearchQuery basicQuery = (BasicSearchQuery) query;
        defaultTitle = String.format("Search results for '%s'", basicQuery.getSearchString());
      }
    }

    IUser user;
    try {
      user = ResourcesPlugin.getSecurityManager().getActiveUser();
      searchSubscriptionPage = new NewSearchSubscriptionWizardPage(errorMsg, defaultTitle, user);
      addPage(searchSubscriptionPage);
    } catch (Exception e) {
      ToolErrorHandler.handleError("An error occurred retrieving the current user profile.", e, true);
    }
  }

  /**
   * Returns a valid <code>ISearchQuery</code> if such a query is active,
   * or null.
  
   * @return BasicSearchQuery
   * @throws NoSearchResultsException
   */
  private BasicSearchQuery getValidSearchResult() throws NoSearchResultsException {
    BasicSearchQuery validQuery = null;
    ISearchResultViewPart searchResultView = NewSearchUI.getSearchResultView();

    if (searchResultView == null) {
      throw new NoSearchResultsException("Search Results view is not open.");
    } else {
      ISearchResult currentSearchResult = searchResultView.getCurrentSearchResult();

      if (currentSearchResult == null) {
        throw new NoSearchResultsException("No searches to which you can subscribe.");
      } else {
        ISearchQuery query = currentSearchResult.getQuery();

        if (query instanceof BasicSearchQuery ||
            query instanceof AdvancedSearchQuery) {
          // we can do this because AdvancedSearchQuery extends BasicSearchQuery
          validQuery = (BasicSearchQuery) query;
        } else {
          throw new NoSearchResultsException("The active search does not support subscriptions.");
        }
      }
    }

    return validQuery;
  }

  /**
   * Method performFinish.
   * @return boolean
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  @Override
  public boolean performFinish() {
    boolean worked = true;

    try {
      String title = searchSubscriptionPage.getTitle();
      Set<Channel> channels = searchSubscriptionPage.getChannels();
      Frequency frequency = searchSubscriptionPage.getFrequency();
      IUser user = ResourcesPlugin.getSecurityManager().getActiveUser();
      String dateStr = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date());
      Trigger[] triggers = {Trigger.NEW, Trigger.MODIFIED};

      SearchSubscription subscription = new SearchSubscription();

      subscription.setChannels(channels.toArray(new Channel[channels.size()]));
      subscription.setFrequency(frequency);
      subscription.setName(user.getUsername() + " " + dateStr);
      subscription.setQuery(getValidSearchResult().buildSearchQuery());
      subscription.setTitle(title);
      subscription.setTriggers(triggers);
      subscription.setUser(user);

      AlertService alertsService = AlertsPlugin.getDefault().getAlertsService();
      alertsService.addSubscription(subscription);

    } catch (Exception e) {
      ToolErrorHandler.handleError("An error occurred trying to create the subscription.", e, true);
      worked = false;
    }

    return worked;
  }

  /**
   */
  private class NoSearchResultsException extends Exception {
    /**
     * Constructor for NoSearchResultsException.
     * @param message String
     */
    public NoSearchResultsException(String message) {
      super(message);
    }
  }
}
