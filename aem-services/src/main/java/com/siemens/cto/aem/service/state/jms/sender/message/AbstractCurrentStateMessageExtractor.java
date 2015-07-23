package com.siemens.cto.aem.service.state.jms.sender.message;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.jms.JMSException;
import javax.jms.MapMessage;

public abstract class AbstractCurrentStateMessageExtractor<T extends OperationalState> implements CurrentStateMessageExtractor {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

    private final StateType stateType;

    protected AbstractCurrentStateMessageExtractor(final StateType theStateType) {
        stateType = theStateType;
    }

    public CurrentState extract(final MapMessage aMessage) throws JMSException {
        final String stateMessage = getStateMessage(aMessage);
        if (stateMessage == null) {
            return new CurrentState<>(getId(aMessage),
                                      getState(aMessage),
                                      getAsOf(aMessage),
                                      stateType);
        } else {
            return new CurrentState<>(getId(aMessage),
                                      getState(aMessage),
                                      getAsOf(aMessage),
                                      stateType,
                                      stateMessage);
        }
    }

    Identifier<?> getId(final MapMessage aMessage) throws JMSException {
        return new Identifier<>(aMessage.getString(CommonStateKey.ID.getKey()));
    }

    DateTime getAsOf(final MapMessage aMessage) throws JMSException {
        return DATE_TIME_FORMATTER.parseDateTime(aMessage.getString(CommonStateKey.AS_OF.getKey()));
    }

    String getStateMessage(final MapMessage aMessage) throws JMSException {
        return aMessage.getString(CommonStateKey.MESSAGE.getKey());
    }

    abstract T getState(MapMessage aMessage) throws JMSException;
}
