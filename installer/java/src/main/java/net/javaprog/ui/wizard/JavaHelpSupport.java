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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.help.*;

import javax.swing.JComponent;

/**
 * Provides for a seamless integration of jwizz with JavaHelp. This class
 * implements ActionListener to listen for help request actions. In order
 * to activate JavaHelp for a specific Wizard instance, just call
 * {@link #enableHelp(Wizard, HelpBroker)} passing the Wizard instance
 * and the HelpBroker used to display the help:
 * <pre>
 * //initialize Wizard instance
 * Wizard wizard = ...;
 * //initialize HelpBroker instance
 * HelpBroker hb = ...;
 * //set help ID using JavaHelp helper class
 * CSH.setHelpIDString(wizard, "myHelpID");
 * //activate JavaHelp
 * JavaHelpSupport.enableHelp(wizard, hb);
 * </pre>
 * 
 * <b>Note:</b> If you want more precise help for single wizard steps,
 * you can also attach a help ID to each step component:
 * <pre>
 * public class MyStep extends AbstractStep {
 *     ...
 *     protected JComponent createComponent() {
 *         //initialize step component
 *         JComponent stepComponent = ...;
 *         //set help ID using JavaHelp helper class
 *         CSH.setHelpIDString(stepComponent, "myHelpID");
 *         return stepComponent;
 *     }
 * }
 * ...
 * //activate JavaHelp
 * JavaHelpSupport.enableHelp(wizard, hb);
 * </pre>
 *
 * @since 0.1.1
 *
 * @author Michael Rudolf
 */
public class JavaHelpSupport implements ActionListener {
    protected Wizard wizard;
    protected HelpBroker hb;
    
    /**
     * Applications should use the static method {@link #enabledHelp} in order
     * to activate JavaHelp support for jwizz.
     *
     * @see #enableHelp(Wizard, HelpBroker)
     */
    private JavaHelpSupport(Wizard wizard, HelpBroker hb) {
        this.wizard = wizard;
        this.hb = hb;
    }
    
    /**
     * Reacts to help request actions by notifying the HelpBroker to display
     * an appropriate help ID.
     */
    public void actionPerformed(ActionEvent e) {
        if (hb instanceof DefaultHelpBroker) {
            ((DefaultHelpBroker)hb).setActivationWindow(wizard);
        }
        JComponent comp = wizard.getModel().getCurrentStep().getComponent();
        String helpID = CSH.getHelpIDString(comp);
        HelpSet hs = CSH.getHelpSet(comp);
        if (hs == null) {
            hs = hb.getHelpSet();
        }
	try {
            Map.ID id = null;
            try {
                id = Map.ID.create(helpID, hs);
            } catch (BadIDException exp2) {
                id = hs.getHomeID();
                if (id == null) {
                    throw (exp2);
                }
            }
            hb.setCurrentID(id);
            hb.setDisplayed(true);
        } catch (Exception e2) {
            e2.printStackTrace();
	}
    }
    
    /**
     * Activates support for JavaHelp for the given Wizard instance. This
     * method registers an instance of <code>JavaHelpSupport</code> as an 
     * {@link ActionListener} with the passed {@link Wizard} object.
     *
     * @param wizard the Wizard instance for which to enable help
     * @param hb the HelpBroker that is responsible for displaying the help
     * @see Wizard#enableHelp(ActionListener)
     */
    public static void enableHelp(Wizard wizard, HelpBroker hb) {
        wizard.enableHelp(new JavaHelpSupport(wizard, hb));
    }
}
