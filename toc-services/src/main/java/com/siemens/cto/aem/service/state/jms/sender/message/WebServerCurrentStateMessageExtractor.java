package com.siemens.cto.aem.service.state.jms.sender.message;

import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.state.message.CommonStateKey;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;

import javax.jms.JMSException;
import javax.jms.MapMessage;

public class WebServerCurrentStateMessageExtractor extends AbstractCurrentStateMessageExtractor<WebServerReachableState> {

    public WebServerCurrentStateMessageExtractor() {
        super(StateType.WEB_SERVER);
    }

    @Override
    WebServerReachableState getState(final MapMessage aMessage) throws JMSException {
        return WebServerReachableState.convertFrom(aMessage.getString(CommonStateKey.STATE.getKey()));
    }
}