package lesson03;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lesson03.handlers.TelnetHandler;

public class NettyBaseServer {
    private static EventLoopGroup auth;
    private static EventLoopGroup worker;
    public NettyBaseServer() {
        auth = new NioEventLoopGroup(1); // light
        worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new StringDecoder(), // in - 1
                                    new StringEncoder(), // out - 1
                                    new TelnetHandler() // in - 2
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(5679).sync();
            System.out.println("Server started");
            future.channel().closeFuture().sync();
            System.out.println("Server finished");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
    public static void shutdownServer(){
        auth.shutdownGracefully();
        worker.shutdownGracefully();
    }

    public static void main(String[] args) {
        new NettyBaseServer();
    }
}
