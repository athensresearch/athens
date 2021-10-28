#!/bin/sh

# webSocketMaxFrameSize env var is a workaround for https://github.com/fluree/db/issues/125

java -Xms512m -Xmx2048m -verbose:gc -XX:-UseParallelGC \
     -XX:OnOutOfMemoryError="kill -9 %p" -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/srv/athens/logs/ \
     -Dorg.asynchttpclient.webSocketMaxFrameSize=10000000 \
     -jar athens-lan-party-standalone.jar
