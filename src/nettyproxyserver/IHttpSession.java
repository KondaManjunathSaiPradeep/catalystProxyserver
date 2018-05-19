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
import io.netty.handler.codec.http.HttpResponse;

/**
 *
 * @author manju
 */
public interface IHttpSession {
    public FullHttpRequest getFullhttpRequest();
    public FullHttpResponse getFullHttpResponse();
    public HttpRequest gethttpRequest();
    public HttpResponse getHttpResponse();
    public void setHttpRequest(FullHttpRequest request);
    public void setHttpResponse(FullHttpResponse response); 
    public void setRequestcontext(ChannelHandlerContext ctx);
    public void setResponseContext(ChannelHandlerContext ctx);
    public ChannelHandlerContext getRequestcontext();
    public ChannelHandlerContext getResponseContext();
}
