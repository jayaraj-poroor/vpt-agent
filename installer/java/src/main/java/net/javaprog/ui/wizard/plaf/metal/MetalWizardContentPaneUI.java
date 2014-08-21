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

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;

import net.javaprog.ui.wizard.*;
import net.javaprog.ui.wizard.plaf.basic.*;

/**
 * Adapts the wizard's content pane to the Metal look and feel.
 *
 * @author Michael Rudolf
 */
public class MetalWizardContentPaneUI extends BasicWizardContentPaneUI {
    public MetalWizardContentPaneUI() {
        UIManager.put(getPropertyPrefix() + "stepDescriptionRenderer", 
                new UIDefaults.ProxyLazyValue(
                MetalStepDescriptionRenderer.UIResource.class.getName()));
        UIManager.put(getPropertyPrefix() + "stepListRenderer", 
                new UIDefaults.ProxyLazyValue(
                MetalStepListRenderer.UIResource.class.getName()));
        UIManager.put(getPropertyPrefix() + "stepBorder", 
                new BorderUIResource.EmptyBorderUIResource(11, 11, 11, 11));
        UIManager.put(getPropertyPrefix() + "stepBackground", 
                UIManager.get("control"));
        UIManager.put(getPropertyPrefix() + "stepForeground", 
                UIManager.get("textText"));
        UIManager.put("WizardNavigator.border", 
                new BorderUIResource.CompoundBorderUIResource(
                new SingleSideEtchedBorder(SwingConstants.TOP), 
                BorderFactory.createEmptyBorder(17, 12, 11, 11)));
        UIManager.put("WizardNavigator.background", UIManager.get("control"));
        UIManager.put("WizardNavigator.foreground", UIManager.get("textText"));
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new MetalWizardContentPaneUI();
    }
}
