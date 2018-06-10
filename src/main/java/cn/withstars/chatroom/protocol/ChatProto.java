package cn.withstars.chatroom.protocol;

import cn.withstars.chatroom.util.DateTimeUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-03-31
 * Time: 17:43
 * Mail: withstars@126.com
 */

/**
 * 自定义聊天协议
 *  head | body | extend
 *  head 协议头部，int型
 *  body 位消息内容,string
 *  extend为协议扩展自断
 */
public class ChatProto {

    public static final int PING_PROTO = 256; // ping消息
    public static final int PONG_PROTO = 324; // pong消息
    public static final int SYST_PROTO = 356; // 系统消息
    public static final int EROR_PROTO = 452; // 错误消息
    public static final int AUTH_PROTO = 624; // 认证消息
    public static final int MESS_PROTO = 726; // 普通消息

    // 协议版本
    private int version = 1;
    //协议头
    private int uri;
    // 协议内容
    private String body;
    // 协议扩展字段
    private Map<String,Object> extend = new HashMap<>();

    /**
     * 构造函数
     * @param uri
     * @param body
     */
    public ChatProto(int uri, String body) {
        this.uri = uri;
        this.body = body;
    }

    /**
     * 构造协议
     * @param head
     * @param body
     * @return JSON字符串
     */
    public static String buildProto(int head, String body){
        ChatProto chatProto = new ChatProto(head, body);
        return JSONObject.toJSONString(chatProto);

    }

    /**
     * 创建PING 消息
     * @return
     */
    public static String buildPingProto(){
        return buildProto(PING_PROTO, null);
    }

    /**
     * 创建PONG 消息
     * @return
     */
    public static String buildPongProto(){
        return buildProto(PONG_PROTO, null);
    }

    /**
     * 创建系统消息
     * @param code
     * @param mess
     * @return
     */
    public static String buildSystProto(int code, Object mess){
        ChatProto chatProto = new ChatProto(SYST_PROTO, null);
        chatProto.extend.put("code", code);
        chatProto.extend.put("mess", mess);
        return JSONObject.toJSONString(chatProto);
    }

    /**
     * 创建认证消息
     * @param isSuccess
     * @return
     */
    public static String buildAuthProto(boolean isSuccess){
        ChatProto chatProto = new ChatProto(AUTH_PROTO, null);
        chatProto.extend.put("isSuccess", isSuccess);
        return JSONObject.toJSONString(chatProto);
    }

    /**
     * 创建错误消息
     * @param code
     * @param mess
     * @return
     */
    public static String buildErorProto(int code, String mess){
        ChatProto chatProto = new ChatProto(EROR_PROTO, null);
        chatProto.extend.put("code", code);
        chatProto.extend.put("mess", mess);
        return JSONObject.toJSONString(chatProto);
    }

    /**
     * 创建普通消息
     * @param userId
     * @param username
     * @param mess
     * @return
     */
    public static String buildMessProto(int userId, String username, String mess){
        ChatProto chatProto = new ChatProto(MESS_PROTO, mess);
        chatProto.extend.put("userId",userId);
        chatProto.extend.put("username", username);
        chatProto.extend.put("time", DateTimeUtil.getCurrentTime());
        return JSONObject.toJSONString(chatProto);
    }
}
