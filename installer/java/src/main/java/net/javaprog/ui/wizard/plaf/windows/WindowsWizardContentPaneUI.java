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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.plaf.*;

import net.javaprog.ui.wizard.*;
import net.javaprog.ui.wizard.plaf.basic.*;

/**
 * Adapts the wizard's content pane to the Windows look and feel. This
 * implementations creates a custom WizardNavigator component in order to
 * reflect the windows wizards' button layout (the next button changes to
 * the finish button at the last step). Furthermore this UI delegate provides
 * its own layout manager that shows the StepListRenderer only for the first
 * and the last step and the StepDescriptionRenderer only for the steps in
 * between.
 *
 * @author Michael Rudolf
 */
public class WindowsWizardContentPaneUI extends BasicWizardContentPaneUI {
    public WindowsWizardContentPaneUI() {
        String prefix = getPropertyPrefix();
        UIManager.put(prefix + "stepDescriptionRenderer", 
            new UIDefaults.ProxyLazyValue(WindowsStepDescriptionRenderer.UIResource.class.getName()));
        UIManager.put(prefix + "stepListRenderer", 
            new UIDefaults.ProxyLazyValue(WindowsStepListRenderer.UIResource.class.getName()));
        UIManager.put(prefix + "stepBorder", 
            new BorderUIResource.EmptyBorderUIResource(12, 40, 11, 11));
        UIManager.put(prefix + "stepForeground", UIManager.get("textText"));
        UIManager.put("WizardNavigator.border", 
            new BorderUIResource.CompoundBorderUIResource(
                new SingleSideEtchedBorder(SwingUtilities.TOP),
                BorderFactory.createEmptyBorder(12, 12, 11, 11)));
        UIManager.put("WizardNavigator.background", UIManager.get("control"));
        UIManager.put("WizardNavigator.foreground", UIManager.get("textText"));
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new WindowsWizardContentPaneUI();
    }

    protected void updateStepComponent() {
        int index = wizard.getModel().getCurrentIndex();
        if(index == 0 || index == wizard.getModel().getStepCount() - 1) {
            UIManager.put(getPropertyPrefix() + "stepBackground", UIManager.get("controlLtHighlight"));
        } else {
            UIManager.put(getPropertyPrefix() + "stepBackground", UIManager.get("control"));
        }
        super.updateStepComponent();
    }
    
    protected BasicWizardNavigator createWizardNavigator() {
        return new WindowsWizardNavigator(wizard);
    }
    
    protected LayoutManager createLayoutManager() {
        return new WindowsWizardLayout();
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return new WindowsPropertyChangeHandler();
    }
    
    protected class WindowsPropertyChangeHandler extends BasicPropertyChangeHandler {
        public void propertyChange(PropertyChangeEvent e) {
            super.propertyChange(e);
            if (Wizard.ICON_PROPERTY.equals(e.getPropertyName())) {
                updateStepListRendererComponent();
                updateStepDescriptionRendererComponent();
            }
        }
    }
    
    protected class WindowsWizardLayout extends BasicWizardLayout {
        public void layoutContainer(Container parent) {
            Dimension size = parent.getSize();
            int navigatorHeight = wizardNavigator!=null ? wizardNavigator.getMinimumSize().height : 0;
            Component stepComponent = currentStep.getComponent();
            int index = wizard.getModel().getCurrentIndex();
            if(index == 0 || index == wizard.getModel().getStepCount() - 1) {
                int stepListWidth;
                if(stepListRenderer != null){
                    ensureContainsComponent(parent, stepListRenderer);
                    stepListWidth = stepListRenderer.getMinimumSize().width;
                    stepListRenderer.setBounds(0, 0, stepListWidth, 
                        size.height - navigatorHeight);
                } else {
                    stepListWidth = 0;
                }
                if(stepDescriptionRenderer != null) {
                    parent.remove(stepDescriptionRenderer);
                }
                stepComponent.setBounds(stepListWidth, 0, 
                    size.width - stepListWidth, size.height - navigatorHeight);
            } else {
                if(stepListRenderer != null) {
                    parent.remove(stepListRenderer);
                }
                int stepDescHeight;
                if(stepDescriptionRenderer != null) {
                    ensureContainsComponent(parent, stepDescriptionRenderer);
                    stepDescHeight = stepDescriptionRenderer.getMinimumSize().height;
                    stepDescriptionRenderer.setBounds(0, 0, size.width, stepDescHeight);
                } else {
                    stepDescHeight = 0;
                }
                stepComponent.setBounds(0, stepDescHeight, size.width, 
                    size.height - (stepDescHeight + navigatorHeight));
            }
            if(wizardNavigator != null) {
                wizardNavigator.setBounds(0, size.height - navigatorHeight, 
                    size.width, navigatorHeight);
            }
        }
        
        public Dimension minimumLayoutSize(Container parent) {
            Dimension min = new Dimension();
            Dimension stepListSize;
            Dimension stepDescSize;
            Dimension currentStepSize = currentStep.getComponent().getMinimumSize();
            int index = wizard.getModel().getCurrentIndex();
            if(index == 0 || index == wizard.getModel().getStepCount() - 1) {
                stepListSize = stepListRenderer!=null ? stepListRenderer.getMinimumSize() : (Dimension)min.clone();
                stepDescSize = (Dimension)min.clone();
            } else {
                stepListSize = (Dimension)min.clone();
                stepDescSize = stepDescriptionRenderer!=null ? stepDescriptionRenderer.getMinimumSize() : (Dimension)min.clone();
            }
            min.width = stepListSize.width + Math.max(
                Math.max(stepDescSize.width, currentStepSize.width), 
                2 * stepListSize.width);
            min.height = Math.max(stepListSize.height, stepDescSize.height + 
                Math.max(currentStepSize.height, 4 * stepDescSize.height));
            if(wizardNavigator != null) {
                Dimension temp = wizardNavigator.getMinimumSize();
                min.width = Math.max(min.width, temp.width);
                min.height += temp.height;
                min.height = Math.max(min.height, (int) ((2 * min.width) / 3));
                min.width = Math.max(min.width, (int) ((3 * min.height) / 2));
            }
            return min;
        }
        
        protected void ensureContainsComponent(Container parent, Component child) {
            parent.remove(child);
            parent.add(child);
        }
    }
}
