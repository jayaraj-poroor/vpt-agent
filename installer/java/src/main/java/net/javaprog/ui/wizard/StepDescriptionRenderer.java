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

import java.awt.Component;

/**
 * Provides an interface for rendering step meta information. Implementing
 * classes should subclass an appropriate <code>java.awt.Component</code>
 * subclass and return a reference to themselves:
 *
 * <pre>
 * public class MyStepDescriptionRenderer
 *     extends JPanel implements StepDescriptionRenderer {
 *
 *     public Component getStepDescriptionRendererComponent(Wizard w) {
 *         return this;
 *     }
 *     ...
 * }
 * </pre>
 *
 * @author Michael Rudolf
 */
public interface StepDescriptionRenderer {
    
    /**
     * Returns a component capable of rendering step meta information.
     *
     * @param wizard the wizard for which a step description should be rendered
     */
    public abstract Component getStepDescriptionRendererComponent(Wizard wizard);
    
    /**
     * Called to update the display for the given step.
     *
     * @param step the step that changed
     */
    public abstract void updateStepDescription(Step step);
}
