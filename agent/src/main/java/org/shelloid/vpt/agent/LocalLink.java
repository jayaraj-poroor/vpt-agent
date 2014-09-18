/*
 Copyright (c) Shelloid Systems LLP. All rights reserved.
 The use and distribution terms for this software are covered by the
 GNU General Public License 3.0 (http://www.gnu.org/copyleft/gpl.html)
 which can be found in the file LICENSE at the root of this distribution.
 By using this software in any fashion, you are agreeing to be bound by
 the terms of this license.
 You must not remove this notice, or any other, from this software.
 */
package org.shelloid.vpt.agent;

import org.shelloid.common.ShelloidUtil;
import org.shelloid.ptcp.HelperFunctions;
import org.shelloid.ptcp.IPseudoTcpNotify;
import static org.shelloid.ptcp.NetworkConstants.*;
import org.shelloid.ptcp.PseudoTcp;
import static org.shelloid.vpt.agent.VPTClient.agentConnMap;
import org.shelloid.vpt.agent.common.ConnectionInfo;
import org.shelloid.vpt.agent.common.PortMapInfo;
import org.shelloid.vpt.agent.util.Platform;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/* @author Harikrishnan */
public class LocalLink {

    public static final HashMap<Integer, PortMapInfo> localAgentPortMap = new HashMap<Integer, PortMapInfo>();
    private final VPTClient client;
    private final ShelloidUtil sutils;
    public static final AttributeKey<ConnectionInfo> CONNECTION_MAPPING = AttributeKey.valueOf("CONNECTION_MAPPING");

    public LocalLink(VPTClient cl) {
        client = cl;
        sutils = ShelloidUtil.getInstance();
    }

    public Channel bind(int port) throws Exception {
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 20)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new AppSideAgentHandler());
                    }
                });
        ChannelFuture f = b.bind(port).sync();
        final Channel ch = f.channel();
        if (f.isSuccess()) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    long timeOut = 1000 * 60 * 5;
                    Platform.shelloidLogger.info("Gracefull shutdown initiated.");
                    ChannelFuture cf = ch.close();
                    cf.awaitUninterruptibly(timeOut);
                    bossGroup.shutdownGracefully().awaitUninterruptibly(timeOut);
                    workerGroup.shutdownGracefully().awaitUninterruptibly(timeOut);
                    Platform.shelloidLogger.info("Gracefull shutdown finidhed.");
                }
            });
            return ch;
        } else {
            throw new Exception("Can't bind to " + port);
        }
    }

    public Bootstrap getClientBootstrap() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new SvcSideAgentHandler());
                    }
                });
        return b;
    }

    void removeBininding(Channel ch) {
        try {
            ch.close().sync();
        } catch (InterruptedException ex) {
            Platform.shelloidLogger.error("InterruptedException while closing channel", ex);
        }
    }

    public class PTCPNotifier implements IPseudoTcpNotify {

        @Override
        public void onTcpOpen(PseudoTcp tcp) {
            Platform.shelloidLogger.debug("TCP open from: " + tcp.attachment());
            onTcpWriteable(tcp);
        }

        @Override
        public void onTcpReadable(PseudoTcp tcp) {
            //System.out.println("onTcpReadable");
            ConnectionInfo connInfo = ((ConnectionInfo) tcp.attachment());
            int len = tcp.recv(connInfo.recvBuffer);
            //System.out.println("Read from TCP: " + len);
            if (len > 0) {
                connInfo.totalReadFromPtcp += len;
                //System.out.println("Total Read from PTCP so far: " + connInfo.totalReadFromPtcp);
            }
            if (len == SOCKET_ERROR) {
                assert (tcp.getError() == EWOULDBLOCK);
                return;
            }
            ByteBuf buf = Unpooled.buffer(len);
            buf.writeBytes(connInfo.recvBuffer, 0, len);
            if (connInfo.isConnected()) {
                connInfo.getChannel().writeAndFlush(buf);
            } else {
                connInfo.pendingChannelWriteBufs.add(buf);
                return;
            }
            if (tcp.getRcvBufLen() > 0) {
                onTcpReadable(tcp);
            } else if (connInfo.getPendingClose()) {
                String connId = connInfo.getPortMapInfo().getPortMapId() + ":" + connInfo.getConnTs();
                try {
                    connInfo.getChannel().close().sync();
                } catch (InterruptedException ex) {
                    Platform.shelloidLogger.error("InterruptedException while closing channel", ex);
                }
                tcp.close(true);
                Platform.shelloidLogger.info("Closing channel from onTcpReadable. So removing from agentConnMap: " + connId);
                agentConnMap.remove(connId);
            }
        }

        @Override
        public void onTcpWriteable(PseudoTcp tcp) {
            //System.out.println("onTcpWriteable");
            ConnectionInfo info = ((ConnectionInfo) tcp.attachment());
            byte[] pendingPtcpWriteBuf = info.getPendingPtcpWriteBuf();
            if (pendingPtcpWriteBuf != null) {
                uplinkData(info, pendingPtcpWriteBuf, pendingPtcpWriteBuf.length);
            }
        }

        @Override
        public void onTcpClosed(PseudoTcp tcp, long error) {
            Platform.shelloidLogger.info("closing channel onTcpClosed");
            ConnectionInfo info = ((ConnectionInfo) tcp.attachment());
            try {
                info.getChannel().close().sync();
            } catch (InterruptedException ex) {
                Platform.shelloidLogger.error("InterruptedException while closing channel", ex);
            }
            info.setChannel(null);
        }

        @Override
        public IPseudoTcpNotify.WriteResult tcpWritePacket(PseudoTcp tcp, byte[] buffer, final int len) {
            //System.out.println("tcpWritePacket");
            ConnectionInfo connInfo = (ConnectionInfo) tcp.attachment();
            Long portMapId = connInfo.getPortMapInfo().getPortMapId();
            client.sendTunnelMessage(client.getChannel(), portMapId, connInfo.isSvcSide(), connInfo.getConnTs(), buffer, len, null);
            return IPseudoTcpNotify.WriteResult.WR_SUCCESS;
        }

        @Override
        public void log(PseudoTcp tcp, IPseudoTcpNotify.LogType type, String msg) {
            Platform.shelloidLogger.debug("TCP Log: (" + type + ")" + msg);
        }
    }

    public void uplinkData(ConnectionInfo connInfo, byte[] data, int length) {
        PseudoTcp ptcp = connInfo.getPtcp();
        if (connInfo.getChannel() == null || ptcp.state() == PseudoTcp.TcpState.TCP_CLOSED) {
            Platform.shelloidLogger.debug("Channel is null or PTCP is closed");
            /* return */
        } else {
            int nWritten = ptcp.send(data, length);
            if (nWritten > 0) {
                connInfo.totalWrittenToPtcp += nWritten;
            }
            if (nWritten == length) {
                connInfo.getChannel().config().setAutoRead(true);
                connInfo.clearPendingPtcpWritebuf();
                Platform.shelloidLogger.info("Completely sent data via PTCP.");
            } else {
                if (nWritten == SOCKET_ERROR && (ptcp.getError() == EWOULDBLOCK || ptcp.getError() == ENOTCONN)) {
                    Platform.shelloidLogger.debug("PTCP Error: (" + ptcp.getError() + ")");
                    nWritten = 0;
                }
                connInfo.getChannel().config().setAutoRead(false);
                Platform.shelloidLogger.debug("PTCP could send only " + nWritten + " bytes of " + length + ".");
                connInfo.setPendingPtcpWriteBuf(data, nWritten, length);
            }
        }
    }

    private void doRemoteClose(ChannelHandlerContext ctx) {
        ConnectionInfo conn = ctx.channel().attr(CONNECTION_MAPPING).get();
        if (conn != null) {
            Long portMapId = conn.getPortMapInfo().getPortMapId();
            String connId = portMapId + ":" + conn.getConnTs();
            client.doRemoteClose(client.getChannel(), ctx.channel(), connId, portMapId, conn.isSvcSide(), conn.getConnTs(), conn.getPtcp(), conn);
        }
    }

    private void sendDataToAgent(Object msg, ChannelHandlerContext ctx) {
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        ConnectionInfo connInfo = ctx.channel().attr(CONNECTION_MAPPING).get();
        assert (connInfo.isPendingPtcpWriteBufEmpty() == true);
        uplinkData(connInfo, bytes, bytes.length);
    }

    class SvcSideAgentHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            sendDataToAgent(msg, ctx);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            Platform.shelloidLogger.info("Sending Remote Close (ReasonL.1)");
            doRemoteClose(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Platform.shelloidLogger.info("Agent opens " + ctx.channel().toString());
            Channel ch = ctx.channel();
            ConnectionInfo connInfo = (ConnectionInfo) ch.attr(CONNECTION_MAPPING).get();
            assert (connInfo != null);
            connInfo.setIsConnected(true);
            ArrayList<ByteBuf> buffs = connInfo.getPendingChannelWriteBufs();
            Iterator<ByteBuf> it = buffs.iterator();
            while (it.hasNext()) {
                ByteBuf buf = it.next();
                try {
                    ch.writeAndFlush(buf);
                } finally {
                }
                it.remove();
            }
            connInfo.clearPendingChannelWriteBufs();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Platform.shelloidLogger.debug("CHANNEL INACTIVE: SVC_SIDE");
            Platform.shelloidLogger.info("Sending Remote Close (ReasonL.2) for channel: " + ctx.channel());
            doRemoteClose(ctx);
        }
    }

    class AppSideAgentHandler extends ChannelInboundHandlerAdapter {

        private final ScheduledExecutorService executer = Executors.newSingleThreadScheduledExecutor();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            sendDataToAgent(msg, ctx);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            Platform.shelloidLogger.info("Sending Remote Close (ReasonL.3)");
            doRemoteClose(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Platform.shelloidLogger.info("Browser opens " + ctx.channel().toString());
            Channel ch = ctx.channel();
            if (ch.attr(CONNECTION_MAPPING).get() != null) {
                Platform.shelloidLogger.debug("channelActive Connection info already exists.");
                return;
            }
            int agentPort = sutils.getLocalPort(ch);
            PortMapInfo info = VPTClient.agentPortMap.get(agentPort);
            if (info != null) {
                long currentTime = generateConnectionTs();
                PseudoTcp ptcp = new PseudoTcp(new PTCPNotifier(), 0);
                String connId = info.getPortMapId() + ":" + currentTime;
                if (VPTClient.agentConnMap.get(connId) != null) {
                    Platform.shelloidLogger.debug("ConnectionInfo already exists for " + connId);
                    try {
                        ch.close().sync();
                    } catch (InterruptedException ex) {
                        Platform.shelloidLogger.error("InterruptedException while closing channel", ex);
                    }
                    return;
                }
                ConnectionInfo connInfo = new ConnectionInfo(ch, info, ptcp, agentPort, false, currentTime, currentTime, true);
                VPTClient.agentConnMap.put(connId, connInfo);
                ch.attr(CONNECTION_MAPPING).set(connInfo);
                ptcp.attach(connInfo);
                Platform.shelloidLogger.info("Connection ID from AppSideAgentHandler.channelActive: " + connId);
                Platform.shelloidLogger.debug("PTCP connecting from App Side");
                ptcp.connect();
                HelperFunctions.adjustClock(executer, ptcp);
            } else {
                Platform.shelloidLogger.error("No port-map info for: " + agentPort);
                try {
                    ch.close().sync();
                } catch (InterruptedException ex) {
                    Platform.shelloidLogger.error("InterruptedException while closing channel", ex);
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            Platform.shelloidLogger.debug("CHANNEL INACTIVE: APP_SIDE");
            Platform.shelloidLogger.info("Sending Remote Close (ReasonL.4)");
            doRemoteClose(ctx);
        }

    }

    private static synchronized long generateConnectionTs() {
        long connTs = System.currentTimeMillis();
        if (connTs == lastConnectionTs) {
            connTs++;
        }
        lastConnectionTs = connTs;
        return connTs;
    }

    static long lastConnectionTs = 0;
}
