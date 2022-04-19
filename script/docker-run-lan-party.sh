#!/bin/sh

java -Xms512m -Xmx2048m -verbose:gc -XX:-UseParallelGC \
     -XX:OnOutOfMemoryError="kill -9 %p" -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/srv/athens/logs/ \
     -XX:ErrorFile=/srv/athens/logs/java_athens_hs_err_pid%p.log \
     -jar athens-lan-party-standalone.jar
