#!/bin/sh

java -Xms256m -Xmx512m -verbose:gc -XX:-UseParallelGC \
     -XX:OnOutOfMemoryError="kill -9 %p" -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/srv/athens/logs/ -jar target/athens-lan-party-standalone.jar
