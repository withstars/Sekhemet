package cn.withstars.chatroom.domain;


import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-03-31
 * Time: 17:07
 * Mail: withstars@126.com
 */

/**
 * 用户实体类
 */
public class User {

    private boolean isAuth = false; //是否认证

    private long time = 0; //登录时长

    private String username; // 昵称

    private int userId; // UID

    private Channel channel; // 通道

    private String Addr; // 地址

    private static AtomicInteger uid = new AtomicInteger(10000); // uid 原子类



    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setUserId() {
        this.userId = uid.incrementAndGet();
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public Channel getChannel() {
        return channel;
    }

    public int getUserId() {
        return userId;
    }

    public long getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    public String getAddr() {
        return Addr;
    }

    public void setAddr(String addr) {
        Addr = addr;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public boolean isAuth() {
        return isAuth;
    }
}
