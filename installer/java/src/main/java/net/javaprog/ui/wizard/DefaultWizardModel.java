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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

/**
 * Default implementation for WizardModel.
 *
 * @author Michael Rudolf
 */
public class DefaultWizardModel implements WizardModel {
    
    /**
     * Contains references to the actual steps.
     *
     * @see #steps()
     * @see #contains(Step)
     * @see #getSteps()
     * @see #getStepCount()
     */
    protected List steps = new LinkedList();
    
    /**
     * The index of the currently displayed step.
     *
     * @see #getCurrentIndex()
     */
    protected int index = 0;
    
    /**
     * Manages the listeners registered with this model.
     *
     * @see #addWizardModelListener(WizardModelListener)
     * @see #removeWizardModelListener(WizardModelListener)
     */
    protected final EventListenerList listenerList = new EventListenerList();
    
    /**
     * A single event object instance pointing to this model. This object
     * gets initialized in the constructor and is passed to all listeners.
     */
    private WizardModelEvent e;
    
    /**
     * Creates a new wizard model from the given step array. The array must not
     * be null and must at least contain one step.
     *
     * @param s the steps from which to create a new wizard model
     */
    public DefaultWizardModel(Step[] s) {
        if (s == null) {
            throw new IllegalArgumentException("Step array cannot be null.");
        } else if (s.length == 0) {
            throw new IllegalArgumentException("Step array cannot be empty.");
        }
        for (int i = 0; i < s.length; i++) {
            steps.add(s[i]);
            if (s[i] instanceof StepModelCustomizer) {
                steps.add(new DummyStep());
                break;
            }
        }
        e = new WizardModelEvent(this);
    }
    
    public void addWizardModelListener(WizardModelListener l) {
        listenerList.add(WizardModelListener.class, l);
    }
    
    public void removeWizardModelListener(WizardModelListener l) {
        listenerList.remove(WizardModelListener.class, l);
    }
    
    public void cancelWizard() {
        fireWizardCanceled();
    }
    
    public void finishWizard() {
        fireWizardFinished();
    }
    
    public Step getCurrentStep() {
        return (Step)steps.get(index);
    }
    
    public int getCurrentIndex() {
        return index;
    }
    
    public Step[] getSteps() {
        return (Step[])steps.toArray(new Step[0]);
    }
    
    public int getStepCount() {
        return steps.size();
    }
    
    /**
     * Provides an iterator for the step sequence.
     */
    public Iterator steps() {
        return steps.iterator();
    }
    
    public boolean contains(Step step) {
        return steps.contains(step);
    }
    
    public void goBack() {
        ((Step)steps.get(--index)).prepareRendering();
        if (steps.get(index) instanceof StepModelCustomizer) {
            for (int i = steps.size() - 1; i > index; i--) {
                steps.remove(i);
            }
            steps.add(new DummyStep());
            fireWizardModelChanged();
        }
        fireStepShown();
    }
    
    public void goNext() {
        if (steps.get(index) instanceof StepModelCustomizer) {
            steps.remove(index + 1);
            Step[] pending = ((StepModelCustomizer)steps.get(index)).getPendingSteps();
            for (int i = 0; i < pending.length; i++) {
                steps.add(pending[i]);
            }
            fireWizardModelChanged();
        }
        ((Step)steps.get(++index)).prepareRendering();
        if (steps.get(index) instanceof StepModelCustomizer) {
            for (int i = steps.size() - 1; i > index; i--) {
                steps.remove(i);
            }
            steps.add(new DummyStep());
            fireWizardModelChanged();
        }
        fireStepShown();
    }
    
    /**
     * Notifies the listeners that a certain step has been shown.
     */
    protected void fireStepShown() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == WizardModelListener.class) {
                ((WizardModelListener)listeners[i+1]).stepShown(e);
            }
        }
    }
    
    /**
     * Notifies the listeners that the wizard has been canceled.
     */
    protected void fireWizardCanceled() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == WizardModelListener.class) {
                ((WizardModelListener)listeners[i+1]).wizardCanceled(e);
            }
        }
    }

    /**
     * Notifies the listeners that the wizard has been finished.
     */
    protected void fireWizardFinished() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == WizardModelListener.class) {
                ((WizardModelListener)listeners[i+1]).wizardFinished(e);
            }
        }
    }
    
    /**
     * Notifies the wizard that the wizard model has been changed.
     */
    protected void fireWizardModelChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == WizardModelListener.class) {
                ((WizardModelListener)listeners[i+1]).wizardModelChanged(e);
            }
        }
    }
    
    /**
     * Step implementation used in dynamic wizard models. This step will
     * be automatically added to your wizard model to show that it is dynamic.
     */
    public static class DummyStep implements Step {
        
        /**
         * Returns false.
         */
        public boolean canFinish() {
            return false;
        }
        
        /**
         * Returns true.
         */
        public boolean canGoBack() {
            return true;
        }
        
        /**
         * Returns true.
         */
        public boolean canGoNext() {
            return true;
        }
        
        /**
         * Returns true.
         *
         * @since 0.1.4
         */
        public boolean canCancel() {
            return true;
        }
        
        /**
         * Returns <code>null</code>. The step actually never gets displayed and
         * therefore does not need a component.
         */
        public JComponent getComponent() {
            return null;
        }
        
        /**
         * Returns an empty string. The step actually never gets displayed and
         * therefore does not need a description.
         */
        public String getDescription() {
            return "";
        }
        
        /**
         * Returns &quot;...&quot; to be displayed in the step list.
         */
        public String getName() {
            return "...";
        }
        
        /**
         * Does nothing.
         */
        public void prepareRendering() {}
        
        /**
         * Does nothing.
         */
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        
        /**
         * Does nothing.
         */
        public void removePropertyChangeListener(PropertyChangeListener l) {}
    }
}
