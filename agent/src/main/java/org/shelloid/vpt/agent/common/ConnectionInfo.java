/*
 Copyright (c) Shelloid Systems LLP. All rights reserved.
 The use and distribution terms for this software are covered by the
 GNU General Public License 3.0 (http://www.gnu.org/copyleft/gpl.html)
 which can be found in the file LICENSE at the root of this distribution.
 By using this software in any fashion, you are agreeing to be bound by
 the terms of this license.
 You must not remove this notice, or any other, from this software.
 */
package org.shelloid.vpt.agent.common;

import org.shelloid.ptcp.NetworkConstants;
import org.shelloid.ptcp.PseudoTcp;
import org.shelloid.vpt.agent.util.Platform;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/* @author Harikrishnan */
public class ConnectionInfo {
    private static final int MAX_RECV_BUFFER = (int) NetworkConstants.MAX_PACKET;
    public byte[] recvBuffer;
    public boolean hasReceivedRemoteClose;
    public ArrayList<ByteBuf> pendingChannelWriteBufs;
    public int noRouteMsgCount;
    private PortMapInfo portMapInfo;
    private final PseudoTcp ptcp;
    private int agentPort;
    private final boolean svcSide;
    private boolean isConnected;

    private final long connTs;
    private long lastRcvdTs;
    private Channel channel;
    private boolean pendingClose;
    private byte[] pendingPtcpWriteBuf;
    public int totalReadFromPtcp;
    public int totalWrittenToPtcp;

    public ConnectionInfo(PseudoTcp ptcp, int agentPort, boolean isSvcSide, long connTs, long lastRcvdTs, boolean isConnected) {
        this.ptcp = ptcp;
        this.agentPort = agentPort;
        this.svcSide = isSvcSide;
        this.connTs = connTs;
        this.lastRcvdTs = lastRcvdTs;
        this.recvBuffer = new byte[MAX_RECV_BUFFER];
        this.isConnected = isConnected; 
        pendingClose = false;
        hasReceivedRemoteClose = false;
        pendingChannelWriteBufs = new ArrayList<>();
        totalReadFromPtcp = 0;
        totalWrittenToPtcp = 0;
        noRouteMsgCount = 0;
    }
    
    public ConnectionInfo(Channel ch, PortMapInfo info, PseudoTcp ptcp, int agentPort, boolean isSvcSide, long connTs, long lastRcvdTs, boolean isConnected) {
        this(ptcp, agentPort, isSvcSide, connTs, lastRcvdTs, isConnected);
        this.channel = ch;
        this.portMapInfo = info;
    }

    public void setPortMapInfo(PortMapInfo portMapInfo) {
        this.portMapInfo = portMapInfo;
    }

    public PortMapInfo getPortMapInfo() {
        return portMapInfo;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isSvcSide() {
        return svcSide;
    }
    
    public byte[] getRecvBuffer() {
        return recvBuffer;
    }

    public void setRecvBuffer(byte[] recvBuffer) {
        this.recvBuffer = recvBuffer;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public PseudoTcp getPtcp() {
        return ptcp;
    }

    public int getAgentPort() {
        return agentPort;
    }

    public void setAgentPort(int port) {
        this.agentPort = port;
    }

    public long getConnTs() {
        return connTs;
    }

    public long getLastRcvdTs() {
        return lastRcvdTs;
    }
    
    public void setLastRcvdTs(long lastRcvdTs) {
        this.lastRcvdTs = lastRcvdTs;
    }

    public boolean getPendingClose() {
        return pendingClose;
    }
    
    public void setPendingClose(boolean pendingClose) {
        this.pendingClose = pendingClose;
    }

    public byte[] getPendingPtcpWriteBuf() {
        return pendingPtcpWriteBuf;
    }

    public void clearPendingPtcpWritebuf() {
        pendingPtcpWriteBuf = null;
    }

    public void setPendingPtcpWriteBuf(byte[] data, int offset, int size) {
        //System.out.println("Size: " + size + " , offset: " + offset);
        pendingPtcpWriteBuf = new byte[size - offset];
        System.arraycopy(data, offset, pendingPtcpWriteBuf, 0, pendingPtcpWriteBuf.length);
    }

    public ArrayList<ByteBuf> getPendingChannelWriteBufs() {
        return pendingChannelWriteBufs;
    }

    public void clearPendingChannelWriteBufs() {
        pendingChannelWriteBufs.clear();
        pendingChannelWriteBufs = null;
    }

    public boolean isPendingPtcpWriteBufEmpty() {
        return pendingPtcpWriteBuf == null;
    }
}
