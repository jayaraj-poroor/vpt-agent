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

import java.awt.Container;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.*;
import java.net.URI;
import java.net.URL;

import javax.swing.*;

/**
 * The main component class.
 *
 * @author Michael Rudolf
 */
public class Wizard extends JDialog {
    
    /**
     * The property name for the step list renderer.
     *
     * @see #setStepListRenderer
     * @see #getStepListRenderer
     */
    public static final String STEP_LIST_RENDERER_PROPERTY = "stepListRenderer";
    
    /**
     * The property name for the step description renderer.
     *
     * @see #setStepDescriptionRenderer
     * @see #getStepDescriptionRenderer
     */
    public static final String STEP_DESCRIPTION_RENDERER_PROPERTY = 
            "stepDescriptionRenderer";
    
    /**
     * The property name for the help listener.
     *
     * @see #enableHelp(ActionListener)
     * @since 0.1.1
     */
    public static final String HELP_LISTENER_PROPERTY = "helpListener";
    
    /**
     * The property name for the icon.
     *
     * @see #setIcon
     * @see #getIcon
     * @since 0.1.4
     */
    public static final String ICON_PROPERTY = "icon";
    
    /**
     * The property name for the wizard model.
     *
     * @see #getModel
     * @see #setModel
     * @since 0.1.4
     */
    public static final String MODEL_PROPERTY = "model";
    
    /**
     * The wizard's model.
     *
     * @see #getModel()
     */
    protected WizardModel model;
    
    /**
     * An icon representing the wizard.
     *
     * @see #getImageIcon()
     */
    protected Icon icon;
    
    /**
     * The renderer for the step list.
     *
     * @see #getStepListRenderer()
     * @see #setStepListRenderer(StepListRenderer)
     */
    protected StepListRenderer stepListRenderer;
    
    /**
     * The renderer for the step description.
     *
     * @see #getStepDescriptionRenderer()
     * @see #setStepDescriptionRenderer(StepDescriptionRenderer)
     */
    protected StepDescriptionRenderer stepDescriptionRenderer;
    
    /**
     * Creates a new wizard with a dummy model and the title string
     * &quot;Wizard&quot;.
     *
     * @since 0.1.4
     */
    public Wizard() {
        this(new DefaultWizardModel(new Step[]{new DefaultWizardModel.DummyStep()}),
                "Wizard");
    }
    
    /**
     * Creates a new wizard from the given model with the given title.
     * 
     * @param model the wizard's step model
     * @param title the string displayed in the dialog's title bar
     */
    public Wizard(WizardModel model, String title) {
        this(null, model, title, null);
    }
    
    /**
     * Creates a new wizard from the given model with the given title.
     * 
     * @param parent the wizard's parent frame.
     * @param model the wizard's step model
     * @param title the string displayed in the dialog's title bar
     */
    public Wizard(Frame parent, WizardModel model, String title) {
        this(parent, model, title, null);
    }
    
    /**
     * Creates a new wizard from the given model with the given title and icon.
     *
     * @param model the wizard's step model
     * @param title the string displayed in the dialog's title bar
     * @param icon  the icon representing the wizard
     *
     * @see #getModel
     * @see #getImageIcon
     */
    public Wizard(WizardModel model, String title, URL icon) {
        this(null, model, title, icon);
    }
    
    /**
     * Creates a new wizard from the given model with the given title and icon.
     *
     * @param parent    the wizard's parent frame.
     * @param model     the wizard's step model
     * @param title     the string displayed in the dialog's title bar
     * @param icon      the icon representing the wizard
     *
     * @see #getModel
     * @see #getIcon
     */
    public Wizard(Frame parent, WizardModel model, String title, URL icon) {
        super(parent, title, true);
        setModel(model);
        setIcon(new ImageIcon(icon));
        setIconImage(new ImageIcon(icon).getImage());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        WizardContentPane c = new WizardContentPane(this);
        c.setName(getRootPane().getName() + ".contentPane");
        getRootPane().setContentPane(c);
    }
    
    /**
     * Returns the wizard's model.
     *
     * @see #setModel
     */
    public WizardModel getModel() {
        return model;
    }
    
    /**
     * Sets the wizard's model.
     *
     * @param model the wizards model
     *
     * @see #getModel
     *
     * @since 0.1.4
     */
    public void setModel(WizardModel model) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null.");
        }
        WizardModel oldModel = this.model;
        this.model = model;
        firePropertyChange(MODEL_PROPERTY, oldModel, model);
    }
    
    /**
     * Returns the image that is used to represent the wizard.
     *
     * @deprecated As of version 0.1.4 replaced by {@link #getIcon}
     */
    public ImageIcon getImageIcon() {
        return icon instanceof ImageIcon ? (ImageIcon)icon : null;
    }
    
    /**
     * Returns the icon that is used to represent the wizard.
     */
    public Icon getIcon() {
        return icon;
    }
    
    /**
     * Sets the icon that is used to represent the wizard.
     *
     * @param icon the new icon for the wizard.
     *
     * @since 0.1.4
     */
    public void setIcon(Icon icon) {
        Icon oldIcon = this.icon;
        this.icon = icon;
        if (icon != null && icon instanceof ImageIcon) {
            ((ImageIcon)icon).setImageObserver(this);
        }
        firePropertyChange(ICON_PROPERTY, oldIcon, icon);
    }
    
    /**
     * Returns the renderer for the step list.
     */
    public StepListRenderer getStepListRenderer() {
        return stepListRenderer;
    }
    
    /**
     * Overrides the step list renderer property.
     *
     * @param renderer the new object to render to step list
     */
    public void setStepListRenderer(StepListRenderer renderer) {
        StepListRenderer oldRenderer = stepListRenderer;
        stepListRenderer = renderer;
        firePropertyChange(STEP_LIST_RENDERER_PROPERTY, oldRenderer, renderer);
    }
    
    /**
     * Returns the renderer for the step description.
     */
    public StepDescriptionRenderer getStepDescriptionRenderer() {
        return stepDescriptionRenderer;
    }
    
    /**
     * Overrides the step description renderer property.
     *
     * @param renderer the new object to renderer the step description
     */
    public void setStepDescriptionRenderer(StepDescriptionRenderer renderer) {
        StepDescriptionRenderer oldRenderer = stepDescriptionRenderer;
        stepDescriptionRenderer = renderer;
        firePropertyChange(STEP_DESCRIPTION_RENDERER_PROPERTY, oldRenderer, renderer);
    }
    
    /**
     * Enables help using the given action listener. The listener will be
     * notified if the help button or the F1 key is pressed. If you want
     * to integrate JavaHelp, please see {@link JavaHelpSupport}.
     *
     * @param listener the ActionListener to notify on help requests
     * @since 0.1.1
     */
    public void enableHelp(ActionListener listener) {
        UIManager.put("Wizard.helpListener", listener);
        firePropertyChange(HELP_LISTENER_PROPERTY, null, listener);
    }
}
