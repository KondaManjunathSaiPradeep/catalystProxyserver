/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nettyproxyserver;

import MITM.SSLCertEngineServiceImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import sun.net.spi.nameservice.dns.DNSNameService;

/**
 *
 * @author manju
 */
public class HttpClient {
    private FullHttpRequest req;
    private IHttpSession session;
    private SSLEngine engine;
    private SSLCertEngineServiceImpl SSLEngineServ = new SSLCertEngineServiceImpl(); 
    public HttpClient(FullHttpRequest req, IHttpSession session){
        this.req = req;
        this.session = session;
        
    }
    public HttpClient(IHttpSession session ){
        this.session = session;
    }
   private void HandleConnectRequest(FullHttpRequest req){
       System.out.println(req.method().name());
      if(req.method().name().equalsIgnoreCase("connect")){
        System.out.println(req.method().name());
          
      }
   
   }
    
    public void ExecuteRequest(FullHttpRequest req){
      final SSLEngine engine = SSLEngineServ.getSSLEngine();
        engine.setUseClientMode(true);
        SslHandler sslhandle = new SslHandler(engine);
        EventLoopGroup group = new NioEventLoopGroup(); 
        Iterator itr = req.headers().iteratorAsString();
      System.out.println(req);
        if(req.method().name().equalsIgnoreCase("connect")){
            try {
                Bootstrap conb = new Bootstrap();
                SSLEngineServ.InitializeSSLContext();
    //            System.out.println(req.toString());
                URL url = new URL("https://"+req.headers().get("Host"));
               
                String hostname = url.getHost();
                int port = (url.getPort()==-1)?url.getDefaultPort():url.getPort();
               // System.out.println("----"+hostname);
               // System.out.println(port);
                conb.group(group)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(new InetSocketAddress(hostname,port))
                        .handler( new ChannelInitializer<SocketChannel>(){
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(sslhandle);
                                ch.pipeline().addLast(new HttpClientCodec(4*8192,32*1024,32*1024));
                                
                                
                                ch.pipeline().addLast("aggregator", new HttpObjectAggregator(100 * 1024 * 1024));
                                ch.pipeline().addLast( new HttpClentHandler(session,engine));
                                
                            }
                        });
                ChannelFuture f = conb.connect().syncUninterruptibly();
                Channel ch = f.awaitUninterruptibly().channel();
               
              // ch.writeAndFlush(req);
            } catch (MalformedURLException ex) {
                Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }else{
            
            try {
                Bootstrap b = new Bootstrap();
                //SSLEngineServ.InitializeSSLContext();
              //  System.out.println("else"+req.method());
              //  System.out.println("else"+req.headers().get("Host"));
              //  System.out.println("else"+req.uri());
              //  System.out.println("else"+req.headers().size());
                Iterator itr1 = req.headers().iteratorAsString();
                URL url = null;
                if(req.uri().startsWith("http")){
                  url = new URL(req.uri());
                  System.out.println(url.getFile());
                  req.setUri(url.getFile());
                  
                }else{
                    if(this.session.getRequestcontext().pipeline().names().contains("ssl")){
                        if(req.headers().get("Host")== null){
                         System.out.println("null"+req);
                         System.out.println("request->content"+req.content().readableBytes());
                         System.out.println("request->content"+req.content().toString(StandardCharsets.UTF_8));
                        }
                        url = new URL("https://"+req.headers().get("Host")+""+req.uri());
                        System.out.println(req.method()+"/t request->remote"+url.toString());
                        System.out.println("request->content"+req.content().readableBytes());
                        System.out.println("request->content"+req.content().toString(StandardCharsets.UTF_8));
                    }else{
                   url = new URL("http://"+req.headers().get("Host")+""+req.uri());
                       System.out.println(req.method()+"/t request->remote"+url.toString());
                       System.out.println("request->content"+req.content().readableBytes());
                       System.out.println("request->content"+req.content().toString(StandardCharsets.UTF_8));
                    }
                }
                
             final   String hostname = url.getHost();
              //  System.out.println("hostname"+hostname);
              final  int port = (url.getPort()==-1)?url.getDefaultPort():url.getPort();
                System.out.println(hostname+":"+port);
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .remoteAddress(new InetSocketAddress(hostname,port))
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .handler( new ChannelInitializer<SocketChannel>(){
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                if(session.getRequestcontext().pipeline().names().contains("ssl")){
                                 SSLEngine engine = SSLEngineServ.getSSLEngine(hostname, port);
                                 engine.setUseClientMode(true);
                                 
                                 SslHandler handle = new SslHandler(engine); 
                                 ch.pipeline().addLast(handle);
                                }
                              //  ch.pipeline().addLast(new HttpClientCodec());
                                
                                ch.pipeline().addLast(new HttpClientCodec(4*8192,32*1024,32*1024));
                                ch.pipeline().addLast("aggregator", new HttpObjectAggregator(100 * 1024 * 1024));
                                ch.pipeline().addLast( new HttpClentHandler(session));
                                
                            }
                        });
                ChannelFuture f = b.connect().syncUninterruptibly();
                Channel ch = f.awaitUninterruptibly().channel();
                ch.writeAndFlush(req.retain()).addListener((future)->{
                   System.out.println(future);
                });
            } catch (MalformedURLException ex) {
                Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
      /*  HttpRequest request = new DefaultHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/tesst.html");
        request.headers().add(HttpHeaderNames.HOST,"localhost:5500");
        request.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        request.headers().add(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);*/
            
    }
    
    public static void main(String[] args) throws SSLException, InterruptedException, IOException, Exception{
        try {
            SSLCertEngineServiceImpl impl = new SSLCertEngineServiceImpl();
            impl.InitializeSSLContext();
             SSLEngine engine = impl.getSSLEngine("home.dell.com",443);
           // SSLEngine engine = impl.getSSLEngine();
            // SSLEngine engine2 = impl.getSSLEngine("localhost", 9090);
            // engine2.setUseClientMode(true);
            // engine.setUseClientMode(true);
//       engine.beginHandshake();
//       System.out.println(engine.getHandshakeSession().getLocalPrincipal());
// System.out.println(engine2.getSession().getPeerHost());
//   SslHandler handle         =  new SslHandler(engine);


//  engine.setUseClientMode(true);
IHttpSession session = new HttpSession();
EventLoopGroup group = new NioEventLoopGroup();
Bootstrap b = new Bootstrap();
b.group(group)
        .channel(NioSocketChannel.class)
        .remoteAddress(new InetSocketAddress("home.dell.com",443))
        .handler( new ChannelInitializer<SocketChannel>(){
            @Override
            
            protected void initChannel(SocketChannel ch) throws Exception {
                SslHandler handler =      new SslHandler(engine);
                engine.setUseClientMode(true);
                engine.setEnabledProtocols(new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"});
                ch.pipeline().addLast("ssl", handler);
                
                //  ch.pipeline().addLast(new HttpClientCodec());
                ch.pipeline().addLast(new HttpClientCodec(4*8192,32*1024,32*1024));
                ch.pipeline().addLast("aggregator", new HttpObjectAggregator(100 * 1024 * 1024));
                
                
                
                
                
                
                handler.handshakeFuture().addListener((future)->{
                    if(future.isSuccess()){
                        System.out.println("hi pradeep handshake is succuess");
                    }else{
                        System.out.println(future.cause());
                    }
                   
                    if(future.isDone()){
                        System.out.println("hi pradeep handshake is done");
                        for(javax.security.cert.X509Certificate s:engine.getHandshakeSession().getPeerCertificateChain()){
                        System.out.println(s);
                        }
                        future.cause().printStackTrace();
                    }
                    future.getNow();
                });
                
                            
                // ch.pipeline().addLast("aggregator", new HttpObjectAggregator(100 * 1024 * 1024));
                ch.pipeline().addLast( new SimpleChannelInboundHandler<Object>(){
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println(msg instanceof FullHttpResponse);
                        System.out.println(msg);
                        if(msg instanceof DefaultLastHttpContent){
                            System.out.println(((DefaultLastHttpContent)msg).content().toString(StandardCharsets.UTF_8));
                        }
                    }
                    
                });
                //  ch.pipeline().addLast("ssl", new SslHandler(engine));
                
            }
        });
ChannelFuture f = b.connect().syncUninterruptibly();
Channel ch = f.awaitUninterruptibly().channel();

HttpRequest request = new DefaultHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.GET,"/WebTracker/C12344594?plist_id=12286515&event_type=MEDIAPLEX&E/FAMILY=inspiron-15-3567-laptop|US|EN");
request.headers().add(HttpHeaderNames.HOST,"home.dell.com");
request.headers().add( HttpHeaderNames.CONNECTION,  HttpHeaderValues.KEEP_ALIVE);
//  request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
// System.out.println(request.uri());
System.out.println(request.toString());
//  System.out.println(request.protocolVersion());
//  System.out.println(request.headers().get("Host"));
ch.writeAndFlush(request);
URL url = new URL("https://home.dell.com/WebTracker/C12344594?plist_id=12286515&event_type=MEDIAPLEX&E/FAMILY=inspiron-15-3567-laptop|US|EN");
InputStream in = url.openConnection().getInputStream();
BufferedReader reader = new BufferedReader(new InputStreamReader(in));
while(reader.readLine() !=null){
  System.out.println("hi");
  DNSNameService ser = new DNSNameService();
  InetAddress[] adr=ser.lookupAllHostAddr("www.dell.com");
  for(InetAddress a :adr ){
  
    System.out.println(a.getHostAddress());
    System.out.println(a.isLoopbackAddress());
  }
}
// handle.handshakeFuture();
        } catch (MalformedURLException ex) {
            Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
            
    }
    
}
