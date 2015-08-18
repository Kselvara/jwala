package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import com.siemens.cto.aem.domain.model.group.*;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.model.webserver.command.ControlGroupWebServerCommand;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.GroupIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.provider.NameSearchParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupChildType;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.group.MembershipDetails;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GroupServiceRestImpl implements GroupServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceRestImpl.class);

    private final GroupService groupService;

    @Autowired
    private GroupControlService groupControlService;

    @Autowired
    private GroupJvmControlService groupJvmControlService;

    @Autowired
    private GroupWebServerControlService groupWebServerControlService;

    @Autowired
    @Qualifier("groupStateService")
    private StateService<Group, GroupState> groupStateService;

    public GroupServiceRestImpl(final GroupService theGroupService) {
        groupService = theGroupService;
    }

    @Override
    public Response getGroups(final NameSearchParameterProvider aGroupNameSearch, final boolean fetchWebServers) {
        LOGGER.debug("Get Groups requested with search: {}", aGroupNameSearch.getName());

        final List<Group> groups;
        if (aGroupNameSearch.isNamePresent()) {
            groups = groupService.findGroups(aGroupNameSearch.getName());
        } else {
            groups = groupService.getGroups(fetchWebServers);
        }

        return ResponseBuilder.ok(groups);
    }

    @Override
    public Response getGroup(final Identifier<Group> aGroupId) {
        LOGGER.debug("Get Group requested: {}", aGroupId);
        return ResponseBuilder.ok(groupService.getGroup(aGroupId));
    }

    @Override
    public Response createGroup(final String aNewGroupName,
                                final AuthenticatedUser aUser) {
        LOGGER.debug("Create Group requested: {}", aNewGroupName);
        return ResponseBuilder.created(groupService.createGroup(new CreateGroupCommand(aNewGroupName),
                                                                aUser.getUser()));
    }

    @Override
    public Response updateGroup(final JsonUpdateGroup anUpdatedGroup,
                                final AuthenticatedUser aUser) {
        LOGGER.debug("Update Group requested: {}", anUpdatedGroup);
        return ResponseBuilder.ok(groupService.updateGroup(anUpdatedGroup.toUpdateGroupCommand(),
                                                           aUser.getUser()));
    }

    @Override
    public Response removeGroup(final Identifier<Group> aGroupId) {
        LOGGER.debug("Delete Group requested: {}", aGroupId);
        groupService.removeGroup(aGroupId);
        return ResponseBuilder.ok();
    }

    @Override
    public Response removeJvmFromGroup(final Identifier<Group> aGroupId,
                                       final Identifier<Jvm> aJvmId,
                                       final AuthenticatedUser aUser) {
        LOGGER.debug("Remove JVM from Group requested: {}, {}", aGroupId, aJvmId);
        return ResponseBuilder.ok(groupService.removeJvmFromGroup(new RemoveJvmFromGroupCommand(aGroupId,
                                                                                                aJvmId),
                                                                  aUser.getUser()));
    }

    @Override
    public Response addJvmsToGroup(final Identifier<Group> aGroupId,
                                   final JsonJvms someJvmsToAdd,
                                   final AuthenticatedUser aUser) {
        LOGGER.debug("Add JVM to Group requested: {}, {}", aGroupId, someJvmsToAdd);
        final AddJvmsToGroupCommand command = someJvmsToAdd.toCommand(aGroupId);
        return ResponseBuilder.ok(groupService.addJvmsToGroup(command,
                                                              aUser.getUser()));
    }

    @Override
    public Response controlGroupJvms(final Identifier<Group> aGroupId,
                                     final JsonControlJvm jsonControlJvm,
                                     final AuthenticatedUser aUser) {
        LOGGER.debug("Control all JVMs in Group requested: {}, {}", aGroupId, jsonControlJvm);
        final JvmControlOperation command = jsonControlJvm.toControlOperation();
        final ControlGroupJvmCommand grpCommand = new ControlGroupJvmCommand(aGroupId,
                                                                             JvmControlOperation.convertFrom(command.getExternalValue()));
        return ResponseBuilder.ok(groupJvmControlService.controlGroup(grpCommand,
                                                                      aUser.getUser()));
    }

    @Override
    public Response controlGroupWebservers(final Identifier<Group> aGroupId,
                                           final JsonControlWebServer jsonControlWebServer,
                                           final AuthenticatedUser aUser) {
        LOGGER.debug("Control all WebServers in Group requested: {}, {}", aGroupId, jsonControlWebServer);
        final WebServerControlOperation command = jsonControlWebServer.toControlOperation();
        final ControlGroupWebServerCommand grpCommand = new ControlGroupWebServerCommand(aGroupId,
                WebServerControlOperation.convertFrom(command.getExternalValue()) );
        return ResponseBuilder.ok(groupWebServerControlService.controlGroup(grpCommand,
                                                                            aUser.getUser()));
    }

    @Override
    public Response controlGroup(final Identifier<Group> aGroupId,
                                 final JsonControlGroup jsonControlGroup,
                                 final AuthenticatedUser aUser) {

        GroupControlOperation groupControlOperation = jsonControlGroup.toControlOperation();
        LOGGER.debug("starting control group {} with operation {}", aGroupId, groupControlOperation);

        ControlGroupCommand grpCommand = new ControlGroupCommand(aGroupId, groupControlOperation);
        return ResponseBuilder.ok(groupControlService.controlGroup(grpCommand,
                                                                   aUser.getUser()));
    }

    @Override
    public Response resetState(final Identifier<Group> aGroupId,
                               final AuthenticatedUser aUser) {
        return ResponseBuilder.ok(groupControlService.resetState(aGroupId,
                                                                 aUser.getUser()));
    }

    @Override
    public Response getCurrentJvmStates(final GroupIdsParameterProvider aGroupIdsParameterProvider) {
        LOGGER.debug("Current Group states requested : {}", aGroupIdsParameterProvider);
        final Set<Identifier<Group>> groupIds = aGroupIdsParameterProvider.valueOf();
        final Set<CurrentState<Group, GroupState>> currentGroupStates;

        if (groupIds.isEmpty()) {
            currentGroupStates = groupStateService.getCurrentStates();
        } else {
            currentGroupStates = groupStateService.getCurrentStates(groupIds);
        }

        return ResponseBuilder.ok(currentGroupStates);
    }

    private List<MembershipDetails> createMembershipDetailsFromJvms(final List<Jvm> jvms) {
        final List<MembershipDetails> membershipDetailsList = new LinkedList<>();
        for (Jvm jvm : jvms) {
            final List<String> groupNames = new LinkedList<>();
            for (LiteGroup group: jvm.getGroups()) {
                groupNames.add(group.getName());
            }
            membershipDetailsList.add(new MembershipDetails(jvm.getJvmName(),
                    GroupChildType.JVM,
                    groupNames));
        }
        return membershipDetailsList;
    }

    private List<MembershipDetails> createMembershipDetailsFromWebServers(final List<WebServer> webServers) {
        final List<MembershipDetails> membershipDetailsList = new LinkedList<>();
        for (WebServer webServer : webServers) {
            final List<String> groupNames = new LinkedList<>();
            for (Group group: webServer.getGroups()) {
                groupNames.add(group.getName());
            }
            membershipDetailsList.add(new MembershipDetails(webServer.getName(),
                                          GroupChildType.WEB_SERVER,
                                          groupNames));
        }
        return membershipDetailsList;
    }

    @Override
    public Response getOtherGroupMembershipDetailsOfTheChildren(final Identifier<Group> id,
                                                                final GroupChildType groupChildType) {
        final List<Jvm> jvmGroupingDetails;
        final List<WebServer> webServerGroupingDetails;

        if (groupChildType != null) {
            if (groupChildType == GroupChildType.JVM) {
                jvmGroupingDetails = groupService.getOtherGroupingDetailsOfJvms(id);
                return ResponseBuilder.ok(createMembershipDetailsFromJvms(jvmGroupingDetails));
            } else if (groupChildType == GroupChildType.WEB_SERVER) {
                webServerGroupingDetails = groupService.getOtherGroupingDetailsOfWebServers(id);
                return ResponseBuilder.ok(createMembershipDetailsFromWebServers(webServerGroupingDetails));
            }
        }

        jvmGroupingDetails = groupService.getOtherGroupingDetailsOfJvms(id);
        final List<MembershipDetails> membershipDetailsList = createMembershipDetailsFromJvms(jvmGroupingDetails);
        webServerGroupingDetails = groupService.getOtherGroupingDetailsOfWebServers(id);
        membershipDetailsList.addAll(createMembershipDetailsFromWebServers(webServerGroupingDetails));

        return ResponseBuilder.ok(membershipDetailsList);
    }

}
