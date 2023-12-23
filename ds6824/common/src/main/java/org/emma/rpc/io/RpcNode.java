package org.emma.rpc.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import org.emma.rpc.common.RpcRequest;
import org.emma.rpc.exception.InvalidRpcRequest;
import org.emma.rpc.utils.RpcUtils;
import org.emma.rpc.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;
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
        this.port = RpcUtils.randPort();
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
        serverBootstrap.childHandler(new RpcChannelMasterInitializer(this));

        LOG.info("#serve bind port {}", this.port);
        serverBootstrap.bind(this.port).sync();
        LOG.info("#serve server started on port {}", this.port);
    }


    /**
     * As we just talked about the class RPCNode will be implemented by several classes like
     * the Master -- the server, when Master extends the RpcNode it will invoke the invoke method,
     * that handles the RpcRequest from the client (Worker) side, invoke the method on the server side, and
     * then wrap the execution result to the response body back to the client side.
     * <p>
     * the Worker -- the client, when Worker extends the RpcNode it will invoke this call method,
     * that wrap the local context variables and info like parameter type list, parameter object list and the method name
     * into the RpcRequest.
     * And then send the request to the server (Master) side to handle and waiting for the response body and then extract
     * via some standard to extract the result value from the response body.
     * <p>
     * Master and Worker both extends this class as parent class, but execute different methods.
     * Master -- invoke
     * Worker -- call
     */
    public Object call(int port, String methodName, Object[] args) throws Exception {
        Object ret = null;

        LOG.info("#call recv port > 0 status {}, method name non-blank status {}, args non-null status {}",
                port > 0, StringUtils.isNotEmpty(methodName), Objects.nonNull(args));

        if (port < 0 || StringUtils.isEmptyOrNull(methodName) || Objects.isNull(args)) {
            LOG.info("#call recv invalid input parameter cannot build rpc request and send, return null!");
            return ret;
        }

        RpcRequest request = new RpcRequest();
        // here we build a request that encapsulate context variables and info into it.
        request.setRequestId(UUID.randomUUID().toString());
        request.setMethodName(methodName);
        request.setParameters(args);
        Class<?>[] parameterTypes = RpcUtils.genParameterTypeArr(args);
        request.setParameterTypes(parameterTypes);

        if (!RpcUtils.isValidRpcRequest(request)) {
            LOG.info("#call created request is not valid!");
            throw new InvalidRpcRequest();
        }

        LOG.info("#call client(Worker) side try to create channel and establish connection to the server(Master) " +
                "via the server port {}", port);

        RpcChannel rpcChannel = new RpcChannel(port);
        rpcChannel.setRequest(request);

        try {
            bind(rpcChannel);
            // okay, now we have remote reflection required context and variables -- the data
            // if we want to commit our request to remote server side, we also need resources which can be provided
            // by current main entry's thread or also the extra threads that are allocated from thread pool
            // that is the executor service instance
            // -- that is this method, after we bind our channel to the specified port
            // -- use this thread pool's thread resource to submit our local context data + variable to the remote side to execute
            // -- and the response data is encapsulated in the ret this object
            ret = executorService.submit(rpcChannel).get();
            LOG.info("#call ret of invoked method {} with parameters {} and retrieve ret from the remote with non-null status {}",
                    methodName, request.getParameters(), Objects.nonNull(ret));
            return ret;
        } catch (Exception e) {
            LOG.error("#call failed via exception, return null! ", e);
            return null;
        }
    }

    private void bind(RpcChannel rpcChannel) throws Exception {
        LOG.info("#bind recv rpc channel non-null status {}", Objects.nonNull(rpcChannel));
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .handler(new RpcChannelWorkerInitializer(rpcChannel));

        // 127 is referring to the current client side's ip address
        // and the rpcChannel#getServerPort is referring to the server side's port value
        bootstrap.connect("127.0.0.1", rpcChannel.getServerPort()).sync();
    }


    protected void shutdown() {
        LOG.info("shutdown gracefully begin");
        this.bossGroup.shutdownGracefully();
        this.workGroup.shutdownGracefully();
        LOG.info("shutdown gracefully end");
    }
}
