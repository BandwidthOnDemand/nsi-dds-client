#!/bin/bash

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
BASEDIR=`cd "$PRGDIR" >/dev/null; pwd`

KEYSTORE=$BASEDIR/config/keystore.jks
TRUSTSTORE=$BASEDIR/config/truststore.jks
PASSWORD="changeit"

java -Xmx1536m -Djava.net.preferIPv4Stack=true  \
	-Dapp.home="$BASEDIR" \
	-Dbasedir="$BASEDIR" \
	-Dcom.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot=true \
        -Djavax.net.ssl.keyStore=$KEYSTORE \
        -Djavax.net.ssl.keyStorePassword=$PASSWORD \
        -Djavax.net.ssl.trustStore=$TRUSTSTORE \
        -Djavax.net.ssl.trustStorePassword=$PASSWORD \
	-jar target/nsi-dds-client-1.0-SNAPSHOT-Main.jar \
	-server https://nsi-aggr-west.es.net/discovery \
        -shell $*
