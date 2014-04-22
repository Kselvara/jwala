package com.siemens.cto.aem.persistence.service.jvm;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;

public interface JvmControlPersistenceService {

    JvmControlHistory addControlHistoryEvent(final Event<ControlJvmCommand> anEvent);
}
