/**
 * 
 */
package gov.pnnl.cat.web.app.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.osaf.cosmo.filters.BufferedRequestWrapper;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This filter checks to see if no authentication is provided.  If not, it tries to authenticate as guest.
 * If guest access fails, it returns 403.
 * @author d3k339
 *
 */
public class LandingPageWebScriptAuthFilter implements Filter {
  
  private ServletContext servletContext;
  private AuthenticationService authService;
  private PersonService personService;
  private TransactionService transactionService;

  private int maxMemoryBuffer = 1024*256;

  /* (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    
    // Wrap request so we can buffer the request in the event
    // it needs to be retried.  If the request isn't buffered,
    // then we can't consume the servlet inputstream again.
    BufferedRequestWrapper wrappedReq = new BufferedRequestWrapper((HttpServletRequest) req, maxMemoryBuffer);
    
    // Wrap response so we can trap 401 & 403 responses before they get to the client
    HttpResponseUnauthorizedWrapper wrappedResp = new HttpResponseUnauthorizedWrapper((HttpServletResponse) resp);
    
    // See if the request works
    chain.doFilter(wrappedReq, wrappedResp);

    // If response is 401, retry as guest
    if(wrappedResp.isUnauthorized()) {
      
      // reset the request
      wrappedResp.clearError();
      wrappedReq.retryRequest();
      wrappedReq.setForceGuest(true); // tell web script chain to use guest authentication
      chain.doFilter(wrappedReq, wrappedResp);
      
      //AuthenticationStatus status = AuthenticationHelper.authenticate(servletContext, wrappedReq, wrappedResp, true, true);

      // if response is forbidden, then return 401 to prompt for auth
      if(wrappedResp.isForbidden()) {
        wrappedResp.flushError(HttpServletResponse.SC_UNAUTHORIZED);
        
      } else {
        // Otherwise flush the error if necessary and proceed as normal.
        wrappedResp.flushError();
      }
      
      // make sure to clear out any session information from the response so
      // we aren't locked to guest authentication
      
    } else {
      // Otherwise flush the error if necessary and proceed as normal.
      wrappedResp.flushError();
 
    }
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    servletContext = config.getServletContext();
    
    // Get spring beans from the web app context
    WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
    authService = serviceRegistry.getAuthenticationService();
    transactionService = serviceRegistry.getTransactionService();
    
    // for the persomService, we need the version that is not permission checked.  
    // make sure we specify our own transaction when we use it
    personService = (PersonService) ctx.getBean("personService");   
    //ticketComponent = (TicketComponent)ctx.getBean("ticketComponent");

  }


}
