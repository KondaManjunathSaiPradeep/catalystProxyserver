/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nettyproxyserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

/**
 *
 * @author manju
 */
public class HttpSession implements IHttpSession {
    private HttpRequest request;
    private HttpRequest response;
    private FullHttpRequest fullrequest;
    private FullHttpResponse fullresponse;
    private ChannelHandlerContext requestcontext;
    private ChannelHandlerContext responseContext;
    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpRequest getResponse() {
        return response;
    }

    public void setResponse(HttpRequest response) {
        this.response = response;
    }

    public FullHttpRequest getFullrequest() {
        return fullrequest;
    }

    public void setFullrequest(FullHttpRequest fullrequest) {
        this.fullrequest = fullrequest;
    }

    public FullHttpResponse getFullresponse() {
        return fullresponse;
    }

    public void setFullresponse(FullHttpResponse fullresponse) {
        this.fullresponse = fullresponse;
    }
    
    public HttpSession(){
    }
    public HttpSession(FullHttpRequest request,FullHttpResponse response){
        this.request = request;
        this.response = this.response;
    }

    public ChannelHandlerContext getRequestcontext() {
        return requestcontext;
    }

    public void setRequestcontext(ChannelHandlerContext requestcontext) {
        this.requestcontext = requestcontext;
    }

    public ChannelHandlerContext getResponseContext() {
        return responseContext;
    }

    public void setResponseContext(ChannelHandlerContext responseContext) {
        this.responseContext = responseContext;
    }
    

    @Override
    public FullHttpRequest gethttpRequest() {
      return this.fullrequest;  
    }

    @Override
    public FullHttpResponse getHttpResponse() {
     return this. fullresponse;   
    }

    @Override
    public void setHttpRequest(FullHttpRequest request) {
        this.fullrequest = request;
    }

    @Override
    public void setHttpResponse(FullHttpResponse response) {
      this.fullresponse = response;  
    }

    @Override
    public FullHttpRequest getFullhttpRequest() {
        return this.fullrequest; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FullHttpResponse getFullHttpResponse() {
      return this.fullresponse;
    }
}
