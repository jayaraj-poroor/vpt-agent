/*
 * 
 */

package org.shelloid.vpt.installer.steps;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.javaprog.ui.wizard.AbstractStep;
import net.javaprog.ui.wizard.Step;

/* @author Harikrishnan */
public class FinishStep extends AbstractStep {
    public FinishStep() {
        super("Finish", "The installation will now be started");
    }
    
    @Override
    protected JComponent createComponent() {
        JPanel stepComponent = new JPanel();
        stepComponent.add(
            new JLabel("<html>The installation wizard will now copy the necessary files<p>"
                + "to your hard drive. Please click \"Finish\".</html>"));
        return stepComponent;
    }
    
    @Override
    public void prepareRendering() {
        setCanFinish(true);
        setCanGoBack(false);
    }
}
