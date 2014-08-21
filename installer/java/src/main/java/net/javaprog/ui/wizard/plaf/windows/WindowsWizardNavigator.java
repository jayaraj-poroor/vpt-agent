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

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.*;

import net.javaprog.ui.wizard.Step;
import net.javaprog.ui.wizard.Wizard;
import net.javaprog.ui.wizard.WizardModel;
import net.javaprog.ui.wizard.plaf.basic.BasicWizardNavigator;

/**
 * This subclass shows only three buttons: back, next and cancel. The
 * next button automatically turns into the finish button at the last
 * step.
 *
 * @author Michael Rudolf
 */
public class WindowsWizardNavigator extends BasicWizardNavigator {
    protected boolean isDisplayingFinishButton = false;
    protected final String NEXT_BUTTON_TEXT;
    protected final int NEXT_BUTTON_MNEMONIC;
    
    public WindowsWizardNavigator(Wizard wizard) {
        super(wizard);
        NEXT_BUTTON_TEXT = nextButton.getText();
        NEXT_BUTTON_MNEMONIC = nextButton.getMnemonic();
    }
    
    protected void layoutButtons() {
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel(
            new GridLayout(1, helpButton != null ? 4 : 3, 5, 0));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(cancelButton);
        if (helpButton != null)
            buttonPanel.add(helpButton);
        add(buttonPanel, BorderLayout.EAST);
    }
    
    public void updateNavigation() {
        WizardModel model = wizard.getModel();
        Step currentStep = model.getCurrentStep();
        backButton.setEnabled(currentStep.canGoBack() && model.getCurrentIndex() > 0);
        if (model.getCurrentIndex() == model.getStepCount() -1) {
            if (!isDisplayingFinishButton) {
                nextButton.setText(finishButton.getText());
                nextButton.setMnemonic(finishButton.getMnemonic());
                nextButton.setActionCommand(FINISH_ACTION);
                isDisplayingFinishButton = true;
            }
        } else {
            if (isDisplayingFinishButton) {
                nextButton.setText(NEXT_BUTTON_TEXT);
                nextButton.setMnemonic(NEXT_BUTTON_MNEMONIC);
                nextButton.setActionCommand(NEXT_ACTION);
                isDisplayingFinishButton = false;
            }
        }
        nextButton.setEnabled(currentStep.canFinish() || (currentStep.canGoNext() && 
                model.getCurrentIndex() < model.getStepCount() -1));
        if (nextButton.isEnabled()) {
            wizard.getRootPane().setDefaultButton(nextButton);
        } else {
            wizard.getRootPane().setDefaultButton(null);
        }
        cancelButton.setEnabled(currentStep.canCancel());
        cancelButton.setNextFocusableComponent(currentStep.getComponent());
    }
}
