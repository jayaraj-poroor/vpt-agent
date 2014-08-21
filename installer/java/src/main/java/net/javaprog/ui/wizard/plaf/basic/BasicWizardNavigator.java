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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.*;

import net.javaprog.ui.wizard.Step;
import net.javaprog.ui.wizard.Wizard;
import net.javaprog.ui.wizard.WizardModel;

/**
 * Provides navigation facilities for the wizard model.
 *
 * @author Michael Rudolf
 */
public class BasicWizardNavigator extends JPanel {
    
    public static final String BACK_ACTION = "BACK";
    public static final String NEXT_ACTION = "NEXT";
    public static final String FINISH_ACTION = "FINISH";
    public static final String CANCEL_ACTION = "CANCEL";
    private static final String propertyPrefix = "WizardNavigator.";
    
    protected Wizard wizard;
    protected JButton backButton;
    protected JButton nextButton;
    protected JButton finishButton;
    protected JButton cancelButton;
    protected JButton helpButton;
    
    /**
     * Creates a new navigation bar for the given wizard.
     */
    public BasicWizardNavigator(Wizard wizard) {
        this.wizard = wizard;
        LookAndFeel lnf = UIManager.getLookAndFeel();
        String prefix = getPropertyPrefix();
        lnf.installBorder(this, prefix + "border");
        lnf.installColors(this, prefix + "background", prefix + "foreground");
        initButtons();
        layoutButtons();
    }
    
    protected String getPropertyPrefix() {
        return propertyPrefix;
    }

    /**
     * Initializes the navigation buttons.
     */
    protected void initButtons() {
        ResourceBundle resources = (ResourceBundle)UIManager.get("Wizard.resources");
        try {
            backButton = new JButton("< " + resources.getString("back"));
            backButton.setMnemonic(resources.getString("back_mnemonic").charAt(0));
            backButton.setActionCommand(BACK_ACTION);
            nextButton = new JButton(resources.getString("next") + " >");
            nextButton.setMnemonic(resources.getString("next_mnemonic").charAt(0));
            nextButton.setActionCommand(NEXT_ACTION);
            finishButton = new JButton(resources.getString("finish"));
            finishButton.setMnemonic(resources.getString("finish_mnemonic").charAt(0));
            finishButton.setActionCommand(FINISH_ACTION);
            cancelButton = new JButton(resources.getString("cancel"));
            cancelButton.setMnemonic(resources.getString("cancel_mnemonic").charAt(0));
            cancelButton.setActionCommand(CANCEL_ACTION);
            ActionListener listener = (ActionListener)UIManager.get("Wizard.helpListener");
            if (listener != null) {
                helpButton = new JButton(resources.getString("help"));
                helpButton.setMnemonic(resources.getString("help_mnemonic").charAt(0));
                helpButton.addActionListener(listener);
            }
        } catch(MissingResourceException mre) {}
    }
    
    /**
     * Lays out the buttons in a horizontal row.
     */
    protected void layoutButtons() {
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel(
            new GridLayout(1, helpButton != null ? 5 : 4, 5, 0));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(finishButton);
        buttonPanel.add(cancelButton);
        if (helpButton != null) {
            buttonPanel.add(helpButton);
        }
        add(buttonPanel, BorderLayout.EAST);
    }
    
    /**
     * Adds the given action listener to the buttons.
     */
    public void addActionListener(ActionListener l) {
        backButton.addActionListener(l);
        nextButton.addActionListener(l);
        finishButton.addActionListener(l);
        cancelButton.addActionListener(l);
    }
    
    /**
     * Removes the given action listener from the buttons.
     */
    public void removeActionListener(ActionListener l) {
        backButton.removeActionListener(l);
        nextButton.removeActionListener(l);
        finishButton.removeActionListener(l);
        cancelButton.removeActionListener(l);
    }
    
    /**
     * Updates the buttons' states according to the model's state.
     */
    public void updateNavigation() {
        WizardModel model = wizard.getModel();
        Step currentStep = model.getCurrentStep();
        backButton.setEnabled(currentStep.canGoBack() && model.getCurrentIndex() > 0);
        nextButton.setEnabled(currentStep.canGoNext() && 
            model.getCurrentIndex() < model.getStepCount() -1);
        finishButton.setEnabled(currentStep.canFinish());
        cancelButton.setEnabled(currentStep.canCancel());
        if(nextButton.isEnabled()) {
            wizard.getRootPane().setDefaultButton(nextButton);
        } else if(finishButton.isEnabled()) {
            wizard.getRootPane().setDefaultButton(finishButton);
        } else {
            wizard.getRootPane().setDefaultButton(null);
        }
        cancelButton.setNextFocusableComponent(currentStep.getComponent());
    }
}
