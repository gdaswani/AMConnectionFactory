General Setup Guide for use inside "KARAF" as an OSGI Service.

1) Copy JNA 5.5.0 Libraries {"jna-5.5.0.jar", "platform-5.5.0.jar"} to C:\services\apache-karaf\lib\ext

2) Karaf Console: 

	type - "feature:install transaction"
	type - "feature:install pax-jdbc-pool-dbcp2"
	type - "bundle:install mvn:org.apache.commons/commons-lang3/3.9"

3) Modify $KARAF_HOME\etc\org.ops4j.pax.transx.tm.geronimo.cfg

  from
  
	org.apache.geronimo.tm.recoverable = true
  
  to
  
  	org.apache.geronimo.tm.recoverable = false

4) Register JNA packages in OSGI container

Modify $KARAF_HOME\etc\config.properties, section "org.osgi.framework.system.packages.extra"

add 

    com.sun.jna; version="5.5.0", \
    com.sun.jna.platform.win32; version="5.5.0"
  
5) Configure Connection Factory with by placing a file in $KAFAF_HOME\etc\am.datasource.amconnectionfactory.cfg

#-- AMConnectionFactory --#
amCredential.database = DEV
amCredential.userName = someuser
amCredential.password = somepassword

6) Copy "doc\connfactory\*" to C:\services\apache-karaf\data\util\connfactory
