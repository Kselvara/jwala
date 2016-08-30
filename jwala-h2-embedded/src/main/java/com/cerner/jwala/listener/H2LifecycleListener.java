package com.cerner.jwala.listener;

import com.cerner.jwala.service.DbService;
import com.cerner.jwala.service.impl.H2ServiceImpl;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A life cycle listener that starts/stops h2 db server
 *
 * Created by JC043760 on 8/28/2016
 */
public class H2LifecycleListener implements LifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2LifecycleListener.class);
    private DbService dbService;

    private String tcpServerParam;
    private String webServerParam;

    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        if (dbService == null) {
            dbService = new H2ServiceImpl(tcpServerParam, webServerParam);
        }

        final LifecycleState lifecycleState = event.getLifecycle().getState();
        if (LifecycleState.STARTING_PREP.equals(lifecycleState) && !dbService.isServerRunning()) {
            LOGGER.info("Initializing H2 on Tomcat lifecyle: {}", lifecycleState);
            dbService.startServer();
        } else if (LifecycleState.DESTROYING.equals(lifecycleState) && dbService.isServerRunning()) {
            LOGGER.info("Destroying H2 on Tomcat lifecyle: {}", lifecycleState);
            dbService.stopServer();
        }
    }

    public void setTcpServerParam(final String tcpServerParam) {
        this.tcpServerParam = tcpServerParam;
    }

    public void setWebServerParam(final String webServerParam) {
        this.webServerParam = webServerParam;
    }
}