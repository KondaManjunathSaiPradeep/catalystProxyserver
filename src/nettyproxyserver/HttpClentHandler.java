/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nettyproxyserver;


import MITM.SSLCertEngineServiceImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;


/**
 *
 * @author manju
 */
@Sharable
public class HttpClentHandler extends SimpleChannelInboundHandler<Object>  {
    private IHttpSession session;
    private SslHandler handler;
    private SSLEngine engine;
    public HttpClentHandler() {
    }
    public HttpClentHandler(IHttpSession session){
        this.session = session;
    }
    public HttpClentHandler(IHttpSession session,SSLEngine engine){
        this.session = session;
        this.engine = engine;
    }
    public HttpClentHandler(IHttpSession session,SslHandler handler){
        this.session = session;
        this.handler = handler;
    }

    HttpClentHandler(IHttpSession session, SslHandler handler, SSLEngine engine) {
        this.handler = handler;
        this.engine = engine;
        this.session = session;
    }
     @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      //  System.out.println(evt);
        
        if (evt instanceof SslHandshakeCompletionEvent) {
            SslHandshakeCompletionEvent hce = (SslHandshakeCompletionEvent) evt;
            if (!hce.isSuccess()
                    && hce.cause().getMessage().contains("unrecognized_name")) {
                
                ctx.close();
                
                return;
            }else
            {
                
      if(this.session.getFullhttpRequest().method().name().equalsIgnoreCase("connect")){
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,new HttpResponseStatus(200,"Connection Established"));
                
                response.headers().add(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
             
             String resp = "HTTP/1.1 200 Connection Established\r\n" +
"FiddlerGateway: Direct\r\n" +
"StartTime: 10:28:42.119\r\n" +
"Connection: close\r\n\r\n";
                SSLCertEngineServiceImpl service = new SSLCertEngineServiceImpl();
               SSLEngine engine = service.GetEngineForDomain(new URL("http://"+this.session.getFullhttpRequest().uri()).getHost());
            //   System.out.println(this.session.getRequestcontext().channel().localAddress());
             //  SSLEngine engine = service.getSSLEngine();
             engine.setUseClientMode(false);
              
               
            //  System.out.println(this.engine.getSession().getPeerCertificates()[0]);
              //SSLEngine serverengine = sniff.clientSslEngineFor(this.session.getFullhttpRequest(), this.engine.getSession());
               //engine.beginHandshake();
             /*  while(netbuf.hasRemaining()){
             System.out.println("-----netbuff"+new String(netbuf.array()));
            System.out.println("--- remaining"+netbuf.hasRemaining());
             SSLEngineResult r = engine.wrap(netbuf, appbuf);
              System.out.println(r);
               
               }*/
           //serverengine.setUseClientMode(false);
           engine.setNeedClientAuth(false);
           //  serverengine.beginHandshake();
             //  appbuf.flip();
              // appbuf.compact();
              //System.out.println("arrr"+new String(new byte[]{appbuf.get()}));
               
               
               
             // System.out.println("buffersize of server"+ engine.getSession().getApplicationBufferSize());
              
            
            SslHandler handler = new SslHandler(engine);
              
               //System.out.println(this.session.getRequestcontext().channel().pipeline().names());
               //System.out.println("client context"+this.session.getRequestcontext().channel().isActive());
                this.session.getRequestcontext().channel().writeAndFlush(response).addListener((future)->{
                //  System.out.println("write"+future);
                 if(future.isSuccess()){
                  //  this.session.getRequestcontext().channel().close();
                 }
                });
                 this.session.getRequestcontext().channel().config().setAutoRead(true);
             this.session.getRequestcontext().channel().pipeline().addFirst("ssl", handler);
           /*    handler.handshakeFuture().addListener((future)->{
              //     System.out.println(future);
                 if(future.isSuccess()){
                  
                 }
                 
               });*/
                
             /*   handler.handshakeFuture().addListener((channel)->{
                
            //        System.out.println("----------------------------ssl"+channel.isSuccess());
                });*/
                
            }
                
                
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
         //To change body of generated methods, choose Tools | Templates.
         //System.out.println("hi");
         final ChannelHandlerContext x = ctx;
       System.out.println("response->request"+this.session.getFullhttpRequest().headers().get("Host")+""+this.session.getFullhttpRequest().uri());
       
       System.out.println(msg);
         //System.out.println(msg instanceof FullHttpResponse);
          //System.out.println(msg instanceof HttpResponse);
         if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
           //System.out.println("reaponse from remote"+this.session.getFullhttpRequest().uri()+""+this.session.getFullhttpRequest().content().toString(Charset.defaultCharset())+"\n---"+response+"\n--"+response.content().toString(StandardCharsets.UTF_8));
            
         //System.out.println(response.content().toString(Charset.defaultCharset()));
        
         //   EmbeddedChannel ch = new EmbeddedChannel(new HttpResponseEncoder()); ch.writeOutbound(((FullHttpResponse) msg).retain()); ByteBuf encoded = ch.readOutbound();
            
           // System.out.println(encoded.toString(Charset.forName("UTF-8")));
         this.session.setHttpResponse(response);
           this.session.getRequestcontext().channel().writeAndFlush(response.retain()).addListener((fut)->{
               
               System.out.println("writing listener"+fut);
           if(fut.isSuccess()){
           x.channel().close();
            // this.session.getRequestcontext().close();
           }
           
           });
           
     this.session.setHttpResponse(response);
        /* ChannelFuture fut = this.session.getRequestcontext().channel().writeAndFlush(response);
           if(fut.isSuccess()){
           //    System.out.println("success");
           }*/
          }
      // ctx.close();
         
    }
    
}
