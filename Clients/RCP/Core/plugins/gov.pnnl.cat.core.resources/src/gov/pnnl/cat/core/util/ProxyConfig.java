package gov.pnnl.cat.core.util;

import gov.pnnl.cat.logging.CatLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.btr.proxy.search.ProxySearch;
import com.btr.proxy.search.ProxySearch.Strategy;
import com.btr.proxy.util.Logger;
import com.btr.proxy.util.Logger.LogBackEnd;
import com.btr.proxy.util.Logger.LogLevel;
import com.btr.proxy.util.PlatformUtil;
import com.btr.proxy.util.PlatformUtil.Platform;

public class ProxyConfig {
  private static final org.apache.log4j.Logger logger = CatLogger.getLogger(ProxyConfig.class);

  private String host; // proxy host
  private int port; // proxy port
  private Proxy proxy = null;
  private ProxySelector proxySelector = null;
  private String proxyUsername;
  private String proxyPassword;
  private String clientMachine;
  private String clientDomain;
  private String repositoryUrl;
  private String repositoryServer;
  private int repositoryPort;
  private String repositoryProtocol;

  private boolean proxyAuthenticationRequired;
  
  public ProxyConfig(String repositoryURL) {
    repositoryUrl = repositoryURL;
    init();
  }
  
  private void init() {
    System.setProperty("java.net.useSystemProxies", "true");
    initializeProxySettings();
    System.setProperty("java.net.useSystemProxies", "false");

  }
  
  private void initializeProxySettings() {
    logger.debug("ProxyConfig:  detecting proxy settings.");
    
    Logger.setBackend(new LogBackEnd() {
      
      public void log(Class<?> clazz, LogLevel loglevel, String msg,
          Object... params) {
        logger.debug(MessageFormat.format(msg, params));
      }
    
      public boolean isLogginEnabled(LogLevel logLevel) {
        return true;
      }
    });
    
    URL url;
    try {
      url = new URL(repositoryUrl);
      repositoryServer = url.getHost();
      repositoryPort = url.getPort();
      repositoryProtocol = url.getProtocol();
    } catch (MalformedURLException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to parse repository url.", e);
    }

    ProxySearch proxySearch = new ProxySearch();
    
    if (PlatformUtil.getCurrentPlattform() == Platform.WIN) {
      proxySearch.addStrategy(Strategy.IE);
      proxySearch.addStrategy(Strategy.FIREFOX);

    } else if (PlatformUtil.getCurrentPlattform() == Platform.LINUX) {
      proxySearch.addStrategy(Strategy.GNOME);
      proxySearch.addStrategy(Strategy.KDE);
      proxySearch.addStrategy(Strategy.FIREFOX);
    } 
   
    proxySearch.addStrategy(Strategy.JAVA);
    proxySearch.addStrategy(Strategy.OS_DEFAULT);
    proxySearch.addStrategy(Strategy.ENV_VAR);

    proxySelector = proxySearch.getProxySelector();

    if(proxySelector == null) {
      logger.debug("No proxy settings found!");
    } else {
    
      List<Proxy> proxies = null;
      try {
        proxies = proxySelector.select(new URI(repositoryUrl));
      } 
      catch (URISyntaxException e) {
        e.printStackTrace();
      }
      if (proxies != null) {
    	  
        for (Proxy curProxy : proxies) {
          
          logger.debug("proxy type : " + curProxy.type());
          InetSocketAddress addr = (InetSocketAddress) curProxy.address();
          if (addr != null) {
            proxy = curProxy;
            break;
          }
        }
      }
      
      if (proxy != null) {
        InetSocketAddress addr = (InetSocketAddress) proxy.address();
        setHost(addr.getHostName());
        setPort(addr.getPort());
        Type type = this.proxy.type();
        if(!type.equals(Type.HTTP)) {
          throw new RuntimeException("Can only support HTTP proxies");
        }

        // Set default username if it exists
        // TODO: this may only work on windows
        String username = System.getenv("USERNAME"); 
        if(username != null) {
          setProxyUsername(username); 
        }

        String computername = System.getenv("COMPUTERNAME");
        if( computername == null) {
          computername = System.getenv("HOSTNAME");          
        } 
        if(computername != null) {
          setClientMachine(computername);
        } 
        
        String domain = System.getenv("USERDOMAIN");
        if(domain != null) {
          setClientDomain(domain);
        }
        
        // Find out if the proxy requires authentication
        logger.debug("Testing if proxy authentication required...");
        proxyAuthenticationRequired = testProxyAuthentication();        
      }

    }
  }
  
  private boolean testProxyAuthentication() {
    int status = 0;
    
    CloseableHttpClient httpclient = HttpClients.createDefault();
    try {
        
        HttpHost target = new HttpHost(repositoryServer, repositoryPort, repositoryProtocol);
        HttpHost proxy = new HttpHost(getHost(), getPort(), "http"); // we only support http proxy

        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
        HttpGet request = new HttpGet("/");
        request.setConfig(config);

        logger.debug("executing request to " + target + " via " + proxy);
        CloseableHttpResponse response = httpclient.execute(target, request);
        try {
            status = response.getStatusLine().getStatusCode();
            logger.debug("response = " + status);

            HttpEntity entity = response.getEntity();

            logger.debug("----------------------------------------");
            logger.debug(response.getStatusLine());
            Header[] headers = response.getAllHeaders();
            for (int i = 0; i<headers.length; i++) {
                logger.debug(headers[i]);
            }
            logger.debug("----------------------------------------");

        } catch (Throwable e) {
          e.printStackTrace();
        } finally {        
            response.close();
        }
    } catch (Throwable e) {
      e.printStackTrace();
    } finally {
        try {
          httpclient.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    return status == 407;
    
  }
  
  public String getRepositoryProtocol() {
    return this.repositoryProtocol;
  }

  /*************************************************************************
   * @return Returns the host.
   ************************************************************************/
  
  public String getHost() {
    return host;
  }

  /*************************************************************************
   * @param host The host to set.
   ************************************************************************/
  
  public void setHost(String host) {
    this.host = host;
    System.setProperty("http.proxyHost", host);
  }

  /*************************************************************************
   * @param port The port to set.
   ************************************************************************/
  
  public void setPort(int port) {
    this.port = port;
    System.setProperty("http.proxyPort", ""+port);
  }

  /*************************************************************************
   * @return Returns the port.
   ************************************************************************/
  
  public int getPort() {
    return port;
  }

  /*************************************************************************
   * @return Returns the proxy.
   ************************************************************************/
  
  public Proxy getProxy() {
    return proxy;
  }

  /*************************************************************************
   * @return Returns the proxySelector.
   ************************************************************************/
  
  public ProxySelector getProxySelector() {
    return proxySelector;
  }

  /*************************************************************************
   * @return Returns the proxyUsername.
   ************************************************************************/
  
  public String getProxyUsername() {
    return proxyUsername;
  }

  /*************************************************************************
   * @param proxyUsername The proxyUsername to set.
   ************************************************************************/
  
  public void setProxyUsername(String proxyUsername) {
    this.proxyUsername = proxyUsername;
    System.getProperties().setProperty("http.proxyUser", proxyUsername);
  }

  /*************************************************************************
   * @return Returns the proxyPassword.
   ************************************************************************/
  
  public String getProxyPassword() {
    return proxyPassword;
  }

  /*************************************************************************
   * @param proxyPassword The proxyPassword to set.
   ************************************************************************/
  
  public void setProxyPassword(String proxyPassword) {
    this.proxyPassword = proxyPassword;
    System.getProperties().setProperty("http.proxyPassword", proxyPassword);    
  }

  /*************************************************************************
   * @return Returns the clientMachine.
   ************************************************************************/
  
  public String getClientMachine() {
    return clientMachine;
  }

  /*************************************************************************
   * @param clientMachine The clientMachine to set.
   ************************************************************************/
  
  public void setClientMachine(String clientMachine) {
    this.clientMachine = clientMachine;
  }

  /*************************************************************************
   * @return Returns the clientDomain.
   ************************************************************************/
  
  public String getClientDomain() {
    return clientDomain;
  }

  /*************************************************************************
   * @param clientDomain The clientDomain to set.
   ************************************************************************/
  
  public void setClientDomain(String clientDomain) {
    this.clientDomain = clientDomain;
  }

  /*************************************************************************
   * @return Returns the proxyAuthenticationRequired.
   ************************************************************************/
  
  public boolean isProxyAuthenticationRequired() {
    return proxyAuthenticationRequired;
  }
  
}