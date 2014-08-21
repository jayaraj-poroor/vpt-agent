/*
 Copyright (c) Shelloid Systems LLP. All rights reserved.
 The use and distribution terms for this software are covered by the
 GNU General Public License 3.0 (http://www.gnu.org/copyleft/gpl.html)
 which can be found in the file LICENSE at the root of this distribution.
 By using this software in any fashion, you are agreeing to be bound by
 the terms of this license.
 You must not remove this notice, or any other, from this software.
 */
package org.shelloid.vpt.agent.common;

/* @author Harikrishnan */
public class CallbackMessage {

    public enum Status {

        AUTH_SUCCESS, DISCONNECTED, AUTH_FAILED
    };
    public final Status status;
    public final Object params[];

    public CallbackMessage(Status status, Object params[]) {
        this.status = status;
        this.params = params;
    }
}
