/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shelloid.ptcp;

import static org.shelloid.ptcp.HelperFunctions.*;
import static org.shelloid.ptcp.IPseudoTcpNotify.LogType.*;
import org.shelloid.ptcp.IPseudoTcpNotify.WriteResult;
import static org.shelloid.ptcp.IPseudoTcpNotify.WriteResult.*;
import static org.shelloid.ptcp.NetworkConstants.*;
import static org.shelloid.ptcp.PseudoTcp.SendFlags.*;
import static org.shelloid.ptcp.PseudoTcp.Shutdown.*;
import static org.shelloid.ptcp.PseudoTcp.TcpState.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;

/**
 *
 * @author Jayaraj Poroor
 */
public class PseudoTcp {
  
    public long now()
    {
        return System.currentTimeMillis()&0xFFFFFFFFL;
    }

    public PseudoTcp(IPseudoTcpNotify notify, long conv)
    {
        m_notify = notify;
        m_shutdown = SD_NONE;
        m_error = 0;

        // Sanity check on buffer sizes (needed for OnTcpWriteable notification logic)
        assert(m_rbuf.length + MIN_PACKET < m_sbuf.length);

        long now = now();

        m_state = TCP_LISTEN;
        m_conv = conv & 0xFFFFFFFFL;
        m_rcv_wnd = m_rbuf.length;
        m_snd_nxt = m_slen = 0;
        m_snd_wnd = 1;
        m_snd_una = m_rcv_nxt = m_rlen = 0;
        m_bReadEnable = true;
        m_bWriteEnable = false;
        m_t_ack = 0;

        m_msslevel = 0;
        m_largest = 0;
        assert(MIN_PACKET > PACKET_OVERHEAD);
        m_mss = MIN_PACKET - PACKET_OVERHEAD;
        m_mtu_advise = MAX_PACKET;

        m_rto_base = 0;

        m_cwnd = 2 * m_mss;
        m_ssthresh = m_rbuf.length;
        m_lastrecv = m_lastsend = m_lasttraffic = now;
        m_bOutgoing = false;

        m_dup_acks = 0;
        m_recover = 0;

        m_ts_recent = m_ts_lastack = 0;

        m_rx_rto = DEF_RTO;
        m_rx_srtt = m_rx_rttvar = 0;
    }
    
    public void attach(Object o)
    {
        attachment = o;
    }
    
    public Object attachment()
    {
        return attachment;
    }
    
    public void cleanup()
    {
        
    }
    
    public boolean isClosed()
    {
        return (m_shutdown != SD_NONE) || (m_state == TCP_CLOSED);
    }

    public synchronized int connect()
    {
        if (m_state != TCP_LISTEN) {
          m_error = EINVAL;
          return -1;
        }

        m_state = TCP_SYN_SENT;
        
        //LOG(LS_INFO) << "State: TCP_SYN_SENT";

        byte[] buffer = new byte[1];
        
        buffer[0] = CTL_CONNECT;
        queue(buffer, 1, true);
        attemptSend();
        return 0;
    }
    
    public synchronized int recv(byte []buffer)
    {
        if (m_state != TCP_ESTABLISHED) 
        {
           m_error = ENOTCONN;
           return SOCKET_ERROR;
        }

        if (m_rlen == 0) 
        {
            m_bReadEnable = true;
            m_error = EWOULDBLOCK;
            return SOCKET_ERROR;
        }

        long read = min(buffer.length, m_rlen);
        //memcpy(buffer, m_rbuf, read);
        System.arraycopy(m_rbuf, 0, buffer, 0, (int)read);
         m_rlen -= read;

         // !?! until we create a circular buffer, we need to move all of the rest of the buffer up!
         //memmove(m_rbuf, m_rbuf + read, sizeof(m_rbuf) - read/*m_rlen*/);
        System.arraycopy(m_rbuf, (int)read, m_rbuf, 0, (int)(m_rbuf.length - read));

        if ((m_rbuf.length - m_rlen - m_rcv_wnd) >= min(m_rbuf.length / 2, m_mss)) 
        {
            boolean bWasClosed = (m_rcv_wnd == 0); // !?! Not sure about this was closed business

            m_rcv_wnd = m_rbuf.length - m_rlen;

            if (bWasClosed) 
            {
                attemptSend(sfImmediateAck);
            }
        }
        return (int) read;
    }
    
    public synchronized int send(byte []buffer, int len)
    {
        assert(len <= buffer.length);
        if (m_state != TCP_ESTABLISHED) 
        {
            m_error = ENOTCONN;
            return SOCKET_ERROR;
        }

        if (m_slen == m_sbuf.length) 
        {
            m_bWriteEnable = true;
            m_error = EWOULDBLOCK;
            return SOCKET_ERROR;
        }

        long written = queue(buffer, len, false);
        if(written < len){
            m_bWriteEnable = true;
        }
        attemptSend();
        return (int) written;
    }
    
    public long getSendBufLen()
    {
        return this.m_slen;
    }

    public long getRcvBufLen()
    {
        return this.m_rlen;
    }
    
    public void close(boolean force)
    {
        m_shutdown = force ? SD_FORCEFUL : SD_GRACEFUL;        
    }
    
    public int getError()
    {
        return m_error;
    }
    
    public void resetError()
    {
        m_error = 0;
    }

    public enum TcpState {
      TCP_LISTEN, TCP_SYN_SENT, TCP_SYN_RECEIVED, TCP_ESTABLISHED, TCP_CLOSED;
      boolean beforeEstablished;
      static {
          TCP_LISTEN.beforeEstablished = true;
          TCP_SYN_SENT.beforeEstablished = true;
          TCP_SYN_RECEIVED.beforeEstablished = true;
          TCP_ESTABLISHED.beforeEstablished = false;
          TCP_CLOSED.beforeEstablished = false;
      }
    };    
    
    public TcpState state() 
    { 
        return m_state; 
    }

    // Call this when the PMTU changes.
    public synchronized void notifyMTU(int mtu)
    {
        m_mtu_advise = mtu;
        if (m_state == TCP_ESTABLISHED) {
           adjustMTU();
        }        
    }

    // Call this based on timeout value returned from GetNextClock.
    // It's ok to call this too frequently.
    public synchronized void notifyClock(long now)
    {
        if (m_state == TCP_CLOSED)
          return;

          // Check if it's time to retransmit a segment
        if (m_rto_base != 0 && (timeDiff(m_rto_base + m_rx_rto, now) <= 0)) 
        {
            if (m_slist.isEmpty()) 
            {
              assert(false);
            } else 
            {
                // Note: (m_slist.front().xmit == 0)) {
                // retransmit segments
                if (!transmit(m_slist.listIterator(), now)) 
                {
                    closedown(ECONNABORTED);
                    return;
                }

                long nInFlight = m_snd_nxt - m_snd_una;
                m_ssthresh = max(nInFlight / 2, 2 * m_mss);
                m_cwnd = m_mss;

                // Back off retransmit timer.  Note: the limit is lower when connecting.
                long rto_limit = m_state.beforeEstablished ? DEF_RTO : MAX_RTO;
                m_rx_rto = min(rto_limit, m_rx_rto * 2);
                m_rto_base = now;
            }
        }

        // Check if it's time to probe closed windows
        if ((m_snd_wnd == 0) && (timeDiff(m_lastsend + m_rx_rto, now) <= 0)) 
        {
            if (timeDiff(now, m_lastrecv) >= 15000) 
            {
              closedown(ECONNABORTED);
              return;
            }

          // probe the window
          packet(m_snd_nxt - 1, 0, null, 0, 0);
          m_lastsend = now;

          // back off retransmit timer
          m_rx_rto = min(MAX_RTO, m_rx_rto * 2);
        }

        // Check if it's time to send delayed acks
        if (m_t_ack != 0 && (timeDiff(m_t_ack + ACK_DELAY, now) <= 0)) 
        {
            packet(m_snd_nxt, 0, null, 0, 0);
        }

        if(PSEUDO_KEEPALIVE)
        {
          // Check for idle timeout
          if ((m_state == TCP_ESTABLISHED) && (timeDiff(m_lastrecv + IDLE_TIMEOUT, now) <= 0)) 
          {
            closedown(ECONNABORTED);
            return;
          }
          // Check for ping timeout (to keep udp mapping open)
          if ((m_state == TCP_ESTABLISHED) && 
              (timeDiff(m_lasttraffic + (m_bOutgoing ? IDLE_PING * 3/2 : IDLE_PING), now) <= 0)) 
          {
            packet(m_snd_nxt, 0, null, 0, 0);
          }
        }
    }

    // Call this whenever a packet arrives.
    // Returns true if the packet was processed successfully.
    public synchronized boolean notifyPacket(byte[] buffer, int len)
    {
        assert(len <= buffer.length);
        if (len > MAX_PACKET) 
        {
            m_notify.log(this, LOG_ERROR, "packet too large");
            return false;
        }
        return parse(buffer, len);
    }

    // Call this to determine the next time NotifyClock should be called.
    // Returns false if the socket is ready to be destroyed.
    public synchronized boolean getNextClock(long now, MutableLong timeout)
    {
        return clock_check(now, timeout);
    }

    public enum SendFlags { sfNone, sfDelayedAck, sfImmediateAck };

    // Note: can't go as high as 1024 * 64, because of uint16 precision
    //JAYARAJ: 32-bit wnd
    static final int kRcvBufSize = 1024 * 1024;
    //static final int kRcvBufSize = 1024 * 60;
    // Note: send buffer should be larger to make sure we can always fill the
    // receiver window
    //JAYARAJ: 32-bit wnd
    static final int kSndBufSize = 1024 * 1600;
    //static final int kSndBufSize = 1024 * 90;

    static class Segment {
      long conv, seq, ack;
      int flags;
      //JAYARAJ: 32-bit wnd
      long wnd;
      //int wnd;
      byte[] data;
      int data_offset;
      long len;
      long tsval, tsecr;
    };

    class SSegment {
      SSegment(long s, long l, boolean c){
          seq = s;
          len = l;
          xmit =0;
          bCtrl = c;
      }
      long seq, len;
      //uint32 tstamp;
      int xmit;
      boolean bCtrl;
    };
      
    static class SList extends LinkedList<SSegment>
    {
    };

    static class RSegment {
      long seq, len;
    };

    long queue(byte[] data, int len, boolean bCtrl)
    {
        assert(len <= data.length);
        if (len > m_sbuf.length - m_slen) {
          assert(!bCtrl);
          len = (int) (m_sbuf.length - m_slen);
        }

        // We can concatenate data if the last segment is the same type
        // (control v. regular data), and has not been transmitted yet
        SSegment back = null;
        if(!m_slist.isEmpty()){
            back = m_slist.getLast();
        }
        
        if (back != null && (back.bCtrl == bCtrl) && (back.xmit == 0)) 
        {
            back.len += len;
        }else 
        {
            SSegment sseg = new SSegment(m_snd_una + m_slen, len, bCtrl);
             m_slist.addLast(sseg);
        }

        //memcpy(m_sbuf + m_slen, data, len);
        System.arraycopy(data, 0, m_sbuf, (int)m_slen, (int)len);
        m_slen += len;
        //LOG(LS_INFO) << "PseudoTcp::queue - m_slen = " << m_slen;
        return len;
    }

    IPseudoTcpNotify.WriteResult packet(long seq, int flags, byte[] data, int offset, int len)
    {
        assert(HEADER_SIZE + len <= MAX_PACKET);

        long now = now();

        byte buffer[] = new byte[(int)MAX_PACKET];
        long_to_bytes(m_conv, buffer, 0);
        long_to_bytes(seq, buffer, 4);
        long_to_bytes(m_rcv_nxt, buffer, 8);
        buffer[12] = 0;
        buffer[13] = (byte) (flags&0xff);
        short_to_bytes((int)m_rcv_wnd, buffer, 14);

        // Timestamp computations
        //JAYARAJ: 32-bit wnd
        long_to_bytes(now, buffer, 18);
        long_to_bytes(m_ts_recent, buffer, 22);
        /*long_to_bytes(now, buffer, 16);
        long_to_bytes(m_ts_recent, buffer, 20);
        */
        m_ts_lastack = m_rcv_nxt;

        //memcpy(buffer + HEADER_SIZE, data, len);
        if(len > 0 && data != null){
            System.arraycopy(data, offset, buffer, (int)HEADER_SIZE, len);
        }

        IPseudoTcpNotify.WriteResult wres = 
                m_notify.tcpWritePacket(this, buffer, (int) (len + HEADER_SIZE));
        // Note: When data is NULL, this is an ACK packet.  We don't read the return value for those,
        // and thus we won't retry.  So go ahead and treat the packet as a success (basically simulate
        // as if it were dropped), which will prevent our timers from being messed up.
        if ((wres != WR_SUCCESS) && (null != data))
        {
          return wres;
        }

        m_t_ack = 0;
        if (len > 0) {
          m_lastsend = now;
        }
        m_lasttraffic = now;
        m_bOutgoing = true;

        return WR_SUCCESS;        
    }
    
    boolean parse(byte[] buffer, int size)
    {
        if (size < 12)
          return false;

        Segment seg = new Segment();
        seg.conv = bytes_to_long(buffer, 0);
        seg.seq = bytes_to_long(buffer, 4);
        seg.ack = bytes_to_long(buffer, 8);
        seg.flags = buffer[13]&0xff;
        seg.wnd = bytes_to_short(buffer, 14);        
        //JAYARAJ: 32-bit wnd
        seg.tsval = bytes_to_long(buffer, 18);
        seg.tsecr = bytes_to_long(buffer, 22);        
/*      seg.tsval = bytes_to_long(buffer, 16);
        seg.tsecr = bytes_to_long(buffer, 20);
*/
        //seg.data = buffer;
        //seg.data_offset = (int)HEADER_SIZE;
        seg.len = size - HEADER_SIZE;        
        seg.data = new byte[(int)seg.len];
        System.arraycopy(buffer, (int)HEADER_SIZE, seg.data, 0, (int)seg.len);

        return process(seg);
    }

    void attemptSend()
    {
        attemptSend(sfNone);
    }
    
    void attemptSend(SendFlags sflags)
    {        
        long now = now();

        if (timeDiff(now, m_lastsend) > m_rx_rto) {
          m_cwnd = m_mss;
        }

        while (true) {
          long cwnd = m_cwnd;
          if ((m_dup_acks == 1) || (m_dup_acks == 2)) { // Limited Transmit
            cwnd += m_dup_acks * m_mss;
          }
          long nWindow = min(m_snd_wnd, cwnd);
          long nInFlight = m_snd_nxt - m_snd_una;
          long nUseable = (nInFlight < nWindow) ? (nWindow - nInFlight) : 0;

          long nAvailable = min(m_slen - nInFlight, m_mss);

          if (nAvailable > nUseable) {
            if (nUseable * 4 < nWindow) {
              // RFC 813 - avoid SWS
              nAvailable = 0;
            } else {
              nAvailable = nUseable;
            }
          }
          if (nAvailable == 0) {
            if (sflags == sfNone)
              return;

            // If this is an immediate ack, or the second delayed ack
            if ((sflags == sfImmediateAck) || (m_t_ack != 0L) ) {
              packet(m_snd_nxt, 0, null, 0, 0);
            } else {
              m_t_ack = now();
            }
            return;
          }

          // Nagle algorithm
          if ((m_snd_nxt > m_snd_una) && (nAvailable < m_mss))  {
            return;
          }

          // Find the next segment to transmit
          ListIterator<SSegment> it = m_slist.listIterator();
          //while (it->xmit > 0) {
          while(it.hasNext()){
            //++it;
            SSegment item = it.next();
            if(!(item.xmit > 0)){
                it.previous();
                break;
            }
            //ASSERT(it != m_slist.end());
          }
          assert(it.hasNext());
          //SList::iterator seg = it;
          SSegment seg = it.next();          

          // If the segment is too large, break it into two
          if (seg.len > nAvailable) {
            SSegment subseg = 
                    new SSegment(seg.seq + nAvailable, seg.len - nAvailable, seg.bCtrl);
            seg.len = nAvailable;
            //m_slist.insert(++it, subseg);
            it.add(subseg);
            it.previous();//reset the iterator, so what it points to seg when calling transmit
          }

          //if (!transmit(seg, now)) {
          //we need to make it point to seg, so:
          assert(it.hasPrevious());
          it.previous();
          if(!transmit(it, now)){
            m_notify.log(this, LOG_VERBOSE,  "transmit failed");
            // TODO: consider closing socket
            return;
          }

          sflags = sfNone;
        }        
    }

    void closedown()
    {
        closedown(0);
    }
    
    void closedown(long err)
    {
        m_slen = 0;

        m_notify.log(this, LOG_INFO, "State: TCP_CLOSED");
        m_state = TCP_CLOSED;
        if (m_notify != null) {
          m_notify.onTcpClosed(this, err);
        }        
        //notify(evClose, err);
    }

    boolean clock_check(long now, MutableLong nTimeout)
    {
        if (m_shutdown == SD_FORCEFUL){
          return false;
        }
        if ((m_shutdown == SD_GRACEFUL) && 
            ((m_state != TCP_ESTABLISHED) || ((m_slen == 0) && (m_t_ack == 0)))) 
        {
          return false;
        }

        if (m_state == TCP_CLOSED) {
          nTimeout.value = CLOSED_TIMEOUT;
          return true;
        }

        nTimeout.value = DEFAULT_TIMEOUT;

        if (m_t_ack != 0) 
        {
          nTimeout.value = min(nTimeout.value, timeDiff(m_t_ack + ACK_DELAY, now));
        }
        if (m_rto_base != 0) 
        {
            nTimeout.value = min(nTimeout.value, timeDiff(m_rto_base + m_rx_rto, now));
        }
        if (m_snd_wnd == 0) 
        {
          nTimeout.value = min(nTimeout.value, timeDiff(m_lastsend + m_rx_rto, now));
        }
        if(PSEUDO_KEEPALIVE)
        {
            if (m_state == TCP_ESTABLISHED) 
            {
              nTimeout.value = min(nTimeout.value,
                timeDiff(m_lasttraffic + (m_bOutgoing ? IDLE_PING * 3/2 : IDLE_PING), now));
            }
        }
        return true;
    }
    
    boolean process(Segment seg) {
        // If this is the wrong conversation, send a reset!?! (with the correct conversation?)
        if (seg.conv != m_conv) {
           //if ((seg.flags & FLAG_RST) == 0) {
            //  packet(tcb, seg.ack, 0, FLAG_RST, 0, 0);
            //}
            m_notify.log(this, LOG_ERROR, "wrong conversation");
            return false;
        }

        long now = now();
        m_lasttraffic = m_lastrecv = now;
        m_bOutgoing = false;

        if (m_state == TCP_CLOSED) {
            // !?! send reset?
            m_notify.log(this, LOG_ERROR, "closed");
            return false;
        }

        // Check if this is a reset segment
        if ((seg.flags & FLAG_RST) != 0) {
            closedown(ECONNRESET);
            return false;
        }

        // Check for control data
        boolean bConnect = false;
        if ((seg.flags & FLAG_CTL) != 0) {
            if (seg.len == 0) {
                m_notify.log(this, LOG_ERROR, "Missing control code");
                return false;
            } else if (seg.data[0] == CTL_CONNECT) {
                bConnect = true;
                if (m_state == TCP_LISTEN) {
                    m_state = TCP_SYN_RECEIVED;
                    m_notify.log(this, LOG_INFO, "State: TCP_SYN_RECEIVED");
                    //m_notify->associate(addr);
                    byte buffer[] = new byte[1];
                    buffer[0] = CTL_CONNECT;
                    queue(buffer, 1, true);
                } else if (m_state == TCP_SYN_SENT) {
                    m_state = TCP_ESTABLISHED;
                    m_notify.log(this, LOG_INFO, "State: TCP_ESTABLISHED");
                    adjustMTU();
                    if (m_notify != null) {
                        m_notify.onTcpOpen(this);
                    }
                    //notify(evOpen);
                }
            } else {
                m_notify.log(this, LOG_WARN, "Unknown control code: " + seg.data[0]);
                return false;
            }
        }else
        if(m_state == TCP_LISTEN)//JAYARAJ: we're receiving a data packet in listen mode
        {
            m_notify.log(this, LOG_ERROR, "Receiving data packet in listen mode");
            return false;
        }
        
        
         // Update timestamp
         if ((seg.seq <= m_ts_lastack) && (m_ts_lastack < seg.seq + seg.len)) {
           m_ts_recent = seg.tsval;
         }

         // Check if this is a valuable ack
         if ((seg.ack > m_snd_una) && (seg.ack <= m_snd_nxt)) {
           // Calculate round-trip time
           if (seg.tsecr != 0) {
             long rtt = timeDiff(now, seg.tsecr);
             if (rtt >= 0) {
               if (m_rx_srtt == 0) {
                 m_rx_srtt = rtt;
                 m_rx_rttvar = rtt / 2;
               } else {
                 m_rx_rttvar = (3 * m_rx_rttvar + Math.abs((long)(rtt - m_rx_srtt))) / 4;
                 m_rx_srtt = (7 * m_rx_srtt + rtt) / 8;
               }
               m_rx_rto = bound(MIN_RTO, m_rx_srtt + max(1, 4 * m_rx_rttvar), MAX_RTO);
             } else {
               assert(false);
             }
           }
           m_snd_wnd = seg.wnd;

           long nAcked = seg.ack - m_snd_una;
           m_snd_una = seg.ack;

           m_rto_base = (m_snd_una == m_snd_nxt) ? 0 : now;

           m_slen -= nAcked;
           //memmove(m_sbuf, m_sbuf + nAcked, m_slen);
           System.arraycopy(m_sbuf, (int)nAcked, m_sbuf, 0, (int)m_slen);
           //LOG(LS_INFO) << "PseudoTcp::process - m_slen = " << m_slen;

           for (long nFree = nAcked; nFree > 0; ) {
             assert(!m_slist.isEmpty());
             SSegment front = m_slist.getFirst();
             if (nFree < front.len) {
               front.len -= nFree;
               nFree = 0;
             } else {
               if (front.len > m_largest) {
                 m_largest = front.len;
               }
               nFree -= front.len;
               m_slist.removeFirst();
             }
           }

           if (m_dup_acks >= 3) {
             if (m_snd_una >= m_recover) { // NewReno
               long nInFlight = m_snd_nxt - m_snd_una;
               m_cwnd = min(m_ssthresh, nInFlight + m_mss); // (Fast Retransmit)
               m_dup_acks = 0;
             } else {
               if (!transmit(m_slist.listIterator(), now)) {
                 closedown(ECONNABORTED);
                 return false;
               }
               m_cwnd += m_mss - min(nAcked, m_cwnd);
             }
           } else {
             m_dup_acks = 0;
             // Slow start, congestion avoidance
             if (m_cwnd < m_ssthresh) {
               m_cwnd += m_mss;
             } else {
               m_cwnd += max(1, m_mss * m_mss / m_cwnd);
             }
           }
           // !?! A bit hacky
           if ((m_state == TCP_SYN_RECEIVED) && !bConnect) {
             m_state = TCP_ESTABLISHED;
             m_notify.log(this, LOG_INFO, "State: TCP_ESTABLISHED");
             adjustMTU();
             if (m_notify != null) {
               m_notify.onTcpOpen(this);
             }
             //notify(evOpen);
           }

           // If we make room in the send queue, notify the user
           // The goal it to make sure we always have at least enough data to fill the
           // window.  We'd like to notify the app when we are halfway to that point.
           final long kIdealRefillSize = (m_sbuf.length + m_rbuf.length) / 2;
           if (m_bWriteEnable && (m_slen < kIdealRefillSize)) {
             m_bWriteEnable = false;
             if (m_notify != null) {
               m_notify.onTcpWriteable(this);
             }
             //notify(evWrite);
           }                      
         } else if (seg.ack == m_snd_una) {
           // !?! Note, tcp says don't do this... but otherwise how does a closed window become open?
           m_snd_wnd = seg.wnd;

           // Check duplicate acks
           if (seg.len > 0) {
             // it's a dup ack, but with a data payload, so don't modify m_dup_acks
           } else if (m_snd_una != m_snd_nxt) {
             m_dup_acks += 1;
             if (m_dup_acks == 3) { // (Fast Retransmit)
               if (!transmit(m_slist.listIterator(), now)) {
                 closedown(ECONNABORTED);
                 return false;
               }
               m_recover = m_snd_nxt;
               long nInFlight = m_snd_nxt - m_snd_una;
               m_ssthresh = max(nInFlight / 2, 2 * m_mss);
               //LOG(LS_INFO) << "m_ssthresh: " << m_ssthresh << "  nInFlight: " << nInFlight << "  m_mss: " << m_mss;
               m_cwnd = m_ssthresh + 3 * m_mss;
             } else if (m_dup_acks > 3) {
               m_cwnd += m_mss;
             }
           } else {
             m_dup_acks = 0;
           }
         }
         

         // Conditions were acks must be sent:
         // 1) Segment is too old (they missed an ACK) (immediately)
         // 2) Segment is too new (we missed a segment) (immediately)
         // 3) Segment has data (so we need to ACK!) (delayed)
         // ... so the only time we don't need to ACK, is an empty segment that points to rcv_nxt!
         
         SendFlags sflags = sfNone;
         if (seg.seq != m_rcv_nxt) {
           sflags = sfImmediateAck; // (Fast Recovery)
         } else if (seg.len != 0) {
           sflags = sfDelayedAck;
         }
         // Adjust the incoming segment to fit our receive buffer
         if (seg.seq < m_rcv_nxt) {
           long nAdjust = m_rcv_nxt - seg.seq;
           if (nAdjust < seg.len) {
             seg.seq += nAdjust;
             //seg.data += nAdjust;
             System.arraycopy(seg.data, (int)nAdjust, seg.data, 0, (int)(seg.data.length - nAdjust));
             seg.len -= nAdjust;
           } else {
             seg.len = 0;
           }
         }
         if ((seg.seq + seg.len - m_rcv_nxt) > (m_rbuf.length - m_rlen)) {
           long nAdjust = seg.seq + seg.len - m_rcv_nxt - (m_rbuf.length - m_rlen);
           if (nAdjust < seg.len) {
             seg.len -= nAdjust;
           } else {
             seg.len = 0;
           }
         }
         
         boolean bIgnoreData = ((seg.flags & FLAG_CTL)!=0) || (m_shutdown != SD_NONE);
         boolean bNewData = false;
         
         if (seg.len > 0) {
           if (bIgnoreData) {
             if (seg.seq == m_rcv_nxt) {
               m_rcv_nxt += seg.len;
             }
           } else {
             long nOffset = seg.seq - m_rcv_nxt;
             //memcpy(m_rbuf + m_rlen + nOffset, seg.data, seg.len);
             System.arraycopy(seg.data, 0, m_rbuf, (int)(m_rlen + nOffset), (int)seg.len);
             if (seg.seq == m_rcv_nxt) {
               m_rlen += seg.len;
               m_rcv_nxt += seg.len;
               m_rcv_wnd -= seg.len;
               bNewData = true;

               ListIterator<RSegment> it = m_rlist.listIterator();
               //while ((it != m_rlist.end()) && (it->seq <= m_rcv_nxt)) {
               while(it.hasNext()){                   
                 RSegment item = it.next();
                 if(item.seq <= m_rcv_nxt) break;
                 
                 if (item.seq + item.len > m_rcv_nxt) {
                   sflags = sfImmediateAck; // (Fast Recovery)
                   long nAdjust = (item.seq + item.len) - m_rcv_nxt;
                   m_rlen += nAdjust;
                   m_rcv_nxt += nAdjust;
                   m_rcv_wnd -= nAdjust;
                 }
                 //it = m_rlist.erase(it);
                 it.remove();//removes item returned by last call to it.next()/it.previous()
               }
             } else {
               RSegment rseg = new RSegment();
               rseg.seq = seg.seq;
               rseg.len = seg.len;
               ListIterator<RSegment> it = m_rlist.listIterator();
               //while (it.hasNext() && (it->seq < rseg.seq)) {
               while(it.hasNext()){
                 RSegment item = it.next();
                 if (!(item.seq < rseg.seq)){
                     it.previous();//go back to the item that satisified our loop condition
                     break;                     
                 }
               }
               it.add(rseg);  //m_rlist.insert(it, rseg);
             }
           }
         }
         
         attemptSend(sflags);

         // If we have new data, notify the user
         //if (bNewData && m_bReadEnable) {
         if (bNewData) {
           m_bReadEnable = false;
           if (m_notify != null) {
             m_notify.onTcpReadable(this);
           }
           //notify(evRead);
         }
         
         return true;
    }

    boolean transmit(ListIterator<SSegment> iseg, long now)
    {
        assert(iseg.hasNext());
        SSegment seg = iseg.next();
        if (seg.xmit >= ((m_state == TCP_ESTABLISHED) ? 15 : 30)) {
          m_notify.log(this, LOG_VERBOSE, "too many retransmits");
          return false;
        }

        long nTransmit = min(seg.len, m_mss);       

        while (true) {
          long seq = seg.seq;
          int flags = (seg.bCtrl ? FLAG_CTL : 0);
          //char * buffer = m_sbuf + (seg->seq - m_snd_una);
          byte[] buffer = m_sbuf;          
          int buffer_offset  = (int)(seg.seq - m_snd_una);
          WriteResult wres = packet(seq, flags, buffer, buffer_offset, (int)nTransmit);
          if (wres == WR_SUCCESS){
            break;
          }
          
          if (wres == WR_FAIL) {
            m_notify.log(this, LOG_VERBOSE, "packet failed");
            return false;
          }

          assert(wres == WR_TOO_LARGE);

          while (true) {
            if (PACKET_MAXIMUMS[(int)(m_msslevel + 1)] == 0) {
              m_notify.log(this, LOG_VERBOSE, "MTU too small");
              return false;
            }
            // !?! We need to break up all outstanding and pending packets and then retransmit!?!

            m_mss = PACKET_MAXIMUMS[(int) ++m_msslevel] - PACKET_OVERHEAD;
            m_cwnd = 2 * m_mss; // I added this... haven't researched actual formula
            if (m_mss < nTransmit) {
              nTransmit = m_mss;
              break;
            }
          }
        }
        
        if (nTransmit < seg.len) {
          m_notify.log(this, LOG_VERBOSE, "mss reduced to " + m_mss);

          SSegment subseg = 
                    new SSegment(seg.seq + nTransmit, seg.len - nTransmit, seg.bCtrl);
          //subseg.tstamp = seg->tstamp;
          subseg.xmit = seg.xmit;
          seg.len = nTransmit;

          //SList::iterator next = seg;
          //m_slist.insert(++next, subseg);
          iseg.add(subseg);
        }
        if (seg.xmit == 0) {
          m_snd_nxt += seg.len;
        }
        seg.xmit += 1;
        //seg->tstamp = now;
        if (m_rto_base == 0) {
          m_rto_base = now;
        }

        return true;
    }

    void adjustMTU()
    {
        // Determine our current mss level, so that we can adjust appropriately later
        for (m_msslevel = 0; PACKET_MAXIMUMS[(int)(m_msslevel + 1)] > 0; ++m_msslevel) {
          if ((PACKET_MAXIMUMS[(int)m_msslevel]) <= m_mtu_advise) {
            break;
          }
        }
        m_mss = m_mtu_advise - PACKET_OVERHEAD;
        // Enforce minimums on ssthresh and cwnd
        m_ssthresh = max(m_ssthresh, 2 * m_mss);
        m_cwnd = max(m_cwnd, m_mss);        
    }

    private IPseudoTcpNotify m_notify;
    enum Shutdown { SD_NONE, SD_GRACEFUL, SD_FORCEFUL };
    Shutdown m_shutdown;
    int m_error;

    // TCB data
    TcpState m_state;
    long m_conv;
    boolean m_bReadEnable, m_bWriteEnable, m_bOutgoing;
    long m_lasttraffic;

    // Incoming data
    static class RList extends LinkedList<RSegment>{};
    RList m_rlist = new RList();

    byte[] m_rbuf= new byte[kRcvBufSize];
    long m_rcv_nxt, m_rcv_wnd, m_rlen, m_lastrecv;

    // Outgoing data
    SList m_slist = new SList();
    byte[] m_sbuf = new byte[kSndBufSize];
    long m_snd_nxt, m_snd_wnd, m_slen, m_lastsend, m_snd_una;
    // Maximum segment size, estimated protocol level, largest segment sent
    long m_mss, m_msslevel, m_largest, m_mtu_advise;
    // Retransmit timer
    long m_rto_base;

    // Timestamp tracking
    long m_ts_recent, m_ts_lastack;

    // Round-trip calculation
    long m_rx_rttvar, m_rx_srtt, m_rx_rto;

    // Congestion avoidance, Fast retransmit/recovery, Delayed ACKs
    long m_ssthresh, m_cwnd;
    int m_dup_acks;
    long m_recover;
    long m_t_ack;
    
    Object attachment = null;
    
    ScheduledFuture timerTask = null;
    
    public void setRunningTimer(ScheduledFuture timer)
    {
        this.timerTask = timer;
    }
    
    public ScheduledFuture runningTimer()
    {
        return timerTask;
    }
  
    public static class MutableLong
    {
        public long value;
        public MutableLong()
        {
            this(0);
        }
        public MutableLong(long v)
        {
            value =v;
        }
    }
}