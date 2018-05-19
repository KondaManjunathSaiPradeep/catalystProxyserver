/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nettyproxyserver;

/**
 *
 * @author manju
 */
import MITM.SSLCertEngineServiceImpl;
import static io.netty.buffer.Unpooled.copiedBuffer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.SslHandler;
import java.net.URISyntaxException;
import javax.net.ssl.SSLEngine;

public class NettyHttpServer
{
    private ChannelFuture channel;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;
    
    public NettyHttpServer()
    {
        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();        
    }

    public void start()
    {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() { shutdown(); }
        });
        
        try
        {
            final ServerBootstrap bootstrap =
                new ServerBootstrap()
                    .group(masterGroup, slaveGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        public void initChannel(final SocketChannel ch) throws Exception
                        {
                            SSLCertEngineServiceImpl imp = new SSLCertEngineServiceImpl();
                            SSLEngine engine = imp.GetEngineForDomain("localhost");
                            engine.setUseClientMode(false);
                            SslHandler handler =new SslHandler(engine);
                            ch.pipeline().addLast("ssl", handler);
                            //handler.handshakeFuture().addListener((future)->{
                            //System.out.println("mitm"+future);
                           // });
                            ch.pipeline().addLast("codec", new HttpServerCodec());
                            ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512*1024));
                            ch.pipeline().addLast( new ChannelInboundHandlerAdapter()
                            {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg)
                                        throws Exception
                                {
                                    
                                    if (msg instanceof FullHttpRequest)
                                    {
                                        final FullHttpRequest request = (FullHttpRequest) msg;
                                        System.out.println(request);
                                       
                                        final String responseMessage = "Hello from Netty!";                                            
                                        FullHttpResponse response = new DefaultFullHttpResponse(
                                            HttpVersion.HTTP_1_1,
                                            HttpResponseStatus.OK,
                                            copiedBuffer(responseMessage.getBytes())
                                        );
                                         DefaultFullHttpResponse connectresponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,new HttpResponseStatus(200,"Connection Established"));
                                         if(request.method().name().equalsIgnoreCase("connect")){
                                             System.out.println("hi");
                                             SSLCertEngineServiceImpl imp = new SSLCertEngineServiceImpl();
                                             SSLEngine engine = imp.GetEngineForDomain("localhost");
                                             engine.setUseClientMode(false);
                                             SslHandler handler =new SslHandler(engine);
                                            // ctx.channel().pipeline().addFirst(handler);
                                              ctx.channel().writeAndFlush(connectresponse);
                                             handler.handshakeFuture().addListener((future)->{
                                               System.out.println(future);
                                                 if(future.isSuccess()){
                                                
                                             
                                             }
                                             
                                             });
                                             
                                         }
    
                                        if (HttpHeaders.isKeepAlive(request))
                                        {
                                            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                                        }
                                        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
                                        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseMessage.length());
                                        
                                      //  
                                        if(request.headers().get("Connection").equalsIgnoreCase("Upgrade") ||
                                            request.headers().contains("Upgrade")){
                                        System.out.println("handshake has been started");
                                           handleHandshake(ctx, request);
                                        }else{
                                           ctx.writeAndFlush(response).addListener((future)->{System.out.println(future);});
                                        }
                                    }
                                    else
                                    {
                                        super.channelRead(ctx, msg);
                                    }
                                }
    
                                
                                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
                                {
                                    ctx.flush();
                                    this.userEventTriggered(ctx, ctx);
                                }
                               
                               public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                //    System.out.println(evt);
                                }
    
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                        throws Exception
                                {
                                    ctx.writeAndFlush(new DefaultFullHttpResponse(
                                        HttpVersion.HTTP_1_1,
                                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                        copiedBuffer(cause.getMessage().getBytes())
                                    ));
                                }    

                               public void handleHandshake(ChannelHandlerContext ctx,FullHttpRequest req) throws URISyntaxException{
                               
                                   WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketURL(req),
                                                                                          null, true);
                                    WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
                                   if (handshaker == null) {
                                         WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                                     } else {
                                         handshaker.handshake(ctx.channel(), req);
                                      }
                               
                               
                               }  
                                protected String getWebSocketURL(HttpRequest req) {
                                    
                                          System.out.println("Req URI : " + req.uri());
                                          String url =  "ws://" + req.headers().get("Host") + req.uri() ;
                                          System.out.println("Constructed URL : " + url);
                                          return url;
                                }
                            });
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            channel = bootstrap.bind(8881).sync();
            //channels.add(bootstrap.bind(8080).sync());
        }
        catch (final InterruptedException e) { }
    }
    
    public void shutdown()
    {
        slaveGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();

        try
        {
            channel.channel().closeFuture().sync();
        }
        catch (InterruptedException e) { }
    }

    public static void main(String[] args)
    {
        new NettyHttpServer().start();
    }
}
