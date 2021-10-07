#!/bin/sh

java -Xms512m -Xmx1536m -verbose:gc -XX:-UseParallelGC \
     -XX:OnOutOfMemoryError="kill -9 %p" -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/srv/athens/logs/ -jar athens-lan-party-standalone.jar
