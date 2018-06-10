package cn.withstars.chatroom.handler;

import cn.withstars.chatroom.domain.User;
import cn.withstars.chatroom.protocol.ChatProto;
import cn.withstars.chatroom.protocol.StatusCode;
import cn.withstars.chatroom.util.Constants;
import cn.withstars.chatroom.util.NettyUtil;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: withstars
 * Date: 2018-06-10
 * Time: 16:52
 * Mail: withstars@126.com
 */
public class UserAuthHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthHandler.class);

    private WebSocketServerHandshaker handshaker;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            //判断Channel是否读空闲,读空闲时移除Channel
            if (event.state().equals(IdleState.READER_IDLE)){
                final String remoteAddress = NettyUtil.parseChannelRemoteAddr(ctx.channel());
                logger.warn("NETTY SERVER PIPIELINE: IDLE exception [{}]", remoteAddress);
                UserInfoManager.removeChannel(ctx.channel());
                UserInfoManager.broadcastInfo(StatusCode.SYS_USER_COUNT, UserInfoManager.getUserCount());
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest){

        }else if (msg instanceof WebSocketFrame){

        }
    }


    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request){
        if (!request.decoderResult().isSuccess() || !"websocket".equals(request.headers().get("Upgrade"))){
            logger.warn("protobuf don't support websocket");
            ctx.channel().close();
            return;
        }
        WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(Constants.WEBSOCKET_URL, null, true);
        handshaker = handshakerFactory.newHandshaker(request);
        if (handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }else {
            // 动态加入websocket编码编解码处理
            handshaker.handshake(ctx.channel(), request);
            // 存储已经连接的channel
            UserInfoManager.addChannel(ctx.channel());
        }
    }

    private void handleWebSocket(ChannelHandlerContext ctx, WebSocketFrame frame){
        // 判断是否关闭链路命令
        if (frame instanceof CloseWebSocketFrame){
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            UserInfoManager.removeChannel(ctx.channel());
            return;
        }
        // 判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame){
            logger.info("Ping message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        // 判断是否是Pong消息
        if (frame instanceof PongWebSocketFrame){
            logger.info("Pong message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        // 本程序目前只支持文本消息
        if (!(frame instanceof TextWebSocketFrame)){
            throw new UnsupportedOperationException(frame.getClass().getName()+" frame type is not supported");
        }

        String message = ((TextWebSocketFrame)frame).text();
        JSONObject json = JSONObject.parseObject(message);
        int code = json.getInteger("code");
        Channel channel = ctx.channel();
        switch (code){
            case StatusCode.PING_CODE:
            case StatusCode.PONG_CODE:
                UserInfoManager.updateUserTime(channel);
                UserInfoManager.sendPong(ctx.channel());
                logger.info("Receive pong message, address : {}", NettyUtil.parseChannelRemoteAddr(channel));
                return;
            case StatusCode.AUTH_CODE:
                boolean isSuccess = UserInfoManager.saveUser(channel, json.getString("username"));
                UserInfoManager.sendInfo(channel,StatusCode.SYS_AUTH_STATE, isSuccess);
                if (isSuccess){
                    UserInfoManager.broadcastInfo(StatusCode.SYS_USER_COUNT, UserInfoManager.getUserCount());
                }
            case StatusCode.MESS_CODE: // 普通的消息留给MessageHanler处理
                 break;

            default:
                logger.warn("The code [{}] can't be auth!!!", code);
                return;
        }
        // 后续消息交给MessageHandler处理
        ctx.fireChannelRead(frame.retain());



    }







}
