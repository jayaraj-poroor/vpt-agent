/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.shelloid.ptcp.HelperFunctions;
import org.shelloid.ptcp.IPseudoTcpNotify;
import org.shelloid.ptcp.NetworkConstants;
import org.shelloid.ptcp.PseudoTcp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jayaraj Poroor
 */
public class PTcpTest {
    
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public PTcpTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void libTest()
    {
        long m = Integer.MAX_VALUE;
        int n = 4239;
        byte[] buf0 = new byte[4];
        HelperFunctions.long_to_bytes(m, buf0, 0);
        long m1 = HelperFunctions.bytes_to_long(buf0, 0);
        assert(m1 == m);
        
        //JAYARAJ 32-bit wnd
        byte[] buf1 = new byte[4];
        HelperFunctions.short_to_bytes(n, buf1, 0);
        long n1 = HelperFunctions.bytes_to_short(buf1, 0);
        assert(n1 == n);
        
    }
    
    @Test
    public void hexStringTest() 
    {
        byte b[] = new byte[] {(byte)0x1e, (byte) 0x34, (byte) 0, (byte) 1, (byte) 0xff};
        String s = HelperFunctions.toHexString(b, 0, b.length);
        System.out.println("Hex string: " + s);
        byte b1[] = HelperFunctions.fromHexString(s);
        assert(Arrays.equals(b1, b));
    }
        
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    //@Test
    public void basicTestPtcp() throws Exception
    {
        final PseudoTcp ptcps[] = new PseudoTcp[2];
        IPseudoTcpNotify notify = new IPseudoTcpNotify()
        {   
            int n = 0;
            @Override
            public void onTcpOpen(PseudoTcp tcp) {
                System.out.println("TCP open from: " + tcp.attachment());
            }

            @Override
            public void onTcpReadable(PseudoTcp tcp) {
                byte buffer[] = new byte[1024];
                int len =0;
                tcp.resetError();
                len = tcp.recv(buffer);
                while(len > 0)
                {
                    byte sbuf[] = new byte[len];
                    System.arraycopy(buffer, 0, sbuf, 0, len);
                    System.out.println("Received by: " + tcp.attachment() + ", msg: "+ new String(sbuf));
                    tcp.resetError();
                    len = tcp.recv(buffer);
                }
                
                if(tcp.getError() != NetworkConstants.EWOULDBLOCK){
                    System.out.println("TCP Error from : " + tcp.attachment() + " code: " + tcp.getError());
                    //handle error
                }
               
            }

            @Override
            public void onTcpWriteable(PseudoTcp tcp) {
                System.out.println("Tcp writable");
            }

            @Override
            public void onTcpClosed(PseudoTcp tcp, long error) {
                System.out.println("TCp closed");
            }

            @Override
            public IPseudoTcpNotify.WriteResult tcpWritePacket(PseudoTcp tcp, byte[] buffer, final int len) {
                //System.out.println("writePacket by: " + tcp.attachment() + 
                  //     ", data:" + HelperFunctions.toHexString(buffer, 0, len));
                if(len <= 0)
                    return IPseudoTcpNotify.WriteResult.WR_SUCCESS;
                final PseudoTcp target = (tcp == ptcps[0]) ? ptcps[1]: ptcps[0];                
                final byte[] buff = new byte[len];
                System.arraycopy(buffer, 0, buff, 0, len);
                new Thread(new Runnable(){
                    public void run()
                    {
                        if (n == 3 || n == 7 || n == 8) {
                            System.out.println("Inducing msg loss");
                            System.out.flush();
                        } else {
                            target.notifyPacket(buff, len);
                        }
                        n++;
                        HelperFunctions.adjustClock(executor, target);
                    }
                }).start();
                return IPseudoTcpNotify.WriteResult.WR_SUCCESS;
            }

            @Override
            public void log(PseudoTcp tcp, IPseudoTcpNotify.LogType type, String msg) {
                System.out.println("LOG: " + tcp.attachment() + ": " + type + ":" + msg);
            }            
        };
        ptcps[0] = new PseudoTcp(notify, 0);
        ptcps[0].attach("A");
        ptcps[1] = new PseudoTcp(notify, 0);
        ptcps[1].attach("B");
        
        ptcps[0].connect();
        HelperFunctions.adjustClock(executor, ptcps[0]);
        
        Thread.sleep(5);
        for(int n =0;n< 20;n++)
        {
            byte[] buf = ("MSG from A to B: " + n).getBytes();
            int len = 0;
            ptcps[0].resetError();
            len = ptcps[0].send(buf, buf.length);
                //adjustClock(ptcps[0]);
            System.out.println("len: " + len + " error: " + ptcps[0].getError());
            buf = ("MSG from B to A: " + n).getBytes();
            ptcps[1].resetError();
            len = ptcps[1].send(buf, buf.length);
            //adjustClock(ptcps[1]);
            System.out.println("len: " + len + " error: " + ptcps[1].getError()); 
        }
        Thread.sleep(1000*5);
        
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void fileTransferPtcp() throws Exception
    {
        System.out.println(System.getProperty("user.dir"));
        final PseudoTcp ptcps[] = new PseudoTcp[2];
        final FileInputStream fin = new FileInputStream("test" + File.separator + "shelloid-blog-logo.png");
        final FileOutputStream fout = new FileOutputStream("test" + File.separator + "shelloid-blog-logo-out.png");
        IPseudoTcpNotify notify = new IPseudoTcpNotify()
        {   
            int n = 0;
            @Override
            public void onTcpOpen(PseudoTcp tcp) {
                System.out.println("TCP open from: " + tcp.attachment());
            }

            @Override
            public void onTcpReadable(PseudoTcp tcp){
                byte buffer[] = new byte[1024];
                int len =0;
                tcp.resetError();
                len = tcp.recv(buffer);
                while(len > 0)
                {
                    if(tcp == ptcps[1])
                    {
                        try{
                            fout.write(buffer, 0, len);
                        }catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    tcp.resetError();
                    len = tcp.recv(buffer);
                }
                
                if(tcp.getError() != NetworkConstants.EWOULDBLOCK){
                    System.out.println("TCP Error from : " + tcp.attachment() + " code: " + tcp.getError());
                    //handle error
                }
               
            }

            @Override
            public void onTcpWriteable(PseudoTcp tcp) {
                System.out.println("Tcp writable");
            }

            @Override
            public void onTcpClosed(PseudoTcp tcp, long error) {
                System.out.println("TCp closed");
            }

            @Override
            public IPseudoTcpNotify.WriteResult tcpWritePacket(PseudoTcp tcp, byte[] buffer, final int len) {
                //System.out.println("writePacket by: " + tcp.attachment() + 
                  //     ", data:" + HelperFunctions.toHexString(buffer, 0, len));
                if(len <= 0)
                    return IPseudoTcpNotify.WriteResult.WR_SUCCESS;
                final PseudoTcp target = (tcp == ptcps[0]) ? ptcps[1]: ptcps[0];                
                final byte[] buff = new byte[len];
                System.arraycopy(buffer, 0, buff, 0, len);
                new Thread(new Runnable(){
                    public void run()
                    {
                        if (n == 3 || n == 7 || n == 8 || n == 11 || n == 24 || n == 31) {
                            System.out.println("Inducing msg loss");
                            System.out.flush();
                        } else {
                            target.notifyPacket(buff, len);
                        }
                        n++;
                        HelperFunctions.adjustClock(executor, target);
                    }
                }).start();
                return IPseudoTcpNotify.WriteResult.WR_SUCCESS;
            }

            @Override
            public void log(PseudoTcp tcp, IPseudoTcpNotify.LogType type, String msg) {
                System.out.println("LOG: " + tcp.attachment() + ": " + type + ":" + msg);
            }            
        };
        ptcps[0] = new PseudoTcp(notify, 0);
        ptcps[0].attach("A");
        ptcps[1] = new PseudoTcp(notify, 0);
        ptcps[1].attach("B");
        
        ptcps[0].connect();
        HelperFunctions.adjustClock(executor, ptcps[0]);
        
        Thread.sleep(5);
        byte[] buf = new byte[1024];
        int len = fin.read(buf);        
        while(len > 0)
        {
            ptcps[0].resetError();
            int n = ptcps[0].send(buf, len);
            Thread.sleep(10);
            len = fin.read(buf);        
        }
        Thread.sleep(1000*5);
        
    }

}
