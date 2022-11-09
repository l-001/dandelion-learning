package org.dandelion.netty.nettys.line;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * TODO netty client line strategy test
 *
 * @author L
 * @version 1.0
 * @date 2022/4/27 16:07
 */
public class NettyLineClient {

    public static void main(String[] args) {
        // 连接服务端端口
        int port = 8080;
        String ip="127.0.0.1";
        new NettyLineClient().connect(port,ip);
    }

    private void connect(int port,String ip) {
        // 配置客户端 NIO 线程组
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        try {
            Bootstrap bs = new Bootstrap();
            bs.group(clientGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        // 创建 NioSocketChannel 成功后，在进行初始化时，将它的 ChannelHandler 设置到 ChannelPipeline 中，用于处理网络 IO 事件
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 处理粘包拆包问题
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            socketChannel.pipeline().addLast(new StringDecoder());

                            socketChannel.pipeline().addLast(new NettyLineClientHandler());
                        }
                    });
            //发起异步连接操作
            ChannelFuture cf = bs.connect(ip, port).sync();
            //等待客户端链路关闭
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            clientGroup.shutdownGracefully();
        }
    }

}




















