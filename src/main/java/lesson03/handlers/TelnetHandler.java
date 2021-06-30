package lesson03.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class TelnetHandler extends SimpleChannelInboundHandler<String> {
    private TelnetUser client;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client connected: " + ctx.channel());
        ctx.writeAndFlush("Hello user!\r\n");
        ctx.writeAndFlush("Welcome to my second Telnet server from NETTY.\r\nPlease enter your command. " +
                "for help enter --help\r\n\n");
        client = new TelnetUser();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client disconnected: " + ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        CommandHandler.query(msg, client, ctx);
    }


}
