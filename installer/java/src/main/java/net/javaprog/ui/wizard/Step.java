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

import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

/**
 * Models a simple step in a sequence of steps. It is recommended
 * to subclass {@link AbstractStep} instead of implementing this interface.
 *
 * @see WizardModel
 * @see AbstractStep
 *
 * @author Michael Rudolf
 */
public interface Step {
    /**
     * Property name that is used for PropertyChangeEvents generated when the
     * <code>canGoBack</code> property is changed.
     *
     * @see #canGoBack
     */
    public static final String BACK_PROPERTY = "back";
    
    /**
     * Property name that is used for PropertyChangeEvents generated when the
     * <code>canGoNext</code> property is changed.
     *
     * @see #canGoNext
     */
    public static final String NEXT_PROPERTY = "next";
    
    /**
     * Property name that is used for PropertyChangeEvents generated when the
     * <code>canFinish</code> property is changed.
     *
     * @see #canFinish
     */
    public static final String FINISH_PROPERTY = "finish";
 
    /**
     * Property name that is used for PropertyChangeEvents generated when the
     * <code>canCancel</code> property is changed.
     *
     * @see #canCancel
     *
     * @since 0.1.4
     */
    public static final String CANCEL_PROPERTY = "cancel";
 
    /**
     * Adds a listener for the back, next, finish and cancel properties.
     *
     * @param l the listener to be added
     *
     * @see #removePropertyChangeListener(PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Removes a previously added listener.
     *
     * @param l the listener to be removed
     *
     * @see #addPropertyChangeListener(PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Returns whether the back action is enabled.
     */
    public boolean canGoBack();

    /**
     * Returns whether the next action is enabled.
     */
    public boolean canGoNext();

    /**
     * Returns whether the finish action is enabled.
     */
    public boolean canFinish();
    
    /**
     * Returns whether the cancel action is enabled.
     *
     * @since 0.1.4
     */
    public boolean canCancel();
    
    /**
     * Is called just before the component will be displayed. This
     * enables the step to adjust certain values according to the value
     * of other steps.
     *
     * @see #getComponent()
     */
    public void prepareRendering();
    
    /**
     * Returns the step's component that will be displayed in the wizard.
     * You should cache this return value in your implementation to avoid
     * performance bottlenecks.
     */
    public JComponent getComponent();
    
    /**
     * Returns the step's name that might be displayed in the 
     * {@link StepDescriptionRenderer} and in the {@link StepListRenderer}.
     */
    public String getName();
    
    /**
     * Returns a descriptive string for this step that might be displayed in
     * the {@link StepDescriptionRenderer}.
     */
    public String getDescription();
}
