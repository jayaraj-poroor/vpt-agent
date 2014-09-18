/*
 Copyright (c) Shelloid Systems LLP. All rights reserved.
 The use and distribution terms for this software are covered by the
 GNU General Public License 3.0 (http://www.gnu.org/copyleft/gpl.html)
 which can be found in the file LICENSE at the root of this distribution.
 By using this software in any fashion, you are agreeing to be bound by
 the terms of this license.
 You must not remove this notice, or any other, from this software.
 */

package org.shelloid.vpt.agent.util;

import java.util.*;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* @author Harikrishnan */
public class Platform {

    public HashMap<String, Integer> streamPublishList;
    public static final Logger shelloidLogger = LoggerFactory.getLogger("org.shelloid.vpt");
    private static Platform platform;
    private final ExecutorService threadPool;

    private Platform() {
        threadPool = Executors.newCachedThreadPool();
        streamPublishList = new HashMap<String, Integer>();
    }

    public static Platform getInstance() {
        if (platform == null) {
            platform = new Platform();
        }
        return platform;
    }

    public void executeInThread(Runnable task) {
        threadPool.execute(task);
    }
}
