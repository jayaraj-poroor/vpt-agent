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

package net.javaprog.ui.wizard;

import javax.swing.JComponent;
import javax.swing.UIManager;

import net.javaprog.ui.wizard.plaf.WizardContentPaneUI;

/**
 * The wizard's content pane. This class overrides several UI-related
 * methods in order to make component layout and painting pluggable.
 *
 * @author Michael Rudolf
 */
public class WizardContentPane extends JComponent {
    
    private static final String uiClassID = "WizardContentPaneUI";
    
    protected Wizard wizard;
    
    /**
     * Creates a new content pane for the given wizard.
     *
     * @param wizard the wizard for which a content pane is being constructed
     */
    public WizardContentPane(Wizard wizard) {
        super();
        this.wizard = wizard;
        updateUI();
    }
    
    public Wizard getWizard() {
        return wizard;
    }
    
    /**
     * This method gets directly called from the constructor and initializes
     * the UI delegate.
     */
    public void updateUI() {
        if (UIManager.get(uiClassID) == null) {
            String lookAndFeelID = UIManager.getLookAndFeel().getID();
            if (lookAndFeelID.equals("Windows")) {
                UIManager.getLookAndFeelDefaults().put(uiClassID, 
                        "net.javaprog.ui.wizard.plaf.windows.WindowsWizardContentPaneUI");
            } else if (lookAndFeelID.equals("JGoodies Plastic")) {
                UIManager.getLookAndFeelDefaults().put(uiClassID, 
                        "net.javaprog.ui.wizard.plaf.plastic.PlasticWizardContentPaneUI");
//            } else if (lookAndFeelID.equals("Motif")) {
//                UIManager.getLookAndFeelDefaults().put(uiClassID, 
//                        "net.javaprog.ui.wizard.plaf.motif.MotifWizardContentPaneUI");
            } else {
                UIManager.getLookAndFeelDefaults().put(uiClassID, 
                        "net.javaprog.ui.wizard.plaf.metal.MetalWizardContentPaneUI");
            }
        }
        setUI((WizardContentPaneUI)UIManager.getUI(this));
    }
    
    public WizardContentPaneUI getUI() {
        return (WizardContentPaneUI)ui;
    }
    
    public void setUI(WizardContentPaneUI ui) {
        super.setUI(ui);
    }
    
    public String getUIClassID() {
        return uiClassID;
    }
}
