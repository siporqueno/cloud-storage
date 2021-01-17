package com.porejemplo.nube.server;

import com.porejemplo.nube.server.auth.repository.StorageType;
import com.porejemplo.nube.server.auth.repository.UserDAO;
import com.porejemplo.nube.server.auth.repository.UserDAOFactory;
import com.porejemplo.nube.server.auth.service.AuthServiceImpl;
import com.porejemplo.nube.server.netty.AuthHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        UserDAO userDAO = UserDAOFactory.getUserDAO(StorageType.SQ_LITE);
        userDAO.connect();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new AuthHandler(new AuthServiceImpl(userDAO)));
                        }
                    });

            ChannelFuture f = b.bind(8189).sync();
            f.channel().closeFuture().sync();
        } finally {
            userDAO.disconnect();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new NettyServer().run();
    }
}
