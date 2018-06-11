package cn.withstars.chatroom.protocol;

import cn.withstars.chatroom.util.DateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(ChatProto.class);

    public static final int PING_PROTO = 1 << 8 | 220; // ping消息
    public static final int PONG_PROTO = 2 << 8 | 220; // pong消息
    public static final int SYST_PROTO = 3 << 8 | 220; // 系统消息
    public static final int EROR_PROTO = 4 << 8 | 220; // 错误消息
    public static final int AUTH_PROTO = 5 << 8 | 220; // 认证消息
    public static final int MESS_PROTO = 6 << 8 | 220; // 普通消息

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
     * @param nick
     * @param mess
     * @return
     */
    public static String buildMessProto(int userId, String nick, String mess){
        ChatProto chatProto = new ChatProto(MESS_PROTO, mess);
        chatProto.extend.put("userId",userId);
        chatProto.extend.put("nick", nick);
        chatProto.extend.put("time", DateTimeUtil.getCurrentTime());
        return JSONObject.toJSONString(chatProto);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getUri() {
        return uri;
    }

    public void setUri(int uri) {
        this.uri = uri;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public void setExtend(Map<String, Object> extend) {
        this.extend = extend;
    }
}
