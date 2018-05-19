/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nettyproxyserver;

import javax.net.ssl.SSLEngine;

/**
 *
 * @author manju
 */
public interface ProxyServer {
    
  public  void startProxyServer(int port);
  public  void shutdownProxyServer(int port);
  public  void setMitmManager(SSLEngine engine);
  public boolean detectSystemProxy(String pacurl);
  public void addHttpRequestInterceptor(IHttpRequestInterceptor interceptor); 
  public void addHttpResponseInterceptor(IHttpResponseInterceptor interceptor);
  public void StoreHttpSessionInDatabase(String ConnectionString);
    
}
