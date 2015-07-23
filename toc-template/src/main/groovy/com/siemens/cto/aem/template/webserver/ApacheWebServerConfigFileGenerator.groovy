package com.siemens.cto.aem.template.webserver

import com.siemens.cto.aem.domain.model.app.Application
import com.siemens.cto.aem.domain.model.jvm.Jvm
import com.siemens.cto.aem.domain.model.webserver.WebServer

import static com.siemens.cto.aem.template.GeneratorUtils.bindDataToTemplate


/**
 * Wrapper that contains methods that generates configuration files for an Apache Web Server
 *
 * Created by Z003BPEJ on 6/20/14.
 */
public class ApacheWebServerConfigFileGenerator {

    private ApacheWebServerConfigFileGenerator() {}

    /**
     * Generate httpd.conf content
     * @param templateFileName the template file name
     * @param apps the applications
     * @return generated httpd.conf content
     */
    public static String getHttpdConf(final String webServerName,
                                      final String templateFileName,
                                      final WebServer webServer,
                                      final List<Jvm> jvms,
                                      final List<Application> apps
                                      ) {
        final binding = [webServerName:webServerName,
                         webServer:webServer,
                         apps:apps,
                         jvms:jvms,
                         comments:""]
        return bindDataToTemplate(binding, templateFileName).toString()
    }

    /**
     * Generate workers.properties content
     * @param templateFileName the template file name
     * @param apps the applications
     * @return generated worker.properties content
     */
    public static String getWorkersProperties(final String webServerName,
                                              final String templateFileName,
                                              final List<Jvm> jvms,
                                              final List<Application> apps) {
        final binding = [webServerName: webServerName,
                         jvms:jvms.collect {jvm:[jvmName: it.jvmName, hostName: it.hostName, ajpPort: it.ajpPort]},
                         apps:apps.collect {app:[name: it.name]},
                         comments:""]
        return bindDataToTemplate(binding, templateFileName).toString()
    }
}