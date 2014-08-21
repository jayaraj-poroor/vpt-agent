/*
 * 
 */
package org.shelloid.vpt.installer.steps;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.javaprog.ui.wizard.AbstractStep;

/* @author Harikrishnan */
public class LicenseStep extends AbstractStep {

    protected JTextArea licenseArea = new JTextArea();

    public LicenseStep() {
        super("License Agreement", "Please read the license carefully");
    }

    protected JComponent createComponent() {
        JPanel stepComponent = new JPanel(new BorderLayout(0, 10));
        stepComponent.add(new JScrollPane(licenseArea));

        final JRadioButton noRadioButton = new JRadioButton(
                "No, I don't accept the license terms", true);
        final JRadioButton yesRadioButton = new JRadioButton(
                "Yes, I accept the license terms");

        ButtonGroup group = new ButtonGroup();
        group.add(noRadioButton);
        group.add(yesRadioButton);
        JPanel choicePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        choicePanel.add(noRadioButton);
        choicePanel.add(yesRadioButton);
        stepComponent.add(choicePanel, BorderLayout.SOUTH);
        ActionListener buttonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setCanGoNext(e.getSource() == yesRadioButton);
            }
        };
        noRadioButton.addActionListener(buttonListener);
        yesRadioButton.addActionListener(buttonListener);
        return stepComponent;
    }

    @Override
    public void prepareRendering() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/LICENSE")));
            String line;
            licenseArea.setText("");
            while ((line = reader.readLine()) != null) {
                licenseArea.append(line + "\r\n");
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        setCanGoNext(false);
    }
}
