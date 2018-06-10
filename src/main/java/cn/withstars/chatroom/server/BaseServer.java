package cn.withstars.chatroom.server;




import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.NumberUp;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-03-31
 * Time: 17:16
 * Mail: withstars@126.com
 */
public abstract class BaseServer implements Server {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected String hostName ="localhost";

    protected int port = 8888;

    protected DefaultEventLoopGroup defaultEventLoopGroup;

    protected NioEventLoopGroup bossGroup;

    protected NioEventLoopGroup workGroup;

    protected NioServerSocketChannel ssch;

    protected ChannelFuture cf;

    protected ServerBootstrap b;

    /**
     *  对服务器初始化
     */
    public void init (){

        defaultEventLoopGroup = new DefaultEventLoopGroup(8, new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"DEFAULT_EVENT_LOOP_GROUP"+index.incrementAndGet());
            }
        });

        bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"BOSS_"+index.incrementAndGet());
            }
        });

        workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"WORK_"+index.incrementAndGet());
            }
        });
        b = new ServerBootstrap();
    }

    /**
     *  关闭服务器
     */
    public void shutdown(){

        if (defaultEventLoopGroup != null){
            defaultEventLoopGroup.shutdownGracefully();
        }
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

}
