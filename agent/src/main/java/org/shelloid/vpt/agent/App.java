/*
 Copyright (c) Shelloid Systems LLP. All rights reserved.
 The use and distribution terms for this software are covered by the
 GNU General Public License 3.0 (http://www.gnu.org/copyleft/gpl.html)
 which can be found in the file LICENSE at the root of this distribution.
 By using this software in any fashion, you are agreeing to be bound by
 the terms of this license.
 You must not remove this notice, or any other, from this software.
 */
package org.shelloid.vpt.agent;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SimpleTrustManagerFactory;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.URI;
import java.nio.channels.FileLock;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.apache.commons.codec.binary.Base64;
import org.shelloid.common.ICallback;
import org.shelloid.common.ShelloidUtil;
import org.shelloid.common.messages.ShelloidHeaderFields;
import org.shelloid.vpt.agent.common.CallbackMessage;
import org.shelloid.vpt.agent.util.AgentReliableMessenger;
import org.shelloid.vpt.agent.util.Configurations;
import org.shelloid.vpt.agent.util.Platform;
import org.slf4j.LoggerFactory;

/* @author Harikrishnan */
public class App {

    private static SystemTray tray = null;
    private static RandomAccessFile file;
    private AgentReliableMessenger messenger;
    private boolean rtmFilesCorrepted;
    private EventLoopGroup group;
    private boolean shuttingDown;

    public static void referDynamicClasses() {
        //referring to dynamically loaded classes referred to in XML config files - so that 
        //minimizeJar option in pom.xml will not remove these classes from jar file
        ch.qos.logback.core.ConsoleAppender.class.toString();
        ch.qos.logback.core.rolling.TimeBasedRollingPolicy.class.toString();
        org.shelloid.vpt.agent.LoggerStartupListener.class.toString();
    }

    public static void main(String args[]) throws Exception {
        referDynamicClasses();
        Configurations.loadPropertiesFile();
        loadLogbakConfigFile(Configurations.get(Configurations.ConfigParams.LOGBACK_FILE_PATH));
        if (args.length > 0) {
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
                Platform.shelloidLogger.error("Look and Feel Error: " + ex.getMessage(), ex);
            }
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "config": {
                        System.out.println("Please edit agent.config file for changing the device identity, if system doesn't support GUI");
                        new ConfigForm(true).setVisible(true);
                        break;
                    }
                    case "startWithGui": {
                        App a = new App();
                        a.setupSystemTray();
                        a.run();
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        } else {
            App a = new App();
            a.run();
        }
    }

    public static void stop() {
        System.out.println("Stopping service");
    }

    public static String getVersion() {
        Package p = App.class.getPackage();
        String version = p.getImplementationVersion();
        if (version == null) {
            version = "devTest";
        }
        return version;
    }

    public static void showTrayMessage(String msg, TrayIcon.MessageType type) {
        if (tray != null && tray.getTrayIcons().length > 0) {
            if (SystemTray.isSupported()) {
                tray.getTrayIcons()[0].displayMessage("Shelloid VPT Agent", msg, type);
            }
        }
    }

    public static void lockFile() throws Exception {
        final String fileName = "lockFile";
        final File lockFile = new File(fileName);
        if (!lockFile.exists()) {
            try {
                lockFile.createNewFile();
            } catch (IOException ex) {
                throw new Exception("Can't create lockFile. Please check whether you have access permissions to the path.");
            }
        }
        file = new RandomAccessFile(lockFile, "rw");
        final FileLock fileLock = file.getChannel().tryLock();
        if (fileLock == null) {
            throw new Exception("Can't get file lock. Check whether another instance is already runnibg.");
        }
    }

    private static void loadLogbakConfigFile(String logFilePath) {
        System.out.println("Reading configuration file from " + logFilePath);
        // assume SLF4J is bound to logback in the current environment
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            context.reset();
            configurator.doConfigure(new File(logFilePath));
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    public App() throws Exception {
        try {
            lockFile();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            throw ex;
        }
        shuttingDown = false;
        rtmFilesCorrepted = !new File("messageStore").exists();
        messenger = new AgentReliableMessenger();
        if (!rtmFilesCorrepted) {
            try {
                messenger.initDb();
            } catch (Throwable ex) {
                Platform.shelloidLogger.error("Reliable messaging file corrupted, deleting file.", ex);
                rtmFilesCorrepted = true;
                messenger.deleteDbFile();
            }
        }
    }

    public synchronized void authenticate(AgentCallBack callback) {
        try {
            initClient(Configurations.get(Configurations.ConfigParams.CLIENT_KEY), Configurations.get(Configurations.ConfigParams.CLIENT_SECRET), callback);
        } catch (Exception ex) {
            callback.callback(new CallbackMessage(CallbackMessage.Status.DISCONNECTED, new Object[]{ex}));
            if (!(ex instanceof ConnectException)) {
                Platform.shelloidLogger.error("Can't connect to web socket: ", ex);
            }
        }
    }

    public void initClient(final String key, final String secret, final ICallback callback) throws Exception {
        final String host = Configurations.get(Configurations.ConfigParams.SERVER_IP);
        final int port = Integer.parseInt(Configurations.get(Configurations.ConfigParams.SERVER_PORT));
        final boolean useProxy = Boolean.parseBoolean(Configurations.get(Configurations.ConfigParams.USE_PROXY));
        final URI uri = new URI("wss://" + host + ":" + port + Configurations.WEBSOCKET_PATH);
        int proxPort;
        String proxyHost;
        String proxyUserName;
        String proxyPassword;
        Platform.shelloidLogger.warn("Client authenticating Authenticating with " + key);
        final SslContext sslCtx = SslContext.newClientContext(ShelloidTrustManagerFactory.INSTANCE);
        group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (useProxy) {
                            p.addLast(new HttpClientCodec(),
                                    new HttpObjectAggregator(ShelloidUtil.getMaxFrameSize()),
                                    new ProxyClient(uri, key, secret, callback));
                        } else {
                            preparePipeLine(ch, key, secret, callback, uri, sslCtx);
                        }
                    }
                });
        if (useProxy) {
            try {
                proxPort = Integer.parseInt(Configurations.get(Configurations.ConfigParams.PROXY_PORT) + "");
                proxyHost = Configurations.get(Configurations.ConfigParams.PROXY_SERVER) + "";
                proxyUserName = Configurations.get(Configurations.ConfigParams.PRPXY_USERNAME) + "";
                proxyPassword = Configurations.get(Configurations.ConfigParams.PRPXY_PASSWORD) + "";
                Platform.shelloidLogger.warn("Using proxy server " + proxyHost + ":" + proxPort);
                Channel ch = b.connect(proxyHost, proxPort).sync().channel();
                HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, uri.getHost() + ":" + uri.getPort());
                request.headers().add("Host", uri.getHost() + ":" + uri.getPort());
                request.headers().add("Proxy-Authorization", "basic " + new String(Base64.encodeBase64((proxyUserName + ":" + proxyPassword).getBytes())));
                ch.writeAndFlush(request).sync();
            } catch (NumberFormatException ex) {
                Configurations.put(Configurations.ConfigParams.USE_PROXY, false + "");
                throw new Exception("Invalid proxy port number: " + Configurations.get(Configurations.ConfigParams.PROXY_PORT) + "\nRetrying without proxy support.");
            }
        } else {
            b.connect(uri.getHost(), uri.getPort()).sync();
        }
    }

    private Image createImage(String imgpath) throws IOException {
        ImageIcon ico = new ImageIcon(getClass().getResource(imgpath));
        return ico.getImage();
    }

    private void setupSystemTray() {
        if (SystemTray.isSupported()) {
            try {
                final ConfigForm configForm = new ConfigForm(false);
                final PopupMenu popup = new PopupMenu();
                final TrayIcon trayIcon = new TrayIcon(createImage("/images/logo.jpg"), "Shelloid VPT Agent");
                tray = SystemTray.getSystemTray();
                MenuItem authenticateItem = new MenuItem("Configure Authentication");
                MenuItem aboutItem = new MenuItem("About Shelloid VPT Agent");
                MenuItem exitItem = new MenuItem("Exit");
                trayIcon.setPopupMenu(popup);
                tray.add(trayIcon);
                authenticateItem.addActionListener(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        configForm.setVisible(true);
                    }
                });
                aboutItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showMessageDialog(null, "Shelloid VPT Agent.\nVersion : " + getVersion() + "\n\n(c) 2014 Shelloid LLC. \nhttps://www.shelloid.com", "Shelloid VPT Client", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                exitItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (JOptionPane.showConfirmDialog(null, "Are you sure to exit Shelloid VPT Agent?", "Shelloid VPT Agent", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                            shuttingDown = true;
                            closeAllConnections();
                            System.exit(0);
                        }
                    }
                });
                popup.add(authenticateItem);
                popup.add(aboutItem);
                popup.addSeparator();
                popup.add(exitItem);
            } catch (Exception ex) {
                Platform.shelloidLogger.warn("System Tray Error: ", ex);
            }
        } else {
            System.out.println("System tray is not supported");
        }
    }

    private VPTClient preparePipeLine(Channel ch, String key, String secret, ICallback callback, URI uri, SslContext sslCtx) {
        HttpHeaders headers = getAuthHeader(key, secret);
        ChannelPipeline p = ch.pipeline();
        final VPTClient clientVar = new VPTClient(WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, headers, ShelloidUtil.getMaxFrameSize()), callback, messenger);
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
        }
        p.addLast("idleStateHandler", new IdleStateHandler(Configurations.PING_SEND_INTERVAL * 2, 0, Configurations.PING_SEND_INTERVAL));
        p.addLast("idleTimeHandler", new ShelloidIdleTimeHandler());
        p.addLast(new HttpClientCodec(), new HttpObjectAggregator(ShelloidUtil.getMaxFrameSize()), clientVar);
        return clientVar;
    }

    private HttpHeaders getAuthHeader(String key, String secret) {
        String version = getVersion();
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(ShelloidHeaderFields.key, key);
        headers.add(ShelloidHeaderFields.secret, secret);
        headers.add(ShelloidHeaderFields.version, version);
        headers.add(ShelloidHeaderFields.resetLastSendAck, rtmFilesCorrepted);
        return headers;
    }

    private void run() throws IOException, AWTException {
        System.out.println("Shelloid VPT Agent " + getVersion() + " started.");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shuttingDown = true;
                closeAllConnections();
            }
        });
        if (Configurations.get(Configurations.ConfigParams.CLIENT_KEY) == null) {
            Platform.shelloidLogger.error("Please configure client key and secret in agent configuration file before starting the service.");
        } else {
            authenticate(new AgentCallBack());
        }
    }

    private void closeAllConnections() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    static class ShelloidTrustManagerFactory extends SimpleTrustManagerFactory {

        public static final TrustManagerFactory INSTANCE = new ShelloidTrustManagerFactory();
        private static final TrustManager tm = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                if (!ShelloidUtil.getInstance().getValByAttributeTypeFromIssuerDN(chain[0].getSubjectDN().toString(), "CN=").endsWith(Configurations.serverCn)) {
                    throw new CertificateException("Certificate CN is not ending with " + Configurations.serverCn);
                }
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                if (!ShelloidUtil.getInstance().getValByAttributeTypeFromIssuerDN(chain[0].getSubjectDN().toString(), "CN=").endsWith(Configurations.serverCn)) {
                    throw new CertificateException("Certificate CN is not ending with " + Configurations.serverCn);
                }
            }
        };

        private ShelloidTrustManagerFactory() {
        }

        @Override
        protected void engineInit(KeyStore keyStore) throws Exception {
        }

        @Override
        protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws Exception {
        }

        @Override
        protected TrustManager[] engineGetTrustManagers() {
            return new TrustManager[]{tm};
        }
    }

    public class AgentCallBack implements ICallback<CallbackMessage> {

        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();

        @Override
        public void callback(CallbackMessage params) {
            switch (params.status) {
                case AUTH_SUCCESS: {
                    if (ex != null) {
                        ex.shutdown();
                    }
                    try {
                        if (rtmFilesCorrepted) {
                            messenger.initDb();
                            rtmFilesCorrepted = false;
                            Platform.shelloidLogger.warn("Created new realtime messaging files.");
                        }
                        String msg = "Authentication Successful for Shelloid VPT Agent.";
                        Platform.shelloidLogger.warn(msg);
                        App.showTrayMessage(msg, TrayIcon.MessageType.INFO);
                    } catch (Exception e) {
                        Platform.shelloidLogger.error("Reliable messaging not available. Exiting...", e);
                        System.exit(-1);
                    }
                    break;
                }
                case AUTH_FAILED: {
                    String msg = "Authentication failed for Shelloid VPT Agent. Please retry";
                    Platform.shelloidLogger.warn(msg);
                    App.showTrayMessage(msg, TrayIcon.MessageType.ERROR);
                    break;
                }
                case DISCONNECTED: {
                    String msg = "Connection to server is closed!";
                    if (params.params[0] != null) {
                        if (((Throwable) params.params[0]).getMessage() == null) {
                            msg += ": " + ((Throwable) params.params[0]).getClass().getName();
                        } else {
                            msg += "\n" + ((Throwable) params.params[0]).getMessage();
                        }
                    }
                    if (!shuttingDown) {
                        msg += "\nWill retry in 5 seconds.";
                        App.showTrayMessage(msg, TrayIcon.MessageType.ERROR);
                        Platform.shelloidLogger.warn(msg);
                        if (ex != null) {
                            ex.shutdown();
                        }
                        ex = Executors.newSingleThreadScheduledExecutor();
                        ex.schedule(new Runnable() {
                            @Override
                            public void run() {
                                Platform.shelloidLogger.warn("Retrying connection....");
                                authenticate(AgentCallBack.this);
                            }
                        }, 5, TimeUnit.SECONDS);
                    }
                    break;
                }
            }
        }
    }

    class ProxyClient extends SimpleChannelInboundHandler<Object> {

        private final URI uri;
        private final String key;
        private final String secret;
        private final ICallback callback;

        private ProxyClient(URI uri, String key, String secret, ICallback callback) {
            this.uri = uri;
            this.callback = callback;
            this.key = key;
            this.secret = secret;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            HttpResponse res = (HttpResponse) msg;
            if (res.getStatus().code() / 100 == 2) {
                Platform.shelloidLogger.warn("Proxy authentication successfull.");
                final SslContext sslCtx = SslContext.newClientContext(App.ShelloidTrustManagerFactory.INSTANCE);
                while (true) {
                    try {
                        ctx.channel().pipeline().removeLast();
                    } catch (Exception ex) {
                        break;
                    }
                }
                VPTClient clientVar = preparePipeLine(ctx.channel(), key, secret, callback, uri, sslCtx);
                clientVar.handshaker.handshake(ctx.channel());
            } else {
                App.showTrayMessage("Proxy authentication Failed.", TrayIcon.MessageType.ERROR);
                Platform.shelloidLogger.warn("Proxy authentication Failed.");
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            Platform.shelloidLogger.warn("Connected to Proxy Server");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            Platform.shelloidLogger.warn("Disconnected from Proxy Server");
        }
    }
}

class ShelloidIdleTimeHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            switch (((IdleStateEvent) evt).state()) {
                case ALL_IDLE: {
                    Platform.shelloidLogger.debug("Client sending PingWebSocketFrame to " + ctx.channel());
                    ctx.channel().writeAndFlush(new PingWebSocketFrame());
                    break;
                }
                case READER_IDLE: {
                    Platform.shelloidLogger.debug("Read Idle for " + (Configurations.PING_SEND_INTERVAL * 2) + " seconds. So closing the channel: " + ctx.channel());
                    ctx.channel().close();
                    break;
                }
            }
        }
    }
}
