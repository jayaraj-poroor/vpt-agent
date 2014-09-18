/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shelloid.vpt.agent.util;

import org.shelloid.common.exceptions.ShelloidNonRetriableException;

/**
 *
 * @author Harikrishnan
 */
public interface ShelloidPolicyAddon {
    public String process (String options) throws ShelloidNonRetriableException;
    public void unprocess(String processedOptions) throws ShelloidNonRetriableException;
}
