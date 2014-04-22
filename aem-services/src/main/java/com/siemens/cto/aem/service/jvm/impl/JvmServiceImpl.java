package com.siemens.cto.aem.service.jvm.impl;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmNameRule;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.jvm.JvmService;

public class JvmServiceImpl implements JvmService {

    private JvmPersistenceService jvmPersistenceService;
    private GroupService groupService;

    public JvmServiceImpl(final JvmPersistenceService theJvmPersistenceService,
                          final GroupService theGroupService) {
        jvmPersistenceService = theJvmPersistenceService;
        groupService = theGroupService;
    }

    @Override
    @Transactional
    public Jvm createJvm(final CreateJvmCommand aCreateJvmCommand,
                         final User aCreatingUser) {

        aCreateJvmCommand.validateCommand();

        final Event<CreateJvmCommand> event = new Event<>(aCreateJvmCommand,
                                                          AuditEvent.now(aCreatingUser));

        return jvmPersistenceService.createJvm(event);
    }

    @Override
    @Transactional
    public Jvm createAndAssignJvm(final CreateJvmAndAddToGroupsCommand aCreateAndAssignCommand,
                                  final User aCreatingUser) {

        //The commands are validated in createJvm() and groupService.addJvmToGroup()

        final Jvm newJvm = createJvm(aCreateAndAssignCommand.getCreateCommand(),
                                     aCreatingUser);

        final Set<AddJvmToGroupCommand> addCommands = aCreateAndAssignCommand.toAddCommandsFor(newJvm.getId());
        addJvmToGroups(addCommands,
                       aCreatingUser);

        return getJvm(newJvm.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Jvm getJvm(final Identifier<Jvm> aJvmId) {

        return jvmPersistenceService.getJvm(aJvmId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> getJvms(final PaginationParameter aPaginationParam) {

        return jvmPersistenceService.getJvms(aPaginationParam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> findJvms(final String aJvmNameFragment,
                              final PaginationParameter aPaginationParam) {

        new JvmNameRule(aJvmNameFragment).validate();
        return jvmPersistenceService.findJvms(aJvmNameFragment,
                                              aPaginationParam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> findJvms(final Identifier<Group> aGroupId,
                              final PaginationParameter aPaginationParam) {

        return jvmPersistenceService.findJvmsBelongingTo(aGroupId,
                                                         aPaginationParam);

    }

    @Override
    @Transactional
    public Jvm updateJvm(final UpdateJvmCommand anUpdateJvmCommand,
                         final User anUpdatingUser) {

        anUpdateJvmCommand.validateCommand();

        final Event<UpdateJvmCommand> event = new Event<>(anUpdateJvmCommand,
                                                          AuditEvent.now(anUpdatingUser));

        jvmPersistenceService.removeJvmFromGroups(anUpdateJvmCommand.getId());

        addJvmToGroups(anUpdateJvmCommand.getAssignmentCommands(),
                       anUpdatingUser);

        return jvmPersistenceService.updateJvm(event);
    }

    @Override
    @Transactional
    public void removeJvm(final Identifier<Jvm> aJvmId) {
        jvmPersistenceService.removeJvm(aJvmId);
    }

    protected void addJvmToGroups(final Set<AddJvmToGroupCommand> someAddCommands,
                                  final User anAddingUser) {
        for (final AddJvmToGroupCommand command : someAddCommands) {
            groupService.addJvmToGroup(command,
                                       anAddingUser);
        }
    }
}
