package com.porejemplo.nube.server;

import com.porejemplo.nube.server.auth.repository.UserDAOSQLite;
import com.porejemplo.nube.server.auth.service.AuthServiceImpl;
import com.porejemplo.nube.server.netty.AuthHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class NettyServer {

    private static Connection connection;

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        connect();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new AuthHandler(new AuthServiceImpl(new UserDAOSQLite(connection))));
                        }
                    });

            ChannelFuture f = b.bind(8189).sync();
            f.channel().closeFuture().sync();
        } finally {
            disconnect();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server\\mainDb.db");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new NettyServer().run();
    }
}
