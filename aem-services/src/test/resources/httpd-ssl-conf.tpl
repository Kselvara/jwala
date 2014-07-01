# Load mod_jk module
LoadModule jk_module modules/mod_jk.so

JkWorkersFile conf/workers.properties
JkShmFile     logs/mod_jk.shm
JkLogFile     logs/mod_jk.log
JkLogLevel    info
JkLogStampFormat "[%a %b %d %H:%M:%S %Y] "

SSLSessionCache shmcb:logs/ssl_cache_shm

<VirtualHost *:443>
DocumentRoot "htdocs"

SSLEngine on
SSLOptions +StrictRequire

<Directory />
SSLRequireSSL
</Directory>

SSLProtocol -all +TLSv1.2 +TLSv1.1 +TLSv1 +SSLv3

SSLCipherSuite HIGH:MEDIUM:!aNULL:+SHA1:+MD5:+HIGH:+MEDIUM
#SSLCipherSuite HIGH all ciphers using 3DES
#SSLCipherSuite MEDIUM all ciphers with 128 bit encryption
#SSLCipherSuite !aNULL Negate noAuthentication (always authenticate)
#SSLCipherSuite SHA1 preferred ofer MD5

SSLSessionCacheTimeout 300

SSLCertificateFile conf/server.cert
SSLCertificateKeyFile conf/server.key

SSLVerifyClient none
SSLProxyEngine off

<IfModule mime.c>
AddType application/x-x509-ca-cert .crt
AddType application/x-pkcs7-crl .crl
</IfModule>

SetEnvIf User-Agent ".*MSIE.*" nokeepalive ssl-unclean-shutdown downgrade-1.0 force-response-1.0

<% apps.each() { %>
JkMount ${it.mount} lb-${it.name}
<% } %>
JkMount  /jkmanager/* status

</VirtualHost>

<IfModule !mpm_netware_module>
<IfModule !mpm_winnt_module>