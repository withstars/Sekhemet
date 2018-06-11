package cn.withstars.chatroom.protocol;



/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-03-31
 * Time: 17:39
 * Mail: withstars@126.com
 */
public class ChatCode {

    public  static final int PING_CODE = 10015; // ping

    public  static final int PONG_CODE = 10016; // pong

    public  static final int AUTH_CODE = 10000; // ping

    public  static final int MESS_CODE = 10086; // pong

    /**
     * 系统消息类型
     */
    public  static final int SYS_USER_COUNT = 20001; // 实时用户数

    public  static final int SYS_AUTH_STATE = 20002; // 认证消息

    public static final int SYS_OTHER_INFO = 20003; // 消息

}
