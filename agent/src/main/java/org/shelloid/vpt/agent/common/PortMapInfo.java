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

import io.netty.channel.Channel;

/* @author Harikrishnan */
public class PortMapInfo {
    private Long portMapId;
    private final Channel channel;
    
    public PortMapInfo(Long portMapId, Channel channel) {
        this.channel = channel;
        this.portMapId = portMapId;
    }

    public Channel getChannel() {
        return channel;
    }

    public Long getPortMapId() {
        return portMapId;
    }

    public void setPortMapId(Long portMapId) {
        this.portMapId = portMapId;
    }
}
