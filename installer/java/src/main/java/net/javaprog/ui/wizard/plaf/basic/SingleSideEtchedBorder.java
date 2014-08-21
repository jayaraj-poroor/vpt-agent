// Copyright (c) 2002-2007, Michael Rudolf
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
// 
// 1.) Redistributions of source code must retain the above copyright notice, this list of
//     conditions and the following disclaimer.
// 2.) Redistributions in binary form must reproduce the above copyright notice, this
//     list of conditions and the following disclaimer in the documentation and/or other
//     materials provided with the distribution.
// 3.) Neither the name Michael Rudolf nor the names of its contributors may
//     be used to endorse or promote products derived from this software without
//     specific prior written permission.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
// CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
// BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
// THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
// IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package net.javaprog.ui.wizard.plaf.basic;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

public class SingleSideEtchedBorder extends EtchedBorder {
    protected int side;
    
    public SingleSideEtchedBorder(int side) {
        this(side, LOWERED);
    }
    
    public SingleSideEtchedBorder(int side, int etchType) {
        super(etchType);
        this.side = side;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.translate(x, y);
        int x1, y1, x2, y2;
        switch(side) {
            case SwingConstants.TOP:
                x2 = width-2;
                x1 = y1 = y2 = 0;
                break;
            case SwingConstants.LEFT:
                y2 = height-2;
                x1 = y1 = x2 = 0;
                break;
            case SwingConstants.RIGHT:
                x1 = x2 = width-2;
                y1 = 0;
                y2 = height-2;
                break;
            default:
                x1 = 0;
                x2 = width-2;
                y1 = y2 = height-2;
        }
        g.setColor(etchType == LOWERED? getShadowColor(c) : getHighlightColor(c));
        g.drawLine(x1, y1, x2, y2);
        g.setColor(etchType == LOWERED? getHighlightColor(c) : getShadowColor(c));
        g.drawLine(x1+1, y1+1, x2+1, y2+1);
        g.translate(-x, -y);
    }
    
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }
    
    public Insets getBorderInsets(Component c, Insets i) {
        switch(side) {
            case SwingConstants.TOP:
                i.top = 2;
                i.left = i.right = i.bottom = 0;
                break;
            case SwingConstants.LEFT:
                i.left = 2;
                i.top = i.right = i.bottom = 0;
                break;
            case SwingConstants.RIGHT:
                i.right = 2;
                i.top = i.left = i.bottom = 0;
                break;
            default:
                i.bottom = 2;
                i.top = i.left = i.right = 0;
        }
        return i;
    }
}
