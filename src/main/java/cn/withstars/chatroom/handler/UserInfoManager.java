package cn.withstars.chatroom.handler;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-06-10
 * Time: 16:52
 * Mail: withstars@126.com
 */

import cn.withstars.chatroom.domain.User;
import cn.withstars.chatroom.protocol.ChatProto;
import cn.withstars.chatroom.util.BlankUtil;
import cn.withstars.chatroom.util.NettyUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Channel 管理器
 */
public class UserInfoManager {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoManager.class);

    // 公平可重入读写锁
    private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);

    private static ConcurrentHashMap<Channel, User> users = new ConcurrentHashMap<>();

    private static AtomicInteger userCount = new AtomicInteger(0);

    /**
     * 添加Channel
     * @param channel
     */
    public static void addChannel(Channel channel){
        String remoteAddr = NettyUtil.parseChannelRemoteAddr(channel);
        if (!channel.isActive()){
            logger.error("Channel is not active, address: {}", remoteAddr);
        }
        User user = new User();
        user.setAddr(remoteAddr);
        user.setChannel(channel);
        user.setTime(System.currentTimeMillis());
        users.put(channel,user);
    }


    /**
     * 检查是否有已保存当前Channel,若有,检查当前通道是否激活，若已激活,设置该Channel对应的User信息
     * @param channel
     * @param nick
     * @return
     */
    public static boolean saveUser(Channel channel, String nick){
        User user = users.get(channel);
        if (user == null){
            return false;
        }
        if (!channel.isActive()){
            logger.error("Channel is not active,address:{},nick:{}",user.getAddr(),nick);
            return false;
        }
        // 增加一个认证用户
        userCount.incrementAndGet();
        user.setNick(nick);
        user.setAuth(true);
        user.setUserId();
        user.setTime(System.currentTimeMillis());
        return true;
    }

    /**
     * 从缓存中移除Channel, 并且关闭Channel
     * @param channel
     */
    public static void removeChannel(Channel channel){
        try {
            logger.warn("Channel will be removed , address is {} ",NettyUtil.parseChannelRemoteAddr(channel));
            rwLock.writeLock().lock();
            channel.close();
            User user = users.get(channel);
            if (user != null){
                User tmp = users.remove(channel);
                if (tmp != null && tmp.isAuth()){
                    userCount.decrementAndGet();
                }
            }

        }finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 广播普通消息
     * @param userId
     * @param nick
     * @param message
     */
    public static void broadcastMess(int userId, String nick, String message){
        if (!BlankUtil.isBlank(message)){
            try {
                rwLock.readLock().lock();
                Set<Channel> keySet = users.keySet();
                for (Channel ch : keySet){
                    User user = users.get(ch);
                    if (user == null|| !user.isAuth()){
                        continue;
                    }
                    ch.writeAndFlush(new TextWebSocketFrame(ChatProto.buildMessProto(userId, nick, message)));
                }
            }finally {
                rwLock.readLock().unlock();
            }
        }
    }

    /**
     * 广播系统消息
     */
    public static void broadcastInfo(int code, Object mesage){
        try{
            rwLock.readLock().lock();
            Set<Channel> keySet = users.keySet();
            for (Channel ch : keySet){
                User user = users.get(ch);
                if (user == null|| !user.isAuth()){
                    continue;
                }
                ch.writeAndFlush(new TextWebSocketFrame(ChatProto.buildSystProto(code, mesage)));
            }
        }finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 广播PING
     */
    public static void broadcastPing(){
        try {
            rwLock.readLock().lock();
            logger.info("BroadcastPing userCount:{}", userCount.intValue());
            Set<Channel> keySet = users.keySet();
            for (Channel ch:keySet){
                User user = users.get(ch);
                if (user == null || !user.isAuth()){
                    continue;
                }
                ch.writeAndFlush(new TextWebSocketFrame(ChatProto.buildPingProto()));
            }
        }finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 发送系统消息
     * @param channel
     * @param code
     * @param mess
     */
    public static void sendInfo(Channel channel, int code, Object mess){
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildSystProto(code, mess)));
    }

    /**
     * 发送PONG
     * @param channel
     */
    public static void sendPong(Channel channel){
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildPongProto()));

    }

    /**
     * 扫描并关闭无效的Channel
     */
    public static void scanNotActiveChannel(){
        Set<Channel> keySet = users.keySet();
        for (Channel ch:keySet){
            User user = users.get(ch);
            if (user == null) {
                continue;
            }
            if (!ch.isOpen() || !ch.isActive() || (!user.isAuth()&&(System.currentTimeMillis()-user.getTime()>10000))){
                removeChannel(ch);
            }
        }
    }

    public static ConcurrentHashMap<Channel, User> getUsers() {
        return users;
    }

    public static int getUserCount() {
        return userCount.get();
    }

    public static User getUser(Channel channel){
        return users.get(channel);
    }

    public static void updateUserTime(Channel channel){
        User user = getUser(channel);
        if (user != null){
            user.setTime(System.currentTimeMillis());
        }
    }
}
