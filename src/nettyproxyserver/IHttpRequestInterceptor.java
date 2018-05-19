/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nettyproxyserver;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 *
 * @author manju
 */
public interface IHttpRequestInterceptor {
    
    public FullHttpRequest interceptHttpRequest(FullHttpRequest request);
    
}
