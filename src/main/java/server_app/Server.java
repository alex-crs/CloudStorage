package server_app;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import server_app.Handlers.ByteBufEncoder;
import server_app.Handlers.MainHandler;
import server_app.SQL_Controllers.AuthService;

public class Server {
    private static EventLoopGroup auth;
    private static EventLoopGroup worker;
    private static final Logger LOGGER = Logger.getLogger(Server.class);

    public Server() {
        auth = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ByteBufEncoder(),
                                    new MainHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(5679).sync();
            AuthService.connect();
            LOGGER.info("Server start");
            future.channel().closeFuture().sync();
            System.out.println("Server finished");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void shutdownServer() {
        auth.shutdownGracefully();
        worker.shutdownGracefully();
    }
}
