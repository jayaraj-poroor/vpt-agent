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

import java.io.*;
import java.util.*;

/* @author Harikrishnan */
public class Configurations {

    public static final String WEBSOCKET_PATH = "/websocket";
    public static final int MAX_NO_ROUTE_MSG = 5;
    public static int PING_SEND_INTERVAL = 30;
    private static final Properties props;
    private static final Properties defaultProps = new Properties();
    public static final int CONN_IDLE_THRESHOLD = 5 * 60 * 1000;
    public static int maxConcurrentMessages;
    public static String serverCn;
    
    static {
        defaultProps.put(ConfigParams.SERVER_CN.toString(), "shelloid.com");
        defaultProps.put(ConfigParams.MAX_CONCURRENT_MESSAGES.toString(), "5");
        defaultProps.put(ConfigParams.SERVER_IP.toString(), "rms.shelloid.com");
        defaultProps.put(ConfigParams.SERVER_PORT.toString() , "443");
        defaultProps.put(ConfigParams.STARTING_PORT_NUMBER.toString() , "5000");
        defaultProps.put(ConfigParams.LOGBACK_FILE_PATH.toString() , "logback.xml");
        defaultProps.put(ConfigParams.LOG_FILE_PATH.toString() , ".");
        props = new Properties(defaultProps);
    }

    public static void loadPropertiesFile() throws Exception {
        InputStream input = new FileInputStream("agent.cfg");
        props.load(input);
        maxConcurrentMessages = Integer.parseInt(get(ConfigParams.MAX_CONCURRENT_MESSAGES));
        serverCn = get(ConfigParams.SERVER_CN);
    }

    public static String get(ConfigParams key) {
        synchronized (props) {
            return props.getProperty(key + "");
        }
    }

    public static void put(ConfigParams key, String value) {
        synchronized (props) {
            props.put(key.toString(), value);
        }
    }

    public static void save() {
        synchronized (props) {
            try {
                File f = new File("agent.cfg");
                OutputStream out = new FileOutputStream(f);
                props.store(out, "Shelloid agent configurations");
            } catch (Exception ex) {
                Platform.shelloidLogger.error("Can's save properties file: ", ex);
            }
        }
    }

    public enum ConfigParams {

        SERVER_IP("server.ip"),
        SERVER_PORT("server.port"),
        CLIENT_KEY("client.key"),
        CLIENT_SECRET("client.secret"),
        STARTING_PORT_NUMBER("client.startingPortNumber"),
        MAX_CONCURRENT_MESSAGES("client.MaxConcurrentMessages"),
        LOGBACK_FILE_PATH("client.logbackFilePath"),
        PROXY_SERVER("client.proxy.server"), 
        PROXY_PORT("client.proxy.port"),
        USE_PROXY("client.useProxy"), 
        PROXY_USERNAME("client.proxy.username"), 
        PROXY_PASSWORD("client.proxy.password"),
        LOG_FILE_PATH("client.logFilePath"),
        SERVER_CN("server.cn"),
        ADDON_DIR("client.addonDir");
        private final String text;

        private ConfigParams(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
