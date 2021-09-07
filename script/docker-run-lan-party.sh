#!/bin/sh

java -Xms64m -Xmx64m -verbose:gc -XX:-UseParallelGC \
     -XX:OnOutOfMemoryError="kill -9 %p" -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/srv/athens/logs/ -jar target/athens-lan-party-standalone.jar
