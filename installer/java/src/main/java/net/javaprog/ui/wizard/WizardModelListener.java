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

import java.util.EventListener;

/**
 * Provides an interface for wizard model event listeners.
 *
 * @see WizardModelEvent
 * @see WizardModel
 *
 * @author Michael Rudolf
 */
public interface WizardModelListener extends EventListener {
    
    /**
     * Called when a step from the model is shown in the wizard.
     *
     * @param e the event object describing the event
     */
    public void stepShown(WizardModelEvent e);
    
    /**
     * Called when the wizard is canceled. This can be accomplished
     * by pressing the Cancel button, closing the wizard's window or
     * directly calling {@link WizardModel#cancelWizard}.
     *
     * @param e the event object describing the event
     *
     * @see WizardModel#cancelWizard()
     */
    public void wizardCanceled(WizardModelEvent e);
    
    /**
     * Called when the wizard is finished. This can either be accomplished
     * by pressing the Finish button or by directly calling
     * {@link WizardModel#finishWizard()}.
     *
     * @param e the event object describing the event
     *
     * @see WizardModel#finishWizard()
     */
    public void wizardFinished(WizardModelEvent e);
    
    /**
     * Called when the wizard model changes due to some 
     * {@link StepModelCustomizer}.
     *
     * @param e the event object describing the event
     *
     * @see StepModelCustomizer
     */
    public void wizardModelChanged(WizardModelEvent e);
}
