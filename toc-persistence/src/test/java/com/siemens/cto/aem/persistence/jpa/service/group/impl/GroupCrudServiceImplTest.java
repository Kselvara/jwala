package com.siemens.cto.aem.persistence.jpa.service.group.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {GroupCrudServiceImplTest.Config.class
        })
public class GroupCrudServiceImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(GroupCrudServiceImplTest.class);
    private String groupName = "groupName";
    private Identifier<Group> groupId;

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {
        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        public WebServerCrudService getWebServerCrudService() {
            return new WebServerCrudServiceImpl();
        }
    }

    @Autowired
    GroupCrudService groupCrudService;

    @Autowired
    WebServerCrudService webServerCrudService;

    @Before
    public void setup() {
        User user = new User("testUser");
        user.addToThread();
        CreateGroupRequest createGroupRequest = new CreateGroupRequest(groupName);
        JpaGroup testGroup = groupCrudService.createGroup(createGroupRequest);
        groupId = new Identifier<>(testGroup.getId());
    }

    @After
    public void tearDown() {
        groupCrudService.removeGroup(groupId);
        User.getThreadLocalUser().invalidate();
    }

    @Test
    public void testGetGroup() {
        JpaGroup group = groupCrudService.getGroup(groupName);
        assertNotNull(group);

        try {
            groupCrudService.getGroup("group does not exist");
        } catch (NotFoundException e) {
            assertTrue(e.getMessageResponseStatus().equals(AemFaultType.GROUP_NOT_FOUND));
        }
    }

    @Test
    public void testUpdateGroupStatus() {
        CurrentState<Group, GroupState> theNewState = new CurrentState<Group, GroupState>(groupId, GroupState.GRP_STARTED, DateTime.now(), StateType.GROUP);
        SetStateRequest<Group, GroupState> setStateRequest = new SetStateRequest<Group, GroupState>(theNewState) {
            @Override
            public void validate() throws BadRequestException {
                LOGGER.debug("empty validate");
            }
        };
        JpaGroup group = groupCrudService.updateGroupStatus(setStateRequest);
        assertEquals(GroupState.GRP_STARTED, group.getState());
    }

    @Test
    public void testGetGroupId() {
        TestCase.assertEquals(groupId.getId(), groupCrudService.getGroupId(groupName));
    }

    @Test
    public void testLinkWebServer() {
        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer", "testHost",
                101, 102, new Path("./statusPath"), new FileSystemPath("./httpdConfPath"), new Path("./svrRootPath"),
                new Path("./docRoot"), WebServerReachableState.WS_UNREACHABLE, StringUtils.EMPTY);
        webServer = webServerCrudService.createWebServer(webServer, "testGroupCrud");
        groupCrudService.linkWebServer(webServer);
        JpaGroup group = groupCrudService.getGroup(groupName);
        List<JpaWebServer> wsList = new ArrayList<>();
        final JpaWebServer jpaWebServer = webServerCrudService.findById(webServer.getId().getId());
        wsList.add(jpaWebServer);
        group.setWebServers(wsList);
        List<JpaGroup> groupsList = new ArrayList<>();
        groupsList.add(group);
        jpaWebServer.setGroups(groupsList);
        groupCrudService.linkWebServer(webServerCrudService.getWebServer(new Identifier<WebServer>(jpaWebServer.getId())));
    }

    @Test
    public void testUploadGroupJvmTemplate() throws FileNotFoundException {
        FileInputStream dataInputStream = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        Jvm jvm = new Jvm(new Identifier<Jvm>(1212L), "testJvm", new HashSet<Group>());
        UploadJvmTemplateRequest uploadJvmTemplateRequest = new UploadJvmTemplateRequest(jvm, "ServerXMLTemplate.tpl",
                dataInputStream, StringUtils.EMPTY) {
            @Override
            public String getConfFileName() {
                return "server.xml";
            }
        };
        groupCrudService.uploadGroupJvmTemplate(uploadJvmTemplateRequest, groupCrudService.getGroup(groupName));
        // twice is nice :)
        groupCrudService.uploadGroupJvmTemplate(uploadJvmTemplateRequest, groupCrudService.getGroup(groupName));
    }

    @Test
    public void testUploadGroupWebServerTemplate() throws FileNotFoundException {
        InputStream dataInputStream = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        WebServer webServer = new WebServer(new Identifier<WebServer>(1313L), new HashSet<Group>(), "testWebServer");
        UploadWebServerTemplateRequest uploadWsTemplateRequest = new UploadWebServerTemplateRequest(webServer,
                "HttpdSslConfTemplate.tpl", dataInputStream, StringUtils.EMPTY) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        groupCrudService.uploadGroupWebServerTemplate(uploadWsTemplateRequest, groupCrudService.getGroup(groupName));
        // twice is ... ok I guess
        groupCrudService.uploadGroupWebServerTemplate(uploadWsTemplateRequest, groupCrudService.getGroup(groupName));
    }

    @Test
    public void testGetGroupJvmTemplateNames() {
        groupCrudService.getGroupJvmsResourceTemplateNames(groupName);
    }

    @Test
    public void testGetGroupWebServerTemplateNames() {
        groupCrudService.getGroupWebServersResourceTemplateNames(groupName);
    }

    @Test(expected = ResourceTemplateUpdateException.class)
    public void testUpdateJvmResourceTemplateThrowsException() {
        // no template exists yet so throw the exception
        groupCrudService.updateGroupJvmResourceTemplate(groupName, "server.xml", "new content");
    }

    @Test
    @Ignore
    // TODO: Test is failing because of null meta data!
    public void testUpdateJvmResourceTemplate() throws FileNotFoundException {
        testUploadGroupJvmTemplate();
        groupCrudService.updateGroupJvmResourceTemplate(groupName, "server.xml", "new content");
        final String groupJvmResourceTemplate = groupCrudService.getGroupJvmResourceTemplate(groupName, "server.xml");
        assertEquals("new content", groupJvmResourceTemplate);
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetGroupJvmResourceTemplateThrowsException() {
        groupCrudService.getGroupJvmResourceTemplate(groupName, "NOTME");
    }

    @Test(expected = ResourceTemplateUpdateException.class)
    public void testUpdateWebServerResourceTemplateThrowsException() {
        // no template exists so throw exception
        groupCrudService.updateGroupWebServerResourceTemplate(groupName, "httpd.conf", "new httpd content");
    }

    @Test
    @Ignore
    // TODO: Test is failing because of null meta data!
    public void testUpdateWebServerResourceTemplate() throws FileNotFoundException {
        testUploadGroupWebServerTemplate();
        groupCrudService.updateGroupWebServerResourceTemplate(groupName, "httpd.conf", "new httpd content");
        final String groupWebServerResourceTemplate = groupCrudService.getGroupWebServerResourceTemplate(groupName, "httpd.conf");
        assertEquals("new httpd content", groupWebServerResourceTemplate);
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetGroupWebServerTemplateThrowsException() {
        groupCrudService.getGroupWebServerResourceTemplate(groupName, "UHUHUH-youdidntsaythemagicword");
    }

    @Test
    @Ignore
    // TODO: Test is failing because of null meta data!
    public void testPopulateGroupAppTemplate() {
        final JpaGroup group = groupCrudService.getGroup(groupName);
        groupCrudService.populateGroupAppTemplate(group, "app.xml", "content!");
        groupCrudService.populateGroupAppTemplate(group, "app.xml", "content new!");
    }

    @Test(expected = ResourceTemplateUpdateException.class)
    public void testUpdateGroupAppTemplateThrowsException() {
        groupCrudService.updateGroupAppResourceTemplate(groupName, "hct.xml", "new hct.xml template");
    }

    @Test
    @Ignore
    // TODO: Test is failing because of null meta data!
    public void testUpdateGroupAppTemplate() {
        groupCrudService.populateGroupAppTemplate(groupCrudService.getGroup(groupName), "hct.xml", "old hct.xml template");
        String appTemplateContent = groupCrudService.getGroupAppResourceTemplate(groupName, "hct.xml");
        assertEquals("old hct.xml template", appTemplateContent);

        groupCrudService.updateGroupAppResourceTemplate(groupName, "hct.xml", "new hct.xml template");
        appTemplateContent = groupCrudService.getGroupAppResourceTemplate(groupName, "hct.xml");
        assertEquals("new hct.xml template", appTemplateContent);
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetGroupAppResourceTemplateThrowsException() {
        groupCrudService.getGroupAppResourceTemplate("noSuchGroup", "noSuchTemplate");
    }

    @Test
    public void testGetGroupAppResourceTemplateNames() {
        List<String> templateNames = groupCrudService.getGroupAppsResourceTemplateNames(groupName);
        assertEquals(0, templateNames.size());
    }

    @Test
    public void testUpdateState() {
        groupCrudService.updateState(groupId, GroupState.GRP_PARTIAL);
    }
}