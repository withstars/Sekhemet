package cn.withstars.chatroom;

import cn.withstars.chatroom.handler.UserAuthHandler;
import cn.withstars.chatroom.handler.UserInfoManager;
import cn.withstars.chatroom.server.BaseServer;
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
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-03-31
 * Time: 17:49
 * Mail: withstars@126.com
 */
public class Server extends BaseServer {

    private ScheduledExecutorService executorService;

    public Server(int port){
        this.port = port;
        executorService = Executors.newScheduledThreadPool(2);
    }

    @Override
    public void start() {
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(defaultEventLoopGroup,
                                new HttpServerCodec(), //请求解码器
                                new HttpObjectAggregator(65536), // 将多个消息转换成成单一的消息对象
                                new ChunkedWriteHandler(),// 支持异步发送大的码流，一般用于发送文件流
                                new IdleStateHandler(60,0,0),// 检测链路是否空闲
                                new UserAuthHandler(), // 处理握手和认证
                                new MessageHandler() // 处理消息的发送
                                );
                    }
                });
        try {
            cf = b.bind().sync();
            InetSocketAddress addr = (InetSocketAddress) cf.channel().localAddress();
            logger.info("WebSocket start successfully, port is {}", addr.getPort());
            // 定时扫描所有的channel，关闭失效的Channel
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    logger.info("------- scanNotActiveChannel -------");
                    UserInfoManager.scanNotActiveChannel();
                }
            }, 3,60, TimeUnit.SECONDS);
        }catch (InterruptedException e){
            logger.error("WebSocketServer start fail", e);
        }
    }

    @Override
    public void shutdown() {
        if (executorService != null){
            executorService.shutdown();
        }
        super.shutdown();
    }
}
