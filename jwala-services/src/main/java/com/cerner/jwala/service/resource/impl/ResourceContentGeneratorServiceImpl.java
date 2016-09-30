package com.cerner.jwala.service.resource.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.template.ResourceFileGenerator;
import com.cerner.jwala.template.exception.ResourceFileGeneratorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Implement {@link ResourceContentGeneratorService}
 * <p>
 * Created by JC043760 on 7/26/2016.
 */
@Service
public class ResourceContentGeneratorServiceImpl implements ResourceContentGeneratorService {

    private final GroupPersistenceService groupPersistenceService;
    private final WebServerPersistenceService webServerPersistenceService;
    private final JvmPersistenceService jvmPersistenceService;
    private final ApplicationPersistenceService applicationPersistenceService;
    private final HistoryService historyService;

    @Autowired
    public ResourceContentGeneratorServiceImpl(final GroupPersistenceService groupPersistenceService,
                                               final WebServerPersistenceService webServerPersistenceService,
                                               final JvmPersistenceService jvmPersistenceService,
                                               final ApplicationPersistenceService applicationPersistenceService,
                                               final HistoryService historyService) {
        this.groupPersistenceService = groupPersistenceService;
        this.webServerPersistenceService = webServerPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.applicationPersistenceService = applicationPersistenceService;
        this.historyService = historyService;
    }

    @Override
    public <T> String generateContent(final String fileName, final String template, final ResourceGroup resourceGroup, final T entity) {
        try {
            return ResourceFileGenerator.generateResourceConfig(fileName, template, null == resourceGroup ? generateResourceGroup() : resourceGroup, entity);
        } catch (ResourceFileGeneratorException e) {
            historyService.createHistory("", resourceGroup.getGroups(), e.getMessage(), EventType.USER_ACTION, SecurityContextHolder.getContext().getAuthentication().getName());
            throw new ResourceFileGeneratorException(e.getMessage(), e);
        }
    }

    /**
     * Create pertinent data to pass to the template generator engine
     *
     * @return {@link ResourceGroup}
     */
    private ResourceGroup generateResourceGroup() {
        final List<Group> groups = groupPersistenceService.getGroups();
        List<Group> groupsToBeAdded = null;

        for (Group group : groups) {
            if (groupsToBeAdded == null) {
                groupsToBeAdded = new ArrayList<>(groups.size());
            }
            final List<Jvm> jvms = jvmPersistenceService.getJvmsAndWebAppsByGroupName(group.getName());
            final List<WebServer> webServers = webServerPersistenceService.getWebServersByGroupName(group.getName());
            final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(group.getName());
            groupsToBeAdded.add(new Group(group.getId(),
                    group.getName(),
                    null != jvms ? new LinkedHashSet<>(jvms) : new LinkedHashSet<Jvm>(),
                    null != webServers ? new LinkedHashSet<>(webServers) : new LinkedHashSet<WebServer>(),
                    group.getCurrentState(),
                    group.getHistory(),
                    null != applications ? new LinkedHashSet<>(applications) : new LinkedHashSet<Application>()));
        }
        return new ResourceGroup(groupsToBeAdded);
    }
}