
var resourceService = {
    createResource: function(groupName, webServerName, jvmName, webAppName, formData) {
        var matrixParam = "";
        if (groupName) {
            matrixParam += ";group=" + groupName;
        }
        if (webServerName) {
            matrixParam += ";webServer=" + webServerName;
        }
        if (jvmName) {
            matrixParam += ";jvm=" + jvmName;
        }
        if (webAppName) {
            matrixParam += ";webApp=" + webAppName;
        }
        return serviceFoundation.promisedPost("v1.0/resources/data" + matrixParam, "json", formData, null, true);
    },
    deleteAllResource: function(resourceName) {
        return serviceFoundation.del("v1.0/resources/template/" + resourceName);
    },
    getResourceAttrData: function() {
        return serviceFoundation.promisedGet("v1.0/resources/data/");
    },
    getResourceTopology: function() {
        return serviceFoundation.promisedGet("v1.0/resources/topology/");
    },
    // TODO: All things regarding resources should be in here therefore we have to put resource related methods for JVM and web server here as well in the future.
    // NOTE: Also make sure to rewrite the REST service calls related to resources (for JVM and web servers) to be in the resource service, not in the group service.
    getAppResources : function(groupName, appName, responseCallback) {
        return serviceFoundation.get("v1.0/resources/" + encodeURIComponent(groupName) + "/" + encodeURIComponent(appName) + "/name", "json", responseCallback);
    },
    getResourceContent: function(resourceName, groupName, webServerName, jvmName, appName) {
        var matrix = "";

        if (groupName) {
            matrix += ";group=" + encodeURIComponent(groupName);
        }

        if (webServerName) {
            matrix += ";webServer=" + encodeURIComponent(webServerName);
        }

        if (jvmName) {
            matrix += ";jvm=" + encodeURIComponent(jvmName);
        }

        if (appName) {
            matrix += ";webApp=" + encodeURIComponent(appName);
        }

        return serviceFoundation.promisedGet("v1.0/resources/" + encodeURIComponent(resourceName) + "/content" + matrix);
    },

    deleteResource: function(resourceName, groupName, webServerName, jvmName, webAppName) {
        var matrixParam = "";
        if (groupName) {
            matrixParam += ";group=" + groupName;
        }
        if (webServerName) {
            matrixParam += ";webServer=" + webServerName;
        }
        if (jvmName) {
            matrixParam += ";jvm=" + jvmName;
        }
        if (webAppName) {
            matrixParam += ";webApp=" + webAppName;
        }
        return serviceFoundation.del("v1.0/resources/template/" + resourceName + matrixParam);
    },
    deleteResources: function(resourceNameArray, groupName, webServerName, jvmName, webAppName) {
        var matrixParam = "";

        resourceNameArray.forEach(function(name){
            matrixParam += ";name=" + encodeURIComponent(name);
        });

        if (groupName) {
            matrixParam += ";group=" + encodeURIComponent(groupName);
        }
        if (webServerName) {
            matrixParam += ";webServer=" + encodeURIComponent(webServerName);
        }
        if (jvmName) {
            matrixParam += ";jvm=" + encodeURIComponent(jvmName);
        }
        if (webAppName) {
            matrixParam += ";webApp=" + encodeURIComponent(webAppName);
        }
        return serviceFoundation.del("v1.0/resources/templates" + matrixParam);
    },
    deployGroupAppResourceToHost: function(groupName, fileName, host) {
        return serviceFoundation.promisedPut("v1.0/groups/" + encodeURIComponent(groupName) + "/apps/conf/" + encodeURIComponent(fileName) +
                                      "?hostName=" + encodeURIComponent(host));
    },
    deployWebServerResource: function(webServerName, fileName) {
        return serviceFoundation.promisedPut("v1.0/webservers/" + encodeURIComponent(webServerName) + "/conf/" + encodeURIComponent(fileName));
    },
    deployJvmResource: function(jvmName, fileName) {
        return serviceFoundation.promisedPut("v1.0/jvms/" + encodeURIComponent(jvmName) + "/conf/" + encodeURIComponent(fileName), "json", null, false);
    },
    deployJvmWebAppResource: function(webAppName, groupName, jvmName, fileName) {
        return serviceFoundation.promisedPut("v1.0/applications/" + encodeURIComponent(webAppName) + "/conf/" +
                                             encodeURIComponent(fileName) + ";groupName=" + encodeURIComponent(groupName) +
                                             ";jvmName=" + encodeURIComponent(jvmName),
                                             "json",
                                             null,
                                             false);
    },
    deployGroupLevelWebServerResource: function(groupName, fileName) {
        return serviceFoundation.promisedPut("v1.0/groups/" + encodeURIComponent(groupName) + "/webservers/conf/" +
                                             encodeURIComponent(fileName), "json", null, false);
    },
    deployGroupLevelJvmResource: function(groupName, fileName) {
        return serviceFoundation.promisedPut("v1.0/groups/" + encodeURIComponent(groupName) + "/jvms/conf/" +
                                             encodeURIComponent(fileName), "json", null, false);
    },
    getExternalProperties: function(){
        return serviceFoundation.promisedGet("v1.0/resources/properties", "json");
    },
    getExternalPropertiesFile: function(callback){
        return serviceFoundation.get("v1.0/resources/properties/file", "json", callback);
    },
    updateResourceContent: function(resourceTemplateName, template, groupName, webServerName, jvmName, webAppName) {
        var matrixParam = "";

        if (groupName) {
            matrixParam += ";group=" + encodeURIComponent(groupName);
        }
        if (webServerName) {
            matrixParam += ";webServer=" + encodeURIComponent(webServerName);
        }
        if (jvmName) {
            matrixParam += ";jvm=" + encodeURIComponent(jvmName);
        }
        if (webAppName) {
            matrixParam += ";webApp=" + encodeURIComponent(webAppName);
        }
        return serviceFoundation.promisedPut("v1.0/resources/template/" + encodeURIComponent(resourceTemplateName) + matrixParam,
                                                    "json",
                                                     template,
                                                     false,
                                                     "text/plain; charset=utf-8")
    },
    previewResourceFile: function(template, groupName, webServerName, jvmName, webAppName, successCallback, errorCallback) {
        var matrixParam = "";

        if (groupName) {
            matrixParam += ";group=" + encodeURIComponent(groupName);
        }
        if (webServerName) {
            matrixParam += ";webServer=" + encodeURIComponent(webServerName);
        }
        if (jvmName) {
            matrixParam += ";jvm=" + encodeURIComponent(jvmName);
        }
        if (webAppName) {
            matrixParam += ";webApp=" + encodeURIComponent(webAppName);
        }
        return serviceFoundation.put("v1.0/resources/template/preview" + matrixParam,
                                     "json",
                                     template,
                                     successCallback,
                                     errorCallback,
                                     false,
                                     "text/plain; charset=utf-8");
    }
};
