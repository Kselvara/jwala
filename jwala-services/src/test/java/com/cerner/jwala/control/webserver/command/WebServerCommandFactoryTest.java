package com.cerner.jwala.control.webserver.command;

import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created on 3/14/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {WebServerCommandFactoryTest.Config.class})
public class WebServerCommandFactoryTest {

    @Autowired
    WebServerCommandFactory webServerCommandFactory;

    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        reset(
                Config.mockBinaryDistributionControlService,
                Config.mockRemoteCommandExecutorService,
                Config.mockSshConfig
        );
    }

    @Test
    public void testExecuteStartScript() {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("web-server-name");
        when(mockWebServer.getHost()).thenReturn("web-server-host");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist to force mkdir and scp", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully created directory", ""));
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully copied script", ""));
        when(Config.mockBinaryDistributionControlService.changeFileMode(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully changed the file mode", ""));

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Start command succeeded", ""));

        RemoteCommandReturnInfo startResult = webServerCommandFactory.executeCommand(mockWebServer, WebServerControlOperation.START);

        assertEquals(0, startResult.retCode);
    }

    @Test
    public void testExecuteStopScript() {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("web-server-name");
        when(mockWebServer.getHost()).thenReturn("web-server-host");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist to force mkdir and scp", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully created directory", ""));
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully copied script", ""));
        when(Config.mockBinaryDistributionControlService.changeFileMode(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully changed the file mode", ""));

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Stop command succeeded", ""));

        RemoteCommandReturnInfo startResult = webServerCommandFactory.executeCommand(mockWebServer, WebServerControlOperation.STOP);

        assertEquals(0, startResult.retCode);
    }

    @Test
    public void testExecuteDeleteScript() {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("web-server-name");
        when(mockWebServer.getHost()).thenReturn("web-server-host");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist to force mkdir and scp", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully created directory", ""));
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully copied script", ""));
        when(Config.mockBinaryDistributionControlService.changeFileMode(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully changed the file mode", ""));

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Delete command succeeded", ""));

        RemoteCommandReturnInfo startResult = webServerCommandFactory.executeCommand(mockWebServer, WebServerControlOperation.DELETE_SERVICE);

        assertEquals(0, startResult.retCode);
    }

    @Test
    public void testExecuteInstallScript() {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("web-server-name");
        when(mockWebServer.getHost()).thenReturn("web-server-host");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist to force mkdir and scp", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully created directory", ""));
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully copied script", ""));
        when(Config.mockBinaryDistributionControlService.changeFileMode(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully changed the file mode", ""));

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Install command succeeded", ""));

        RemoteCommandReturnInfo startResult = webServerCommandFactory.executeCommand(mockWebServer, WebServerControlOperation.INSTALL_SERVICE);

        assertEquals(0, startResult.retCode);
    }

    @Test
    public void testExecuteViewHttpdConf() {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("web-server-name");
        when(mockWebServer.getHost()).thenReturn("web-server-host");

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "View HTTPD conf command succeeded", ""));

        RemoteCommandReturnInfo startResult = webServerCommandFactory.executeCommand(mockWebServer, WebServerControlOperation.VIEW_HTTP_CONFIG_FILE);

        assertEquals(0, startResult.retCode);
    }

    @Test(expected = WebServerServiceException.class)
    public void testExecuteUnsupportedAction() {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("web-server-name");
        when(mockWebServer.getHost()).thenReturn("web-server-host");

        // currently BACK_UP is not a  supported Web Server command
        RemoteCommandReturnInfo startResult = webServerCommandFactory.executeCommand(mockWebServer, WebServerControlOperation.BACK_UP);

        assertEquals(0, startResult.retCode);
    }

    @Test
    public void testExecuteInstallScriptForAlreadyExistingScript() {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("web-server-name");
        when(mockWebServer.getHost()).thenReturn("web-server-host");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists - just run the script", ""));

        when(Config.mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Install command succeeded", ""));

        RemoteCommandReturnInfo startResult = webServerCommandFactory.executeCommand(mockWebServer, WebServerControlOperation.INSTALL_SERVICE);

        assertEquals(0, startResult.retCode);
        verify(Config.mockBinaryDistributionControlService, never()).createDirectory(anyString(), anyString());
        verify(Config.mockBinaryDistributionControlService, never()).secureCopyFile(anyString(), anyString(), anyString());
        verify(Config.mockBinaryDistributionControlService, never()).changeFileMode(anyString(), anyString(), anyString(), anyString());
    }

    @Test(expected = WebServerServiceException.class)
    public void testExecuteInstallScriptFailsCreatingDirectory() {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("web-server-name");
        when(mockWebServer.getHost()).thenReturn("web-server-host");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist to force create directory", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Failed to create directory"));


        RemoteCommandReturnInfo startResult = webServerCommandFactory.executeCommand(mockWebServer, WebServerControlOperation.INSTALL_SERVICE);

        assertEquals(0, startResult.retCode);
        verify(Config.mockRemoteCommandExecutorService, never()).executeCommand(any(RemoteExecCommand.class));
        verify(Config.mockBinaryDistributionControlService, never()).secureCopyFile(anyString(), anyString(), anyString());
        verify(Config.mockBinaryDistributionControlService, never()).changeFileMode(anyString(), anyString(), anyString(), anyString());
    }

    @Test(expected = WebServerServiceException.class)
    public void testExecuteInstallScriptFailsSecureCopy() {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("web-server-name");
        when(mockWebServer.getHost()).thenReturn("web-server-host");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist to force create directory", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully created directory", ""));
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Failed secure copy"));

        RemoteCommandReturnInfo startResult = webServerCommandFactory.executeCommand(mockWebServer, WebServerControlOperation.INSTALL_SERVICE);

        assertEquals(0, startResult.retCode);
        verify(Config.mockRemoteCommandExecutorService, never()).executeCommand(any(RemoteExecCommand.class));
        verify(Config.mockBinaryDistributionControlService, never()).changeFileMode(anyString(), anyString(), anyString(), anyString());
    }

    @Test(expected = WebServerServiceException.class)
    public void testExecuteInstallScriptFailsFileChange() {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("web-server-name");
        when(mockWebServer.getHost()).thenReturn("web-server-host");

        when(Config.mockBinaryDistributionControlService.checkFileExists(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist to force create directory", ""));
        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully created directory", ""));
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Successfully copied script", ""));
        when(Config.mockBinaryDistributionControlService.changeFileMode(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Failed to change file mode to executable"));

        RemoteCommandReturnInfo startResult = webServerCommandFactory.executeCommand(mockWebServer, WebServerControlOperation.INSTALL_SERVICE);

        assertEquals(0, startResult.retCode);
        verify(Config.mockRemoteCommandExecutorService, never()).executeCommand(any(RemoteExecCommand.class));
    }

    static class Config {
        private static final SshConfiguration mockSshConfig = mock(SshConfiguration.class);
        private static final RemoteCommandExecutorService mockRemoteCommandExecutorService = mock(RemoteCommandExecutorService.class);
        private static final BinaryDistributionControlService mockBinaryDistributionControlService = mock(BinaryDistributionControlService.class);

        @Bean
        public SshConfiguration getSshConfig() {
            return mockSshConfig;
        }

        @Bean
        public RemoteCommandExecutorService getRemoteCommandExecutorService() {
            return mockRemoteCommandExecutorService;
        }

        @Bean
        public BinaryDistributionControlService getBinaryDistributionControlService() {
            return mockBinaryDistributionControlService;
        }

        @Bean
        public WebServerCommandFactory getWebServerCommandFactory() {
            return new WebServerCommandFactory();
        }
    }
}
