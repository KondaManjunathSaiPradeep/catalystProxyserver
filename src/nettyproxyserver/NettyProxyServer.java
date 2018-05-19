/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nettyproxyserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author manju
 */
public class NettyProxyServer  {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
       /* Date d = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy MM dd HH_mm_ss");
		String text = sf.format(d);
		//SimpleLayout sl = new SimpleLayout();
		PatternLayout pl = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} | %X{"+Thread.currentThread().getName()+"}  %-5p[%t] %c{1}:%L - %m%n");
		
		
	 
	  
	  Appender app1 = new ConsoleAppender(pl);
	 
	  org.apache.log4j.Logger.getRootLogger().addAppender(app1);
         org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);*/
        EventLoopGroup e = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup(4);
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        ServerBootstrap b = new ServerBootstrap();
        b.group(e);
        b.localAddress(new InetSocketAddress(4444));
        b.channel(NioServerSocketChannel.class);
       // b.handler(new LoggingHandler(LogLevel.INFO));
        b.childHandler(new ChannelInitializer<SocketChannel>(){
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("codec", new HttpServerCodec(4*8192,2*8192,2*8192));
                 //ch.pipeline().addLast("request",new HttpRequestDecoder());
                ch.pipeline().addLast("aggregator", new HttpObjectAggregator(32*1024*1024));
                
               
               // ch.pipeline().addLast("respdecoder", new HttpResponseDecoder());
               
                ch.pipeline().addLast(new dummyhandler(new HttpSession()));
                
               
                
            }
        });
        
        b.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true));
        b.option(ChannelOption.SO_BACKLOG, 128)
          .childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture f = b.bind().sync();
        System.out.println("server started at"+f.channel().localAddress());
    }
    
}
