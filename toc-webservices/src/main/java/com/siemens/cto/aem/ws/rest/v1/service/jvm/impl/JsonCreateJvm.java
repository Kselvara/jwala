package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.ssh.DecryptPassword;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonCreateJvm.JsonCreateJvmDeserializer.class)
public class JsonCreateJvm {

    private final String jvmName;
    private final String hostName;
    private final String httpPort;
    private final String httpsPort;
    private final String redirectPort;
    private final String shutdownPort;
    private final String ajpPort;
    private final String statusPath;
    private final String systemProperties;
    private final String userName;
    private final String encryptedPassword;
    
    private final Set<String> groupIds;

    public JsonCreateJvm(final String theJvmName,
                         final String theHostName,
                         final String theHttpPort,
                         final String theHttpsPort,
                         final String theRedirectPort,
                         final String theShutdownPort,
                         final String theAjpPort,
                         final String theStatusPath,
                         final String theSystemProperties,
                         final String theUsername,
                         final String theEncryptedPassword) {
        this(theJvmName,
             theHostName,
             Collections.<String>emptySet(),
             theHttpPort,
             theHttpsPort,
             theRedirectPort,
             theShutdownPort,
             theAjpPort,
             theStatusPath,
             theSystemProperties,
             theUsername,
             theEncryptedPassword);
    }

    public JsonCreateJvm(final String theJvmName,
                         final String theHostName,
                         final Set<String> someGroupIds,
                         final String theHttpPort,
                         final String theHttpsPort,
                         final String theRedirectPort,
                         final String theShutdownPort,
                         final String theAjpPort,
                         final String theStatusPath,
                         final String theSystemProperties,
                         final String theUsername,
                         final String theEncrypedPassword) {
        jvmName = theJvmName;
        hostName = theHostName;
        httpPort = theHttpPort;
        httpsPort = theHttpsPort;
        redirectPort = theRedirectPort;
        shutdownPort = theShutdownPort;
        ajpPort = theAjpPort;
        statusPath = theStatusPath;
        systemProperties = theSystemProperties;
        groupIds = Collections.unmodifiableSet(new HashSet<>(someGroupIds));
        userName = theUsername;
        encryptedPassword =theEncrypedPassword;
    }

    public boolean areGroupsPresent() {
        return !groupIds.isEmpty();
    }

    public CreateJvmRequest toCreateJvmRequest() throws BadRequestException {

        return new CreateJvmRequest(jvmName,
                                    hostName,
                                    JsonUtilJvm.stringToInteger(httpPort),
                                    JsonUtilJvm.stringToInteger(httpsPort),
                                    JsonUtilJvm.stringToInteger(redirectPort),
                                    JsonUtilJvm.stringToInteger(shutdownPort),
                                    JsonUtilJvm.stringToInteger(ajpPort),
                                    new Path(statusPath),
                                    systemProperties,
                                    userName,
                                    encryptedPassword);
    }

    public CreateJvmAndAddToGroupsRequest toCreateAndAddRequest() {
        final Set<Identifier<Group>> groups = convertGroupIds();

        return new CreateJvmAndAddToGroupsRequest(jvmName,
                                                  hostName,
                                                  groups,
                                                  JsonUtilJvm.stringToInteger(httpPort),
                                                  JsonUtilJvm.stringToInteger(httpsPort),
                                                  JsonUtilJvm.stringToInteger(redirectPort),
                                                  JsonUtilJvm.stringToInteger(shutdownPort),
                                                  JsonUtilJvm.stringToInteger(ajpPort),
                                                  new Path(statusPath),
                                                  systemProperties,
                                                  userName,
                                                  encryptedPassword);
    }

    protected Set<Identifier<Group>> convertGroupIds() {
        return new IdentifierSetBuilder(groupIds).build();
    }

    static class JsonCreateJvmDeserializer extends AbstractJsonDeserializer<JsonCreateJvm> {

        public JsonCreateJvmDeserializer() {
        }

        @Override
        public JsonCreateJvm deserialize(final JsonParser jp,
                                         final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final JsonNode jvmNode = rootNode.get("jvmName");
            final JsonNode hostNameNode = rootNode.get("hostName");

            final JsonNode httpPortNode = rootNode.get("httpPort");
            final JsonNode httpsPortNode = rootNode.get("httpsPort");
            final JsonNode redirectPortNode = rootNode.get("redirectPort");
            final JsonNode shutdownPortNode = rootNode.get("shutdownPort");
            final JsonNode ajpPortNode = rootNode.get("ajpPort");
            final JsonNode statusPathNode = rootNode.get("statusPath");
            final JsonNode systemProperties = rootNode.get("systemProperties");
            final JsonNode userName = rootNode.get("userName");
            final JsonNode encryptedPassword = rootNode.get("encryptedPassword");
            
            final Set<String> rawGroupIds = deserializeGroupIdentifiers(rootNode);
            final String jsonPassword = encryptedPassword==null ? null : encryptedPassword.getTextValue();
            final String pw;
            if (jsonPassword!=null && jsonPassword.length()>0) {
                pw = new DecryptPassword().encrypt(encryptedPassword.getTextValue());
            } else {
                pw = "";
            }
            
            return new JsonCreateJvm(jvmNode.getTextValue(),
                                     hostNameNode.getTextValue(),
                                     rawGroupIds,
                                     httpPortNode.getValueAsText(),
                                     httpsPortNode.getValueAsText(),
                                     redirectPortNode.getValueAsText(),
                                     shutdownPortNode.getValueAsText(),
                                     ajpPortNode.getValueAsText(),
                                     statusPathNode.getTextValue(),
                                     systemProperties.getTextValue(),
                                     userName==null? null : userName.getTextValue(),
                                     pw);
        }
    }
}