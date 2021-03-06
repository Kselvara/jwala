Feature: Testing errors caused by invalid template, meta data or file type

  Scenario: Deploy a webapp resource with error
    Given I logged in
    And I am in the Configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | jdk.media      |
      | mediaType       | JDK              |
      | archiveFilename | jdk.media.archive  |
      | remoteDir       | media.remote.dir |
    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55     |
      | mediaType       | Apache Tomcat            |
      | archiveFilename | apache.tomcat.media.archive |
      | remoteDir       | media.remote.dir         |
    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk.media          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a web app with the following parameters:
      | webappName  | seleniumWebapp |
      | contextPath | /hello         |
      | group       | seleniumGroup  |
    And I created a web app resource with the following parameters:
      | group        | seleniumGroup                            |
      | webApp       | seleniumWebapp                           |
      | deployName   | hello-world.war                          |
      | deployPath   | webapp.resource.deploy.path              |
      | templateName | hello-world7z-unpack-error.war           |
    And I enter attribute in the file MetaData with the following parameters:
      | componentName  | seleniumWebapp  |
      | componentType  | Web Apps        |
      | fileName       | hello-world.war |
      | group          | seleniumGroup   |
      | attributeKey   | "unpack"        |
      | attributeValue | true            |
      | override       | true            |
    And I wait for notification "Saved"
    When I attempt to deploy the web app resource with the following parameters:
      | fileName     | hello-world.war  |
      | deployOption | individual |
    Then I verify failure to unzip the war file with deployPath "webapp.resource.deploy.path" and name as "hello-world"
    And I created a web app resource with the following parameters:
      | group        | seleniumGroup                       |
      | webApp       | seleniumWebapp                      |
      | deployName   | hello.xml                           |
      | deployPath   | webapp.context.resource.deploy.path |
      | templateName | hello.xml.tpl                       |
    And I enter text in resource edit box and save with the following parameters:
      | fileName | hello.xml |
      | tabLabel | Template  |
      | text     | \n${{\n   |
      | position | Context   |
    And I click the ok button to override JVM Templates
    And I wait for notification "Saved"

    #resource error deploy to a host
    And I attempt to deploy the web app resource with the following parameters:
      | fileName     | hello.xml  |
      | deployOption | individual |
    And I confirm error popup for resourceFile "hello.xml" and web app "seleniumWebapp"

    #resource error deploy to all
    And I attempt to deploy the web app resource with the following parameters:
      | fileName     | hello.xml |
      | deployOption | all       |
    And I confirm error popup for resourceFile "hello.xml" and web app "seleniumWebapp"

    #app resources under jvm
    And I go to the web-app file in resources under individual jvm with the following parameters:
      | app     | seleniumWebapp |
      | jvmName | seleniumJvm    |
      | group   | seleniumGroup  |
      | file    | hello.xml      |

    And I attempt to deploy the resource "hello.xml"
    And I confirm error popup for resourceFile "hello.xml" and web app "seleniumWebapp"

    #resource error-Operations
    When I try to generate the webapp with the following parameters:
      | webAppName | seleniumWebapp |
      | group      | seleniumGroup  |
    Then I confirm error popup for resourceFile "hello.xml" and web app "seleniumWebapp"

  Scenario: deploy an individual  web-server resource with error-template-from resource and Operations
    Given I logged in
    And I am in the Configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | apache.httpd.media     |
      | mediaType       | Apache HTTPD            |
      | archiveFilename | apache.httpd.media.archive |
      | remoteDir       | media.remote.dir        |

    And I created a web server with the following parameters:
      | webserverName      | seleniumWebserver   |
      | hostName           | host1               |
      | portNumber         | 80                  |
      | httpsPort          | 443                 |
      | group              | seleniumGroup       |
      | apacheHttpdMediaId | apache.httpd.media |
      | statusPath         | /apache_pb.png      |

    And I created a web server resource with the following parameters:
      | group        | seleniumGroup              |
      | webServer    | seleniumWebserver          |
      | deployName   | httpd.conf                 |
      | deployPath   | httpd.resource.deploy.path |
      | templateName | httpdconf.tpl              |

    #Meta Data resource error
    And I enter text in resource edit box and save with the following parameters:
      | fileName | httpd.conf |
      | tabLabel | Meta Data  |
      | text     | \n{{\n     |
      | position | {          |
    And I confirm to unable to save error popup

    # Abandon changes
    Given I click the "Ext Properties" topology node
    And I press enter to confirm that I don't want to save changes to the resource
    And I click the "seleniumWebserver" topology node

    #previous metaData invalid characters value is erased
    And I enter text in resource edit box and save with the following parameters:
      | fileName | httpd.conf |
      | tabLabel | Template   |
      | text     | ${{        |
      | position | #          |
    And I wait for notification "Saved"

    #deploy from resources-template error
    And I attempt to deploy the resource "httpd.conf"
    And I confirm resource deploy error popup for file "httpd.conf" and webserver "seleniumWebserver"

    #deploy from operations -template error
    When I try to generate webserver with the following parameters:
      | webserverName | seleniumWebserver |
      | group         | seleniumGroup     |
    Then I confirm resource deploy error popup for file "httpd.conf" and webserver "seleniumWebserver"






  Scenario: Test cases on the insertion of illegal characters for a JVM section resource
    Given I logged in
    And I am in the Configuration tab
    And I created a group with the name "seleniumGroup"
    And I created a media with the following parameters:
      | mediaName       | jdk.media      |
      | mediaType       | JDK              |
      | archiveFilename | jdk.media.archive  |
      | remoteDir       | media.remote.dir |

    And I created a media with the following parameters:
      | mediaName       | apache-tomcat-7.0.55     |
      | mediaType       | Apache Tomcat            |
      | archiveFilename | apache.tomcat.media.archive |
      | remoteDir       | media.remote.dir         |

    And I created a jvm with the following parameters:
      | jvmName    | seleniumJvm          |
      | tomcat     | apache-tomcat-7.0.55 |
      | jdk        | jdk.media          |
      | hostName   | host1                |
      | portNumber | 9000                 |
      | group      | seleniumGroup        |
    And I created a JVM resource with the following parameters:
      | group        | seleniumGroup                       |
      | jvm          | seleniumJvm                         |
      | deployName   | server.xml                          |
      | deployPath   | jvm.server.xml.resource.deploy.path |
      | templateName | server.xml.tpl                      |

    #Test error-deploy from resources-MetaData error
    And I enter text in resource edit box and save with the following parameters:
      | fileName | server.xml     |
      | tabLabel | Meta Data      |
      | text     | \n{{\n         |
      | position | {              |
    And I confirm to unable to save error popup

    # Abandon changes
    Given I click the "Ext Properties" topology node
    And I press enter to confirm that I don't want to save changes to the resource
    And I click the "seleniumJvm" topology node

    And I enter text in resource edit box and save with the following parameters:
      | fileName | server.xml                                 |
      | tabLabel | Template                                   |
      | text     | \n${{\n                                    |
      | position | Licensed to the Apache Software Foundation |

    #Test error-deploy from resources-Template error
    When I attempt to deploy the resource "server.xml"
    Then I confirm resource deploy error popup for file "server.xml" and jvm "seleniumJvm"

     #Test error-deploy from operations-Template error
    When I try to generate jvm with the following parameters:
      | jvmName | seleniumJvm   |
      | group   | seleniumGroup |
    Then I confirm resource deploy error popup for file "server.xml" and jvm "seleniumJvm"
    Given I am in the Configuration tab
    #Multiple resources
    And I created a JVM resource with the following parameters:
      | group        | seleniumGroup                   |
      | jvm          | seleniumJvm                     |
      | deployName   | setenv.bat                      |
      | deployPath   | jvm.setenv.resource.deploy.path |
      | templateName | setenv.bat.tpl                  |

    And I enter text in resource edit box and save with the following parameters:
      | fileName | setenv.bat      |
      | tabLabel | Template        |
      | text     | \n${{\n         |
      | position | CALL:stpSetHome |

    And I wait for notification "Saved"

    When I try to generate jvm with the following parameters:
      | jvmName | seleniumJvm   |
      | group   | seleniumGroup |
    Then I confirm multiple resource deploy error popup for file "server.xml" and file "setenv.bat" and jvm "seleniumJvm"

    And I am in the Configuration tab
    And I created a group JVM resource with the following parameters:
      | group        | seleniumGroup                       |
      | deployName   | server.xml                          |
      | deployPath   | jvm.server.xml.resource.deploy.path |
      | templateName | server.xml.tpl                      |

    # Test error-deploy from resources-MetaData error
    When I enter text in resource edit box and save with the following parameters:
      | fileName | server.xml  |
      | tabLabel | Meta Data   |
      | text     | \n{{\n      |
      | position | {           |
    Then I confirm to unable to save error popup

    # Abandon changes
    Given I click the "Ext Properties" topology node
    And I press enter to confirm that I don't want to save changes to the resource
    And I click the "JVMs" topology node

    # Insert illegal characters inside a comment
    And I enter text in resource edit box and save with the following parameters:
      | fileName | server.xml                                 |
      | tabLabel | Template                                   |
      | text     | \n${{\n                                    |
      | position | Licensed to the Apache Software Foundation |
    And I click the ok button to override JVM Templates
    And I wait for notification "Saved"

    # Test error-deploy from resources-Template error
    When I attempt to deploy the jvm group resource "server.xml"
    Then I confirm resource deploy error popup for file "server.xml" and jvm "seleniumJvm"

    # Test error-deploy from operations-Template error
    When I attempt to generate JVMs of group "seleniumGroup"
    Then I confirm resource deploy error popup for file "server.xml" and jvm "seleniumJvm"
