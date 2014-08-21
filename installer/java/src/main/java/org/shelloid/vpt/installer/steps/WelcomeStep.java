/*
 * 
 */

package org.shelloid.vpt.installer.steps;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.javaprog.ui.wizard.AbstractStep;

/* @author Harikrishnan */
public class WelcomeStep extends AbstractStep {

    public WelcomeStep() {
        super("Welcome", "This is the Installation Wizard");
    }
    
    @Override
    protected JComponent createComponent() {
        JPanel stepComponent = new JPanel();
        stepComponent.add(
            new JLabel("<html>This wizard will guide you through the installation process.<p>"
                + "You can navigate through the steps using the buttons below.</html>"));
        return stepComponent;
    }

    @Override
    public void prepareRendering() {}
}
