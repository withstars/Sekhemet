package cn.withstars.chatroom.handler;

import cn.withstars.chatroom.domain.User;
import cn.withstars.chatroom.protocol.ChatProto;
import cn.withstars.chatroom.protocol.StatusCode;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-06-10
 * Time: 16:51
 * Mail: withstars@126.com
 */

/**
 * 消息处理业务逻辑
 */
public class MessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        UserInfoManager.removeChannel(ctx.channel());
        UserInfoManager.broadcastInfo(StatusCode.SYS_USER_COUNT, UserInfoManager.getUserCount());
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Connection error and close the channel", cause);
        UserInfoManager.removeChannel(ctx.channel());
        UserInfoManager.broadcastInfo(StatusCode.SYS_USER_COUNT, UserInfoManager.getUserCount());

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        User user = UserInfoManager.getUser(ctx.channel());
        if (user != null && user.isAuth()){
            JSONObject json = JSONObject.parseObject(frame.text());
            // 广播返回用户发送的文本
            UserInfoManager.broadcastMess(user.getUserId(), user.getUsername(), json.getString("mess"));
        }
    }
}
