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
package gov.pnnl.cat.web.app.servlet;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.bean.repository.User;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 */
public class CatAuthServletFilter implements Filter {
	
	private static final String PARAM_TICKET = "ticket";
	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_AUTH_SECRET_KEY = "catAuthKey";
	private static final String PARAM_AUTH_SECRET_VALUE = "3d7da6bc3be411ddbad5b59c019bb77d";

	private TicketComponent m_ticketComponent;
	private ServletContext m_context;

	private AuthenticationService m_authService;
	private PersonService m_personService;
	private TransactionService m_transactionService;


	/**
	 * Method destroy.
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * Method doFilter.
	 * @param req ServletRequest
	 * @param resp ServletResponse
	 * @param chain FilterChain
	 * @throws IOException
	 * @throws ServletException
	 * @see javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

		// assume it's an HTTP request
		HttpServletRequest httpReq = (HttpServletRequest) req;

		BufferedReader reader = req.getReader();
		reader.mark(1024);
		
		// look for a ticket parameter
		if (httpReq.getParameter(PARAM_TICKET) == null) {

			// no ticket found.  see if the other needed params are attached
			String secretkey = httpReq.getParameter(PARAM_AUTH_SECRET_KEY);
			String username = httpReq.getParameter(PARAM_USERNAME);
			
			if ((secretkey != null) && (secretkey.equals(PARAM_AUTH_SECRET_VALUE)) && (username != null)) {
				// all info verified, generate a ticket for this user
				CatAuthFilterTransactionWork work = new CatAuthFilterTransactionWork(username);
				User user = m_transactionService.getRetryingTransactionHelper().doInTransaction(work, true);

				// see if we have a valid user
				if (user != null) {
					// user appears valid
					// wrap the HttpServletRequest so the downstream servlet can extract our fabricated ticket
					CatHttpServletRequest catHttpRequest = new CatHttpServletRequest(httpReq);
					catHttpRequest.setAuthTicket(user.getTicket());
					
					reader.reset();

					// continue the chain with our wrapped request object
					chain.doFilter(catHttpRequest, resp);
					//m_ticketComponent.invalidateTicketById(user.getTicket()); // NEEDS TO BE IN TX
					return;
				}
			}
		}
		// something happened above, so continue the chain using the original objects
		chain.doFilter(req, resp);

	}

	/**
	 * Method init.
	 * @param config FilterConfig
	 * @throws ServletException
	 * @see javax.servlet.Filter#init(FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		m_context = config.getServletContext();

		// Setup the authentication context

		WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(m_context);

		// grab a reference to the beans we need
		ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
		m_authService = serviceRegistry.getAuthenticationService();
		
		// for the persomService, we need the version that is not permission checked.  
		// make sure we specify our own transaction when we use it
		m_personService = (PersonService) ctx.getBean("personService");   
		m_ticketComponent = (TicketComponent)ctx.getBean("ticketComponent");
		m_transactionService = serviceRegistry.getTransactionService();

	}

	/**
	 */
	public class CatAuthFilterTransactionWork implements RetryingTransactionCallback<User> {
		private User user;
		private String username;
		
		/**
		 * Constructor for CatAuthFilterTransactionWork.
		 * @param username String
		 */
		public CatAuthFilterTransactionWork(String username) {
			this.username = username;
		}

		/**
		 * Method execute.
		 * @return User
		 * @throws Throwable
		 * @see org.alfresco.repo.transaction.RetryingTransactionHelper$RetryingTransactionCallback#execute()
		 */
		public User execute() throws Throwable {
			// no username specified.  return set the user object to null
			if (this.username == null) {
				return null;
			}
			
			// try to create a new user object
			user = new User(username, m_ticketComponent.getCurrentTicket(username, true), m_personService.getPerson(username));
			try {
				// now, authenticate this user and see if it will fly
				m_authService.validate(user.getTicket());
			} catch (AuthenticationException ae) {
				// we couldn't authenticate this user, set the user to null
				return null;
			}
			// we have to return something....
			return user;
		}

	}

	/**
	 */
	public class CatHttpServletRequest extends HttpServletRequestWrapper {
		private String authTicket;
		private HttpServletRequest req;

		/**
		 * Constructor for CatHttpServletRequest.
		 * @param req HttpServletRequest
		 */
		public CatHttpServletRequest(HttpServletRequest req) {
			super(req);
			this.req = req;
		}

		/**
		 * Method setAuthTicket.
		 * @param authTicket String
		 */
		public void setAuthTicket(String authTicket) {
			this.authTicket = authTicket;
		}

		/**
		 * Return the "ticket" parameter if asked for it
		 * otherwise, defer to the provided HttpServletRequest
		 * @param name String
		 * @return String
		 * @see javax.servlet.ServletRequest#getParameter(String)
		 */
		public String getParameter(String name) {
			if (name.equals(PARAM_TICKET)) {
				return authTicket;				
			} else {
				return req.getParameter(name);
			}
		}

	}

}
