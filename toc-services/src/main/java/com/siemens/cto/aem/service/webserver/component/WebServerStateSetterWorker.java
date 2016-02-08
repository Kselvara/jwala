package com.siemens.cto.aem.service.webserver.component;

import com.siemens.cto.aem.common.domain.model.webserver.WebServerState;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.state.WebServerSetStateRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.ssl.hc.HttpClientRequestFactory;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Sets a web server's state. This class is meant to be a spring bean wherein its "work" method pingWebServer
 * is ran asynchronously as a spun up thread.
 *
 * Note!!! This class has be given it's own package named "component" to denote it as a Spring component
 * that is subject to component scanning. In addition, this was also done to avoid the problem of it's unit test
 * which uses Spring config and component scanning from picking up other Spring components that it does not need
 * which also results to spring bean definition problems.
 *
 * Created by Z003BPEJ on 6/25/2015.
 */
@Service
public class WebServerStateSetterWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerStateSetterWorker.class);
    private HttpClientRequestFactory httpClientRequestFactory;
    private Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap;
    private WebServerService webServerService;
    private StateNotificationService stateNotificationService;
    private GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc;
    @Autowired
    private ClientFactoryHelper clientFactoryHelper;

    private static final ConcurrentHashMap<Identifier<WebServer>, WebServerReachableState> webServerLastPersistedStateMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Identifier<WebServer>, String> webServerLastPersistedErrorStatusMap = new ConcurrentHashMap<>();

    /**
     * Note: Setting of class variables through the constructor preferred but @Async does not work
     * if WebServerStateSetterWorker is instantiated like this in the config file:
     *
     * @Bean
     * WebServerStateSetterWorker webServerStateSetterWorker() {
     *     return new WebServerStateSetterWorker(...);
     * }
     *
     * It should totally be instantiated by Spring (e.g. using @Autowired or if using XML using <Bean>...<Bean/>)
     *
     * Bean definition using context xml and constructor injection would have worked but for consistency
     * (as the application is mostly Spring annotation driven), it was avoided.
     */
    public WebServerStateSetterWorker() {}

    /**
     * Ping the web server via http get.
     * @param webServer the web server to ping.
     */
    @Async("webServerTaskExecutor")
    public Future<?> pingWebServer(WebServer webServer) {
        ClientHttpResponse response = null;
        if (!isWebServerBusy(webServer)) {
            try {
                response = clientFactoryHelper.requestGet(webServer.getStatusUri());
                LOGGER.info(">>> Response = {} from web server {}", response.getStatusCode(), webServer.getId().getId());
                if (response.getStatusCode() == HttpStatus.OK) {
                    setState(webServer, WebServerReachableState.WS_REACHABLE, StringUtils.EMPTY);
                } else {
                    setState(webServer, WebServerReachableState.WS_UNREACHABLE,
                             "Request for '" + webServer.getStatusUri() + "' failed with a response code of '" +
                             response.getStatusCode() + "'");
                }
            } catch (final IOException ioe) {
                LOGGER.info(ioe.getMessage(), ioe);
                setState(webServer, WebServerReachableState.WS_UNREACHABLE, StringUtils.EMPTY);
            } catch (final RuntimeException rte) {
                LOGGER.error(rte.getMessage(), rte);
            } finally {
                if (response != null) {
                    response.close();
                }
            }

        }

        return new AsyncResult<>(null);
    }

    /**
     * Checks if a web server is either starting or stopping or is down (in a failed state).
     * @param webServer the webServer
     * @return true if web server is starting or stopping
     */
    private boolean isWebServerBusy(final WebServer webServer) {
        return  webServerReachableStateMap.get(webServer.getId()) == WebServerReachableState.WS_START_SENT ||
                webServerReachableStateMap.get(webServer.getId()) == WebServerReachableState.WS_STOP_SENT;
    }

    /**
     * Sets the web server state if the web server is not starting or stopping.
     * @param webServer the web server
     * @param webServerReachableState {@link com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState}
     * @param msg a message
     */
    private void setState(final WebServer webServer, final WebServerReachableState webServerReachableState, final String msg) {
        if (!isWebServerBusy(webServer)) {
            boolean stateAndOrMsgChanged = false;
            if (!webServerLastPersistedStateMap.containsKey(webServer.getId()) ||
                !webServerLastPersistedStateMap.get(webServer.getId()).equals(webServerReachableState)) {
                    webServerLastPersistedStateMap.put(webServer.getId(), webServerReachableState);
                    stateAndOrMsgChanged = true;
            }

            if (StringUtils.isNotEmpty(msg) && (!webServerLastPersistedErrorStatusMap.containsKey(webServer.getId()) ||
                !webServerLastPersistedErrorStatusMap.get(webServer.getId()).equals(msg))) {
                    webServerLastPersistedErrorStatusMap.put(webServer.getId(), msg);
                    stateAndOrMsgChanged = true;
            }

            if (stateAndOrMsgChanged) {
                webServerService.updateState(webServer.getId(), webServerReachableState, msg);
                stateNotificationService.notifyStateUpdated(new WebServerState(webServer.getId(), webServerReachableState,
                        DateTime.now()));
                grpStateComputationAndNotificationSvc.computeAndNotify(webServer.getId(), webServerReachableState);
            }
        }
    }

    /**
     * Sets the web server state.
     * @param id the web server id {@link com.siemens.cto.aem.common.domain.model.id.Identifier}
     * @param state the state {@link com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState}
     * @param msg a message
     * @return {@link SetStateRequest}
     */
    private SetStateRequest<WebServer, WebServerReachableState> createStateCommand(final Identifier<WebServer> id,
                                                                                   final WebServerReachableState state,
                                                                                   final String msg) {
            if (StringUtils.isEmpty(msg)) {
                return new WebServerSetStateRequest(new CurrentState<>(id,
                                                                       state,
                                                                       DateTime.now(),
                                                                       StateType.WEB_SERVER));
            }
            return new WebServerSetStateRequest(new CurrentState<>(id,
                                                    state,
                                                    DateTime.now(),
                                                    StateType.WEB_SERVER,
                                                    msg));
    }

    public void setWebServerReachableStateMap(Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap) {
        this.webServerReachableStateMap = webServerReachableStateMap;
    }

    public void setWebServerService(WebServerService webServerService) {
        this.webServerService = webServerService;
    }

    public void setStateNotificationService(StateNotificationService stateNotificationService) {
        this.stateNotificationService = stateNotificationService;
    }

    public void setGrpStateComputationAndNotificationSvc(GrpStateComputationAndNotificationSvc grpStateComputationAndNotificationSvc) {
        this.grpStateComputationAndNotificationSvc = grpStateComputationAndNotificationSvc;
    }

}
