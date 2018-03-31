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
 *
 * 用户实体类
 */
public class User {

    private long time = 0;

    private String username;

    private int userId;

    private Channel channel;

    private String Addr;

    // uid 原子类
    private static AtomicInteger uid = new AtomicInteger(100000);

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
}
