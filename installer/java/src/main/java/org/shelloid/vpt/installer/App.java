/*
 * 
 */

package org.shelloid.vpt.installer;

import org.shelloid.vpt.installer.steps.FinishStep;
import org.shelloid.vpt.installer.steps.InstallStep;
import org.shelloid.vpt.installer.steps.LicenseStep;
import org.shelloid.vpt.installer.steps.WelcomeStep;
import java.io.UnsupportedEncodingException;
import javax.swing.UIManager;
import net.javaprog.ui.wizard.*;

/* @author Harikrishnan */
public class App {
    public static final String appFolderName = "vpt";
    public static void main(String args[]) throws Exception{
        boolean useConsole = false;
        if (args.length > 0)
        {
            if (args[0].equals("console")){
                useConsole = true;
            }
        }
        if (useConsole)
        {
            InstallStep step = new InstallStep();
            System.out.println("Updating program configuration files...");
            if (step.updatePropertiesFile())
            {
                System.out.println("Configuration files updation successful.");
                System.out.println("Installing service...");
                if (step.installService()){
                    System.out.println("Service installation successful.");
                }
                else{
                    System.out.println("Service installation failed.");
                }
            }
            else{
                System.out.println("Configuration file updation failed.");
            }
        }
        else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) { }
            WizardModel model = new DefaultWizardModel(new Step[]{
                new WelcomeStep(),
                new LicenseStep(),
                new InstallStep(),
                new FinishStep()
            });
            Wizard wizard = new Wizard(model, "Installation Wizard", new App().getClass().getClassLoader().getResource("install.gif"));
            wizard.pack();
            wizard.setLocationRelativeTo(null);
            wizard.setVisible(true);
        }
    }
}
