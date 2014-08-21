/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shelloid.ptcp;


/**
 *
 * @author Jayaraj Poroor
 */
public class NetworkConstants {
//////////////////////////////////////////////////////////////////////
// Network Constants
//////////////////////////////////////////////////////////////////////

// Standard MTUs
static final int PACKET_MAXIMUMS[] = {
  65535,    // Theoretical maximum, Hyperchannel
  32000,    // Nothing
  17914,    // 16Mb IBM Token Ring
  8166,   // IEEE 802.4
  //4464,   // IEEE 802.5 (4Mb max)
  4352,   // FDDI
  //2048,   // Wideband Network
  2002,   // IEEE 802.5 (4Mb recommended)
  //1536,   // Expermental Ethernet Networks
  //1500,   // Ethernet, Point-to-Point (default)
  1492,   // IEEE 802.3
  1006,   // SLIP, ARPANET
  //576,    // X.25 Networks
  //544,    // DEC IP Portal
  //512,    // NETBIOS
  508,    // IEEE 802/Source-Rt Bridge, ARCNET
  296,    // Point-to-Point (low delay)
  //68,     // Official minimum
  0,      // End of list marker
};

public static final long MAX_PACKET = 65535;
// Note: we removed lowest level because packet overhead was larger!
static final long MIN_PACKET = 296;

static final long IP_HEADER_SIZE = 20; // (+ up to 40 bytes of options?)
static final long ICMP_HEADER_SIZE = 8;
static final long UDP_HEADER_SIZE = 8;
// TODO: Make JINGLE_HEADER_SIZE transparent to this code?
static final long JINGLE_HEADER_SIZE = 64; // when relay framing is in use

//////////////////////////////////////////////////////////////////////
// Global Constants and Functions
//////////////////////////////////////////////////////////////////////
//
//    0                   1                   2                   3
//    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
//    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//  0 |                      Conversation Number                      |
//    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//  4 |                        Sequence Number                        |
//    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//  8 |                     Acknowledgment Number                     |
//    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//    |               |   |U|A|P|R|S|F|                               |
// 12 |    Control    |   |R|C|S|S|Y|I|            Window             |
//    |               |   |G|K|H|T|N|N|                               |
//    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
// 16 |                       Timestamp sending                       |
//    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
// 20 |                      Timestamp receiving                      |
//    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
// 24 |                             data                              |
//    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
//
//////////////////////////////////////////////////////////////////////

static final long MAX_SEQ = 0xFFFFFFFF;
//JAYARAJ: 32-bit wnd
static final long HEADER_SIZE = 26;
//static final long HEADER_SIZE = 24;
static final long PACKET_OVERHEAD = HEADER_SIZE + UDP_HEADER_SIZE + IP_HEADER_SIZE + JINGLE_HEADER_SIZE;

static final long MIN_RTO   =   250; // 250 ms (RFC1122, Sec 4.2.3.1 "fractions of a second")
static final long DEF_RTO   =  3000; // 3 seconds (RFC1122, Sec 4.2.3.1)
static final long MAX_RTO   = 60000; // 60 seconds
static final long ACK_DELAY =   100; // 100 milliseconds

static final int FLAG_CTL = 0x02;
static final int FLAG_RST = 0x04;

static final int CTL_CONNECT = 0;
//const uint8 CTL_REDIRECT = 1;
static final int CTL_EXTRA = 255;

/*
const uint8 FLAG_FIN = 0x01;
const uint8 FLAG_SYN = 0x02;
const uint8 FLAG_ACK = 0x10;
*/

static final long CTRL_BOUND = 0x80000000;

static final long DEFAULT_TIMEOUT = 4000; // If there are no pending clocks, wake up every 4 seconds
static final long CLOSED_TIMEOUT = 60 * 1000; // If the connection is closed, once per minute

//#if PSEUDO_KEEPALIVE
// !?! Rethink these times
static final long IDLE_PING = 20 * 1000; // 20 seconds (note: WinXP SP2 firewall udp timeout is 90 seconds)
static final long IDLE_TIMEOUT = 90 * 1000; // 90 seconds;
//#endif // PSEUDO_KEEPALIVE    

static final boolean PSEUDO_KEEPALIVE = true;

public static final int EINVAL = 1;
public static final int ECONNABORTED = 2;
public static final int ENOTCONN = 3;
public static final int EWOULDBLOCK = 4;
public static final int ECONNRESET = 5;
public static final int SOCKET_ERROR = -1;

}
