/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shelloid.vpt.agent.util;

import java.io.File;
import org.shelloid.common.exceptions.ShelloidNonRetriableException;

/**
 *
 * @author Harikrishnan
 */
public interface ShelloidPolicyAddon {
    public String process (File addonDir, String host, int port, String options) throws ShelloidNonRetriableException;
    public void unprocess(File addonDir, String host, int port, String processedOptions) throws ShelloidNonRetriableException;
}
