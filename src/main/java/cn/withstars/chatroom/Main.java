package cn.withstars.chatroom;

import cn.withstars.chatroom.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-03-31
 * Time: 17:19
 * Mail: withstars@126.com
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){
        final Server server = new Server(Constants.DEFAULT_PORT);
        server.init();
        server.start();

        // 注册进程钩子，在jvm进程关闭前释放资源
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                server.shutdown();
                logger.warn("=== jvm shutdown ===");
                System.exit(0);
            }
        });
    }
}
