package cn.withstars.chatroom.handler;

import cn.withstars.chatroom.domain.User;
import cn.withstars.chatroom.protocol.ChatProto;
import cn.withstars.chatroom.util.BlankUtil;
import cn.withstars.chatroom.util.NettyUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.soap.SOAPBinding;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-03-31
 * Time: 18:05
 * Mail: withstars@126.com
 */
public class UserManager {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

    private static ReentrantReadWriteLock rwLock =new ReentrantReadWriteLock(true);

    private static ConcurrentMap<Channel,User> users = new ConcurrentHashMap<>();
    private static AtomicInteger userCount = new AtomicInteger(0);

    public static void addChannel(Channel channel){
        String remoteAddr = NettyUtil.parseChannelRemoteAddr(channel);
        if (!channel.isActive()){
            logger.error("channel is not actived,address {}",remoteAddr);
        }
        User user =new User();
        user.setChannel(channel);
        user.setTime(System.currentTimeMillis());
        user.setAddr(remoteAddr);
        users.put(channel,user);

    }

    /**
     * 增加用户
     */
    public static boolean addUser(Channel channel,String username){
        User user = users.get(channel);
        if (user == null){
            return false;
        }
        if (!channel.isActive()){
            logger.error("channel is not actice,address :{},nickname:{}",user.getAddr(),user.getUsername());
        }

        //增加一个用户
        userCount.incrementAndGet();
        user.setUsername(username);
        user.setUserId();
        user.setTime(System.currentTimeMillis());
        return true;

    }

    /**
     * 从缓存中删除channel,并关闭channel
     */
    public static void removeChannel(Channel channel){
        logger.warn("channel will be removed,address is {}",NettyUtil.parseChannelRemoteAddr(channel));
        rwLock.writeLock().lock();
        channel.close();
        User user =users.get(channel);
        if (user != null){
            User temp = users.remove(channel);
            if (temp != null){
                userCount.decrementAndGet();
            }
        }
        rwLock.writeLock().unlock();

    }

    /**
     * 广播普通消息
     */
    public static void broadcastMessage(int uid, String username, String message){
        if (!BlankUtil.isBlank(message)){
            try {
                rwLock.readLock().lock();
                Set<Channel> keyset = users.keySet();
                for (Channel cl : keyset){
                    User user = users.get(cl);
                    if (user ==null )
                        continue;

                    cl.writeAndFlush(new TextWebSocketFrame(ChatProto.buildMessProto(uid,username,message)));
                }

            }finally {
                rwLock.readLock().unlock();
            }
        }
    }


    /**
     * 广播系统消息
     */
    public static void broadcastSysMessage(int code, String message){
        try {
            rwLock.readLock().lock();
            logger.info("broadcastPing userCount {}",userCount.intValue());
            Set<Channel> keySet = users.keySet();
            for (Channel channel :keySet){
                User user = users.get(channel);
                if (user == null)
                    continue;
                channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildSystProto(code,message)));
            }


        }finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 发送PING
     */
    public static void broadcastPing(){
        try {
            rwLock.readLock().lock();
            logger.info("broadcastPing usercount :{}",userCount);
            Set<Channel> channels = users.keySet();
            for (Channel channel : channels){
                User user = users.get(channel);
                if (user ==null)
                    continue;
                channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildPingProto));
            }
        }finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 发送系统消息
     */
    public static void sendSysMessage(Channel channel, int Code , Object message){
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildSystProto(code, message)));
    }

    public static void sendPong(Channel channel){
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildPongProto()));
    }

    /**
     *  扫描并关闭失效的channel
     */
    public static void scanNotActiveChannel(){
        Set<Channel> channels = users.keySet();
        for (Channel channel : channels){
            User user = users.get(channel);
            if (user == null) continue;
            if (!channel.isActive() || !channel.isOpen() || (System.currentTimeMillis() - user.getTime()) > 10000){
                removeChannel(channel);
            }
        }
    }

    public static User getUser(Channel channel){
        return users.get(channel);
    }

    public static ConcurrentMap<Channel, User> getUsers() {
        return users;
    }
}
