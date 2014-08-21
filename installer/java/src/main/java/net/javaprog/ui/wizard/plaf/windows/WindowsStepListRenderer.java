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

package net.javaprog.ui.wizard.plaf.windows;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.javaprog.ui.wizard.*;

/**
 * Instead of displaying a step list, this component paints the wizard's icon.
 *
 * @author Michael Rudolf
 */
public class WindowsStepListRenderer extends JPanel implements StepListRenderer {
    protected Icon icon;
    private Image backgroundImage;
    private Image foregroundImage;
    private int foregroundImageX;
    private int foregroundImageY = 30;
    
    public WindowsStepListRenderer() {
        super(null);
        setBackground(Color.blue.darker().darker());
        setMinimumSize(new Dimension(175, 0));
    }
    
    public Component getStepListRendererComponent(Wizard wizard) {
        icon = wizard.getIcon();
        backgroundImage = null;
        foregroundImage = null;
        return this;
    }
    
    /**
     * Creates the bluescale, enlarged background image from the given icon.
     */
    protected Image createBackgroundImage(Icon icon) {
        Image source;
        if (icon instanceof ImageIcon) {
            source = ((ImageIcon)icon).getImage();
        } else {
            source = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB);
        }
        while (source.getWidth(null) == -1) {
            //wait for the image to be completely loaded
            Thread.yield();
        }
        int width = (icon.getIconWidth() / icon.getIconHeight()) * getHeight();
        BufferedImage bgImage = new BufferedImage(getWidth(), getHeight(), 
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bgImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(source, 0, 0, width, getHeight(), getBackground(), this);
        while (bgImage.getWidth(null) == -1) {
            //wait for the image to be completely loaded
            Thread.yield();
        }
        graphics.dispose();
        int pix, alpha, avg;
        int[] data = bgImage.getRGB(0, 0, getWidth(), getHeight(), null, 0, getWidth());
        for (int loop = 0 ; loop < getWidth() * getHeight(); loop++) {
            pix = data[loop];
            //store alpha value
            alpha = pix & 0xff000000;
            //compute the average of red, green and blue
            avg = (((pix >> 16) & 0xff) + ((pix >> 8) & 0xff) + (pix & 0xff)) / 3;
            //set pixel using stored alpha value and the computed average as blue
            data[loop] = avg | alpha;
        }
        bgImage.setRGB(0, 0, getWidth(), getHeight(), data, 0, getWidth());
        return bgImage;
    }
    
    /**
     * Creates the foreground image from the given icon.
     */
    protected Image createForegroundImage(Icon icon) {
        Image source;
        if (icon instanceof ImageIcon) {
            source = ((ImageIcon)icon).getImage();
        } else {
            source = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB);
        }
        while (source.getWidth(null) == -1) {
            //wait for the image to be completely loaded
            Thread.yield();
        }
        if (icon.getIconWidth() > getWidth() / 2 || icon.getIconWidth() < getWidth() / 3) {
            int width = getWidth() / 2 - foregroundImageY / 2;
            int height = (icon.getIconHeight() / icon.getIconWidth()) * width;
            BufferedImage temp = new BufferedImage(width, height, 
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = temp.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(source, 0, 0, temp.getWidth(), temp.getHeight(), 
                    this);
            graphics.dispose();
            source = temp;
        }
        return source;
    }
    
    /**
     * Paints a bluescale, enlarged version of the wizard's icon and on top
     * of that the original icon surrounded by a white border.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (icon == null) {
            return;
        }
        if (backgroundImage == null) {
            backgroundImage = createBackgroundImage(icon);
        }
        //draw background image
        g.drawImage(backgroundImage, 0, 0, this);
        if (foregroundImage == null) {
            foregroundImage = createForegroundImage(icon);
        }
        //determine foreground image position
        foregroundImageX = (getWidth() - foregroundImageY) - foregroundImage.getWidth(null);
        //draw white border
        g.setColor(Color.white);
        for(int i = 1; i < 6; i++) {
            g.drawRect(foregroundImageX - i, foregroundImageY - i,
                foregroundImage.getWidth(null) + 2 * i, foregroundImage.getHeight(null) + 2 * i);
        }
        //draw foreground image
        g.drawImage(foregroundImage, foregroundImageX, foregroundImageY, this);
    }
    
    /**
     * Does nothing because there actually is no step list to update.
     */
    public void updateStepList(WizardModel model) {}
    
    public static class UIResource extends WindowsStepListRenderer 
            implements javax.swing.plaf.UIResource {}
}
