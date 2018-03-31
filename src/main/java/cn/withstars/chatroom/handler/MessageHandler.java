package cn.withstars.chatroom.handler;

import cn.withstars.chatroom.domain.User;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-03-31
 * Time: 18:05
 * Mail: withstars@126.com
 */
public class MessageHandler extends SimpleChannelInboundHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame frame) throws Exception {
        User user = UserManager.getUser(channelHandlerContext.channel());
        if (user != null ){
            JSONObject jsonObject = JSONObject.parseObject(frame.text());
            UserManager.broadcastMessage(user.getUserId(),user.getUsername(), jsonObject.getString("mess"));
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
