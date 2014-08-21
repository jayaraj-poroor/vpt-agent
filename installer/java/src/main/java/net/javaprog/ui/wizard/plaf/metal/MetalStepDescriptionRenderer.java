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

import java.awt.Component;
import java.awt.Font;

import javax.swing.*;

import net.javaprog.ui.wizard.Step;
import net.javaprog.ui.wizard.StepDescriptionRenderer;
import net.javaprog.ui.wizard.Wizard;
import net.javaprog.ui.wizard.plaf.basic.SingleSideLineBorder;

public class MetalStepDescriptionRenderer extends JLabel 
        implements StepDescriptionRenderer {
    public MetalStepDescriptionRenderer() {
        setFont(getFont().deriveFont(Font.BOLD));
        setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(12, 12, 0, 11),
        new SingleSideLineBorder(UIManager.getColor("textText"), 
                SwingConstants.BOTTOM)));
        setOpaque(true);
    }
    
    public Component getStepDescriptionRendererComponent(Wizard wizard) {
        return this;
    }
    
    public void updateStepDescription(Step step) {
        setText(step.getName());
    }
    
    public static class UIResource extends MetalStepDescriptionRenderer 
            implements javax.swing.plaf.UIResource {}
}
