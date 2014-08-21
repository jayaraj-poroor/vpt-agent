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

import java.awt.*;

import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class SingleSideLineBorder extends LineBorder {
    protected int side;
    
    public SingleSideLineBorder(Color color, int side) {
        super(color);
        this.side = side;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.translate(x, y);
        Color oldColor = g.getColor();
        g.setColor(lineColor);
        switch(side) {
            case SwingConstants.TOP:
                g.drawLine(0, 0, 0, width-1);
                break;
            case SwingConstants.LEFT:
                g.drawLine(0, 0, height-1, 0);
                break;
            case SwingConstants.RIGHT:
                g.drawLine(width-1, 0, width-1, height-1);
                break;
            default:
                g.drawLine(0, height-1, width-1, height-1);
        }
        g.setColor(oldColor);
        g.translate(-x, -y);
    }
    
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }
    
    public Insets getBorderInsets(Component c, Insets i) {
        switch(side) {
            case SwingConstants.TOP:
                i.top = 1;
                i.left = i.right = i.bottom = 0;
                break;
            case SwingConstants.LEFT:
                i.left = 1;
                i.top = i.right = i.bottom = 0;
                break;
            case SwingConstants.RIGHT:
                i.right = 1;
                i.top = i.left = i.bottom = 0;
                break;
            default:
                i.bottom = 1;
                i.top = i.left = i.right = 0;
        }
        return i;
    }
}
