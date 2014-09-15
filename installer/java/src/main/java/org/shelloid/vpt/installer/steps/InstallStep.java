/*
 * 
 */
package org.shelloid.vpt.installer.steps;

import org.shelloid.vpt.installer.App;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Properties;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.javaprog.ui.wizard.AbstractStep;

/* @author Harikrishnan */
public class InstallStep extends AbstractStep {

    private JProgressBar progress;
    private JLabel label;
    private final String os;
    private final String currentDir;

    public InstallStep() throws Exception {
        super("Installing Shelloid VPT Agent", "Please wait while installing Shelloid VPT Agent...");
        String path = InstallStep.class.getProtectionDomain().getCodeSource().getLocation().getPath(); 
        String decodedPath = URLDecoder.decode(path, "UTF-8");
        File f = new File(new URL("file://" + decodedPath.replace(" ", "%20")).toURI());
        currentDir = f.getParent();
        System.out.println("JAR path: " + currentDir);
        os = System.getProperty("os.name").toLowerCase();
    }

    @Override
    protected JComponent createComponent() {
        final JPanel stepComponent = new JPanel();
        stepComponent.setLayout(new BoxLayout(stepComponent, BoxLayout.Y_AXIS));
        stepComponent.add(Box.createVerticalGlue());
        progress = new JProgressBar();
        label = new JLabel("Installing Shelloid VPT...");
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.add(label, BorderLayout.NORTH);
        inputPanel.add(progress);
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, inputPanel.getPreferredSize().height));
        progress.setValue(25);
        stepComponent.add(inputPanel);
        stepComponent.add(Box.createVerticalGlue());
        new Thread(new Runnable() {
            @Override
            public void run() {
                process(stepComponent);
            }
        }).start();
        return stepComponent;
    }

    private void process(JComponent component) {
        progress.setValue(50);
        label.setText("Configuring files...");
        if (updatePropertiesFile()) {
            progress.setValue(75);
            label.setText("Installing Service....");
            if (installService()) {
                label.setText("Installation Finished.");
                progress.setValue(100);
                setCanGoNext(true);
            } else {
                error(component, "Can't install the service.");
            }
        } else {
            error(component, "Can't update properties file.");
        }
    }

    private void error(JComponent component, String msg) {
        JOptionPane.showMessageDialog(component, msg);
        label.setText("Installation Error: " + msg);
        setCanCancel(true);
        setCanGoBack(true);
        setCanGoNext(false);
    }

    @Override
    public void prepareRendering() {
        setCanGoNext(false);
        setCanGoBack(false);
        setCanCancel(false);
    }

    public boolean updatePropertiesFile() {
        try {
            File file = new File("conf" + File.separatorChar + "wrapper.conf");
            Properties p = new Properties();
            p.load(new FileReader(file));
            if (os.startsWith("win")) {
                p.setProperty("wrapper.java.command", "javaw.exe");
            }
            else {
                p.setProperty("wrapper.java.command", "java");
            }
            p.setProperty("tmp.path", System.getProperty("java.io.tmpdir"));
            p.setProperty("wrapper.java.app.jar", currentDir + File.separatorChar + App.appFolderName + File.separatorChar + p.getProperty("shelloid.agent.jar"));
            p.setProperty("wrapper.working.dir", currentDir + File.separatorChar + App.appFolderName);
            p.setProperty("wrapper.filter.script.0", currentDir +  File.separatorChar + "scripts" +  File.separatorChar + "trayMessage.gv");
            p.setProperty("wrapper.logfile", currentDir +  File.separatorChar + "log" + File.separatorChar + "wrapper.log");
            p.store(new FileWriter(file), "Installed on " + new Date().toString());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean installService() {
        try {
            Process p;
            String cmd;
            if (os.startsWith("win")) {
                String installServicePath = System.getProperty("user.dir") + File.separatorChar + "bat" + File.separatorChar + "installService.bat";
                cmd = "cmd.exe /C start cmd /k \"" +installServicePath +"\"";
                System.out.println("Starting " + cmd);
            } else {
                String installServicePath = System.getProperty("user.dir") + File.separatorChar + "bin" + File.separatorChar + "installDaemon.sh";
                System.out.println("Changing executable permissions of shell scripts...");
                ProcessBuilder builder = new ProcessBuilder("sh" , "-c", "chmod ugo+x " + System.getProperty("user.dir") + File.separatorChar + "bin" + File.separatorChar + "*.sh");
                builder.start().waitFor();
                cmd = "sh " + installServicePath;
                System.out.println("Starting " + cmd);
            }
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
