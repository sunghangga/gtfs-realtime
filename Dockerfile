FROM openjdk:8-jdk-alpine
COPY target/gtfs-realtime-0.0.1-SNAPSHOT.jar gtfs-realtime.jar
ENTRYPOINT ["java","-Xmx2G","-jar","/gtfs-realtime.jar"]