/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nettyproxyserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import static io.netty.buffer.Unpooled.copiedBuffer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.net.ssl.SSLEngine;

/**
 *
 * @author manju
 */
@Sharable
public class dummyhandler extends SimpleChannelInboundHandler<HttpObject> {
//    public static EmbeddableChannel = new EmbeddableChannel();
    private IHttpSession session;
    private SSLEngine engine;
    public dummyhandler() {
    }
    public dummyhandler(IHttpSession session){
        this.session = session;
    }
    public dummyhandler(IHttpSession session,SSLEngine engine){
        this.engine = engine;
    }

 /*    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf)msg;
        
        
        System.out.println(in.toString(Charset.forName("UTF-8")));
        System.out.println("I am K M Sai Pradeep");
    }*/
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
        //System.out.println("dummy"+ctx.channel().isActive());
        //System.out.println("dummy"+ctx.channel().isWritable());
       // System.out.println(this.engine.getSession().getPeerPrincipal());
  //     SslHandler handler = (SslHandler)ctx.channel().pipeline().get(SslHandler.class);
//       System.out.println("dummy"+handler.engine().getSession().getLocalCertificates()[0]);
       // System.out.println("dummy-----"+evt);
       // System.out.println(ctx.channel().isActive());
       // System.out.println(ctx.channel().isWritable());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        
         if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
        //  System.out.println(request.uri());
           session.setRequestcontext(ctx);
           session.setHttpRequest(request);
           
           
           
           
           
           // EmbeddedChannel ch = new EmbeddedChannel(new HttpRequestEncoder()); ch.writeOutbound(((FullHttpRequest) msg).retain()); ByteBuf encoded = ch.readOutbound();
        /* FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, copiedBuffer("Sai Pradeep".getBytes()));
          response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
                                        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, "Sai Pradeep".length());
            ChannelFuture fut = ctx.writeAndFlush(response);
          System.out.println(fut.cause());*/
          
          
            HttpClient client = new HttpClient(session);
           client.ExecuteRequest(request);
         
          }
         
    } 
    public static void main(String[] args) throws MalformedURLException{
        URL url = new URL("https://www.facebook.com");
        System.out.println(url.getHost());
        System.out.println(url.getDefaultPort());
             
         }
}
