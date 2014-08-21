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

/**
 * Models a sequence of steps.
 *
 * @see Step
 * @see WizardModelListener
 *
 * @author Michael Rudolf
 */
public interface WizardModel {
    
    /**
     * Adds a listener for the back, next and finish actions.
     *
     * @param l the listener to add
     */
    public void addWizardModelListener(WizardModelListener l);
    
    /**
     * Removes a previously added listener.
     *
     * @param l the listener to remove
     */
    public void removeWizardModelListener(WizardModelListener l);
    
    /**
     * Returns the currently visible step.
     */
    public Step getCurrentStep();
    
    /**
     * Returns the index of the currently visible step in the sequence.
     */
    public int getCurrentIndex();
    
    /**
     * Returns whether the sequence contains the given step.
     *
     * @param step the step that should be tested for containment
     */
    public boolean contains(Step step);
    
    /**
     * Returns the sequence of steps as a plain array.
     */
    public Step[] getSteps();
    
    /**
     * Returns the number of steps in this sequence.
     */
    public int getStepCount();
    
    /**
     * Requests that the next step should be shown if possible. This method
     * must notify the registered WizardListeners of the request.
     */
    public void goNext();
    
    /**
     * Requests that the previous step should be shown if possible. This method
     * must notify the registered WizardListeners of the request.
     */
    public void goBack();
    
    /**
     * Cancels the wizard. This method must notify the registered WizardListeners
     * of the cancel event.
     */
    public void cancelWizard();
    
    /**
     * Finishes the wizard if possible. This method must notify the registered
     * WizardListeners of the finish event.
     */
    public void finishWizard();
}
