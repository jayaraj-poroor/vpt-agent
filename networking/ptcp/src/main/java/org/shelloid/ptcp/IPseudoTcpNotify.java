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
public interface IPseudoTcpNotify {
  public void onTcpOpen(PseudoTcp tcp);
  public void onTcpReadable(PseudoTcp tcp);
  public void onTcpWriteable(PseudoTcp tcp);
  public void onTcpClosed(PseudoTcp tcp, long error);

  // Write the packet onto the network
  enum WriteResult { WR_SUCCESS, WR_TOO_LARGE, WR_FAIL };
  public WriteResult tcpWritePacket(PseudoTcp tcp, byte[] buffer, int len);
  
  enum LogType {LOG_INFO, LOG_WARN, LOG_ERROR, LOG_VERBOSE, LOG_DEBUG};
  public void log(PseudoTcp tcp, LogType type, String msg);
};