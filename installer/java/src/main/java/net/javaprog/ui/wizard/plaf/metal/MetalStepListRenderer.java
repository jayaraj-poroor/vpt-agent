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

package net.javaprog.ui.wizard.plaf.metal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.*;

import net.javaprog.ui.wizard.*;
import net.javaprog.ui.wizard.plaf.basic.SingleSideLineBorder;

public class MetalStepListRenderer extends JPanel implements StepListRenderer {
    protected JLabel headerLabel;
    protected JPanel labelPanel;
    protected JLabel[] labels;
    protected Font normal;
    protected Font bold;
    
    public MetalStepListRenderer() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        Color textHighlight = UIManager.getColor("textHighlight");
        setBackground(new Color(textHighlight.getRed(), textHighlight.getGreen(), 
                textHighlight.getBlue()));
        super.setForeground(UIManager.getColor("textHighlightText"));
        ResourceBundle resources = (ResourceBundle)UIManager.get("Wizard.resources");
        try {
            headerLabel = new JLabel(resources.getString("steps"));
            headerLabel.setForeground(getForeground());
            headerLabel.setBorder(new SingleSideLineBorder(getForeground(), 
                    SwingConstants.BOTTOM));
            add(headerLabel, BorderLayout.NORTH);
        } catch(MissingResourceException mre) {}
        labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.setOpaque(false);
        add(labelPanel, BorderLayout.CENTER);
    }
    
    public Component getStepListRendererComponent(Wizard wizard) {
        labelPanel.removeAll();
        Step[] steps = wizard.getModel().getSteps();
        labels = new JLabel[steps.length];
        Color fg = getForeground();
        for(int i = 0; i < steps.length; i++) {
            labels[i] = new JLabel(Integer.toString(i+1) + ". " + steps[i].getName());
            labels[i].setForeground(fg);
            labelPanel.add(labels[i]);
        }
        labelPanel.add(Box.createVerticalGlue());
        normal = labels[0].getFont();
        bold = normal.deriveFont(Font.BOLD);
        updateStepList(wizard.getModel());
        return this;
    }    
    
    public void updateStepList(WizardModel model) {
        int current = model.getCurrentIndex();
        if(current > 0) {
            labels[current-1].setFont(normal);
        }
        labels[current].setFont(bold);
        if(current < model.getStepCount()-1) {
            labels[current+1].setFont(normal);
        }
    }
    
    public void setForeground(Color fg) {
        super.setForeground(fg);
        if (headerLabel != null && labels != null) {
            headerLabel.setForeground(fg);
            for (int i = 0; i < labels.length; i++) {
                labels[i].setForeground(fg);
            }
        }
    }
    
    public static class UIResource extends MetalStepListRenderer
            implements javax.swing.plaf.UIResource {}
}
