package cn.jasper.iot.mqtt.handler;

import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @description: TCP连接超时处理
 * 有些没有严格遵MQTT规范的客户端连接时TCP连接成功，但是没有发送CONNECT，可能导致大量SocketChannel连接积压、内在暴涨、高频率GC和较长的STW时间
 * 在TCP连接阶段把SocketChannel缓存起来，设置超时时间关闭连接
 * 如果成功发CONNECT则把SocketChannel从缓存中删除
 * @author: jasper
 * @create: 2020-12-23 15:07
 */
public class TcpTimeoutHandler extends ChannelDuplexHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpTimeoutHandler.class);
    private int tcpTimeoutTimeSeconds;
    private ScheduledFuture<?> tcpTimeout;
    public TcpTimeoutHandler(int tcpTimeoutTimeSeconds){
        this.tcpTimeoutTimeSeconds = tcpTimeoutTimeSeconds;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        if(tcpTimeoutTimeSeconds>0){
            tcpTimeout = schedule(ctx, new TcpTimeoutHandler.TcpTimeoutTask(ctx),
                    tcpTimeoutTimeSeconds, TimeUnit.SECONDS);
        }
        super.channelActive(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 收到CONNECT的ACK后把定时任务取消
        if (tcpTimeoutTimeSeconds > 0) {
            ctx.write(msg, promise.unvoid()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    tcpTimeout.cancel(true);
                }
            });
        } else {
            ctx.write(msg, promise);
        }
    }

    ScheduledFuture<?> schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
        return ctx.executor().schedule(task, delay, unit);
    }
    private final class TcpTimeoutTask implements Runnable{
        private final ChannelHandlerContext ctx;
        public TcpTimeoutTask(ChannelHandlerContext ctx){
            this.ctx = ctx;
        }
        @Override
        public void run() {
            ChannelFuture future = ctx.channel().close();
            future.addListener(new GenericFutureListener(){
                @Override
                public void operationComplete(Future future) throws Exception {
                    LOGGER.info("Channel close,The CONNECT sent by the client was not received during the timeout[{}] period",tcpTimeoutTimeSeconds);
                }
            });
        }
    }
}
