package org.emma.rpc.io;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class RpcNode {
    private static final Logger LOG = LoggerFactory.getLogger(RpcNode.class);

    private final ExecutorService executorService = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private Integer port;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workGroup;

    public RpcNode() {
        this.port = randPort();
    }

    /**
     * Entry of establishing the RPC service.
     */
    public void serve() throws Exception {
        this.bossGroup = new NioEventLoopGroup();
        this.workGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(this.bossGroup, this.workGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new RpcChannelInitializer(this));

        LOG.info("#serve bind port {}", this.port);
        serverBootstrap.bind(this.port).sync();
        LOG.info("#serve server started on port {}", this.port);
    }

    private int randPort() {
        int start = 11000, end = 13000;
        return (int) ((Math.random() * (end - start)) + start);
    }

    protected void shutdown() {
        LOG.info("shutdown ");

    }
}
