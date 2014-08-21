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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

/**
 * Provides a basic Step implementation. This class manages the
 * PropertyChangeListeners and the caching of the step component.
 *
 * @author Michael Rudolf
 */
public abstract class AbstractStep implements Step {
    protected boolean canGoBack = true;
    protected boolean canGoNext = true;
    protected boolean canFinish = false;
    protected boolean canCancel = true;
    
    /**
     * The name displayed in the step list.
     *
     * @see #getName()
     */
    protected String name;
    
    /**
     * The description displayed by the step description renderer.
     *
     * @see #getDescription()
     */
    protected String description;
    
    /**
     * The component displayed in the wizard dialog.
     *
     * @see #getComponent()
     */
    protected JComponent component;
    
    /**
     * Manages the listeners registered with this step.
     *
     * @see #addPropertyChangeListener(PropertyChangeListener)
     * @see #removePropertyChangeListener(PropertyChangeListener)
     */
    protected final EventListenerList listenerList = new EventListenerList();
    
    /**
     * Creates a new Step with the given name and description.
     *
     * @param name          the name of the step
     * @param description   a short description of the step
     */
    public AbstractStep(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        listenerList.add(PropertyChangeListener.class, l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listenerList.remove(PropertyChangeListener.class, l);
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Returns the step's component. This method caches the value retrieved
     * from {@link #createComponent()}.
     *
     * @see #createComponent()
     */
    public JComponent getComponent() {
        if (component == null) {
            component = createComponent();
        }
        return component;
    }
    
    /**
     * Subclasses must overwrite this method and return the step's component.
     * This method will only get called once and the result will be cached.
     * Please ensure that all child containers are NOT opaque (i.e. all JPanel
     * instances added to this component should not paint their background)
     * otherwise the step rendering might get messed up.
     *
     * @see JComponent#setOpaque(boolean)
     * @see #getComponent()
     */
    protected abstract JComponent createComponent();
    
    public boolean canGoBack() {
        return canGoBack;
    }
    
    /**
     * Changes the <code>canGoBack</code> property and notifies the listeners.
     *
     * @param b whether the back button should be enabled or not
     *
     * @see #canGoBack
     */
    protected void setCanGoBack(boolean b) {
        if (b != canGoBack) {
            canGoBack = b;
            firePropertyChange(BACK_PROPERTY, b ? Boolean.FALSE : Boolean.TRUE,
                    b ? Boolean.TRUE : Boolean.FALSE);
        }
    }
    
    public boolean canGoNext() {
        return canGoNext;
    }
    
    /**
     * Changes the <code>canGoNext</code> property and notifies the listeners.
     *
     * @param b whether the next button should be enabled or not
     *
     * @see #canGoNext
     */
    protected void setCanGoNext(boolean b) {
        if (b != canGoNext) {
            canGoNext = b;
            firePropertyChange(NEXT_PROPERTY, b ? Boolean.FALSE : Boolean.TRUE,
                    b ? Boolean.TRUE : Boolean.FALSE);
        }
    }
    
    public boolean canFinish() {
        return canFinish;
    }
    
    /**
     * Changes the <code>canFinish</code> property and notifies the listeners.
     *
     * @param b whether the finish button should be enabled or not
     *
     * @see #canFinish
     */
    protected void setCanFinish(boolean b) {
        if (b != canFinish) {
            canFinish = b;
            firePropertyChange(FINISH_PROPERTY, b ? Boolean.FALSE : Boolean.TRUE,
                    b ? Boolean.TRUE : Boolean.FALSE);
        }
    }
    
    public boolean canCancel() {
        return canCancel;
    }
    
    /**
     * Changes the <code>canCancel</code> property and notifies the listeners.
     *
     * @param b whether the wizard should be cancelable or not
     *
     * @see #canCancel
     *
     * @since 0.1.4
     */
    protected void setCanCancel(boolean b) {
        if (b != canCancel) {
            canCancel = b;
            firePropertyChange(CANCEL_PROPERTY, b ? Boolean.FALSE : Boolean.TRUE,
                    b ? Boolean.TRUE : Boolean.FALSE);
        }
    }
    
    /**
     * Notifies the listeners of a property change.
     */
    protected void firePropertyChange(String prop, Object oldValue, Object newValue) {
        Object[] listeners = listenerList.getListenerList();
        PropertyChangeEvent e = new PropertyChangeEvent(this, prop, oldValue, newValue);
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == PropertyChangeListener.class) {
                ((PropertyChangeListener)listeners[i+1]).propertyChange(e);
            }
        }
    }
}
