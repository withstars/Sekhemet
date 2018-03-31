package cn.withstars.chatroom;

import cn.withstars.chatroom.server.CoreServer;
import cn.withstars.chatroom.handler.MessageHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-03-31
 * Time: 17:49
 * Mail: withstars@126.com
 */
public class Server extends CoreServer {

    private ScheduledExecutorService executorService;

    public Server(int port){
        portNum = port;
        executorService = Executors.newScheduledThreadPool(2);
    }

    public void start(){

        serverBootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_BACKLOG,1024)
                .localAddress(new InetSocketAddress(portNum))
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(defaultEventLoopGroup,
                                new HttpServerCodec(),
                                new HttpObjectAggregator(65536),
                                new ChunkedWriteHandler(),
                                new IdleStateHandler(60,0,0),
                                new MessageHandler()
                                );
                    }
                } );



    }



}
