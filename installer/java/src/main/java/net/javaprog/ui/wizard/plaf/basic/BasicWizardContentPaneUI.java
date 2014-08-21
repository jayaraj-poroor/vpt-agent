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

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.*;
import java.util.Locale;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import net.javaprog.ui.wizard.*;
import net.javaprog.ui.wizard.plaf.WizardContentPaneUI;

/**
 * A basic WizardContentPaneUI implementation providing common behaviour
 * and look.
 *
 * @author Michael Rudolf
 */
public abstract class BasicWizardContentPaneUI extends WizardContentPaneUI {
    
    private static final String propertyPrefix = "Wizard.";
    
    protected Wizard wizard;
    protected WizardContentPane wizardContentPane;
    
    protected Component stepDescriptionRenderer;
    protected Component stepListRenderer;
    protected Step currentStep;
    protected BasicWizardNavigator wizardNavigator;
    
    protected PropertyChangeListener propertyChangeListener;
    protected WizardModelListener wizardModelListener;
    protected WindowListener windowListener;
    protected ActionListener actionListener;
    
    private KeyStroke escKeyStroke;
    private KeyStroke helpKeyStroke;
    
    public BasicWizardContentPaneUI() {}
    
    protected String getPropertyPrefix() {
        return propertyPrefix;
    }
    
    public void installUI(JComponent c) {
        wizardContentPane = (WizardContentPane)c;
        wizard = wizardContentPane.getWizard();
        wizardContentPane.setLayout(createLayoutManager());
        installDefaults();
        installComponents();
        installListeners();
        currentStep = wizard.getModel().getCurrentStep();
        if (wizardModelListener != null) {
            wizardModelListener.stepShown(new WizardModelEvent(wizard.getModel()));
        }
    }
    
    protected void installComponents() {
        updateStepDescriptionRendererComponent();
        updateStepListRendererComponent();
        if ((wizardNavigator = createWizardNavigator()) != null) {
            wizardContentPane.add(wizardNavigator);
        }
    }
    
    protected void installDefaults() {
        String prefix = getPropertyPrefix();
        if (UIManager.get(prefix + "resources") == null) {
            try {
                ResourceBundle resources = ResourceBundle.getBundle("net.javaprog.ui.wizard.plaf.basic.bundle");
                UIManager.put(prefix + "resources", resources);
            } catch(MissingResourceException mre) {
                throw new RuntimeException(mre.getMessage());
            }
        }
        if (wizard.getStepDescriptionRenderer() == null || 
            wizard.getStepDescriptionRenderer() instanceof UIResource) {
            wizard.setStepDescriptionRenderer((StepDescriptionRenderer)
                UIManager.get(prefix + "stepDescriptionRenderer"));
        }
        if (wizard.getStepListRenderer() == null || 
            wizard.getStepListRenderer() instanceof UIResource) {
            wizard.setStepListRenderer((StepListRenderer)
                UIManager.get(prefix + "stepListRenderer"));
        }
    }
    
    protected void installListeners() {
        if ((propertyChangeListener = createPropertyChangeListener()) != null) {
            wizard.addPropertyChangeListener(propertyChangeListener);
        }
        if ((windowListener = createWindowListener()) != null) {
            wizard.addWindowListener(windowListener);
        }
        if ((wizardModelListener = createWizardModelListener()) != null) {
            wizard.getModel().addWizardModelListener(wizardModelListener);
        }
        escKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        if ((actionListener = createActionListener()) != null) {
            if(wizardNavigator != null)
                wizardNavigator.addActionListener(actionListener);
            wizard.getRootPane().registerKeyboardAction(actionListener, 
                BasicWizardNavigator.CANCEL_ACTION, escKeyStroke, 
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        ActionListener listener = (ActionListener)UIManager.get("Wizard.helpListener");
        if (listener != null) {
            helpKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
            wizard.getRootPane().registerKeyboardAction(listener, 
                helpKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
    }
    
    public void uninstallUI(JComponent c) {
        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();
        wizardContentPane.setLayout(null);
        wizardContentPane = null;
        wizard = null;
    }
    
    protected void uninstallComponents() {
        if (stepDescriptionRenderer != null) {
            wizardContentPane.remove(stepDescriptionRenderer);
            stepDescriptionRenderer = null;
        }
        if (stepListRenderer != null) {
            wizardContentPane.remove(stepListRenderer);
            stepListRenderer = null;
        }
        wizardContentPane.remove(currentStep.getComponent());
        if (wizardNavigator != null) {
            wizardContentPane.remove(wizardNavigator);
            wizardNavigator = null;
        }
    }
    
    protected void uninstallDefaults() {
        if (wizard.getStepDescriptionRenderer() instanceof UIResource)
            wizard.setStepDescriptionRenderer(null);
        if (wizard.getStepListRenderer() instanceof UIResource)
            wizard.setStepListRenderer(null);
    }
    
    protected void uninstallListeners() {
        if (wizardModelListener != null) {
            wizard.getModel().removeWizardModelListener(wizardModelListener);
            wizardModelListener = null;
        }
        if (windowListener != null) {
            wizard.removeWindowListener(windowListener);
            windowListener = null;
        }
        if (propertyChangeListener != null) {
            wizard.removePropertyChangeListener(propertyChangeListener);
            propertyChangeListener = null;
        }
        if (wizardNavigator != null && actionListener != null)
            wizardNavigator.removeActionListener(actionListener);
        wizard.getRootPane().unregisterKeyboardAction(escKeyStroke);
        escKeyStroke = null;
        if (helpKeyStroke != null) {
            wizard.getRootPane().unregisterKeyboardAction(helpKeyStroke);
            helpKeyStroke = null;
        }
        actionListener = null;
    }
    
    protected void updateStepDescriptionRendererComponent() {
        if (stepDescriptionRenderer != null) {
            wizardContentPane.remove(stepDescriptionRenderer);
            stepDescriptionRenderer = null;
        }
        StepDescriptionRenderer renderer = wizard.getStepDescriptionRenderer();
        if (renderer != null) {
            stepDescriptionRenderer = renderer.getStepDescriptionRendererComponent(wizard);
            wizardContentPane.add(stepDescriptionRenderer);
        }
    }
    
    protected void updateStepListRendererComponent() {
        if (stepListRenderer != null) {
            wizardContentPane.remove(stepListRenderer);
            stepListRenderer = null;
        }
        StepListRenderer renderer = wizard.getStepListRenderer();
        if (renderer != null) {
            stepListRenderer = renderer.getStepListRendererComponent(wizard);
            if (stepListRenderer != null) {
                wizardContentPane.add(stepListRenderer);
            }
        }
    }
    
    protected void updateStepComponent() {
        if (propertyChangeListener != null) {
            currentStep.removePropertyChangeListener(propertyChangeListener);
        }
        Component currentComponent = currentStep.getComponent();
        if (currentComponent != null) {
            wizardContentPane.remove(currentComponent);
        }
        currentStep = wizard.getModel().getCurrentStep();
        if (propertyChangeListener != null)
            currentStep.addPropertyChangeListener(propertyChangeListener);
        LookAndFeel laf = UIManager.getLookAndFeel();
        JComponent c = currentStep.getComponent();
        String prefix = getPropertyPrefix();
        laf.installBorder(c, prefix + "stepBorder");
        laf.installColors(c, prefix + "stepBackground", prefix + "stepBackground");
        wizardContentPane.add(c);
    }
    
    protected BasicWizardNavigator createWizardNavigator() {
        return new BasicWizardNavigator(wizard);
    }
    
    protected PropertyChangeListener createPropertyChangeListener() {
        return new BasicPropertyChangeHandler();
    }
    
    protected WindowListener createWindowListener() {
        return new BasicWindowHandler(wizard);
    }
    
    protected WizardModelListener createWizardModelListener() {
        return new BasicWizardModelHandler();
    }
    
    protected ActionListener createActionListener() {
        return new BasicActionHandler(wizard.getModel());
    }
    
    protected LayoutManager createLayoutManager() {
        return new BasicWizardLayout();
    }
    
    protected class BasicPropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String property = e.getPropertyName();
            if (Wizard.STEP_DESCRIPTION_RENDERER_PROPERTY.equals(property)) {
                updateStepDescriptionRendererComponent();
                wizardContentPane.revalidate();
                wizardContentPane.repaint();
            } else if (Wizard.STEP_LIST_RENDERER_PROPERTY.equals(property)) {
                updateStepListRendererComponent();
                wizardContentPane.revalidate();
                wizardContentPane.repaint();
            } else if (Step.BACK_PROPERTY.equals(property) || 
                    Step.NEXT_PROPERTY.equals(property) || 
                    Step.FINISH_PROPERTY.equals(property)) {
                if(wizardNavigator != null)
                    wizardNavigator.updateNavigation();
            } else if (Wizard.HELP_LISTENER_PROPERTY.equals(property)) {
                JComponent temp = wizardContentPane;
                uninstallUI(wizardContentPane);
                installUI(temp);
            }
        }
    }
    
    protected class BasicWizardModelHandler implements WizardModelListener {
        public void stepShown(WizardModelEvent e) {
            updateStepComponent();
            StepListRenderer listRenderer = wizard.getStepListRenderer();
            if (listRenderer != null)
                listRenderer.updateStepList(wizard.getModel());
            StepDescriptionRenderer descRenderer = wizard.getStepDescriptionRenderer();
            if (descRenderer != null)
                descRenderer.updateStepDescription(currentStep);
            if (wizardNavigator != null)
                wizardNavigator.updateNavigation();
            wizardContentPane.revalidate();
            wizardContentPane.repaint();
            currentStep.getComponent().requestFocusInWindow();
        }
        
        public void wizardCanceled(WizardModelEvent e) {
            wizard.dispose();
        }
        
        public void wizardFinished(WizardModelEvent e) {
            wizard.dispose();
        }
        
        public void wizardModelChanged(WizardModelEvent e) {
            updateStepListRendererComponent();
        }
    }
    
    protected class BasicWizardLayout implements LayoutManager {
        public void layoutContainer(Container parent) {
            Dimension size = parent.getSize();
            int stepListWidth = stepListRenderer!=null ? 
                    stepListRenderer.getMinimumSize().width : 0;
            int stepDescHeight;
            int navigatorHeight;
            if (stepDescriptionRenderer != null) {
                stepDescHeight = stepDescriptionRenderer.getMinimumSize().height;
                stepDescriptionRenderer.setBounds(stepListWidth, 0,
                        size.width - stepListWidth, stepDescHeight);
            } else {
                stepDescHeight = 0;
            }
            if (wizardNavigator != null) {
                navigatorHeight = wizardNavigator.getMinimumSize().height;
                wizardNavigator.setBounds(0, size.height - navigatorHeight,
                        size.width, navigatorHeight);
            } else {
                navigatorHeight = 0;
            }
            if (stepListRenderer != null) {
                stepListRenderer.setBounds(0, 0, stepListWidth,
                        size.height - navigatorHeight);
            }
            Component stepComponent = currentStep.getComponent();
            stepComponent.setBounds(stepListWidth, stepDescHeight,
                    size.width - stepListWidth, 
                    size.height - (stepDescHeight + navigatorHeight));
        }
        
        public Dimension minimumLayoutSize(Container parent) {
            Dimension min = new Dimension();
            Dimension stepListSize;
            Dimension stepDescSize;
            Dimension currentStepSize = currentStep.getComponent().getMinimumSize();
            if (stepListRenderer != null) {
                stepListSize = stepListRenderer.getMinimumSize();
            } else {
                stepListSize = (Dimension)min.clone();
            }
            if (stepDescriptionRenderer != null) {
                stepDescSize = stepDescriptionRenderer.getMinimumSize();
            } else {
                stepDescSize = (Dimension)min.clone();
            }
            min.width = stepListSize.width + Math.max(
                    Math.max(stepDescSize.width, currentStepSize.width),
                    2*stepListSize.width);
            min.height = Math.max(stepListSize.height, stepDescSize.height + 
                    Math.max(currentStepSize.height,
                            Math.max(4 * stepDescSize.height, 250)));
            if (wizardNavigator != null) {
                Dimension temp = wizardNavigator.getMinimumSize();
                min.width = Math.max(min.width, temp.width + stepListSize.width);
                min.height += temp.height;
                min.height = Math.max(min.height, (int) ((2 * min.width) / 3));
                min.width = Math.max(min.width, (int) ((3 * min.height) / 2));
            }
            return min;
        }
        
        public Dimension preferredLayoutSize(Container parent) {
            return minimumLayoutSize(parent);
        }
        
        public void addLayoutComponent(String name, Component comp) {}
        public void removeLayoutComponent(Component comp) {}
    }
}
