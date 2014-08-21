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

import javax.swing.*;

import net.javaprog.ui.wizard.Step;
import net.javaprog.ui.wizard.StepDescriptionRenderer;
import net.javaprog.ui.wizard.Wizard;
import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

public class WindowsStepDescriptionRenderer extends JPanel 
        implements StepDescriptionRenderer {
    
    protected JLabel nameLabel;
    protected JLabel descriptionLabel;
    protected JLabel iconLabel;
    
    public WindowsStepDescriptionRenderer() {
        super(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                new SingleSideEtchedBorder(SwingConstants.BOTTOM),
                BorderFactory.createEmptyBorder(5, 15, 5, 5)));
        setBackground(Color.WHITE);
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        nameLabel = new JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        textPanel.add(nameLabel);
        descriptionLabel = new JLabel();
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(2, 27, 0, 0));
        textPanel.add(descriptionLabel);
        textPanel.add(Box.createVerticalGlue());
        textPanel.setOpaque(false);
        add(textPanel);
        iconLabel = new JLabel();
    }
    
    public Component getStepDescriptionRendererComponent(Wizard wizard) {
        remove(iconLabel);
        Icon icon = wizard.getIcon();
        if (icon != null) {
            if (icon.getIconHeight() > 36) {
                icon = getScaledIcon(icon);
            }
            iconLabel.setIcon(icon);
            add(iconLabel, BorderLayout.EAST);
        }
        return this;
    }
    
    /**
     * Scales the given icon so that it is 36 pixel wide.
     *
     * @param icon the icon to scale
     */
    protected Icon getScaledIcon(Icon icon) {
        Image image;
        if (icon instanceof ImageIcon) {
            image = ((ImageIcon)icon).getImage();
        } else {
            image = new BufferedImage(icon.getIconWidth(), 
                    icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            icon.paintIcon(this, image.getGraphics(), 0, 0);
        }
        int height = (int) 36 * (icon.getIconWidth() / icon.getIconHeight());
        if (height == 0){
            height = 36;
        }
        BufferedImage scaled = new BufferedImage(36, height, 
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        while (image.getWidth(null) == -1) {
            //wait for the image to be completely loaded
            Thread.yield();
        }
        graphics.drawImage(image, 0, 0, scaled.getWidth(), scaled.getHeight(), 
                null);
        graphics.dispose();
        return new ImageIcon(scaled);
    }
    
    public void updateStepDescription(Step step) {
        nameLabel.setText(step.getName());
        descriptionLabel.setText(step.getDescription());
    }
    
    public static class UIResource extends WindowsStepDescriptionRenderer
            implements javax.swing.plaf.UIResource {}
}
