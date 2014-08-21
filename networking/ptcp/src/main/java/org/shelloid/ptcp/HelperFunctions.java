/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shelloid.ptcp;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author Jayaraj Poroor
 */
public class HelperFunctions {
    public static void long_to_bytes(long val, byte[] buf, int offset) {
        buf[offset] = (byte) ((val >> 24)&0xff);
        buf[offset+1] = (byte) ((val >> 16)&0xff);
        buf[offset+2] = (byte) ((val >> 8)&0xff);
        buf[offset+3] = (byte) (val&0xff);
    }

    public static void short_to_bytes(int val, byte[] buf, int offset) {
        long_to_bytes(val, buf, offset);
    }
    
    /*public static void short_to_bytes(int val, byte[] buf, int offset) {
        buf[offset] = (byte) ((val >> 8)&0xff);
        buf[offset+1] = (byte) (val&0xff);
    }*/
    

    public static long bytes_to_long(byte[] buf, int offset) {
      return ( (buf[offset]&0xffL)   << 24) +
             ( (buf[offset+1]&0xffL) << 16) +
             ( (buf[offset+2]&0xffL) << 8 ) +
             ( (buf[offset+3]&0xffL)      )
              ;
    }

    public static long bytes_to_short(byte[] buf, int offset) {
        return bytes_to_long(buf, offset);
    }
    
    /*public static int bytes_to_short(byte[] buf, int offset) {
      return ( (buf[offset]&0xff)   << 8 ) +
             ( (buf[offset+1]&0xff)      )
              ;
    }*/
    
    public static long max(long m, long n)
    {
        return m >= n ? m : n;
    }

    public static long min(long m, long n)
    {
        return m <= n ? m : n;
    }
    
    public static long bound(long lower, long middle, long upper) 
    {
        long max = middle >= lower ? middle : lower;
        long min = upper  <= max   ? upper  : max;
        return min;
        //return talk_base::_min(talk_base::_max(lower, middle), upper);
    }    
    
    //See: talk_base::TimeDiff
    public static long timeDiff(long later, long earlier)
    {
        return later - earlier;
    }
    
    public static void adjustClock(final ScheduledExecutorService executor, final PseudoTcp tcp) {
        if (tcp.runningTimer() != null) {
            tcp.runningTimer().cancel(false);
            tcp.setRunningTimer(null);
        }
        if(tcp.isClosed()){
            //System.out.println("adjustClock: tcp is closed: " + tcp);
            return;
        }else
        {
            //System.out.println("adjustClock: tcp is still open:  " + tcp);
        }
        PseudoTcp.MutableLong timeout = new PseudoTcp.MutableLong();
        tcp.getNextClock(tcp.now(), timeout);

        if (timeout.value >= 0) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    //System.out.println("notifyClock: " + tcp);
                    //if(!tcp.isClosed()){
                        tcp.notifyClock(tcp.now());
                        tcp.setRunningTimer(null);                        
                        adjustClock(executor, tcp);
                    //}
                }
            };
            ScheduledFuture future = executor.schedule(task, timeout.value, TimeUnit.MILLISECONDS);
            tcp.setRunningTimer(future);            
        }else
        {
            //System.out.println("nothing to schedule");
        }
    }
    
    
    public static String toHexString(byte[] buf, int offset, int len)
    {
        if(buf == null) return "null";
        StringBuilder b = new StringBuilder();
        for(int i=offset,j=0;j<len && i < buf.length;i++,j++)
        {
            int idx0 = (buf[i]&0xff) >> 4;
            int idx1 = buf[i]&0xf;
            b.append(hexChars[idx0]);
            b.append(hexChars[idx1]);
        }
        return b.toString();
    }
    
    public static byte[] fromHexString(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }    
    
    public static char hexChars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
    '9', 'A', 'B', 'C', 'D', 'E', 'F'};
}
