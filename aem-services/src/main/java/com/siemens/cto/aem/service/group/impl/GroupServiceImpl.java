package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.*;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.rule.group.GroupNameRule;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class GroupServiceImpl implements GroupService {

    private final StateNotificationGateway stateNotificationGateway;
    
    private final GroupPersistenceService groupPersistenceService;

    public GroupServiceImpl(final GroupPersistenceService theGroupPersistenceService,
            final StateNotificationGateway theStateNotificationGateway) {
        groupPersistenceService = theGroupPersistenceService;
        stateNotificationGateway = theStateNotificationGateway;
    }

    @Override
    @Transactional
    public Group createGroup(final CreateGroupCommand aCreateGroupCommand,
                             final User aCreatingUser) {

        aCreateGroupCommand.validateCommand();

        return groupPersistenceService.createGroup(createEvent(aCreateGroupCommand,
                                                               aCreatingUser));
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroup(final Identifier<Group> aGroupId) {
        return groupPersistenceService.getGroup(aGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroup(final String name) {
        return groupPersistenceService.getGroup(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroups() {
        return groupPersistenceService.getGroups();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> findGroups(final String aGroupNameFragment) {

        new GroupNameRule(aGroupNameFragment).validate();
        return groupPersistenceService.findGroups(aGroupNameFragment);
    }

    @Override
    @Transactional
    public Group updateGroup(final UpdateGroupCommand anUpdateGroupCommand,
                             final User anUpdatingUser) {

        anUpdateGroupCommand.validateCommand();
        Group group = groupPersistenceService.updateGroup(
                createEvent(anUpdateGroupCommand, anUpdatingUser));
        
        stateNotificationGateway.groupStateUpdateRequest(group);
        return group;
    }

    @Override
    @Transactional
    public void removeGroup(final Identifier<Group> aGroupId) {
        groupPersistenceService.removeGroup(aGroupId);
    }

    @Override
    @Transactional
    public Group addJvmToGroup(final AddJvmToGroupCommand aCommand,
                               final User anAddingUser) {

        aCommand.validateCommand();
        Group group = groupPersistenceService.addJvmToGroup(createEvent(aCommand,
                                                                 anAddingUser));
        stateNotificationGateway.groupStateUpdateRequest(group);
        return group;
    }

    @Override
    @Transactional
    public Group addJvmsToGroup(final AddJvmsToGroupCommand aCommand,
                                final User anAddingUser) {

        aCommand.validateCommand();
        for (final AddJvmToGroupCommand command : aCommand.toCommands()) {
            addJvmToGroup(command,
                          anAddingUser);
        }

        Group group = getGroup(aCommand.getGroupId());

        stateNotificationGateway.groupStateUpdateRequest(group);
        return group;
    }

    @Override
    @Transactional
    public Group removeJvmFromGroup(final RemoveJvmFromGroupCommand aCommand,
                                    final User aRemovingUser) {

        aCommand.validateCommand();
        Group group = groupPersistenceService.removeJvmFromGroup(createEvent(aCommand,
                                                                      aRemovingUser));
        stateNotificationGateway.groupStateUpdateRequest(group);
        return group;
    }

    @Override
    @Transactional
    public List<Jvm> getOtherGroupingDetailsOfJvms(Identifier<Group> id) {
        final List<Jvm> otherGroupConnectionDetails = new LinkedList<>();
        final Group group = groupPersistenceService.getGroup(id, false);
        final Set<Jvm> jvms = group.getJvms();

        for (Jvm jvm : jvms) {
            final Set<LiteGroup> tmpGroup = new LinkedHashSet<>();
            if (jvm.getGroups() != null && !jvm.getGroups().isEmpty()) {
                for (LiteGroup liteGroup : jvm.getGroups()) {
                    if (!id.getId().equals(liteGroup.getId().getId())) {
                        tmpGroup.add(liteGroup);
                    }
                }
                if (!tmpGroup.isEmpty()) {
                    otherGroupConnectionDetails.add(new Jvm(jvm.getId(), jvm.getJvmName(), tmpGroup));
                }
            }
        }
        return otherGroupConnectionDetails;
    }

    @Override
    @Transactional
    public List<WebServer> getOtherGroupingDetailsOfWebServers(Identifier<Group> id) {
        final List<WebServer> otherGroupConnectionDetails = new ArrayList<>();
        final Group group = groupPersistenceService.getGroup(id, true);
        final Set<WebServer> webServers = group.getWebServers();

        for (WebServer webServer: webServers) {
            final Set<Group> tmpGroup = new LinkedHashSet<>();
            if (webServer.getGroups() != null && !webServer.getGroups().isEmpty()) {
                for (Group webServerGroup : webServer.getGroups()) {
                    if (!id.getId().equals(webServerGroup.getId().getId())) {
                        tmpGroup.add(webServerGroup);
                    }
                }
                if (!tmpGroup.isEmpty()) {
                    otherGroupConnectionDetails.add(new WebServer(webServer.getId(),
                                                        webServer.getGroups(),
                                                        webServer.getName()));
                }
            }
        }

        return otherGroupConnectionDetails;
    }

    protected <T> Event<T> createEvent(final T aCommand,
                                       final User aUser) {
        return new Event<T>(aCommand,
                            AuditEvent.now(aUser));
    }
}
