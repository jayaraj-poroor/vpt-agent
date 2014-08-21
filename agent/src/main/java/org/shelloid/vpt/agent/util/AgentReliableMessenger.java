/*
 Copyright (c) Shelloid Systems LLP. All rights reserved.
 The use and distribution terms for this software are covered by the
 GNU General Public License 3.0 (http://www.gnu.org/copyleft/gpl.html)
 which can be found in the file LICENSE at the root of this distribution.
 By using this software in any fashion, you are agreeing to be bound by
 the terms of this license.
 You must not remove this notice, or any other, from this software.
 */

package org.shelloid.vpt.agent.util;

import org.shelloid.common.messages.MessageFields;
import org.shelloid.common.messages.ShelloidMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/* @author Harikrishnan */
public class AgentReliableMessenger {

    private static final int MAX_PENDING_MSG_QUEUE_SIZE = 1000;

    private Atomic.Long lastSendAckNum;
    private Atomic.Long nextSeqNum;
    private BlockingQueue<ShelloidMessage> pendingMsgQueue;
    private DB db;

    public void initDb() throws IOException {
        db = DBMaker.newFileDB(new File("messageStore"))
                .closeOnJvmShutdown()
                .make();
        pendingMsgQueue = db.getQueue("pendingMsgQueue");
        lastSendAckNum = db.getAtomicLong("lastSendAckNum");
        nextSeqNum = db.getAtomicLong("nextSeqNum");
    }
    
    public void deleteDbFile() {
        new File("messageStore").delete();
    }

    public void sendToClient(ShelloidMessage msg, Channel ch) {
        boolean toSend = false;
        synchronized (pendingMsgQueue) {
            msg.put(MessageFields.seqNum, getNewMsgSeqNo());
            Platform.shelloidLogger.info("Client Scheduling Reliable Msg: " + msg.getJson());
            if (pendingMsgQueue.peek() == null) {
                toSend = true;
            }
            /*
             TODO: 
             if (pendingMsgQueue.size() > MAX_PENDING_MSG_QUEUE_SIZE) {
             pendingMsgQueue.remove();
             Platform.shelloidLogger.warn("Removing message from pending message queue since it overflown");
             }
             */
            pendingMsgQueue.add(msg);
            db.commit();
        }
        if (toSend) {
            sendImmediate(msg, ch);
        }
    }

    public void sendImmediate(ShelloidMessage msg, Channel ch) {
        sendToWebSocket(msg.getJson(), ch);
    }

    public void processAckMsg(String seqNo, Channel ch) {
        long ackSeqNum = Long.parseLong(seqNo);
        if (ackSeqNum == -1) {
            setLastSentAckNum(-1);
        }
        ShelloidMessage msg = null;
        synchronized (pendingMsgQueue) {
            boolean removeFromQ = true;
            while(removeFromQ){
                msg = pendingMsgQueue.peek();
                if(msg == null || msg.getLong(MessageFields.seqNum) > ackSeqNum){
                    removeFromQ = false;
                }else{
                    pendingMsgQueue.remove();
                }
            }
            db.commit();
        }
        if(msg != null){
            sendImmediate(msg, ch);
        }
    }

    public long getLastSendAckNum() {
        long num = lastSendAckNum.get();
        Platform.shelloidLogger.debug("Client returning lastSendAckNum (" + num + ") from file ");
        return num;
    }

    public void setLastSentAckNum(long ack) {
        Platform.shelloidLogger.debug("Client saving lastSendAckNum (" + ack + ") to file");
        lastSendAckNum.set(ack);
        db.commit();
    }

    private long getNewMsgSeqNo() {
        long num = nextSeqNum.addAndGet(1);
        Platform.shelloidLogger.debug("Client generating new message id ("+num+") from file ");
        db.commit();
        return num;
    }

    private void sendToWebSocket(String msg, Channel ch) {
        Platform.shelloidLogger.debug("Client Sending " + msg);
        ch.writeAndFlush(new TextWebSocketFrame(msg));
    }
}
